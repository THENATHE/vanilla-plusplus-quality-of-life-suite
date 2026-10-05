package me.pajic.toolpouch.menu;

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
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShulkerBoxContainerMenu implements Container, MenuProvider {

    private final ItemStack shulker;
    protected NonNullList<ItemStack> items;
	private final int slotInToolPouch;

    public ShulkerBoxContainerMenu(ItemStack shulker, int size, int slotInToolPouch) {
        this.shulker = shulker;
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
		this.slotInToolPouch = slotInToolPouch;
    }

    @Override
    public void startOpen(@NotNull ContainerUser user) {
        if (user instanceof Player) {
            shulker.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
        }
    }

    @Override
    public void stopOpen(@NotNull ContainerUser user) {
        if (user instanceof Player player) {
            shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
			ToolPouchUtil.replaceItemInToolPouch(player, shulker, stack -> stack.is(ItemTags.SHULKER_BOXES), slotInToolPouch);
        }
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
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
    public void setChanged() {}

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
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
        return new ShulkerBoxMenu(i, inventory, this);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return !(Block.byItem(stack.getItem()) instanceof ShulkerBoxBlock);
    }
}
