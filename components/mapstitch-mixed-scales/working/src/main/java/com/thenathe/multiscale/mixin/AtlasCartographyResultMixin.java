package com.thenathe.multiscale.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.thenathe.multiscale.AtlasCartographyCopy;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.inventory.CartographyTableMenu$5")
public abstract class AtlasCartographyResultMixin {
    @Shadow @Final CartographyTableMenu this$0;

    @WrapOperation(method = "onTake", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/Slot;remove(I)Lnet/minecraft/world/item/ItemStack;", ordinal = 0))
    private ItemStack atlasCopy$keepSource(Slot slot, int amount, Operation<ItemStack> original) {
        if (AtlasCartographyCopy.matches(this$0.container.getItem(0), this$0.container.getItem(1))) return ItemStack.EMPTY;
        return original.call(slot, amount);
    }
}
