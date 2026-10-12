package com.thenathe.suite.wrench;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class DebugWrench {
    public static final String MOD_ID = "debugwrench";
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MOD_ID);
    private static final long SELECTION_COOLDOWN_TICKS = 6;
    // Shared across stacks and targets, without retaining disconnected players.
    private static final Map<ServerPlayer, Long> LAST_SELECTION_TICK = new WeakHashMap<>();
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath("debugwrench", "debug_wrench");
    public static final Item WRENCH = new DebugWrenchItem(new Item.Properties()
        .setId(ResourceKey.create(Registries.ITEM, ITEM_ID))
        .stacksTo(1)
        .component(DataComponents.DEBUG_STICK_STATE, DebugStickState.EMPTY)
        .component(DataComponents.LORE, new ItemLore(List.of(
            Component.translatableWithFallback("item.debugwrench.debug_wrench.select", "Left-click: select block property"),
            Component.translatableWithFallback("item.debugwrench.debug_wrench.change", "Right-click: change value"),
            Component.translatableWithFallback("item.debugwrench.debug_wrench.reverse", "Sneak to cycle backward"))))
        .component(DataComponents.ITEM_NAME, Component.translatableWithFallback("item.debugwrench.debug_wrench", "Debug Wrench")));

    private DebugWrench() {}

    public static void initialize() {
        Registry.register(BuiltInRegistries.ITEM, ITEM_ID, WRENCH);
        if (FabricLoader.getInstance().isModLoaded("polymer-core")
                && FabricLoader.getInstance().isModLoaded("polymer-resource-pack")) {
            DebugWrenchPolymer.initialize();
        }

        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(WRENCH)) return InteractionResult.PASS;
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (player instanceof ServerPlayer serverPlayer) interact(serverPlayer, pos, stack, false);
            // Always consume, including rejected edits and blocks without properties.
            return InteractionResult.FAIL;
        });
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(WRENCH)) return InteractionResult.PASS;
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (player instanceof ServerPlayer serverPlayer) interact(serverPlayer, hit.getBlockPos(), stack, true);
            // Run before container/door interactions so the wrench edits their state.
            return InteractionResult.SUCCESS;
        });
    }

    /** Vanilla debug-stick state behavior, available to players who may build. */
    public static boolean interact(ServerPlayer player, BlockPos pos, ItemStack stack, boolean cycle) {
        var level = player.level();
        if (!stack.is(WRENCH) || player.isSpectator() || !player.getAbilities().mayBuild
                || !level.isInWorldBounds(pos) || !level.hasChunkAt(pos)
                || !level.mayInteract(player, pos) || !player.mayInteract(level, pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        var block = state.typeHolder();
        var properties = block.value().getStateDefinition().getProperties();
        if (properties.isEmpty()) {
            player.sendOverlayMessage(Component.literal("Debug Wrench: " + block.getRegisteredName() + " has no properties."));
            return false;
        }
        var selection = stack.getOrDefault(DataComponents.DEBUG_STICK_STATE, DebugStickState.EMPTY);
        Property<?> property = selection.properties().get(block);
        boolean backward = player.isSecondaryUseActive();
        if (cycle) {
            if (property == null || !properties.contains(property)) property = properties.iterator().next();
            BlockState changed = cycleState(state, property, backward);
            // Match the vanilla debug stick: notify clients, suppress neighbor shape updates.
            // Keeping the block type preserves its block entity and inventory.
            if (!level.setBlock(pos, changed, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE)) return false;
            player.sendOverlayMessage(Component.literal("Debug Wrench: " + property.getName() + " = " + valueName(changed, property)));
        } else {
            // Use one world clock so changing dimensions cannot bypass the delay.
            long tick = level.getServer().overworld().getGameTime();
            Long previous = LAST_SELECTION_TICK.get(player);
            if (previous != null && tick >= previous && tick - previous < SELECTION_COOLDOWN_TICKS) {
                return false;
            }
            property = relative(properties, property, backward);
            stack.set(DataComponents.DEBUG_STICK_STATE, selection.withProperty(block, property));
            LAST_SELECTION_TICK.put(player, tick);
            player.sendOverlayMessage(Component.literal("Debug Wrench: selected " + property.getName() + " = " + valueName(state, property)));
        }
        return true;
    }

    private static <T extends Comparable<T>> BlockState cycleState(BlockState state, Property<T> property, boolean backward) {
        return state.setValue(property, relative(property.getPossibleValues(), state.getValue(property), backward));
    }

    private static <T> T relative(Iterable<T> values, T current, boolean backward) {
        return backward ? Util.findPreviousInIterable(values, current) : Util.findNextInIterable(values, current);
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }
}
