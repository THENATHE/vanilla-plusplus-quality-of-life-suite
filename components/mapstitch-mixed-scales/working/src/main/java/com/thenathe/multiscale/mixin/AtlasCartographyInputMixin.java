package com.thenathe.multiscale.mixin;

import me.pajic.mapstitch.item.ModItems;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net.minecraft.world.inventory.CartographyTableMenu$3", "net.minecraft.world.inventory.CartographyTableMenu$4"})
public abstract class AtlasCartographyInputMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void atlasCopy$allowInputs(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        int index = ((Slot) (Object) this).getContainerSlot();
        if ((index == 0 && stack.is(ModItems.ATLAS)) || (index == 1 && stack.is(Items.BOOK))) cir.setReturnValue(true);
    }
}
