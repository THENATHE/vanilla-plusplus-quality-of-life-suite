package net.atlas.defaulted.utils;

import net.atlas.defaulted.component.backport.PhantomDataComponents;
import net.atlas.defaulted.component.phantom.EnchantmentOverrides;
import net.atlas.defaulted.mixin.EnchantmentHelperAccessor;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.function.Consumer;

public class Hooks {
    public static boolean addEnchantableStatusForDefaultEnchantments(boolean isEmpty, ItemEnchantments enchantments, ItemStack itemStack) {
        if (itemStack.getItem().defaulted$has(PhantomDataComponents.ENCHANTMENT_OVERRIDES.get())) {
            EnchantmentOverrides overrides = itemStack.getItem().defaulted$get(PhantomDataComponents.ENCHANTMENT_OVERRIDES.get());
            ItemEnchantments defaultEnchantments = itemStack.getPrototype().getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (!defaultEnchantments.isEmpty() && overrides.isStillEnchantable())
                isEmpty |= defaultEnchantments.equals(enchantments);
        }
        return isEmpty;
    }

    public static boolean considerNonEnchantedIfMatchingDefaultEnchantments(boolean hasEnchantments, ItemStack itemStack) {
        if (itemStack.getItem().defaulted$has(PhantomDataComponents.ENCHANTMENT_OVERRIDES.get())) {
            EnchantmentOverrides overrides = itemStack.getItem().defaulted$get(PhantomDataComponents.ENCHANTMENT_OVERRIDES.get());
            DataComponentType<ItemEnchantments> componentType = EnchantmentHelperAccessor.getComponentType(itemStack);
            ItemEnchantments defaultEnchantments = itemStack.getPrototype().getOrDefault(componentType, ItemEnchantments.EMPTY);
            if (!defaultEnchantments.isEmpty() && overrides.grindsBackToDefault()) {
                ItemEnchantments enchantments = itemStack.getOrDefault(componentType, ItemEnchantments.EMPTY);
                hasEnchantments &= !defaultEnchantments.equals(enchantments);
            }
        }
        return hasEnchantments;
    }

    public static Consumer<ItemEnchantments.Mutable> wrapConsumer(Consumer<ItemEnchantments.Mutable> consumer, ItemStack itemStack) {
        Consumer<ItemEnchantments.Mutable> wrapped = consumer;
        if (itemStack.getItem().defaulted$has(PhantomDataComponents.ENCHANTMENT_OVERRIDES.get())) {
            EnchantmentOverrides overrides = itemStack.getItem().defaulted$get(PhantomDataComponents.ENCHANTMENT_OVERRIDES.get());
            DataComponentType<ItemEnchantments> componentType = EnchantmentHelperAccessor.getComponentType(itemStack);
            ItemEnchantments defaultEnchantments = itemStack.getPrototype().getOrDefault(componentType, ItemEnchantments.EMPTY);
            if (!defaultEnchantments.isEmpty() && overrides.grindsBackToDefault()) {
                wrapped = mutable -> {
                    consumer.accept(mutable);
                    defaultEnchantments.entrySet().forEach(entry -> mutable.upgrade(entry.getKey(), entry.getIntValue()));
                };
            }
        }
        return wrapped;
    }
}
