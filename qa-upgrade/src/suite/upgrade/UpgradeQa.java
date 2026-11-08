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
        boolean passed = mismatches.isEmpty() && chalkMismatches.isEmpty() && petIndexed;
        report.addProperty("passed", passed);
        Files.writeString(CONTROL.resolve("verify.json"), report.toString());
        Files.writeString(CONTROL.resolve("result.txt"), (passed ? "PASS" : "FAIL") + " verify\n");
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
