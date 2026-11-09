package suite.upgrade;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.thenathe.suite.pets.PetIndex;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.ProblemReporter;
import com.mojang.authlib.GameProfile;
import me.pajic.toolpouch.util.ToolPouchUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Writes representative suite data with one release and reads it back with the next.
 * Phase "write" fills chests from items.json, places chalk marks and indexes a pet;
 * phase "verify" compares the re-encoded stacks, block states and saved pet index.
 */
public final class UpgradeQa implements ModInitializer {
    private static final Path CONTROL = Path.of(System.getProperty("suite.upgrade.control"));
    private static final String PHASE = System.getProperty("suite.upgrade.phase");
    private static final BlockPos ORIGIN = new BlockPos(0, -60, 0);
    private int ticks;

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++ticks != 40) return;
            try {
                if (PHASE.equals("write")) write(server);
                else verify(server);
            } catch (Exception exception) {
                fail(exception);
            }
            server.halt(false);
        });
    }

    private static void write(MinecraftServer server) throws Exception {
        ServerLevel level = server.overworld();
        level.setChunkForced(0, 0, true);
        var map = MapItem.create(level, 0, 0, (byte) 1, true, false);
        int mapId = map.get(net.minecraft.core.component.DataComponents.MAP_ID).id();
        var definitions = JsonParser.parseString(Files.readString(CONTROL.resolve("items.json"))).getAsJsonArray();
        var snapshot = new JsonObject();
        var stacks = new JsonArray();
        var ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        List<ItemStack> items = new ArrayList<>();
        items.add(map);
        for (var definition : definitions) {
            var text = definition.toString().replace("\"$MAP_ID\"", Integer.toString(mapId));
            items.add(ItemStack.CODEC.parse(ops, JsonParser.parseString(text)).getOrThrow());
        }
        // Tool Pouch 1.2 slot toggles are stored on the pouch; older releases have no such component.
        if (BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(Identifier.parse("toolpouch:disabled_slots"))) {
            items.add(ItemStack.CODEC.parse(ops, JsonParser.parseString("{\"id\":\"toolpouch:tool_pouch\",\"count\":1,\"components\":{"
                    + "\"minecraft:container\":[{\"slot\":0,\"item\":{\"id\":\"minecraft:elytra\",\"count\":1}},"
                    + "{\"slot\":1,\"item\":{\"id\":\"vpp_mod_suite:pouch_magnet\",\"count\":1}}],"
                    + "\"toolpouch:disabled_slots\":[0,1]}}")).getOrThrow());
        }
        for (int i = 0; i < items.size(); i++) {
            BlockPos chest = chestPos(i / 27);
            if (i % 27 == 0) level.setBlockAndUpdate(chest, Blocks.CHEST.defaultBlockState());
            ((ChestBlockEntity) level.getBlockEntity(chest)).setItem(i % 27, items.get(i).copy());
            stacks.add(ItemStack.CODEC.encodeStart(ops, items.get(i)).getOrThrow());
        }
        snapshot.add("stacks", stacks);
        snapshot.add("chalk", placeChalk(level));
        var wolf = EntityTypes.WOLF.spawn(level, ORIGIN.offset(4, 0, 4), EntitySpawnReason.COMMAND);
        wolf.setPersistenceRequired();
        PetIndex.get(server).update(wolf);
        snapshot.addProperty("pet", wolf.getUUID().toString());
        snapshot.add("elytra_players", writeElytraPlayers(server));
        server.saveEverything(false, true, true);
        Files.writeString(CONTROL.resolve("snapshot.json"), snapshot.toString());
        Files.writeString(CONTROL.resolve("result.txt"), "PASS write " + items.size() + " stacks\n");
    }

    private static void verify(MinecraftServer server) throws Exception {
        ServerLevel level = server.overworld();
        var snapshot = JsonParser.parseString(Files.readString(CONTROL.resolve("snapshot.json"))).getAsJsonObject();
        var ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        var expected = snapshot.getAsJsonArray("stacks");
        var report = new JsonObject();
        var mismatches = new JsonArray();
        for (int i = 0; i < expected.size(); i++) {
            var chest = (ChestBlockEntity) level.getBlockEntity(chestPos(i / 27));
            var actual = chest == null ? ItemStack.EMPTY : chest.getItem(i % 27);
            var encoded = actual.isEmpty() ? null : ItemStack.CODEC.encodeStart(ops, actual).getOrThrow();
            if (encoded == null || !encoded.equals(expected.get(i))) {
                var entry = new JsonObject();
                entry.addProperty("index", i);
                entry.add("expected", expected.get(i));
                entry.add("actual", encoded);
                mismatches.add(entry);
            }
        }
        var chalkMismatches = new JsonArray();
        var chalk = snapshot.getAsJsonObject("chalk");
        for (var entry : chalk.entrySet()) {
            var pos = BlockPos.of(Long.parseLong(entry.getKey()));
            var now = describe(level.getBlockState(pos));
            if (!now.equals(entry.getValue().getAsString())) chalkMismatches.add(entry.getKey() + ": " + entry.getValue().getAsString() + " -> " + now);
        }
        var pet = UUID.fromString(snapshot.get("pet").getAsString());
        boolean petIndexed = PetIndex.get(server).get(pet) != null;
        report.addProperty("stacks", expected.size());
        report.add("stack_mismatches", mismatches);
        report.addProperty("chalk_marks", chalk.size());
        report.add("chalk_mismatches", chalkMismatches);
        report.addProperty("pet_index_preserved", petIndexed);
        var elytra = verifyElytraPlayers(server, snapshot.getAsJsonObject("elytra_players"));
        report.add("elytra_players", elytra);
        boolean passed = mismatches.isEmpty() && chalkMismatches.isEmpty() && petIndexed && elytra.get("passed").getAsBoolean();
        report.addProperty("passed", passed);
        Files.writeString(CONTROL.resolve("verify.json"), report.toString());
        Files.writeString(CONTROL.resolve("result.txt"), (passed ? "PASS" : "FAIL") + " verify\n");
    }

    /**
     * Two players with an Elytra in their pouch, saved the way the previous release saves player
     * data: one with the suite's old Elytra toggle off, one with it on.
     */
    private static JsonObject writeElytraPlayers(MinecraftServer server) throws Exception {
        var result = new JsonObject();
        for (boolean enabled : new boolean[] {false, true}) {
            var player = fakePlayer(server, enabled ? "ElytraOn" : "ElytraOff");
            var pouch = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("toolpouch:tool_pouch")));
            pouch.set(net.minecraft.core.component.DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.fromItems(
                    List.of(new ItemStack(net.minecraft.world.item.Items.CLOCK), new ItemStack(net.minecraft.world.item.Items.ELYTRA))));
            player.getInventory().setItem(4, pouch);
            try {
                // The suite's own Elytra toggle, in releases before 1.5.2.
                Class.forName("com.thenathe.toolpouchcompat.Compat").getMethod("set", ServerPlayer.class, boolean.class).invoke(null, player, enabled);
            } catch (NoSuchMethodException newerSuite) {
                // From 1.5.2 the same choice is a Tool Pouch disabled slot.
                Class.forName("com.thenathe.toolpouchcompat.PouchElytraToggle").getMethod("set", ServerPlayer.class, boolean.class).invoke(null, player, enabled);
            }
            result.addProperty((enabled ? "on" : "off") + "_glides_before", ToolPouchUtil.getElytraFromToolPouch(player, false) != null);
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, server.registryAccess());
            player.saveWithoutId(output);
            NbtIo.writeCompressed(output.buildResult(), CONTROL.resolve("player-" + (enabled ? "on" : "off") + ".dat"));
        }
        return result;
    }

    private static JsonObject verifyElytraPlayers(MinecraftServer server, JsonObject before) throws Exception {
        var result = new JsonObject();
        boolean passed = before != null;
        for (boolean enabled : new boolean[] {false, true}) {
            String name = enabled ? "on" : "off";
            CompoundTag saved = NbtIo.readCompressed(CONTROL.resolve("player-" + name + ".dat"), NbtAccounter.unlimitedHeap());
            var player = fakePlayer(server, enabled ? "ElytraOn" : "ElytraOff");
            player.load(TagValueInput.create(ProblemReporter.DISCARDING, server.registryAccess(), saved));
            Class.forName("com.thenathe.toolpouchcompat.LegacyElytraToggle").getMethod("apply", ServerPlayer.class).invoke(null, player);
            boolean glides = ToolPouchUtil.getElytraFromToolPouch(player, false) != null;
            var pouch = ToolPouchUtil.getToolPouch(player);
            boolean slotDisabled = ToolPouchUtil.isSlotDisabled(pouch, 1);
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, server.registryAccess());
            player.saveWithoutId(output);
            boolean legacyKeyKept = output.buildResult().contains("ToolPouchElytraEnabled");
            result.addProperty(name + "_glides_before", before == null ? null : before.get(name + "_glides_before").getAsBoolean());
            result.addProperty(name + "_glides_after", glides);
            result.addProperty(name + "_elytra_slot_disabled", slotDisabled);
            result.addProperty(name + "_legacy_flag_still_saved", legacyKeyKept);
            passed &= glides == enabled && slotDisabled == !enabled && !legacyKeyKept;
        }
        result.addProperty("passed", passed);
        return result;
    }

    private static ServerPlayer fakePlayer(MinecraftServer server, String name) {
        var profile = new GameProfile(UUID.nameUUIDFromBytes(("vpp-upgrade-" + name).getBytes()), name);
        var player = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
        player.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player,
                CommonListenerCookie.createInitial(profile, false)) {
            @Override
            public void send(Packet<?> packet) {
            }
        };
        return player;
    }

    private static JsonObject placeChalk(ServerLevel level) {
        var placed = new JsonObject();
        int x = 0;
        for (var id : List.of("chalk:white_chalk_mark", "chalk:white_glow_chalk_mark", "chalk:red_chalk_mark",
                "chalk:blue_glow_chalk_mark", "chalk:black_chalk_mark")) {
            var block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
            if (block == Blocks.AIR) throw new IllegalStateException("Missing chalk block " + id);
            for (var face : List.of(Direction.UP, Direction.NORTH)) {
                BlockPos support = ORIGIN.offset(8 + x, 0, 8);
                level.setBlockAndUpdate(support, Blocks.STONE.defaultBlockState());
                BlockPos mark = support.relative(face);
                BlockState state = block.defaultBlockState();
                for (Property<?> property : state.getProperties()) {
                    if (property.getName().equals("facing")) state = with(state, property, face.getSerializedName());
                    if (property.getName().equals("orientation")) state = with(state, property, Integer.toString(x % 8));
                }
                level.setBlockAndUpdate(mark, state);
                placed.addProperty(Long.toString(mark.asLong()), describe(level.getBlockState(mark)));
                x += 2;
            }
        }
        return placed;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState with(BlockState state, Property property, String value) {
        return (BlockState) property.getValue(value).map(v -> state.setValue(property, (Comparable) v)).orElse(state);
    }

    private static String describe(BlockState state) {
        return state.toString();
    }

    private static BlockPos chestPos(int index) {
        return ORIGIN.offset(index * 2, 0, 0);
    }

    private static void fail(Exception exception) {
        try {
            Files.writeString(CONTROL.resolve("result.txt"), "FAIL " + exception + "\n");
        } catch (Exception ignored) {
        }
        exception.printStackTrace();
    }
}
