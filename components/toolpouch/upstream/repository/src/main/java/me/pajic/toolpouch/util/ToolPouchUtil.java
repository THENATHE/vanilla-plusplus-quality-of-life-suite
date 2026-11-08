package me.pajic.toolpouch.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.compat.AccessoryUtil;
import me.pajic.toolpouch.component.ModDataComponents;
import me.pajic.toolpouch.item.ModItems;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.menu.ToolPouchSlot;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ToolPouchUtil {

	private static FilteredContents filteredContentsCache = null;

	public static boolean toolPouchHasItem(Player player, Predicate<ItemStackTemplate> predicate) {
		return getToolPouchContents(player).nonEmptyItemsStream().anyMatch(predicate);
	}

	public static List<ItemStackTemplate> getItemsFromToolPouch(Player player, Predicate<ItemStackTemplate> predicate) {
		return getToolPouchContents(player).nonEmptyItemsStream().filter(predicate).toList();
	}

	public static ItemStack addItemToToolPouch(Player player, ItemStack stack) {
		ItemStack remainder = stack.copy();
		if (player.hasInfiniteMaterials()) return remainder;
		ItemStack toolPouch = getToolPouch(player);
		if (toolPouch.isEmpty()) return remainder;
		SimpleContainer container = getContainer(player, toolPouch);
		int size = getToolPouchSize(toolPouch);
		for (int i = 0; i < size && !remainder.isEmpty(); i++) {
			ItemStack existing = container.getItem(i);
			if (!existing.isEmpty() && !isSlotDisabled(toolPouch, i) && ItemStack.isSameItemSameComponents(existing, remainder)) {
				int maxStackSize = new ToolPouchSlot(container, i, 0, 0, false).getMaxStackSize(remainder);
				int amount = Math.min(remainder.getCount(), maxStackSize - existing.getCount());
				if (amount > 0) {
					existing.grow(amount);
					remainder.shrink(amount);
				}
			}
		}
		for (int i = 0; i < size && !remainder.isEmpty(); i++) {
			if (container.getItem(i).isEmpty() && !isSlotDisabled(toolPouch, i)) {
				remainder = new ToolPouchSlot(container, i, 0, 0, false).safeInsert(remainder);
			}
		}
		if (remainder.getCount() != stack.getCount()) saveContainer(toolPouch, container);
		return remainder;
	}

	public static void removeItemFromToolPouch(Player player, ItemStackTemplate template, int amount) {
		if (player.hasInfiniteMaterials() || amount <= 0) return;
		ItemStack toolPouch = getToolPouch(player);
		if (toolPouch.isEmpty()) return;
		SimpleContainer container = getContainer(player, toolPouch);
		ItemStack target = template.create();
		for (int i = 0; i < container.getContainerSize() && amount > 0; i++) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty() && !isSlotDisabled(toolPouch, i) && ItemStack.isSameItemSameComponents(stack, target)) {
				amount -= container.removeItem(i, amount).getCount();
			}
		}
		saveContainer(toolPouch, container);
	}

	public static ItemStack getItemInToolPouch(ItemStack toolPouch, int slot) {
		NonNullList<ItemStack> items = readItems(toolPouch);
		return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
	}

	public static void setItemInToolPouch(ItemStack toolPouch, int slot, ItemStack stack) {
		SimpleContainer container = makeContainer(toolPouch);
		if (slot < 0 || slot >= container.getContainerSize()) return;
		container.setItem(slot, stack);
		saveContainer(toolPouch, container);
	}

	public static IntList findSlotsInToolPouch(ItemStack toolPouch, Predicate<ItemStack> predicate) {
		NonNullList<ItemStack> items = readItems(toolPouch);
		IntList slots = new IntArrayList();
		for (int i = 0; i < items.size(); i++) {
			ItemStack stack = items.get(i);
			if (!stack.isEmpty() && !isSlotDisabled(toolPouch, i) && predicate.test(stack)) slots.add(i);
		}
		return slots;
	}

	public static List<Integer> getDisabledSlots(ItemStack toolPouch) {
		return toolPouch.getOrDefault(ModDataComponents.DISABLED_SLOTS, List.of());
	}

	public static boolean isSlotDisabled(ItemStack toolPouch, int slot) {
		return getDisabledSlots(toolPouch).contains(slot);
	}

	public static void toggleSlot(ItemStack toolPouch, int slot) {
		if (slot < 0 || slot >= getToolPouchSize(toolPouch)) return;
		List<Integer> disabled = new ArrayList<>(getDisabledSlots(toolPouch));
		if (!disabled.remove(Integer.valueOf(slot))) disabled.add(slot);
		disabled.sort(null);
		if (disabled.isEmpty()) toolPouch.remove(ModDataComponents.DISABLED_SLOTS);
		else toolPouch.set(ModDataComponents.DISABLED_SLOTS, List.copyOf(disabled));
	}

	public static void damageElytraInToolPouch(Player player) {
		if (
				player.hasInfiniteMaterials() ||
				!(player instanceof ServerPlayer serverPlayer)
		) return;
		ItemStack toolPouch = getToolPouch(player);
		if (toolPouch.isEmpty()) return;
		SimpleContainer container = getContainer(player, toolPouch);
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty() && !isSlotDisabled(toolPouch, i) && stack.has(DataComponents.GLIDER) && !stack.nextDamageWillBreak()) {
				stack.hurtAndBreak(1, serverPlayer.level(), serverPlayer, _ -> {});
				container.setChanged();
				saveContainer(toolPouch, container);
				return;
			}
		}
	}

	public static @Nullable ItemStackTemplate getElytraFromToolPouch(Player player, boolean allowBroken) {
		List<ItemStackTemplate> elytras = ToolPouchUtil.getItemsFromToolPouch(player, stack -> stack.get(DataComponents.GLIDER) != null);
		for (ItemStackTemplate stack : elytras) {
			if (allowBroken || !ItemStackTemplateUtil.nextDamageWillBreak(stack)) return stack;
		}
		return null;
	}

	public static @Nullable ItemStackTemplate getSelectedAmmo(Player player) {
		List<ItemStackTemplate> ammo = getItemsFromToolPouch(player, GameplayUtil.getSupportedAmmo(player));
		if (ammo.isEmpty()) return null;
		int slot = ((PlayerExtension) player).toolpouch$getArrowSlot();
		return ammo.get(slot >= 0 && slot < ammo.size() ? slot : 0);
	}

	public static void updateAttributeModifiersFromToolPouch(
			Player player, List<ItemStack> oldItems, List<ItemStack> newItems
	) {
		for (EquipmentSlotGroup group : EquipmentSlotGroup.values()) {
			oldItems.forEach(stack -> stack.forEachModifier(group, (attribute, modifier, _) -> {
				AttributeInstance instance = player.getAttribute(attribute);
				if (instance != null) instance.removeModifier(getPouchModifierId(modifier));
			}));
		}
		for (EquipmentSlotGroup group : EquipmentSlotGroup.values()) {
			newItems.forEach(stack -> stack.forEachModifier(group, (attribute, modifier, _) -> {
				AttributeInstance instance = player.getAttribute(attribute);
				if (instance != null) {
					if (instance.getPermanentModifiers().stream().anyMatch(m -> m.id().equals(modifier.id()))) {
						instance.removeModifier(modifier.id());
					}
					Identifier id = getPouchModifierId(modifier);
					instance.removeModifier(id);
					instance.addTransientModifier(new AttributeModifier(id, modifier.amount(), modifier.operation()));
				}
			}));
		}
	}

	private static Identifier getPouchModifierId(AttributeModifier modifier) {
		return ToolPouch.id("pouch/" + modifier.id().getNamespace() + "/" + modifier.id().getPath());
	}

	public static int getToolPouchRows(ItemStack toolPouch) {
		if (isNetheritePouch(toolPouch)) return ToolPouch.CONFIG.netheriteToolPouchRows.get();
		return ToolPouch.CONFIG.toolPouchRows.get();
	}

	public static int getToolPouchColumns(ItemStack toolPouch) {
		if (isNetheritePouch(toolPouch)) return ToolPouch.CONFIG.netheriteToolPouchColumns.get();
		return ToolPouch.CONFIG.toolPouchColumns.get();
	}

	private static boolean isNetheritePouch(ItemStack toolPouch) {
		return toolPouch.is(ModItems.NETHERITE_TOOL_POUCH) || toolPouch.getOrDefault(ModDataComponents.IS_NETHERITE_POUCH, false);
	}

	public static int getToolPouchSize(ItemStack toolPouch) {
		return getToolPouchRows(toolPouch) * getToolPouchColumns(toolPouch);
	}

	public static NonNullList<ItemStack> readItems(ItemStack toolPouch) {
		NonNullList<ItemStack> all = NonNullList.withSize(256, ItemStack.EMPTY);
		getToolPouchContents(toolPouch).copyInto(all);
		int size = getToolPouchSize(toolPouch);
		for (int i = all.size() - 1; i >= size; i--) {
			if (!all.get(i).isEmpty()) {
				size = i + 1;
				break;
			}
		}
		NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
		for (int i = 0; i < size; i++) items.set(i, all.get(i));
		return items;
	}

	public static void saveContainer(ItemStack toolPouch, SimpleContainer container) {
		List<ItemStack> items = container.getItems();
		toolPouch.set(
				DataComponents.CONTAINER,
				items.stream().allMatch(ItemStack::isEmpty) ? ItemContainerContents.EMPTY : ItemContainerContents.fromItems(items)
		);
	}

	private static SimpleContainer getContainer(Player player, ItemStack toolPouch) {
		if (player.containerMenu instanceof ToolPouchMenu menu && menu.getToolPouch() == toolPouch) return menu.getContainer();
		return makeContainer(toolPouch);
	}

	private static SimpleContainer makeContainer(ItemStack toolPouch) {
		NonNullList<ItemStack> items = readItems(toolPouch);
		SimpleContainer container = new SimpleContainer(items.size());
		for (int i = 0; i < items.size(); i++) container.setItem(i, items.get(i));
		return container;
	}

	private static ItemContainerContents getToolPouchContents(ItemStack toolPouch) {
		return toolPouch.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
	}

	public static ItemContainerContents getToolPouchContents(Player player) {
		ItemStack toolPouch = getToolPouch(player);
		ItemContainerContents contents = getToolPouchContents(toolPouch);
		List<Integer> disabled = getDisabledSlots(toolPouch);
		if (disabled.isEmpty()) return contents;
		FilteredContents cache = filteredContentsCache;
		if (cache != null && cache.contents == contents && cache.disabled == disabled) return cache.filtered;
		NonNullList<ItemStack> items = NonNullList.withSize(256, ItemStack.EMPTY);
		contents.copyInto(items);
		for (int slot : disabled) {
			if (slot >= 0 && slot < items.size()) items.set(slot, ItemStack.EMPTY);
		}
		ItemContainerContents filtered = items.stream().allMatch(ItemStack::isEmpty) ?
                ItemContainerContents.EMPTY : ItemContainerContents.fromItems(items);
		filteredContentsCache = new FilteredContents(contents, disabled, filtered);
		return filtered;
	}

	public static boolean isCarrying(Player player, ItemStack stack) {
		if (stack.isEmpty()) return false;
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			if (inventory.getItem(i) == stack) return true;
		}
		if (player.getItemBySlot(EquipmentSlot.LEGS) == stack) return true;
		return AccessoryUtil.INSTANCE != null && AccessoryUtil.INSTANCE.tryGetToolPouch(player) == stack;
	}

	public static ItemStack getToolPouch(Player player) {
		ItemStack pouch = ItemStack.EMPTY;
        if (AccessoryUtil.INSTANCE != null) pouch = AccessoryUtil.INSTANCE.tryGetToolPouch(player);
		if (!pouch.isEmpty()) return pouch;
		pouch = player.getItemBySlot(EquipmentSlot.LEGS);
		return GameplayUtil.isValidContainerHolder(pouch) ? pouch : ToolPouch.CONFIG.allowUseFromInventory.get() ?
				player.getInventory().getNonEquipmentItems()
				.stream().filter(stack -> stack.is(GameplayUtil.TOOL_POUCHES))
				.findFirst().orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
	}

	private record FilteredContents(ItemContainerContents contents, List<Integer> disabled, ItemContainerContents filtered) {}
}
