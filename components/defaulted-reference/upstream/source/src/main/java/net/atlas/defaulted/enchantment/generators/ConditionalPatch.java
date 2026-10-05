package net.atlas.defaulted.enchantment.generators;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.defaulted.enchantment.EnchantmentBuilder;
import net.atlas.defaulted.enchantment.EnchantmentOverrides;
import net.atlas.defaulted.enchantment.EnchantmentPatchGenerator;
import net.atlas.defaulted.enchantment.generators.condition.EnchantmentPatchConditions;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Collections;
import java.util.List;

public record ConditionalPatch(EnchantmentPatchConditions.EnchantmentPatchCondition condition, List<EnchantmentPatchGenerator> generators, EnchantmentOverrides overrides) implements EnchantmentPatchGenerator {
    public static final MapCodec<ConditionalPatch> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(EnchantmentPatchConditions.MAP_CODEC.forGetter(ConditionalPatch::condition),
                        EnchantmentPatchGenerator.CODEC.listOf().optionalFieldOf("patch_generators", Collections.emptyList()).forGetter(ConditionalPatch::generators),
                        EnchantmentOverrides.CODEC.forGetter(ConditionalPatch::overrides))
                .apply(instance, ConditionalPatch::new));


    @Override
    public void patchDataComponentMap(Holder<Enchantment> enchantment, EnchantmentBuilder builder) {
        if (condition.matches(enchantment, builder)) {
            builder.apply(overrides);
            generators.forEach(enchantmentPatchGenerator -> enchantmentPatchGenerator.patchDataComponentMap(enchantment, builder));
        }
    }

    @Override
    public MapCodec<? extends EnchantmentPatchGenerator> codec() {
        return CODEC;
    }
}
