package net.atlas.defaulted.init;

import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.enchantment.EnchantmentPatchGenerator;
import net.atlas.defaulted.enchantment.generators.AddEffectGenerator;
import net.atlas.defaulted.enchantment.generators.ConditionalPatch;
import net.atlas.defaulted.init.registry.Bootstrapper;

public class EnchantmentPatchGenerators extends Bootstrapper<MapCodec<? extends EnchantmentPatchGenerator>> {
    public static final EnchantmentPatchGenerators INSTANCE = new EnchantmentPatchGenerators();

    public EnchantmentPatchGenerators() {
        super(Defaulted.ENCHANTMENT_PATCH_GENERATOR_TYPE, "defaulted");
    }
    @Override
    protected void bootstrap() {
        register("conditional", () -> ConditionalPatch.CODEC);
        register("modify_list_effect", () -> AddEffectGenerator.CODEC);
    }
}
