package net.atlas.defaulted.component.generators;

import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;

public record ChangeTierGenerator(ToolMaterialWrapper toolMaterialWrapper) implements PatchGenerator {
    public static final MapCodec<ChangeTierGenerator> CODEC = ToolMaterialWrapper.CODEC.xmap(ChangeTierGenerator::new, ChangeTierGenerator::toolMaterialWrapper);
    
    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        item.defaulted$setToolMaterial(toolMaterialWrapper);
    }

    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
    
}