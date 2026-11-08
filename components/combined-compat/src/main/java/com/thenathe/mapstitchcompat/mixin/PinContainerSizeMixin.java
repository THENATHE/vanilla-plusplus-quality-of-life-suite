package com.thenathe.mapstitchcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.pin.PinUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MapStitch 1.2.1 copies a container item into a fixed 27-slot list before taking a pin from it
 * and writes that list back, deleting everything past slot 26 of a larger backpack or pouch.
 * Size the copy to the stored contents instead; 27-slot containers are unchanged.
 * Remove this once MapStitch sizes the list itself (see docs/UPDATING.md).
 */
@Mixin(value = PinUtil.class, remap = false)
public abstract class PinContainerSizeMixin {
    @WrapOperation(method = "removeFromContainer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/NonNullList;withSize(ILjava/lang/Object;)Lnet/minecraft/core/NonNullList;"))
    private static NonNullList<?> mapstitchcompat$keepEveryContainerSlot(int size, Object fill,
            Operation<NonNullList<?>> original, @Local ItemContainerContents container) {
        return original.call(Math.max(size, container.size()), fill);
    }
}
