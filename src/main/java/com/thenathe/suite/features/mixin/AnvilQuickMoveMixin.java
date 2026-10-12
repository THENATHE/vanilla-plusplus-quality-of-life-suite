package com.thenathe.suite.features.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Keep the taken result's enchantments available to SSO's anvil cost check. */
@Mixin(ItemCombinerMenu.class)
public abstract class AnvilQuickMoveMixin {
    @WrapOperation(method = "quickMoveStack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/Slot;onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V"))
    private void suite$preserveAnvilResult(Slot slot, Player player, ItemStack remainder,
                                         Operation<Void> original,
                                         @Local(argsOnly = true) int slotIndex,
                                         @Local(ordinal = 0) ItemStack beforeMove) {
        // Vanilla passes the depleted slot stack after a successful full transfer.
        // SSO interprets that empty stack as unenchanted and clears the XP cost.
        if ((Object) this instanceof AnvilMenu menu && slotIndex == menu.getResultSlot()
                && remainder.isEmpty()) {
            original.call(slot, player, beforeMove);
        } else {
            original.call(slot, player, remainder);
        }
    }
}
