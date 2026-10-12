package com.thenathe.suite.pets;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.UUID;

public final class PetHorn {
    public static final String BINDING_KEY = "vpp_pet_call";
    public record Binding(UUID pet, UUID owner, PetKind kind) {}

    private PetHorn() {}

    public static Binding binding(ItemStack stack) {
        if (!stack.is(Items.GOAT_HORN)) return null;
        CompoundTag saved = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            .copyTag().getCompoundOrEmpty(BINDING_KEY);
        if (saved.getIntOr("version", 0) != 1) return null;
        try {
            return new Binding(UUID.fromString(saved.getStringOr("pet", "")),
                UUID.fromString(saved.getStringOr("owner", "")),
                PetKind.valueOf(saved.getStringOr("kind", "")));
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }

    public static void bind(ItemStack stack, UUID pet, UUID owner, PetKind kind, Component name) {
        if (!stack.is(Items.GOAT_HORN)) throw new IllegalArgumentException("A pet call needs a goat horn");
        reset(stack);
        CompoundTag binding = new CompoundTag();
        binding.putInt("version", 1);
        binding.putString("pet", pet.toString());
        binding.putString("owner", owner.toString());
        binding.putString("kind", kind.name());
        Component originalName = stack.get(DataComponents.CUSTOM_NAME);
        if (originalName != null) binding.store("original_name", ComponentSerialization.CODEC, originalName);
        Boolean originalGlint = stack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        if (originalGlint != null) binding.putBoolean("original_glint", originalGlint);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.put(BINDING_KEY, binding));
        stack.set(DataComponents.CUSTOM_NAME, name.copy());
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public static boolean reset(ItemStack stack) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!data.contains(BINDING_KEY)) return false;
        CompoundTag old = data.getCompoundOrEmpty(BINDING_KEY);
        if (old.contains("original_name")) {
            old.read("original_name", ComponentSerialization.CODEC).ifPresent(name ->
                stack.set(DataComponents.CUSTOM_NAME, name));
        } else stack.remove(DataComponents.CUSTOM_NAME);
        if (old.contains("original_glint")) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, old.getBooleanOr("original_glint", false));
        } else stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        data.remove(BINDING_KEY);
        if (data.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return true;
    }
}
