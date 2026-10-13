package com.thenathe.backpackcrafting.client;

import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import java.util.function.Supplier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/** Keeps temporary crafting slots outside Sorted's global keep-rule interface. */
public final class SortedRuleContext {
    private static final ScopedValue<Boolean> SUPPRESSED = ScopedValue.newInstance();

    private SortedRuleContext() { }

    public static boolean isCraftingSlot(AbstractContainerMenu menu, Slot slot) {
        return menu instanceof BackpackCraftingMenu backpack && slot != null
                && slot.index >= 0 && slot.index < backpack.getStorageStart()
                && backpack.getSlot(slot.index) == slot;
    }

    public static boolean isSuppressed() {
        return SUPPRESSED.orElse(false);
    }

    public static <T> T withoutRules(Supplier<T> action) {
        return ScopedValue.where(SUPPRESSED, true).call(action::get);
    }
}
