package net.atlas.defaulted.enchantment;

import com.google.gson.JsonElement;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.base.BasePatches;
import net.atlas.defaulted.codec.HeterogeneousHolderSetListCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Collections;
import java.util.List;

public record EnchantmentPatches(List<HolderSet<Enchantment>> elements, EnchantmentOverrides overrides, List<EnchantmentPatchGenerator> generators, int priority) implements Comparable<EnchantmentPatches>, BasePatches<Enchantment, EnchantmentPatchGenerator> {
    public static final Codec<EnchantmentPatches> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance ->
            instance.group(HeterogeneousHolderSetListCodec.create(Registries.ENCHANTMENT, RegistryFixedCodec.create(Registries.ENCHANTMENT), false).fieldOf("elements").forGetter(EnchantmentPatches::elements))
                    .and(additionalDetails(instance))
                    .apply(instance, EnchantmentPatches::new)));

    @Override
    public int compareTo(EnchantmentPatches o) {
        return priority - o.priority;
    }

    public void apply(Holder<Enchantment> enchantment, EnchantmentBuilder enchantmentBuilder) {
        if (!matchEnchantment(enchantment)) return;
        enchantmentBuilder.apply(this.overrides());
    }

    public void applyGenerators(Holder<Enchantment> enchantment, EnchantmentBuilder enchantmentBuilder) {
        if (!matchEnchantment(enchantment)) return;
        this.generators().forEach(patchGenerator -> patchGenerator.patchDataComponentMap(enchantment, enchantmentBuilder));
    }

    public boolean matchEnchantment(Holder<Enchantment> enchantment) {
        if (!(this.elements.isEmpty() || this.elements.stream().allMatch(holders -> holders.size() <= 0)))
            return this.elements.stream().anyMatch(holders -> holders.contains(enchantment));
        return true;
    }

    public static Products.P3<RecordCodecBuilder.Mu<EnchantmentPatches>, EnchantmentOverrides, List<EnchantmentPatchGenerator>, Integer> additionalDetails(RecordCodecBuilder.Instance<EnchantmentPatches> instance) {
        return instance.group(EnchantmentOverrides.CODEC.forGetter(EnchantmentPatches::overrides),
                EnchantmentPatchGenerator.CODEC.listOf().optionalFieldOf("patch_generators", Collections.emptyList()).forGetter(EnchantmentPatches::generators),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("priority", 1000).forGetter(EnchantmentPatches::priority));
    }

    @Override
    public ResourceKey<Registry<EnchantmentPatches>> key() {
        return Defaulted.ENCHANTMENT_PATCHES_TYPE;
    }

    @Override
    public JsonElement save(RegistryAccess registries) {
        return CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), this).getOrThrow();
    }
}
