package com.thenathe.backpackcrafting.menu;

import com.thenathe.backpackcrafting.BackpackCrafting;
import java.util.List;
import me.pajic.tiered_backpacks.ui.BackpackSlot;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Prediction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;

/** Native backpack crafting, independent of the developer's menu implementation. */
public final class BackpackCraftingMenu extends AbstractCraftingMenu {
    private final Player player;
    private final ItemStack backpack;
    private final SimpleContainer storage;
    private final int rows;
    private final int columns;
    private final int storageStart;
    private final int inventoryStart;
    private boolean placingRecipe;

    public BackpackCraftingMenu(int id, Inventory inventory, ItemStack backpack, int gridSize) {
        super(BackpackCrafting.MENU_TYPE, id, checkedSize(gridSize), checkedSize(gridSize));
        this.player = inventory.player;
        this.backpack = backpack;
        var dimensions = BackpackUtil.getBackpackDimensions(backpack);
        rows = dimensions.rows.get();
        columns = dimensions.columns.get();
        storage = new BackpackStorage(backpack, rows * columns);
        int center = getStorageWidth() / 2;
        addResultSlot(player, getStorageWidth() + 16 + gridSize * 18 + 30,
                getCraftingPanelY() + 20 + (gridSize * 18 - 16) / 2);
        addCraftingGridSlots(getStorageWidth() + 16, getCraftingPanelY() + 20);
        storageStart = slots.size();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                addSlot(new BackpackSlot(storage, row * columns + column,
                        center - columns * 9 + column * 18 + 1, 18 + row * 18));
            }
        }
        inventoryStart = slots.size();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(playerSlot(inventory, 9 + row * 9 + column,
                        center - 80 + column * 18, getInventoryY() + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(playerSlot(inventory, column, center - 80 + column * 18, getInventoryY() + 58));
        }
    }

    private static int checkedSize(int size) {
        if (size != 2 && size != 3) throw new IllegalArgumentException("Crafting grid must be 2x2 or 3x3");
        return size;
    }

    private Slot playerSlot(Inventory inventory, int index, int x, int y) {
        return new Slot(inventory, index, x, y) {
            @Override
            public boolean mayPickup(Player owner) {
                return !isProtected(getItem());
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return !isProtected(getItem()) && stack != backpack;
            }
        };
    }

    private boolean isProtected(ItemStack stack) {
        return BackpackUtil.isValidContainerHolder(stack)
                || me.pajic.toolpouch.util.GameplayUtil.isValidContainerHolder(stack);
    }

    @Override
    public PostPlaceAction handlePlacement(boolean useMaxItems, boolean bypassClearTest, RecipeHolder<?> recipe,
                                         ServerLevel level, Inventory inventory) {
        if (!(recipe.value() instanceof CraftingRecipe craftingRecipe)
                || craftingRecipe.placementInfo().isImpossibleToPlace()
                || craftingRecipe.placementInfo().ingredients().size() > getGridWidth() * getGridHeight()
                || craftingRecipe instanceof ShapedRecipe shaped
                && (shaped.getWidth() > getGridWidth() || shaped.getHeight() > getGridHeight())) {
            return PostPlaceAction.NOTHING;
        }
        CraftingInventory sources = new CraftingInventory(inventory, this);
        if (!sources.canClearGrid()) return PostPlaceAction.NOTHING;
        return super.handlePlacement(useMaxItems, true, recipe, level, sources);
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents contents) {
        super.fillCraftSlotsStackedContents(contents);
        for (int slot = 0; slot < storage.getContainerSize(); slot++) {
            ItemStack stack = storage.getItem(slot);
            if (canUseIngredient(stack)) contents.accountSimpleStack(stack);
        }
    }

    @Override
    public void clicked(int slot, int button, ContainerInput input, Player owner) {
        if (input == ContainerInput.SWAP && button >= 0 && button < owner.getInventory().getContainerSize()
                && isProtected(owner.getInventory().getItem(button))) return;
        super.clicked(slot, button, input, owner);
    }

    @Override
    public void slotsChanged(Container container) {
        if (!placingRecipe && player != null && player.level() instanceof ServerLevel level) updateResult(level, null);
    }

    private void updateResult(ServerLevel level, RecipeHolder<CraftingRecipe> hint) {
        CraftingResultUpdater.update(this, level, player, craftSlots, resultSlots, hint);
    }

    @Override
    protected void beginPlacingRecipe() { placingRecipe = true; }

    @Override
    protected void finishPlacingRecipe(ServerLevel level, RecipeHolder<CraftingRecipe> recipe) {
        placingRecipe = false;
        updateResult(level, recipe);
    }

    @Override
    public ItemStack quickMoveStack(Player owner, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(owner)) return ItemStack.EMPTY;
        ItemStack moving = slot.getItem();
        ItemStack original = moving.copy();
        if (index == 0) {
            moving.getItem().onCraftedBy(moving, owner);
            boolean moved = moveItemStackTo(moving, inventoryStart, slots.size(), true);
            if (!moving.isEmpty()) moved |= moveItemStackTo(moving, storageStart, inventoryStart, false);
            if (!moved) return ItemStack.EMPTY;
            slot.onQuickCraft(moving, original);
        } else if (index < inventoryStart) {
            if (!moveItemStackTo(moving, inventoryStart, slots.size(), false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(moving, storageStart, inventoryStart, false)) {
            return ItemStack.EMPTY;
        }
        if (moving.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (moving.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(owner, moving);
        if (index == 0) owner.drop(moving, false, Prediction.PREDICTED);
        return original;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void removed(Player owner) {
        super.removed(owner);
        if (!owner.level().isClientSide()) clearContainer(owner, craftSlots);
    }

    @Override
    public boolean stillValid(Player owner) {
        return BackpackUtil.isValidContainerHolder(backpack)
                && (owner.level().isClientSide() || BackpackCrafting.hasBackpack(owner, backpack)
                && BackpackCrafting.getCraftingSize(owner) >= getGridWidth());
    }

    @Override public Slot getResultSlot() { return slots.get(0); }
    @Override public List<Slot> getInputGridSlots() { return slots.subList(1, storageStart); }
    @Override public RecipeBookType getRecipeBookType() { return RecipeBookType.CRAFTING; }
    @Override protected Player owner() { return player; }
    public ItemStack getBackpack() { return backpack; }
    public SimpleContainer getStorage() { return storage; }
    public boolean isStorageSlot(Slot slot) { return slot.container == storage; }
    boolean canUseIngredient(ItemStack stack) { return !isProtected(stack); }
    public void fillPlayerStackedContents(Inventory inventory, StackedItemContents contents) {
        for (int index = 0; index < 36; index++) {
            ItemStack stack = inventory.getItem(index);
            if (canUseIngredient(stack)) contents.accountSimpleStack(stack);
        }
    }
    public int getStorageStart() { return storageStart; }
    public int getInventoryStart() { return inventoryStart; }
    public int getStorageWidth() { return 16 + Math.max(9, columns) * 18; }
    public int getWidth() { return getStorageWidth() + 8 + getCraftingPanelWidth(); }
    public int getHeight() { return Math.max(getCraftingPanelHeight(), getInventoryY() + 82); }
    public int getCraftingPanelWidth() { return getGridWidth() * 18 + 64; }
    public int getCraftingPanelHeight() { return getGridHeight() * 18 + 56; }
    public int getCraftingPanelY() { return (getHeight() - getCraftingPanelHeight()) / 2; }
    public int getInventoryY() { return 18 + rows * 18 + 14; }

    /** Refreshes external collector changes and commits each menu mutation immediately. */
    private static final class BackpackStorage extends SimpleContainer {
        private final ItemStack backpack;
        private ItemContainerContents observed;
        BackpackStorage(ItemStack backpack, int size) {
            super(size);
            this.backpack = backpack;
            refresh();
        }
        private void refresh() {
            ItemContainerContents current = backpack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            if (current != observed) {
                current.copyInto(getItems());
                observed = current;
            }
        }
        @Override public ItemStack getItem(int slot) { refresh(); return super.getItem(slot); }
        @Override public ItemStack removeItem(int slot, int count) { refresh(); return super.removeItem(slot, count); }
        @Override public ItemStack removeItemNoUpdate(int slot) {
            refresh();
            ItemStack removed = super.removeItemNoUpdate(slot);
            setChanged();
            return removed;
        }
        @Override public void setItem(int slot, ItemStack stack) { refresh(); super.setItem(slot, stack); }
        @Override public void setChanged() {
            super.setChanged();
            // Configuration can shrink a tier after items were saved. Preserve hidden overflow.
            var saved = new java.util.ArrayList<>(observed.itemCopies().toList());
            while (saved.size() < getContainerSize()) saved.add(ItemStack.EMPTY);
            for (int index = 0; index < getContainerSize(); index++) saved.set(index, super.getItem(index));
            observed = ItemContainerContents.fromItems(saved);
            backpack.set(DataComponents.CONTAINER, observed);
        }
    }
}
