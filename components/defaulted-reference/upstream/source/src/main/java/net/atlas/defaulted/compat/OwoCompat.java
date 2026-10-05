package net.atlas.defaulted.compat;

import io.wispforest.owo.ext.DerivedComponentMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;

public class OwoCompat {
	public static DataComponentMap deriveComponentMap(ItemStack itemStack, DataComponentMap prototype) {
		DerivedComponentMap derived = new DerivedComponentMap(prototype);
		derived.derive(itemStack);
		return derived;
    }

    public static DataComponentMap unwrapDerivedComponentMap(DataComponentMap prototype) {
		if (prototype instanceof DerivedComponentMap derivedComponentMap) {
            try {
                Field base = DerivedComponentMap.class.getDeclaredField("base");
				base.setAccessible(true);
				return (DataComponentMap) base.get(derivedComponentMap);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
        return prototype;
    }

    public static boolean isDerived(DataComponentMap prototype) {
        return prototype instanceof DerivedComponentMap;
    }
}
