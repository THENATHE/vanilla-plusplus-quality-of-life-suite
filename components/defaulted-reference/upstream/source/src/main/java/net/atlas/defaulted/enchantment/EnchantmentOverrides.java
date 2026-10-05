package net.atlas.defaulted.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.defaulted.enchantment.value_provider.ValueProvider;
import net.atlas.defaulted.utils.DataComponentPatchUtils;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record EnchantmentOverrides(Optional<Component> description,
                                   EnchantmentDefinition definition,
                                   Optional<HolderSet<Enchantment>> exclusiveSet,
                                   DataComponentPatch effectsPatch) {
    public static final MapCodec<EnchantmentOverrides> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                            ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(EnchantmentOverrides::description),
                            EnchantmentDefinition.CODEC.forGetter(EnchantmentOverrides::definition),
                            RegistryCodecs.holderSet(Registries.ENCHANTMENT).optionalFieldOf("exclusive_set").forGetter(EnchantmentOverrides::exclusiveSet),
                            DataComponentPatchUtils.codec(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE).optionalFieldOf("patch", DataComponentPatch.EMPTY).forGetter(EnchantmentOverrides::effectsPatch)
                    )
                    .apply(i, EnchantmentOverrides::new)
    );

    public static EnchantmentOverrides.Builder builder() {
        return new EnchantmentOverrides.Builder();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private Component description;
        private HolderSet<Item> supportedItems;
        private HolderSet<Item> primaryItems;
        private boolean forcePrimaryItemsReplacement = false;
        private ValueProvider weight;
        private ValueProvider maxLevel;
        private Cost minCost;
        private Cost maxCost;
        private ValueProvider anvilCost;
        private List<EquipmentSlotGroup> slots;
        private List<EquipmentSlotGroup> addedSlots = Collections.emptyList();
        private List<EquipmentSlotGroup> removedSlots = Collections.emptyList();
        private HolderSet<Enchantment> exclusiveSet;
        private DataComponentPatch patch = DataComponentPatch.EMPTY;

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder supportedItems(HolderSet<Item> supportedItems) {
            this.supportedItems = supportedItems;
            return this;
        }

        public Builder primaryItems(HolderSet<Item> primaryItems) {
            this.primaryItems = primaryItems;
            return this;
        }

        public Builder forcePrimaryItemsReplacement(boolean forcePrimaryItemsReplacement) {
            this.forcePrimaryItemsReplacement = forcePrimaryItemsReplacement;
            return this;
        }

        public Builder weight(ValueProvider weight) {
            this.weight = weight;
            return this;
        }

        public Builder maxLevel(ValueProvider maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        public Builder minCost(Cost minCost) {
            this.minCost = minCost;
            return this;
        }

        public Builder maxCost(Cost maxCost) {
            this.maxCost = maxCost;
            return this;
        }

        public Builder anvilCost(ValueProvider anvilCost) {
            this.anvilCost = anvilCost;
            return this;
        }

        public Builder slots(List<EquipmentSlotGroup> slots) {
            this.slots = slots;
            return this;
        }

        public Builder addedSlots(List<EquipmentSlotGroup> addedSlots) {
            this.addedSlots = addedSlots;
            return this;
        }

        public Builder removedSlots(List<EquipmentSlotGroup> removedSlots) {
            this.removedSlots = removedSlots;
            return this;
        }

        public Builder exclusiveSet(HolderSet<Enchantment> exclusiveSet) {
            this.exclusiveSet = exclusiveSet;
            return this;
        }

        public Builder patch(DataComponentPatch patch) {
            this.patch = patch;
            return this;
        }

        public EnchantmentOverrides build() {
            return new EnchantmentOverrides(Optional.ofNullable(this.description),
                    new EnchantmentDefinition(Optional.ofNullable(this.supportedItems),
                            Optional.ofNullable(this.primaryItems),
                            this.forcePrimaryItemsReplacement,
                            Optional.ofNullable(this.weight),
                            Optional.ofNullable(this.maxLevel),
                            Optional.ofNullable(this.minCost),
                            Optional.ofNullable(this.maxCost),
                            Optional.ofNullable(this.anvilCost),
                            new Slots(Optional.ofNullable(this.slots),
                                    this.addedSlots,
                                    this.removedSlots)),
                    Optional.ofNullable(this.exclusiveSet),
                    this.patch);
        }
    }
}