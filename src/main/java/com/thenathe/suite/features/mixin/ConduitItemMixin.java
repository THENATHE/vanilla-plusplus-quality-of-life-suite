package com.thenathe.suite.features.mixin;

import com.thenathe.suite.features.FeatureConfig;
import com.thenathe.suite.features.StructurePlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ConduitItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void suite$placeInWater(Level level, Player rawPlayer, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> callback) {
        if (!FeatureConfig.CONFIG.conduitPlacement.get() || !(rawPlayer instanceof ServerPlayer player)
                || !player.getItemInHand(hand).is(Items.CONDUIT)) return;
        double distance = Math.min(player.blockInteractionRange(), FeatureConfig.CONFIG.conduitPlacementDistance.get());
        var hit = player.pick(distance, 0, false);
        Vec3 target = hit.getType() == HitResult.Type.MISS ? player.getEyePosition().add(player.getLookAngle().scale(distance))
                : hit.getLocation().subtract(player.getLookAngle().scale(0.01));
        BlockPos pos = BlockPos.containing(target);
        if (!StructurePlacement.canPlaceConduit(player, hand, pos)) return;
        var context = new BlockPlaceContext(player, hand, player.getItemInHand(hand),
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        callback.setReturnValue(((BlockItem) player.getItemInHand(hand).getItem()).place(context));
    }
}
