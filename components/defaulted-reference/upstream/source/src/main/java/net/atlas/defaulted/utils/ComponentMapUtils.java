package net.atlas.defaulted.utils;

import net.atlas.defaulted.mixin.PatchedDataComponentMapAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;

public class ComponentMapUtils {
    public static DataComponentMap prototype(Holder<Item> holder) {
        return (components(holder) instanceof PatchedDataComponentMap patchedDataComponentMap ?
                PatchedDataComponentMapAccessor.class.cast(patchedDataComponentMap).defaulted$getPrototype() :
                components(holder));
    }

    public static DataComponentMap components(Holder<Item> holder) {
        //? >=26.1 {
        return holder.components();
        //?} <26.1 {
        /*return holder.value().components();
        *///?}
    }
}
