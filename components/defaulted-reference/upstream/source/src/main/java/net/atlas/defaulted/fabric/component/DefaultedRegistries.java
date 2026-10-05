package net.atlas.defaulted.fabric.component;

//? fabric {
import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.enchantment.EnchantmentPatchGenerator;
import net.atlas.defaulted.init.EnchantmentPatchGenerators;
import net.atlas.defaulted.init.ItemPatchGenerators;

public class DefaultedRegistries {
    public static void registerPatchGenerator(String path, MapCodec<? extends PatchGenerator> mapCodec) {
        ItemPatchGenerators.INSTANCE.register(path, () -> mapCodec);
    }

    public static void registerEnchantmentPatchGenerator(String path, MapCodec<? extends EnchantmentPatchGenerator> mapCodec) {
        EnchantmentPatchGenerators.INSTANCE.register(path, () -> mapCodec);
    }
}
//?}