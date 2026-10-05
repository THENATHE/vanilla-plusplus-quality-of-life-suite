package net.atlas.defaulted.component.generators;

import java.util.List;
import java.util.Optional;

//? <=1.21.1
//import com.mojang.datafixers.util.Either;
import net.atlas.defaulted.utils.Codecs;
import org.apache.logging.log4j.LogManager;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
//? >1.21.1
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Tool;
//? >1.21.1 {
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;
//?} <=1.21.1 {
/*import net.atlas.defaulted.component.backport.Enchantable;
import net.atlas.defaulted.component.backport.PhantomDataComponents;
import net.atlas.defaulted.component.backport.Repairable;
*///?}
import net.minecraft.world.level.block.Block;

public record ModifyTierStatsGenerator(List<TierComponents> components,  Optional<TagKey<Block>> toolMineable, Optional<Float> damageFactor) implements PatchGenerator {
    public static final MapCodec<ModifyTierStatsGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(TierComponents.CODEC.listOf().fieldOf("components").forGetter(ModifyTierStatsGenerator::components),
            TagKey.codec(Registries.BLOCK).optionalFieldOf("mineable_blocks").forGetter(ModifyTierStatsGenerator::toolMineable),
            Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("max_damage_factor").forGetter(ModifyTierStatsGenerator::damageFactor))
        .apply(instance, ModifyTierStatsGenerator::new));

    @SuppressWarnings("deprecation")
    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        ToolMaterialWrapper tier = item.defaulted$getToolMaterial();
        if (tier != null) {
            if (components.contains(TierComponents.DURABILITY)) {
                int maxDamage = tier.durability();
                if (damageFactor.isPresent()) maxDamage = Math.round(maxDamage * damageFactor.get());
                Defaulted.setDurability(maxDamage, patchedDataComponentMap);
            }
            if (components.contains(TierComponents.ENCHANTABLE))
                //? >1.21.1
                patchedDataComponentMap.set(DataComponents.ENCHANTABLE, new Enchantable(tier.enchantmentValue()));
                //? <=1.21.1
                //item.defaulted$set(PhantomDataComponents.ENCHANTABLE.get(), new Enchantable(tier.getEnchantmentValue()));
            if (components.contains(TierComponents.REPAIRABLE))
                //? >1.21.1
                patchedDataComponentMap.set(DataComponents.REPAIRABLE, new Repairable(BuiltInRegistries.ITEM.getOrThrow(tier.repairItems())));
                //? <=1.21.1
                //item.defaulted$set(PhantomDataComponents.REPAIRABLE.get(), new Repairable(Either.right(tier.repairItems())));
            if (components.contains(TierComponents.TOOL)) {
                if (toolMineable.isEmpty()) {
                    LogManager.getLogger("Defaulted").warn("Attempted to update tool component for tiered item " + item.builtInRegistryHolder() + " but mineable tag is not present!");
                    return;
                }
                patchedDataComponentMap.set(DataComponents.TOOL, new Tool(List.of(Tool.Rule.deniesDrops(/*? >1.21.1 {*/ BuiltInRegistries.BLOCK.getOrThrow(tier.incorrectBlocksForDrops()) /*?} <=1.21.1 {*/ /*tier.incorrectBlocksForDrops() *//*?}*/), Tool.Rule.minesAndDrops(/*? >1.21.1 {*/ BuiltInRegistries.BLOCK.getOrThrow(toolMineable.get()) /*?} <=1.21.1 {*/ /*toolMineable.get() *//*?}*/, tier.speed())), 1.0F, 1/*? >=1.21.5 {*/ , true /*?}*/));
            }
        }
    }

    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
}
