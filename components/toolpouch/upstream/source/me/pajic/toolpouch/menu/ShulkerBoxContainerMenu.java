package me.pajic.toolpouch.menu;

import me.pajic.toolpouch.util.GameplayUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShulkerBoxContainerMenu implements Container, MenuProvider {

	private final ItemStack toolPouch;
	private final int slotInToolPouch;
    private final ItemStack shulker;
    protected NonNullList<ItemStack> items;

    public ShulkerBoxContainerMenu(ItemStack toolPouch, int slotInToolPouch, ItemStack shulker, int size) {
		this.toolPouch = toolPouch;
		this.slotInToolPouch = slotInToolPouch;
        this.shulker = shulker;
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public void startOpen(@NotNull ContainerUser user) {
        if (user instanceof Player) {
            shulker.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
        }
    }

    @Override
    public void stopOpen(@NotNull ContainerUser user) {
        if (user instanceof Player) save();
    }

	private void save() {
		if (!ToolPouchUtil.getItemInToolPouch(toolPouch, slotInToolPouch).is(ItemTags.SHULKER_BOXES)) return;
		shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
		ToolPouchUtil.setItemInToolPouch(toolPouch, slotInToolPouch, shulker.copy());
	}

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(items, slot, amount);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, @Nullable ItemStack stack) {
        this.items.set(slot, stack == null ? ItemStack.EMPTY : stack);
        if (stack != null && stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
    }

    @Override
    public void setChanged() {
		save();
	}

    @Override
    public boolean stillValid(@NotNull Player player) {
        return ToolPouchUtil.getToolPouch(player) == toolPouch &&
				ToolPouchUtil.getItemInToolPouch(toolPouch, slotInToolPouch).is(ItemTags.SHULKER_BOXES);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return shulker.getHoverName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        return new ShulkerBoxMenu(i, inventory, this) {
			@Override
			public void clicked(int slotIndex, int buttonNum, @NotNull ContainerInput input, @NotNull Player player) {
				if (!movesToolPouch(this, slotIndex, buttonNum, input, player)) super.clicked(slotIndex, buttonNum, input, player);
			}
		};
    }

	private static boolean movesToolPouch(AbstractContainerMenu menu, int slotIndex, int buttonNum, ContainerInput input, Player player) {
		Inventory inventory = player.getInventory();
		if (
				input == ContainerInput.SWAP && buttonNum >= 0 && buttonNum < inventory.getContainerSize() &&
				GameplayUtil.isValidContainerHolder(inventory.getItem(buttonNum))
		) return true;
		return slotIndex >= 0 && slotIndex < menu.slots.size() && GameplayUtil.isValidContainerHolder(menu.slots.get(slotIndex).getItem());
	}

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return !(Block.byItem(stack.getItem()) instanceof ShulkerBoxBlock);
    }
}
