package net.atlas.defaulted.enchantment;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Supplier;

public class EnchantmentBuilder implements DataComponentHolder {
    Component description;
    HolderSet<Item> supportedItems;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<HolderSet<Item>> primaryItems;
    int weight;
    int maxLevel;
    Enchantment.Cost minCost;
    Enchantment.Cost maxCost;
    int anvilCost;
    List<EquipmentSlotGroup> slots;
    HolderSet<Enchantment> exclusiveSet;
    PatchedDataComponentMap effects;
    boolean getsChanged = false;
    public EnchantmentBuilder(Component description, Enchantment.EnchantmentDefinition definition, HolderSet<Enchantment> exclusiveSet, DataComponentMap effects) {
        this.description = description;
        this.supportedItems = definition.supportedItems();
        this.primaryItems = definition.primaryItems();
        this.weight = definition.weight();
        this.maxLevel = definition.maxLevel();
        this.minCost = definition.minCost();
        this.maxCost = definition.maxCost();
        this.anvilCost = definition.anvilCost();
        this.slots = new ArrayList<>(definition.slots());
        this.exclusiveSet = exclusiveSet;
        this.effects = new PatchedDataComponentMap(effects);
    }

    public static EnchantmentBuilder of(Enchantment enchantment) {
        return new EnchantmentBuilder(enchantment.description(), enchantment.definition(), enchantment.exclusiveSet(), enchantment.effects());
    }

    public void setChanged(Supplier<Boolean> isSame) {
        this.getsChanged = this.getsChanged || !isSame.get();
    }

    public void setDescription(Component description) {
        setChanged(() -> Objects.equals(description, this.description));
        this.description = description;
    }

    public void setSupportedItems(HolderSet<Item> supportedItems) {
        setChanged(() -> Objects.equals(supportedItems, this.supportedItems));
        this.supportedItems = supportedItems;
    }

    public void setPrimaryItems(HolderSet<Item> primaryItems) {
        setChanged(() -> Objects.equals(primaryItems, this.primaryItems.orElse(null)));
        this.primaryItems = Optional.ofNullable(primaryItems);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setPrimaryItems(Optional<HolderSet<Item>> primaryItems) {
        setChanged(() -> Objects.equals(primaryItems, this.primaryItems));
        this.primaryItems = primaryItems;
    }

    public void setWeight(int weight) {
        setChanged(() -> this.weight == weight);
        this.weight = Mth.clamp(weight, 1, 1024);
    }

    public void setMaxLevel(int maxLevel) {
        setChanged(() -> this.maxLevel == maxLevel);
        this.maxLevel = Mth.clamp(maxLevel, 1, 255);
    }

    public void setMinCost(Enchantment.Cost minCost) {
        setChanged(() -> Objects.equals(minCost, this.minCost));
        this.minCost = minCost;
    }

    public void setMaxCost(Enchantment.Cost maxCost) {
        setChanged(() -> Objects.equals(maxCost, this.maxCost));
        this.maxCost = maxCost;
    }

    public void setAnvilCost(int anvilCost) {
        setChanged(() -> this.anvilCost == anvilCost);
        this.anvilCost = Math.max(anvilCost, 0);
    }

    public void setSlots(List<EquipmentSlotGroup> slots) {
        setChanged(() -> Objects.equals(slots, this.slots));
        this.slots = new ArrayList<>(slots);
    }

    public void setExclusiveSet(HolderSet<Enchantment> exclusiveSet) {
        setChanged(() -> Objects.equals(exclusiveSet, this.exclusiveSet));
        this.exclusiveSet = exclusiveSet;
    }

    public void modifySlots(List<EquipmentSlotGroup> added, List<EquipmentSlotGroup> removed) {
        if (added.isEmpty() && removed.isEmpty()) return;
        setChanged(() -> !(added.isEmpty() && removed.isEmpty()));
        this.slots.removeAll(removed);
        this.slots.addAll(added);
    }

    public void applyEffects(DataComponentPatch effects) {
        if (effects.isEmpty()) return;
        setChanged(() -> !effects.isEmpty());
        this.effects.applyPatch(effects);
    }

    public <T> void set(DataComponentType<T> type, T value) {
        setChanged(() -> Objects.equals(value, this.effects.get(type)));
        this.effects.set(type, value);
    }

    public void apply(EnchantmentOverrides overrides) {
        overrides.description().ifPresent(this::setDescription);
        apply(overrides.definition());
        overrides.exclusiveSet().ifPresent(this::setExclusiveSet);
        applyEffects(overrides.effectsPatch());
    }

    public void apply(EnchantmentDefinition definition) {
        definition.supportedItems().ifPresent(this::setSupportedItems);
        if (!definition.forcePrimaryItemsReplacement()) definition.primaryItems().ifPresent(this::setPrimaryItems);
        else this.setPrimaryItems(definition.primaryItems());
        definition.weight().ifPresent(weight -> this.setWeight(weight.roundedValue(Optional.of(this.weight))));
        definition.maxLevel().ifPresent(maxLevel -> this.setMaxLevel(maxLevel.roundedValue(Optional.of(this.maxLevel))));
        definition.anvilCost().ifPresent(anvilCost -> this.setAnvilCost(anvilCost.roundedValue(Optional.of(this.anvilCost))));
        definition.minCost().ifPresent(cost -> this.setMinCost(cost.toCost(this.minCost)));
        definition.maxCost().ifPresent(cost -> this.setMaxCost(cost.toCost(this.maxCost)));
        this.slots(definition.slots());
    }

    public void slots(Slots slots) {
        slots.slots().ifPresentOrElse(this::setSlots, () -> this.modifySlots(slots.addedSlots(), slots.removedSlots()));
    }

    public boolean isChanged() {
        return this.getsChanged;
    }

    public Enchantment build() {
        return new Enchantment(this.description,
                new Enchantment.EnchantmentDefinition(this.supportedItems,
                        this.primaryItems,
                        this.weight,
                        this.maxLevel,
                        this.minCost,
                        this.maxCost,
                        this.anvilCost,
                        Collections.unmodifiableList(this.slots)),
                this.exclusiveSet,
                this.effects);
    }

    @Override
    public @NonNull DataComponentMap getComponents() {
        return this.effects;
    }
}
