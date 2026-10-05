package net.atlas.defaulted.component.generators;

import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.utils.Codecs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
//? <=1.21.1
//import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
//? >1.21.1
import net.minecraft.world.item.component.Consumable;

public record EditUseDurationGenerator(Float useSeconds) implements PatchGenerator {
    public static final MapCodec<EditUseDurationGenerator> CODEC = Codecs.NON_NEGATIVE_FLOAT.fieldOf("seconds").xmap(EditUseDurationGenerator::new, EditUseDurationGenerator::useSeconds);
    
    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        //? >1.21.1 {
        Consumable consumable = patchedDataComponentMap.get(DataComponents.CONSUMABLE);
        if (consumable != null) {
            Consumable newConsumable = new Consumable(useSeconds, consumable.animation(), consumable.sound(), consumable.hasConsumeParticles(), consumable.onConsumeEffects());
            patchedDataComponentMap.set(DataComponents.CONSUMABLE, newConsumable);
        }
        //?} <=1.21.1 {
        /*FoodProperties foodProperties = patchedDataComponentMap.get(DataComponents.FOOD);
        if (foodProperties != null) {
            FoodProperties newFoodProperties = new FoodProperties(foodProperties.nutrition(), foodProperties.saturation(), foodProperties.canAlwaysEat(), useSeconds, foodProperties.usingConvertsTo(), foodProperties.effects());
            patchedDataComponentMap.set(DataComponents.FOOD, newFoodProperties);
        }
        *///?}
    }
    
    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
    
}
