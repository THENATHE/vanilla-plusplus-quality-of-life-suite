package com.thenathe.multiscale;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Inserts what fits and leaves the remainder in the stack, also for creative players. */
public final class InventoryInsertion {
    private InventoryInsertion() {}

    public static void insert(Player player, ItemStack stack) {
        var inventory = player.getInventory();
        if (!player.hasInfiniteMaterials()) {
            inventory.add(stack);
            return;
        }
        // Inventory.add empties the stack for creative players even when nothing fits,
        // which would silently delete retained atlases and extracted maps.
        while (!stack.isEmpty()) {
            int slot = inventory.getSlotWithRemainingSpace(stack);
            if (slot >= 0) {
                var existing = inventory.getItem(slot);
                int moved = Math.min(stack.getCount(), inventory.getMaxStackSize(existing) - existing.getCount());
                if (moved > 0) {
                    existing.grow(moved);
                    stack.shrink(moved);
                    continue;
                }
            }
            slot = inventory.getFreeSlot();
            if (slot < 0) return;
            inventory.setItem(slot, stack.split(Math.min(stack.getCount(), inventory.getMaxStackSize(stack))));
        }
    }
}
