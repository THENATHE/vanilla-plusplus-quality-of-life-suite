package net.atlas.defaulted.component.generators;

import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.backport.PhantomDataComponents;
import net.atlas.defaulted.utils.DataComponentPatchUtils;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;

public record PhantomDataComponentPatchGenerator(DataComponentPatch patch) implements PatchGenerator {
    public static final MapCodec<PhantomDataComponentPatchGenerator> CODEC = DataComponentPatchUtils.codec(PhantomDataComponents.INSTANCE.getRegistry()).fieldOf("patch").xmap(PhantomDataComponentPatchGenerator::new, PhantomDataComponentPatchGenerator::patch);
    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        item.defaulted$getPhantomComponentMap().applyPatch(patch);
    }

    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
}