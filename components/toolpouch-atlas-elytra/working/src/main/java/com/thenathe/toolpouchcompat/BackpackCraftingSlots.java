package com.thenathe.toolpouchcompat;

import me.pajic.tiered_backpacks.ui.BackpackSlot;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/** Optional addon recognition without making this compatibility module depend on it. */
public final class BackpackCraftingSlots {
    public static final String MENU_CLASS = "com.thenathe.backpackcrafting.menu.BackpackCraftingMenu";

    public static boolean supports(AbstractContainerMenu menu) {
        return menu.getClass().getName().equals(MENU_CLASS);
    }

    public static boolean maySort(AbstractContainerMenu menu, Slot slot) {
        if (menu instanceof ToolPouchMenu pouchMenu && !(slot.container instanceof Inventory)) {
            // Disabled pouch slots are tracked by index, so moving their items would change which items are enabled.
            return !ToolPouchUtil.isSlotDisabled(pouchMenu.getToolPouch(), slot.getContainerSlot());
        }
        return !supports(menu) || slot.container instanceof Inventory || slot instanceof BackpackSlot;
    }

    private BackpackCraftingSlots() {}
}
