package me.pajic.tiered_backpacks.ui;

import me.pajic.tiered_backpacks.menu.ModMenuTypes;
import me.pajic.tiered_backpacks.network.ModNetworking;
import me.pajic.tiered_backpacks.util.BackpackDimensions;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BackpackMenu extends AbstractContainerMenu {

    private static final int MAX_CONTENTS_SIZE = 256;
    private final ItemStack backpack;
    private final int padding = 8;
    private final int titleSpace = 10;
    private final boolean valid;
    private boolean loaded;
    private SimpleContainer container = new SimpleContainer(0);

    public BackpackMenu(int containerId, Inventory playerInventory, ItemStack backpack) {
        super(ModMenuTypes.BACKPACK_MENU, containerId);
        this.backpack = backpack;
        this.valid = BackpackUtil.isValidContainerHolder(backpack);
        if (valid) {
            BackpackDimensions dimensions = BackpackUtil.getBackpackDimensions(backpack);
            int rows = dimensions.rows.get();
            int columns = dimensions.columns.get();
            int width = getWidth(columns);
            int height = getHeight(rows);
            ItemContainerContents contents = backpack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            int size = rows * columns;
            NonNullList<ItemStack> items = NonNullList.withSize(Math.max(size, MAX_CONTENTS_SIZE), ItemStack.EMPTY);
            contents.copyInto(items);
            container = new SimpleContainer(size) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    if (loaded) saveContents();
                }
            };
            for (int i = 0; i < size; i++) container.setItem(i, items.get(i));
            Player player = playerInventory.player;
            if (!player.level().isClientSide()) {
                List<ItemStack> overflow = items.subList(size, items.size()).stream().filter(stack -> !stack.isEmpty()).toList();
                if (!overflow.isEmpty()) {
                    saveContents();
                    overflow.forEach(stack -> BackpackUtil.giveOrDrop(player, stack));
                }
            }
            loaded = true;
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < columns; x++) {
                    int slotX = width / 2 - columns * 9 + x * 18 + 1;
                    int slotY = padding + titleSpace + y * 18 - 1;
                    addSlot(new BackpackSlot(container, y * columns + x, slotX, slotY));
                }
            }
            for (int y = 0; y < 3; ++y) {
                for (int x = 0; x < 9; ++x) {
                    int slotX = width / 2 - 9 * 9 + x * 18 + 1;
                    int slotY = height - padding - 4 * 18 - 3 + y * 18 - 1;
                    this.addSlot(new BackpackSlot(playerInventory, x + y * 9 + 9, slotX, slotY));
                }
            }
            for (int x = 0; x < 9; ++x) {
                int slotX = width / 2 - 9 * 9 + x * 18 + 1;
                int slotY = height - padding - 4 * 18 - 3 + 3 * 18 + 4 - 1;
                this.addSlot(new BackpackSlot(playerInventory, x, slotX, slotY));
            }
        }
    }

    public BackpackMenu(int containerId, Inventory playerInventory, ModNetworking.S2CBackpackScreenPayload payload) {
        this(containerId, playerInventory, payload.backpack());
    }

    public int getWidth(int columns) {
        return padding * 2 + Math.max(columns, 9) * 18;
    }

    public int getHeight(int rows) {
        return padding * 2 + titleSpace * 2 + 8 + (rows + 4) * 18;
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            BackpackDimensions dimensions = BackpackUtil.getBackpackDimensions(backpack);
            int rows = dimensions.rows.get();
            int columns = dimensions.columns.get();
            if (index < rows * columns) {
                if (!moveItemStackTo(itemStack2, rows * columns, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(itemStack2, 0, rows * columns, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (valid) saveContents();
    }

    private void saveContents() {
        backpack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.getItems()));
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return BackpackUtil.isValidContainerHolder(backpack);
    }

    public ItemStack getBackpack() {
        return backpack;
    }
}
