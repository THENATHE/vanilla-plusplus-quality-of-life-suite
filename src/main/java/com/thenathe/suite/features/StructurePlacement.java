package com.thenathe.suite.features;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConduitBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Bounded vanilla structures, with permissions and inventory accounting per block. */
public final class StructurePlacement {
    private static final ThreadLocal<Boolean> CHECKING_PROTECTION = ThreadLocal.withInitial(() -> false);

    private StructurePlacement() {}

    public static List<BlockPos> beaconPositions(BlockPos origin) {
        List<BlockPos> positions = new ArrayList<>();
        int layers = Math.clamp(FeatureConfig.CONFIG.beaconMaximumLayers.get(), 1, 4);
        for (int layer = 1; layer <= layers; layer++) {
            for (int x = -layer; x <= layer; x++) {
                for (int z = -layer; z <= layer; z++) positions.add(origin.offset(x, -layer, z));
            }
        }
        return positions;
    }

    public static List<BlockPos> conduitPositions(BlockPos origin) {
        List<BlockPos> positions = new ArrayList<>();
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    if ((x == 0 && (Math.abs(y) == 2 || Math.abs(z) == 2))
                            || (y == 0 && (Math.abs(x) == 2 || Math.abs(z) == 2))
                            || (z == 0 && (Math.abs(x) == 2 || Math.abs(y) == 2))) {
                        positions.add(origin.offset(x, y, z));
                    }
                }
            }
        }
        return positions;
    }

    public static InteractionResult use(net.minecraft.world.entity.player.Player rawPlayer,
            net.minecraft.world.level.Level rawLevel, InteractionHand hand, BlockHitResult hit) {
        if (CHECKING_PROTECTION.get() || !(rawPlayer instanceof ServerPlayer player)
                || !(rawLevel instanceof ServerLevel level) || player.isSpectator()) return InteractionResult.PASS;
        BlockPos origin = hit.getBlockPos();
        BlockState center = level.getBlockState(origin);
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof BlockItem item)) return InteractionResult.PASS;
        BlockState material = item.getBlock().defaultBlockState();
        boolean beacon = FeatureConfig.CONFIG.beaconPlacement.get() && center.is(Blocks.BEACON)
                && material.is(BlockTags.BEACON_BASE_BLOCKS);
        boolean conduit = FeatureConfig.CONFIG.conduitPlacement.get() && center.is(Blocks.CONDUIT)
                && material.is(BlockTags.CONDUIT_EFFECT_BLOCK);
        if (!beacon && !conduit) return InteractionResult.PASS;
        int moved = fill(player, hand, origin, beacon, held, material, player.isShiftKeyDown());
        // A completed or protected structure does not turn this click into accidental placement.
        return moved > 0 ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public static int fill(ServerPlayer player, InteractionHand hand, BlockPos origin, boolean beacon,
            ItemStack held, BlockState material, boolean bulk) {
        ServerLevel level = player.level();
        if (held.isEmpty() || !canEdit(player, origin, held)) return 0;
        if (!(held.getItem() instanceof BlockItem item) || !material.is(item.getBlock())
                || !(beacon ? material.is(BlockTags.BEACON_BASE_BLOCKS) : material.is(BlockTags.CONDUIT_EFFECT_BLOCK))) return 0;
        var positions = beacon ? beaconPositions(origin) : conduitPositions(origin);
        int remaining = bulk ? held.getCount() : 1;
        int placed = 0;
        for (BlockPos pos : positions) {
            if (remaining == 0 || held.isEmpty()) break;
            BlockState previous = level.getBlockState(pos);
            if (previous.is(beacon ? BlockTags.BEACON_BASE_BLOCKS : BlockTags.CONDUIT_EFFECT_BLOCK)) continue;
            if (!canReplace(player, hand, pos, held, material)) continue;
            if (!replace(player, pos, origin, previous, material,
                    beacon ? FeatureConfig.CONFIG.dropReplacedBeaconBlocks.get() : FeatureConfig.CONFIG.dropReplacedConduitBlocks.get())) continue;
            held.consume(1, player);
            placed++;
            remaining--;
        }
        if (!beacon && FeatureConfig.CONFIG.clearCompletedConduit.get()
                && positions.stream().allMatch(pos -> level.getBlockState(pos).is(BlockTags.CONDUIT_EFFECT_BLOCK))) {
            clearConduitInterior(player, hand, origin, held);
        }
        return placed;
    }

    public static boolean canEdit(ServerPlayer player, BlockPos pos, ItemStack held) {
        var level = player.level();
        return !player.isSpectator() && level.isInWorldBounds(pos) && level.isLoaded(pos)
                && level.getWorldBorder().isWithinBounds(pos) && level.mayInteract(player, pos)
                && !player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer())
                && player.mayUseItemAt(pos, Direction.UP, held);
    }

    private static boolean canReplace(ServerPlayer player, InteractionHand hand, BlockPos pos,
            ItemStack held, BlockState replacement) {
        ServerLevel level = player.level();
        BlockState previous = level.getBlockState(pos);
        if (!canEdit(player, pos, held) || previous.getDestroySpeed(level, pos) < 0
                || previous.hasBlockEntity() || !level.isUnobstructed(replacement, pos, net.minecraft.world.phys.shapes.CollisionContext.of(player))) return false;
        CHECKING_PROTECTION.set(true);
        try {
            if (!previous.isAir() && !PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(
                    level, player, pos, previous, level.getBlockEntity(pos))) return false;
            return UseBlockCallback.EVENT.invoker().interact(player, level, hand,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)) != InteractionResult.FAIL;
        } finally {
            CHECKING_PROTECTION.remove();
        }
    }

    public static boolean canPlaceConduit(ServerPlayer player, InteractionHand hand, BlockPos pos) {
        return player.level().isWaterAt(pos) && canReplace(player, hand, pos, player.getItemInHand(hand),
                Blocks.CONDUIT.defaultBlockState().setValue(ConduitBlock.WATERLOGGED, true));
    }

    private static boolean replace(ServerPlayer player, BlockPos pos, BlockPos origin,
            BlockState previous, BlockState replacement, boolean dropAtCenter) {
        ServerLevel level = player.level();
        // Compute loot before removal; publish it only after a successful replacement.
        var drops = previous.isAir() || player.isCreative() ? List.<ItemStack>of()
                : Block.getDrops(previous, level, pos, null, player, player.getMainHandItem());
        if (!level.setBlock(pos, replacement, Block.UPDATE_ALL)) return false;
        for (ItemStack drop : drops) Block.popResource(level, dropAtCenter ? origin.above() : pos, drop);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, replacement));
        return true;
    }

    private static void clearConduitInterior(ServerPlayer player, InteractionHand hand, BlockPos origin, ItemStack held) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos pos = origin.offset(x, y, z);
                    BlockState previous = player.level().getBlockState(pos);
                    if (player.level().isWaterAt(pos) || previous.is(BlockTags.CONDUIT_EFFECT_BLOCK)) continue;
                    if (canReplace(player, hand, pos, held, Blocks.WATER.defaultBlockState())) {
                        replace(player, pos, origin, previous, Blocks.WATER.defaultBlockState(),
                                FeatureConfig.CONFIG.dropReplacedConduitBlocks.get());
                    }
                }
            }
        }
    }

    public static void dismantle(net.minecraft.world.level.Level rawLevel, net.minecraft.world.entity.player.Player rawPlayer,
            BlockPos origin, BlockState center, net.minecraft.world.level.block.entity.BlockEntity ignored) {
        if (CHECKING_PROTECTION.get() || !(rawPlayer instanceof ServerPlayer player)
                || !(rawLevel instanceof ServerLevel level)) return;
        boolean beacon = center.is(Blocks.BEACON) && FeatureConfig.CONFIG.beaconPlacement.get()
                && FeatureConfig.CONFIG.breakBeaconBase.get();
        boolean conduit = center.is(Blocks.CONDUIT) && FeatureConfig.CONFIG.conduitPlacement.get()
                && FeatureConfig.CONFIG.breakConduitFrame.get();
        if (!beacon && !conduit) return;
        var positions = beacon ? beaconPositions(origin) : conduitPositions(origin);
        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(beacon ? BlockTags.BEACON_BASE_BLOCKS : BlockTags.CONDUIT_EFFECT_BLOCK)
                    || !canEdit(player, pos, player.getMainHandItem())) continue;
            CHECKING_PROTECTION.set(true);
            boolean allowed;
            try {
                allowed = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, state, level.getBlockEntity(pos));
            } finally {
                CHECKING_PROTECTION.remove();
            }
            if (!allowed) continue;
            var drops = player.isCreative() ? List.<ItemStack>of()
                    : Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, player.getMainHandItem());
            if (!level.removeBlock(pos, false)) continue;
            for (ItemStack drop : drops) Block.popResource(level, origin, drop);
            PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(level, player, pos, state, null);
        }
    }
}
