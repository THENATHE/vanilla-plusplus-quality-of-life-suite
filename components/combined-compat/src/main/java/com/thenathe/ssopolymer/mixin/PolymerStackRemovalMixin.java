package com.thenathe.ssopolymer.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Polymer clears fallback-item defaults with set(type, null). SSO's broken-state hook on
 * ItemStack.set unboxes a DAMAGE value without a null check, so a non-damageable item shown
 * as a damageable fallback (the toolboxes use axes) could fail to encode and disconnect the
 * player, depending on component iteration order. remove() has the same effect on the stack.
 */
@Mixin(value = PolymerItemUtils.class, remap = false)
public abstract class PolymerStackRemovalMixin {
    @WrapOperation(method = "createItemStack(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/TooltipFlag;Lnet/fabricmc/fabric/api/networking/v1/context/PacketContext;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object ssoPolymer$removeInsteadOfNull(ItemStack stack, DataComponentType<?> type, Object value,
                                                       Operation<Object> original) {
        return value == null ? stack.remove(type) : original.call(stack, type, value);
    }
}
