package qa;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import com.thenathe.suite.wrench.DebugWrench;
import net.fabricmc.loader.api.FabricLoader;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.core.registries.BuiltInRegistries;
import javax.imageio.ImageIO;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipFile;

/** Runs against actual registries, recipes, world updates and Fabric event invokers. */
public final class DebugWrenchChecks {
    private static final BlockPos CENTER = new BlockPos(256, 100, 256);
    private final StringBuilder report = new StringBuilder();
    private MinecraftServer server;
    private ServerLevel level;
    private ServerPlayer player;
    private int assertions;
    private int failures;

    public static int run(MinecraftServer server) {
        return new DebugWrenchChecks().checkAll(server);
    }

    private int checkAll(MinecraftServer running) {
        server = running;
        try {
            level = server.overworld();
            GameProfile profile = new GameProfile(UUID.fromString("5c0c7892-b393-4d06-92b3-f089732d9c26"), "DebugWrenchQA");
            player = new ServerPlayer(server, level, profile, ClientInformation.createDefault());
            player.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player,
                CommonListenerCookie.createInitial(profile, false));
            level.getChunkAt(CENTER);
            player.setPos(CENTER.getX() + .5, CENTER.getY(), CENTER.getZ() + .5);
            caseOf("survival non-op selection and value cycling through Fabric callbacks", this::survival);
            caseOf("left-click cooldown boundaries, shared player gate and unrestricted right click", this::selectionCooldown);
            caseOf("reverse selection, reverse values and wraparound", this::reverse);
            caseOf("per-stack and per-block-type selection persistence", this::selectionMemory);
            caseOf("blocks without states and chest inventory preservation", this::stateSafety);
            caseOf("adventure spectator border and native spawn protection", this::permissions);
            caseOf("ordinary iron nuggets do not invoke wrench actions", this::ordinaryItem);
            caseOf("crafting recipe registered", this::recipe);
            if (FabricLoader.getInstance().isModLoaded("polymer-core")) {
                caseOf("Polymer conversion and fixed16 generated resource pack", this::polymer);
            } else {
                check(DebugWrench.WRENCH.getClass().getInterfaces().length == 0, "native wrench item has no mandatory Polymer interface");
            }
        } catch (Throwable failure) {
            failures++;
            appendFailure("harness setup", failure);
        }
        report.append("RESULT: ").append(failures == 0 ? "PASS" : "FAIL")
            .append("; assertions=").append(assertions).append("; failed_cases=").append(failures).append('\n');
        System.out.println("Debug Wrench suite QA:\n" + report);
        if (failures != 0) throw new AssertionError(report.toString());
        return assertions;
    }

    private void survival() {
        ItemStack wrench = reset();
        check(!server.getPlayerList().isOp(new NameAndId(player.getGameProfile())), "test player is not an operator");
        put(Blocks.OAK_LOG.defaultBlockState());
        BlockState original = level.getBlockState(CENTER);
        check(attack() == InteractionResult.FAIL, "left click cancels normal block breaking");
        check(level.getBlockState(CENTER).equals(original), "selection does not alter block state");
        check(selected(wrench, Blocks.OAK_LOG) == RotatedPillarBlock.AXIS, "log axis selected");
        check(use().consumesAction(), "right click consumed");
        check(level.getBlockState(CENTER).getValue(RotatedPillarBlock.AXIS) != original.getValue(RotatedPillarBlock.AXIS), "survival rotates log axis");
        check(wrench.getCount() == 1 && !wrench.isDamaged(), "wrench is reusable");
        check(wrench.get(DataComponents.LORE).lines().size() == 3, "native and fallback stacks include all three control instructions");
        put(Blocks.OAK_STAIRS.defaultBlockState());
        attack();
        Property<?> first = selected(wrench, Blocks.OAK_STAIRS);
        attack();
        check(selected(wrench, Blocks.OAK_STAIRS) != first, "left click advances selected property");
    }

    private void reverse() {
        ItemStack wrench = reset();
        put(Blocks.OAK_STAIRS.defaultBlockState());
        attack();
        Property<?> first = selected(wrench, Blocks.OAK_STAIRS);
        attack();
        player.setShiftKeyDown(true);
        attack();
        check(selected(wrench, Blocks.OAK_STAIRS) == first, "shift reverses property selection");
        player.setShiftKeyDown(false);
        put(Blocks.OAK_LOG.defaultBlockState());
        BlockState original = level.getBlockState(CENTER);
        use();
        player.setShiftKeyDown(true);
        use();
        check(level.getBlockState(CENTER).equals(original), "shift reverses value cycling");
        player.setShiftKeyDown(false);
        for (int i = 0; i < RotatedPillarBlock.AXIS.getPossibleValues().size(); i++) use();
        check(level.getBlockState(CENTER).equals(original), "value sequence wraps exactly");
    }

    private void selectionCooldown() {
        ItemStack wrench = reset();
        put(Blocks.OAK_STAIRS.defaultBlockState());
        check(attackNow() == InteractionResult.FAIL, "first selection consumes attack");
        Property<?> first = selected(wrench, Blocks.OAK_STAIRS);
        check(first != null, "first selection accepted immediately");
        check(attackNow() == InteractionResult.FAIL, "same-tick repeat still cancels block breaking");
        check(selected(wrench, Blocks.OAK_STAIRS) == first, "same-tick repeat does not skip a property");
        check(!DebugWrench.interact(player, CENTER, wrench, false), "direct selection call observes cooldown too");

        ItemStack second = new ItemStack(DebugWrench.WRENCH);
        player.setItemInHand(InteractionHand.MAIN_HAND, second);
        attackNow();
        check(selected(second, Blocks.OAK_STAIRS) == null, "changing wrench stacks cannot bypass cooldown");
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        put(Blocks.OAK_LOG.defaultBlockState());
        attackNow();
        check(selected(wrench, Blocks.OAK_LOG) == null, "changing targeted block type cannot bypass cooldown");
        BlockState before = level.getBlockState(CENTER);
        check(DebugWrench.interact(player, CENTER, wrench, true), "right click allowed during selection cooldown");
        check(!level.getBlockState(CENTER).equals(before), "right click changes state during cooldown");
        check(DebugWrench.interact(player, CENTER, wrench, true), "second right click in same tick also allowed");

        GameProfile otherProfile = new GameProfile(UUID.fromString("5c0c7892-b393-4d06-92b3-f089732d9c28"), "WrenchQAOther");
        ServerPlayer other = new ServerPlayer(server, level, otherProfile, ClientInformation.createDefault());
        other.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), other,
            CommonListenerCookie.createInitial(otherProfile, false));
        other.setGameMode(GameType.SURVIVAL);
        other.setPos(player.getX(), player.getY(), player.getZ());
        ItemStack otherWrench = new ItemStack(DebugWrench.WRENCH);
        check(DebugWrench.interact(other, CENTER, otherWrench, false), "another player can select during first player's cooldown");
        check(selected(otherWrench, Blocks.OAK_LOG) == RotatedPillarBlock.AXIS, "other player's selection is applied");

        put(Blocks.OAK_STAIRS.defaultBlockState());
        advanceTicks(5);
        player.setShiftKeyDown(true);
        attackNow();
        check(selected(wrench, Blocks.OAK_STAIRS) == first, "selection still blocked after five ticks even when sneaking");
        player.setShiftKeyDown(false);
        advanceTicks(1);
        attackNow();
        check(selected(wrench, Blocks.OAK_STAIRS) != first, "selection allowed at six ticks; rejected clicks do not extend delay");

        wrench = reset();
        put(Blocks.OAK_LOG.defaultBlockState());
        player.setGameMode(GameType.ADVENTURE);
        check(!DebugWrench.interact(player, CENTER, wrench, false), "forbidden selection is rejected before cooldown");
        player.setGameMode(GameType.SURVIVAL);
        check(DebugWrench.interact(player, CENTER, wrench, false), "forbidden selection does not start cooldown");
        wrench = reset();
        put(Blocks.STONE.defaultBlockState());
        check(!DebugWrench.interact(player, CENTER, wrench, false), "stateless block cannot select");
        put(Blocks.OAK_LOG.defaultBlockState());
        check(DebugWrench.interact(player, CENTER, wrench, false), "stateless block does not start cooldown");
    }

    private void selectionMemory() {
        ItemStack first = reset();
        put(Blocks.OAK_STAIRS.defaultBlockState());
        attack();
        attack();
        Property<?> selection = selected(first, Blocks.OAK_STAIRS);
        ItemStack second = new ItemStack(DebugWrench.WRENCH);
        player.setItemInHand(InteractionHand.MAIN_HAND, second);
        attack();
        check(selected(second, Blocks.OAK_STAIRS) != selection, "fresh stack has independent selection");
        check(selected(first, Blocks.OAK_STAIRS) == selection, "other stack selection retained");
        player.setItemInHand(InteractionHand.MAIN_HAND, first);
        put(Blocks.OAK_LOG.defaultBlockState());
        attack();
        check(selected(first, Blocks.OAK_STAIRS) == selection, "switching block types retains stairs property");
        check(selected(first, Blocks.OAK_LOG) == RotatedPillarBlock.AXIS, "same stack remembers log property too");
        ItemStack copied = first.copy();
        check(selected(copied, Blocks.OAK_STAIRS) == selection, "selection survives stack copy");
        var ops = RegistryOps.create(JsonOps.INSTANCE, server.registryAccess());
        var encoded = ItemStack.CODEC.encodeStart(ops, first).getOrThrow();
        ItemStack saved = ItemStack.CODEC.parse(ops, encoded).getOrThrow();
        check(saved.is(DebugWrench.WRENCH), "saved item decodes as real wrench");
        check(selected(saved, Blocks.OAK_STAIRS) == selection && selected(saved, Blocks.OAK_LOG) == RotatedPillarBlock.AXIS,
            "both property selections survive actual item save/load serialization");
        put(Blocks.OAK_STAIRS.defaultBlockState());
        BlockState before = level.getBlockState(CENTER);
        use();
        List<Property<?>> changed = new ArrayList<>();
        for (Property<?> property : before.getProperties()) {
            if (!before.getValue(property).equals(level.getBlockState(CENTER).getValue(property))) changed.add(property);
        }
        check(changed.equals(List.of(selection)), "returning to stairs changes only remembered property");
    }

    private void stateSafety() {
        ItemStack wrench = reset();
        put(Blocks.STONE.defaultBlockState());
        attack();
        use();
        check(level.getBlockState(CENTER).is(Blocks.STONE), "stateless stone survives both actions");
        put(Blocks.CHEST.defaultBlockState());
        ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(CENTER);
        chest.setItem(0, new ItemStack(Items.DIAMOND, 17));
        // Select facing deterministically, regardless of vanilla property ordering.
        for (int i = 0; i < 5 && selected(wrench, Blocks.CHEST) != ChestBlock.FACING; i++) attack();
        check(selected(wrench, Blocks.CHEST) == ChestBlock.FACING, "chest facing selected");
        Direction before = level.getBlockState(CENTER).getValue(ChestBlock.FACING);
        use();
        check(level.getBlockState(CENTER).getValue(ChestBlock.FACING) != before, "chest rotates");
        check(level.getBlockEntity(CENTER) == chest, "same chest block entity retained");
        check(chest.getItem(0).is(Items.DIAMOND) && chest.getItem(0).getCount() == 17, "chest contents retained");
    }

    private void permissions() {
        ItemStack wrench = reset();
        put(Blocks.OAK_LOG.defaultBlockState());
        BlockState before = level.getBlockState(CENTER);
        for (GameType mode : List.of(GameType.ADVENTURE, GameType.SPECTATOR)) {
            player.setGameMode(mode);
            attack();
            use();
            check(level.getBlockState(CENTER).equals(before), mode + " does not modify block");
            check(selected(wrench, Blocks.OAK_LOG) == null, mode + " does not select property");
        }
        player.setGameMode(GameType.SURVIVAL);
        double border = level.getWorldBorder().getSize();
        try {
            level.getWorldBorder().setSize(32);
            use();
            check(level.getBlockState(CENTER).equals(before), "outside world border denied");
        } finally {
            level.getWorldBorder().setSize(border);
        }
        LevelData.RespawnData spawn = server.getRespawnData();
        NameAndId operator = new NameAndId(UUID.fromString("5c0c7892-b393-4d06-92b3-f089732d9c27"), "QAOperator");
        try {
            server.getPlayerList().op(operator);
            server.setRespawnData(LevelData.RespawnData.of(level.dimension(), CENTER, 0, 0));
            check(server.isUnderSpawnProtection(level, CENTER, player), "native spawn protection active for non-op");
            attack();
            use();
            check(level.getBlockState(CENTER).equals(before), "protected block not changed");
            check(selected(wrench, Blocks.OAK_LOG) == null, "protected block selection denied");
        } finally {
            server.setRespawnData(spawn);
            server.getPlayerList().deop(operator);
        }
    }

    private void ordinaryItem() {
        reset();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_NUGGET));
        put(Blocks.OAK_LOG.defaultBlockState());
        BlockState before = level.getBlockState(CENTER);
        check(attack() == InteractionResult.PASS, "ordinary nugget attack passes through");
        check(use() == InteractionResult.PASS, "ordinary nugget use passes through");
        check(level.getBlockState(CENTER).equals(before), "ordinary nugget leaves block unchanged");
    }

    private void recipe() {
        var key = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("debugwrench", "debug_wrench"));
        var recipe = server.getRecipeManager().byKey(key);
        check(recipe.isPresent(), "debugwrench:debug_wrench recipe loads");
        check(recipe.orElseThrow().value() instanceof CraftingRecipe, "recipe uses crafting table");
        CraftingInput input = CraftingInput.of(3, 3, List.of(
            new ItemStack(Items.IRON_INGOT), ItemStack.EMPTY, new ItemStack(Items.IRON_INGOT),
            ItemStack.EMPTY, new ItemStack(Items.IRON_INGOT), ItemStack.EMPTY,
            ItemStack.EMPTY, new ItemStack(Items.COPPER_INGOT), ItemStack.EMPTY));
        var match = server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        check(match.isPresent() && match.get().id().equals(key), "three iron and one copper match wrench pattern");
        ItemStack result = match.orElseThrow().value().assemble(input);
        check(result.is(DebugWrench.WRENCH) && result.getCount() == 1, "recipe crafts one real server wrench");
        List<ItemStack> incorrect = new ArrayList<>(input.items());
        incorrect.set(7, new ItemStack(Items.STICK));
        check(!((CraftingRecipe) recipe.orElseThrow().value()).matches(CraftingInput.of(3, 3, incorrect), level), "stick does not substitute for copper handle");
    }

    private void polymer() throws Exception {
        PolymerChecks.run(this);
    }

    private static final class PolymerChecks {
        private static void run(DebugWrenchChecks qa) throws Exception {
            ItemStack wrench = qa.reset();
            var context = ((PacketContextProvider) qa.player.connection).getPacketContext();
            ItemStack fallback = PolymerItemUtils.getPolymerItemStack(wrench, context, qa.server.registryAccess());
            qa.check(fallback.is(Items.IRON_NUGGET), "vanilla client receives iron nugget base");
            qa.check(DebugWrench.ITEM_ID.equals(fallback.get(DataComponents.ITEM_MODEL)), "fixed wrench model does not depend on resource-pack detection");
            PolymerCommonUtils.setHasResourcePack(context, PolymerResourcePackUtils.getMainUuid(), true);
            ItemStack client = PolymerItemUtils.getPolymerItemStack(wrench, context, qa.server.registryAccess());
            qa.check(client.is(Items.IRON_NUGGET), "accepted pack retains iron nugget base");
            qa.check(DebugWrench.ITEM_ID.equals(client.get(DataComponents.ITEM_MODEL)), "accepted pack activates custom wrench model");
            qa.check(PolymerItemUtils.getRealItemStack(client, qa.server.registryAccess()).is(DebugWrench.WRENCH), "Polymer stack decodes to custom wrench");
            qa.player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.COBBLESTONE, 16));
            qa.put(Blocks.OAK_LOG.defaultBlockState());
            var hit = new BlockHitResult(Vec3.atCenterOf(CENTER), Direction.UP, CENTER, false);
            qa.check(((PolymerItem) PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, DebugWrench.WRENCH)).isPolymerBlockInteraction(qa.level.getBlockState(CENTER), qa.player,
                InteractionHand.MAIN_HAND, wrench, qa.level, hit, InteractionResult.SUCCESS), "Polymer handles interaction to suppress client offhand retry");
            qa.use();
            qa.check(qa.player.getOffhandItem().is(Items.COBBLESTONE) && qa.player.getOffhandItem().getCount() == 16, "main hand wrench leaves offhand blocks untouched");
            qa.check(PolymerResourcePackUtils.isRequired(), "Polymer pack marked required");
            var built = PolymerResourcePackUtils.getInstance().build(Path.of("qa-wrench-resource-pack.zip").toAbsolutePath());
            qa.check(!built.hadIssues(), "Polymer resource pack builds without issues");
            try (ZipFile zip = new ZipFile(built.path().toFile())) {
                for (String asset : List.of("pack.mcmeta", "assets/debugwrench/items/debug_wrench.json",
                        "assets/debugwrench/models/item/debug_wrench.json", "assets/debugwrench/textures/item/debug_wrench.png")) {
                    qa.check(zip.getEntry(asset) != null, "generated pack contains " + asset);
                }
                try (var imageStream = zip.getInputStream(zip.getEntry("assets/debugwrench/textures/item/debug_wrench.png"))) {
                    var image = ImageIO.read(imageStream);
                    qa.check(image.getWidth() == 16 && image.getHeight() == 16, "pack wrench icon is exactly 16x16 pixels");
                }
                qa.check(zip.getEntry("assets/debugwrench/textures/item/debug_wrench_32.png") == null,
                        "integrated wrench has no 32px alternate icon");
                try (var reader = new InputStreamReader(zip.getInputStream(zip.getEntry("assets/debugwrench/models/item/debug_wrench.json")),
                        StandardCharsets.UTF_8)) {
                    var model = JsonParser.parseReader(reader).getAsJsonObject();
                    qa.check("minecraft:item/handheld".equals(model.get("parent").getAsString()), "wrench uses vanilla handheld tool model transforms");
                    qa.check("debugwrench:item/debug_wrench".equals(model.getAsJsonObject("textures").get("layer0").getAsString()),
                        "handheld model references packaged crescent-wrench sprite");
                }
            }
        }
    }

    private ItemStack reset() {
        advanceTicks(6);
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        player.getInventory().clearContent();
        put(Blocks.AIR.defaultBlockState());
        ItemStack wrench = new ItemStack(DebugWrench.WRENCH);
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        return wrench;
    }

    private Property<?> selected(ItemStack stack, Block block) {
        return stack.getOrDefault(DataComponents.DEBUG_STICK_STATE, DebugStickState.EMPTY).properties().get(block.builtInRegistryHolder());
    }

    private InteractionResult attack() {
        advanceTicks(6);
        return attackNow();
    }

    private InteractionResult attackNow() {
        return AttackBlockCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, CENTER, Direction.UP);
    }

    private void advanceTicks(long ticks) {
        // The harness runs synchronously; advance its disposable world's real game clock.
        ServerLevel overworld = server.overworld();
        ((ServerLevelData) overworld.getLevelData()).setGameTime(overworld.getGameTime() + ticks);
    }

    private InteractionResult use() {
        return UseBlockCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(CENTER), Direction.UP, CENTER, false));
    }

    private void put(BlockState state) {
        level.setBlock(CENTER, state, Block.UPDATE_SKIP_ALL_SIDEEFFECTS);
    }

    private void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
        assertions++;
    }

    private void caseOf(String name, CheckedRunnable action) {
        try {
            action.run();
            report.append("PASS: ").append(name).append('\n');
        } catch (Throwable failure) {
            failures++;
            appendFailure(name, failure);
        }
    }

    private void appendFailure(String name, Throwable failure) {
        StringWriter stack = new StringWriter();
        failure.printStackTrace(new PrintWriter(stack));
        report.append("FAIL: ").append(name).append('\n').append(stack);
    }

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }
}
