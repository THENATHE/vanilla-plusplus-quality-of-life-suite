package com.thenathe.suite.pickup.mixin;

import com.thenathe.suite.pickup.PickupUpgrades;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class CollectorPickupMixin {
    @Shadow private UUID target;

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void suite$collectToBackpack(Player player, CallbackInfo callback) {
        ItemEntity entity = (ItemEntity) (Object) this;
        if (!(player instanceof ServerPlayer serverPlayer) || !entity.isAlive() || entity.hasPickUpDelay()
                || (target != null && !target.equals(player.getUUID()))) return;
        ItemStack stack = entity.getItem();
        Item item = stack.getItem();
        int initialCount = stack.getCount();
        if (PickupUpgrades.collectIntoBackpack(serverPlayer, stack) == 0) return;

        int pickedUp = initialCount - stack.getCount();
        player.take(entity, pickedUp);
        player.awardStat(Stats.ITEM_PICKED_UP.get(item), pickedUp);
        player.onItemPickup(entity);
        if (stack.isEmpty()) {
            entity.discard();
            callback.cancel();
        } else {
            // Let vanilla and upstream pouch hooks process the remaining items normally.
            entity.setItem(stack);
        }
    }
}
