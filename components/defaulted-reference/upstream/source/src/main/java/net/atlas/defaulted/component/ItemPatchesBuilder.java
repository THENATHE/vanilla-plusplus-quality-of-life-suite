package net.atlas.defaulted.component;

import net.atlas.defaulted.base.BasePatchesBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;

import java.util.List;

public class ItemPatchesBuilder extends BasePatchesBuilder<Item, PatchGenerator, ItemPatches, DataComponentPatch> {
    public ItemPatchesBuilder(List<HolderSet<Item>> elements) {
        super(elements);
        this.data = DataComponentPatch.builder().build();
    }

    @Override
    public void writeData(String input, Object o) {
        if (input.equals("patch"))
            this.data = (DataComponentPatch) o;
    }

    @Override
    public ItemPatches build() {
        return new ItemPatches(this.elements, this.generators, this.data, this.priority);
    }
}
