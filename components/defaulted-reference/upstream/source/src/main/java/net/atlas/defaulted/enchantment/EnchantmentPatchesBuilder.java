package net.atlas.defaulted.enchantment;

import net.atlas.defaulted.base.BasePatchesBuilder;
import net.atlas.defaulted.enchantment.value_provider.ValueProvider;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public class EnchantmentPatchesBuilder extends BasePatchesBuilder<Enchantment, EnchantmentPatchGenerator, EnchantmentPatches, EnchantmentOverrides.Builder> {
    public EnchantmentPatchesBuilder(List<HolderSet<Enchantment>> elements) {
        super(elements);
        this.data = EnchantmentOverrides.builder();
    }

    @Override
    public void writeData(String input, Object o) {
        switch (input) {
            case "patch" -> this.data.patch((DataComponentPatch) o);
            case "description" -> this.data.description((Component) o);
            case "supported_items" -> this.data.supportedItems((HolderSet<Item>) o);
            case "primary_items" -> this.data.primaryItems((HolderSet<Item>) o);
            case "force_primary_items_replacement" -> this.data.forcePrimaryItemsReplacement((Boolean) o);
            case "weight" -> this.data.weight((ValueProvider) o);
            case "max_level" -> this.data.maxLevel((ValueProvider) o);
            case "min_cost" -> this.data.minCost((Cost) o);
            case "max_cost" -> this.data.maxCost((Cost) o);
            case "anvil_cost" -> this.data.anvilCost((ValueProvider) o);
            case "slots" -> this.data.slots((List<EquipmentSlotGroup>) o);
            case "added_slots" -> this.data.addedSlots((List<EquipmentSlotGroup>) o);
            case "removed_slots" -> this.data.removedSlots((List<EquipmentSlotGroup>) o);
            case "exclusive_set" -> this.data.exclusiveSet((HolderSet<Enchantment>) o);
        }
    }

    @Override
    public EnchantmentPatches build() {
        return new EnchantmentPatches(this.elements, this.data.build(), this.generators, this.priority);
    }
}
