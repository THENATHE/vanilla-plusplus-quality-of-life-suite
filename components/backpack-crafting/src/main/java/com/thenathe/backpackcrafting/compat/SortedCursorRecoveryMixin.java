package com.thenathe.backpackcrafting.compat;

import com.thenathe.backpackcrafting.client.SortedRuleContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/** Keeps interrupted storage operations from returning held items to the crafting panel. */
@Pseudo
@Mixin(targets = "com.sorted.sort.SortQueue", remap = false)
public abstract class SortedCursorRecoveryMixin {
    @WrapOperation(method = "returnTarget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/Slot;mayPlace(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean backpackCrafting$storageRecovery(Slot slot, ItemStack stack,
                                                           Operation<Boolean> original,
                                                           AbstractContainerMenu menu, ItemStack cursor, int origin) {
        return !SortedRuleContext.isCraftingSlot(menu, slot) && original.call(slot, stack);
    }
}
