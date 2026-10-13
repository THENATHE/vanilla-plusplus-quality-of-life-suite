package com.thenathe.backpackcrafting.menu;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** A temporary view used only by vanilla's server recipe placer, never installed on the player. */
final class CraftingInventory extends Inventory {
    private static final int PLAYER_SLOTS = 36;
    private final Inventory inventory;
    private final BackpackCraftingMenu menu;

    CraftingInventory(Inventory inventory, BackpackCraftingMenu menu) {
        super(inventory.player, new EntityEquipment());
        this.inventory = inventory;
        this.menu = menu;
    }

    @Override
    public int getContainerSize() { return PLAYER_SLOTS + menu.getStorage().getContainerSize(); }

    @Override
    public ItemStack getItem(int index) {
        return index < PLAYER_SLOTS ? inventory.getItem(index) : menu.getStorage().getItem(index - PLAYER_SLOTS);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return index < PLAYER_SLOTS ? inventory.removeItem(index, count)
                : menu.getStorage().removeItem(index - PLAYER_SLOTS, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return index < PLAYER_SLOTS ? inventory.removeItemNoUpdate(index)
                : menu.getStorage().removeItemNoUpdate(index - PLAYER_SLOTS);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index < PLAYER_SLOTS) inventory.setItem(index, stack);
        else menu.getStorage().setItem(index - PLAYER_SLOTS, stack);
    }

    @Override
    public NonNullList<ItemStack> getNonEquipmentItems() {
        NonNullList<ItemStack> items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        for (int index = 0; index < items.size(); index++) items.set(index, getItem(index));
        return items;
    }

    @Override
    public void fillStackedContents(StackedItemContents contents) {
        // The menu supplies crafting-grid and backpack counts through its usual recipe-book hook.
        menu.fillPlayerStackedContents(inventory, contents);
    }

    @Override
    public int findSlotMatchingCraftingIngredient(Holder<Item> ingredient, ItemStack current) {
        for (int index = 0; index < getContainerSize(); index++) {
            ItemStack stack = getItem(index);
            if (!stack.isEmpty() && menu.canUseIngredient(stack) && stack.is(ingredient)
                    && Inventory.isUsableForCrafting(stack)
                    && (current.isEmpty() || ItemStack.isSameItemSameComponents(current, stack))) return index;
        }
        return -1;
    }

    @Override
    public int getSlotWithRemainingSpace(ItemStack stack) {
        for (int index = 0; index < getContainerSize(); index++) {
            ItemStack current = getItem(index);
            if (mayReturnTo(index, stack) && !current.isEmpty()
                    && ItemStack.isSameItemSameComponents(current, stack)
                    && current.getCount() < limitAt(index, current)) return index;
        }
        return -1;
    }

    private boolean mayReturnTo(int index, ItemStack stack) {
        if (!menu.canUseIngredient(getItem(index))) return false;
        return index < PLAYER_SLOTS || menu.slots.get(menu.getStorageStart() + index - PLAYER_SLOTS).mayPlace(stack);
    }

    private int limitAt(int index, ItemStack stack) {
        return index < PLAYER_SLOTS ? inventory.getMaxStackSize(stack)
                : menu.getStorage().getMaxStackSize(stack);
    }

    boolean canClearGrid() {
        var destinations = new java.util.ArrayList<ItemStack>(getContainerSize());
        for (int index = 0; index < getContainerSize(); index++) destinations.add(getItem(index).copy());
        for (var slot : menu.getInputGridSlots()) {
            ItemStack returning = slot.getItem().copy();
            for (int pass = 0; pass < 2 && !returning.isEmpty(); pass++) {
                for (int index = 0; index < destinations.size() && !returning.isEmpty(); index++) {
                    ItemStack existing = destinations.get(index);
                    if (!mayReturnTo(index, returning) || (pass == 0 && existing.isEmpty())
                            || (pass == 1 && !existing.isEmpty())) continue;
                    if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, returning)) continue;
                    int moved = Math.min(returning.getCount(), limitAt(index, returning) - existing.getCount());
                    if (moved <= 0) continue;
                    destinations.set(index, existing.isEmpty() ? returning.copyWithCount(moved)
                            : existing.copyWithCount(existing.getCount() + moved));
                    returning.shrink(moved);
                }
            }
            if (!returning.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public void placeItemBackInInventory(ItemStack stack, boolean sendUpdate, Prediction prediction) {
        // Normal menu synchronization publishes both inventories; synthetic combined indexes must never go on the wire.
        for (int pass = 0; pass < 2 && !stack.isEmpty(); pass++) {
            for (int index = 0; index < getContainerSize() && !stack.isEmpty(); index++) {
                ItemStack existing = getItem(index);
                if (!mayReturnTo(index, stack) || (pass == 0 && existing.isEmpty())
                        || (pass == 1 && !existing.isEmpty())) continue;
                if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) continue;
                int available = limitAt(index, stack) - existing.getCount();
                int moved = Math.min(stack.getCount(), available);
                if (moved <= 0) continue;
                ItemStack result = existing.isEmpty() ? stack.copyWithCount(moved) : existing.copyWithCount(existing.getCount() + moved);
                stack.shrink(moved);
                setItem(index, result);
            }
        }
        if (!stack.isEmpty()) {
            player.drop(stack.copy(), false, prediction);
            stack.setCount(0);
        }
    }

    @Override
    public void setChanged() {
        inventory.setChanged();
        menu.getStorage().setChanged();
    }
}
