package com.thenathe.suite.features.mixin;

import com.thenathe.suite.features.ShulkerPickup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class ShulkerInventoryMixin {
    @Shadow @Final public Player player;

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void suite$insertOffhand(int slot, ItemStack incoming, CallbackInfoReturnable<Boolean> callback) {
        if (slot != -1 || !(player instanceof ServerPlayer serverPlayer)) return;
        if (ShulkerPickup.collect(serverPlayer, incoming) > 0 && incoming.isEmpty()) callback.setReturnValue(true);
    }
}
