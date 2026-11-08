package me.pajic.toolpouch.menu;

import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.util.GameplayUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ToolPouchMenu extends AbstractContainerMenu {

	private final ItemStack toolPouch;
	private final int padding = 8;
	private final int titleSpace = 10;
	private final boolean valid;
	private final SimpleContainer container;
	private boolean initialized = false;

	public ToolPouchMenu(int containerId, Inventory playerInventory, ItemStack toolPouch) {
		super(ModMenuTypes.TOOL_POUCH_MENU, containerId);
		this.toolPouch = toolPouch;
		this.valid = GameplayUtil.isValidContainerHolder(toolPouch);
		NonNullList<ItemStack> items = valid ? ToolPouchUtil.readItems(toolPouch) : NonNullList.create();
		container = new SimpleContainer(items.size()) {
			@Override
			public void setChanged() {
				super.setChanged();
				if (initialized) ToolPouchUtil.saveContainer(toolPouch, this);
			}
		};
		for (int i = 0; i < items.size(); i++) container.setItem(i, items.get(i));
		initialized = true;
		if (valid) {
			int rows = ToolPouchUtil.getToolPouchRows(toolPouch);
			int columns = ToolPouchUtil.getToolPouchColumns(toolPouch);
			int width = getWidth(columns);
			int height = getHeight(rows);
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
		if (!valid) return;
		if (player.isAlive() && player.level() instanceof ServerLevel serverLevel) {
			for (int i = ToolPouchUtil.getToolPouchSize(toolPouch); i < container.getContainerSize(); i++) {
				ItemStack stack = container.removeItemNoUpdate(i);
				if (!stack.isEmpty() && !player.addItem(stack)) player.spawnAtLocation(serverLevel, stack);
			}
		}
		ToolPouchUtil.saveContainer(toolPouch, container);
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return valid && GameplayUtil.isValidContainerHolder(toolPouch) &&
				(player.level().isClientSide() || ToolPouchUtil.isCarrying(player, toolPouch));
	}

	public ItemStack getToolPouch() {
		return toolPouch;
	}

	public SimpleContainer getContainer() {
		return container;
	}
}
