package com.thenathe.suite.pets.mixin;

import com.thenathe.suite.pets.SwiftFlight;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.BiConsumer;

@Mixin(EnchantmentHelper.class)
public abstract class SwiftFlightAttributeMixin {
    @ModifyVariable(method = {
        "forEachModifier(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
        "forEachModifier(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V"
    }, at = @At("HEAD"), argsOnly = true)
    private static BiConsumer<Holder<Attribute>, AttributeModifier> vpp$swiftFlightEnabled(
        BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        return SwiftFlight.configuredModifiers(consumer);
    }
}
