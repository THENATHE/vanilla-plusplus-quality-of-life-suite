package com.thenathe.suite.pets;

import com.thenathe.suite.features.FeatureConfig;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.UniformGenerator;

import java.util.function.BiConsumer;

public final class SwiftFlight {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("swift_flight", "swift_flight");

    private SwiftFlight() {}

    public static void initialize() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (key.identifier().equals(Identifier.withDefaultNamespace("chests/end_city_treasure"))
                && FeatureConfig.CONFIG.swiftFlight.get() && FeatureConfig.CONFIG.swiftFlightBookLoot.get()) {
                var enchantment = registries.lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ID));
                table.withPool(LootPool.lootPool().setRolls(Holder.direct(new ConstantValue(1)))
                    .when(LootItemRandomChanceCondition.randomChance(0.08F))
                    .add(LootItem.lootTableItem(Items.BOOK).apply(new SetEnchantmentsFunction.Builder()
                        .withEnchantment(enchantment, Holder.direct(new UniformGenerator(
                            Holder.direct(new ConstantValue(1)), Holder.direct(new ConstantValue(3))))))));
            }
        });
    }

    public static BiConsumer<Holder<Attribute>, AttributeModifier> configuredModifiers(
        BiConsumer<Holder<Attribute>, AttributeModifier> original) {
        return (attribute, modifier) -> {
            boolean swiftFlight = modifier.id().getNamespace().equals("minecraft")
                && modifier.id().getPath().startsWith("enchantment.swift_flight/");
            // Both equipment addition and removal use this callback. Keep the ID so unequipping
            // a harness can still remove its old modifier after the feature has been disabled.
            original.accept(attribute, swiftFlight && !FeatureConfig.CONFIG.swiftFlight.get()
                ? new AttributeModifier(modifier.id(), 0.0, modifier.operation()) : modifier);
        };
    }
}
