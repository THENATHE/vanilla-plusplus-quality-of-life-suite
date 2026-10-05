package me.pajic.toolpouch.menu;

import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.util.GameplayUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;

public class ToolPouchMenu extends AbstractContainerMenu {

	private final ItemStack toolPouch;
	private final int padding = 8;
	private final int titleSpace = 10;
	private SimpleContainer container;

	public ToolPouchMenu(int containerId, Inventory playerInventory, ItemStack toolPouch) {
		super(ModMenuTypes.TOOL_POUCH_MENU, containerId);
		this.toolPouch = toolPouch;
		if (GameplayUtil.isValidContainerHolder(toolPouch)) {
			int rows = ToolPouchUtil.getToolPouchRows(toolPouch);
			int columns = ToolPouchUtil.getToolPouchColumns(toolPouch);
			int width = getWidth(columns);
			int height = getHeight(rows);
			ItemContainerContents contents = toolPouch.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
			NonNullList<ItemStack> items = NonNullList.withSize(rows * columns, ItemStack.EMPTY);
			contents.copyInto(items);
			container = new SimpleContainer(rows * columns);
			for (int i = 0; i < items.size(); i++) container.setItem(i, items.get(i));
			for (int y = 0; y < rows; y++) {
				for (int x = 0; x < columns; x++) {
					int slotX = width / 2 - columns * 9 + x * 18;
					int slotY = padding + titleSpace + y * 18;
					addSlot(new ToolPouchSlot(container, y * columns + x, slotX, slotY, false));
				}
			}
			for (int y = 0; y < 3; ++y) {
				for (int x = 0; x < 9; ++x) {
					int slotX = width / 2 - 9 * 9 + x * 18;
					int slotY = height - padding - 4 * 18 - 3 + y * 18;
					this.addSlot(new ToolPouchSlot(playerInventory, x + y * 9 + 9, slotX, slotY, true));
				}
			}
			for (int x = 0; x < 9; ++x) {
				int slotX = width / 2 - 9 * 9 + x * 18;
				int slotY = height - padding - 4 * 18 - 3 + 3 * 18 + 4;
				this.addSlot(new ToolPouchSlot(playerInventory, x, slotX, slotY, true));
			}
		} else {
			removed(playerInventory.player);
		}
	}

	public ToolPouchMenu(int containerId, Inventory playerInventory, ModPayloads.S2CToolPouchScreenPayload payload) {
		this(containerId, playerInventory, payload.toolPouch());
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
		ToolPouchSlot slot = (ToolPouchSlot) slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			int size = ToolPouchUtil.getToolPouchSize(toolPouch);
			if (index < size) {
				if (!moveItemStackTo(itemStack2, size, slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!moveItemStackTo(itemStack2, 0, size, false)) {
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
		ToolPouchUtil.updateAttributeModifiersFromToolPouch(
				player,
                //~ if >26.2 'allItemsCopyStream' -> 'itemCopies'
				toolPouch.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).itemCopies().toList(),
				container.getItems()
		);
		if (container.getItems().isEmpty() || container.getItems().stream().allMatch(ItemStack::isEmpty)) {
			toolPouch.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		}
		else toolPouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.getItems()));
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return GameplayUtil.isValidContainerHolder(toolPouch);
	}

	public ItemStack getToolPouch() {
		return toolPouch;
	}
}
