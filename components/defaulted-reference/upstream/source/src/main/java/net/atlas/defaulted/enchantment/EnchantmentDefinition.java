package net.atlas.defaulted.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.defaulted.enchantment.value_provider.ValueProvider;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import java.util.Optional;

public record EnchantmentDefinition(Optional<HolderSet<Item>> supportedItems,
                                    Optional<HolderSet<Item>> primaryItems,
                                    boolean forcePrimaryItemsReplacement,
                                    Optional<ValueProvider> weight,
                                    Optional<ValueProvider> maxLevel,
                                    Optional<Cost> minCost,
                                    Optional<Cost> maxCost,
                                    Optional<ValueProvider> anvilCost,
                                    Slots slots) {
    public static final MapCodec<EnchantmentDefinition> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                            RegistryCodecs.holderSet(Registries.ITEM).optionalFieldOf("supported_items").forGetter(EnchantmentDefinition::supportedItems),
                            RegistryCodecs.holderSet(Registries.ITEM).optionalFieldOf("primary_items").forGetter(EnchantmentDefinition::primaryItems),
                            Codec.BOOL.optionalFieldOf("force_primary_items_replacement", false).forGetter(EnchantmentDefinition::forcePrimaryItemsReplacement),
                            ValueProvider.CODEC.optionalFieldOf("weight").forGetter(EnchantmentDefinition::weight),
                            ValueProvider.CODEC.optionalFieldOf("max_level").forGetter(EnchantmentDefinition::maxLevel),
                            Cost.CODEC.optionalFieldOf("min_cost").forGetter(EnchantmentDefinition::minCost),
                            Cost.CODEC.optionalFieldOf("max_cost").forGetter(EnchantmentDefinition::maxCost),
                            ValueProvider.CODEC.optionalFieldOf("anvil_cost").forGetter(EnchantmentDefinition::anvilCost),
                            Slots.CODEC.forGetter(EnchantmentDefinition::slots)
                    )
                    .apply(i, EnchantmentDefinition::new)
    );
}