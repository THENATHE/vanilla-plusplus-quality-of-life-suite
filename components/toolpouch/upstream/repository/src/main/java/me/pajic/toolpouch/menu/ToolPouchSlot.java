package me.pajic.toolpouch.menu;

import it.unimi.dsi.fastutil.objects.ObjectBooleanImmutablePair;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.util.AllowedItem;
import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class ToolPouchSlot extends Slot {

	private final boolean isPlayerInventory;

	public ToolPouchSlot(Container container, int slot, int x, int y, boolean isPlayerInventory) {
		super(container, slot, x, y);
		this.isPlayerInventory = isPlayerInventory;
	}

	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		return isPlayerInventory || canPlace(stack);
	}

	@Override
	public boolean mayPickup(@NotNull Player player) {
		return !GameplayUtil.isValidContainerHolder(getItem());
	}

	@Override
	public int getMaxStackSize(@NonNull ItemStack stack) {
		if (isPlayerInventory) return super.getMaxStackSize(stack);
		Optional<AllowedItem> allowedItem = ToolPouch.CONFIG.allowedItems.stream().filter(ai ->
				GameplayUtil.itemMatches(stack, ai.id.get()).rightBoolean()
		).findFirst();
		if (allowedItem.isPresent()) {
			int s = allowedItem.get().maxStackSize.get();
			return s == 0 ? super.getMaxStackSize(stack) : s;
		}
		return super.getMaxStackSize(stack);
	}

	private boolean canPlace(ItemStack stack) {
		return ToolPouch.CONFIG.allowedItems.stream().anyMatch(allowedItem -> stackAllowed(stack, allowedItem));
	}

	private boolean stackAllowed(ItemStack stack, AllowedItem allowedItem) {
		ObjectBooleanImmutablePair<Optional<TagKey<Item>>> pair = GameplayUtil.itemMatches(stack, allowedItem.id.get());
		return pair.rightBoolean() && stackCountCheck(stack, allowedItem.maxStackCount.get(), pair.left());
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private boolean stackCountCheck(ItemStack stack, int maxStackCount, Optional<TagKey<Item>> tag) {
		if (maxStackCount == 0) return true;
		int containerStackCount = 0;
		for (ItemStack containerStack : container) {
			if (tag.isPresent() && containerStack.is(tag.get())) containerStackCount++;
			else if (containerStack.is(stack.getItem())) containerStackCount++;
		}
		return containerStackCount < maxStackCount;
	}
}
