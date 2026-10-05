package net.atlas.defaulted.component.generators;

import java.util.Collections;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.generators.condition.PatchConditions;
import net.atlas.defaulted.component.generators.condition.PatchConditions.PatchCondition;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;

public record ConditionalPatch(PatchCondition condition, List<PatchGenerator> generators, DataComponentPatch dataComponentPatch) implements PatchGenerator {
    public static final MapCodec<ConditionalPatch> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(PatchConditions.MAP_CODEC.forGetter(ConditionalPatch::condition),
                        PatchGenerator.CODEC.listOf().optionalFieldOf("patch_generators", Collections.emptyList()).forGetter(ConditionalPatch::generators),
                        DataComponentPatch.CODEC.optionalFieldOf("patch", DataComponentPatch.EMPTY).forGetter(ConditionalPatch::dataComponentPatch))
                .apply(instance, ConditionalPatch::new));

    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        if (condition.matches(item, patchedDataComponentMap)) {
            patchedDataComponentMap.applyPatch(dataComponentPatch);
            generators.forEach(patchGenerator -> patchGenerator.patchDataComponentMap(item, patchedDataComponentMap));
        }
    }

    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
}
