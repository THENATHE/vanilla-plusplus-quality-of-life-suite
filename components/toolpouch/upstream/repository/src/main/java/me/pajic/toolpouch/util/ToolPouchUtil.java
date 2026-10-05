package me.pajic.toolpouch.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.compat.AccessoryUtil;
import me.pajic.toolpouch.item.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;
import java.util.function.Predicate;

public class ToolPouchUtil {

	public static boolean toolPouchHasItem(Player player, Predicate<ItemStackTemplate> predicate) {
		return getToolPouchContents(player).nonEmptyItemsStream().anyMatch(predicate);
	}

	public static List<ItemStackTemplate> getItemsFromToolPouch(Player player, Predicate<ItemStackTemplate> predicate) {
		return getToolPouchContents(player).nonEmptyItemsStream().filter(predicate).toList();
	}

	public static ItemStack addItemToToolPouch(Player player, ItemStack stack) {
		ItemStack remainder = ItemStack.EMPTY;
		if (!player.hasInfiniteMaterials()) {
			ItemStack toolPouch = getToolPouch(player);
			SimpleContainer container = makeContainer(toolPouch);
			if (container.canAddItem(stack)) remainder = container.addItem(stack);
			toolPouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.getItems()));
		}
		return remainder;
	}

	public static void removeItemFromToolPouch(Player player, Item item, int amount) {
		if (!player.hasInfiniteMaterials()) {
			ItemStack toolPouch = getToolPouch(player);
			SimpleContainer container = makeContainer(toolPouch);
			container.removeItemType(item, amount);
			toolPouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.getItems()));
		}
	}

	public static void replaceItemInToolPouch(Player player, ItemStack stack, Predicate<ItemStack> predicate, int index) {
		ItemStack toolPouch = getToolPouch(player);
		SimpleContainer container = makeContainer(toolPouch);
		IntArrayList indices = new IntArrayList();
		for (int i = 0; i < container.getContainerSize(); i++) {
			if (predicate.test(container.getItem(i))) indices.add(i);
		}
		container.setItem(indices.getInt(index), stack);
		toolPouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.getItems()));
	}

	public static void updateElytraInToolPouch(Player player) {
		if (!player.hasInfiniteMaterials()) {
			ItemStackTemplate elytraTemplate = getElytraFromToolPouch(player, false);
			if (elytraTemplate != null && !ItemStackTemplateUtil.isEmpty(elytraTemplate)) {
				ItemStack elytra = elytraTemplate.create();
				elytra.setDamageValue(elytra.getDamageValue() + 1);
				ItemStack toolPouch = getToolPouch(player);
				SimpleContainer container = makeContainer(toolPouch);
				IntArrayList indices = new IntArrayList();
				for (int i = 0; i < container.getContainerSize(); i++) {
					if (elytra.is(container.getItem(i).getItem())) indices.add(i);
				}
				container.setItem(indices.getInt(0), elytra);
				toolPouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.getItems()));
			}
		}
	}

	public static ItemStackTemplate getElytraFromToolPouch(Player player, boolean allowBroken) {
		List<ItemStackTemplate> elytras = ToolPouchUtil.getItemsFromToolPouch(player, stack -> stack.get(DataComponents.GLIDER) != null);
		if (!elytras.isEmpty()) {
			for (ItemStackTemplate stack : elytras) {
				if (allowBroken || !ItemStackTemplateUtil.nextDamageWillBreak(stack)) return stack;
			}
		}
		return null;
	}

	@SuppressWarnings({"DataFlowIssue"})
	public static void updateAttributeModifiersFromToolPouch(
			Player player, List<ItemStack> oldItems, NonNullList<ItemStack> newItems
	) {
		for (EquipmentSlotGroup group : EquipmentSlotGroup.values()) {
			if (!oldItems.isEmpty()) oldItems.forEach(stack -> stack.forEachModifier(
					group, (attribute, modifier, _) ->
							player.getAttribute(attribute).removeModifier(modifier)
			));
			if (!newItems.isEmpty()) newItems.forEach(stack -> stack.forEachModifier(
					group, (attribute, modifier, _) ->
							player.getAttribute(attribute).addOrReplacePermanentModifier(modifier)
			));
		}
	}

	public static int getToolPouchRows(ItemStack toolPouch) {
		if (toolPouch.is(ModItems.NETHERITE_TOOL_POUCH)) return ToolPouch.CONFIG.netheriteToolPouchRows.get();
		return ToolPouch.CONFIG.toolPouchRows.get();
	}

	public static int getToolPouchColumns(ItemStack toolPouch) {
		if (toolPouch.is(ModItems.NETHERITE_TOOL_POUCH)) return ToolPouch.CONFIG.netheriteToolPouchColumns.get();
		return ToolPouch.CONFIG.toolPouchColumns.get();
	}

	public static int getToolPouchSize(ItemStack toolPouch) {
		return getToolPouchRows(toolPouch) * getToolPouchColumns(toolPouch);
	}

	private static SimpleContainer makeContainer(ItemStack toolPouch) {
		int size = getToolPouchSize(toolPouch);
		NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
		getToolPouchContents(toolPouch).copyInto(items);
		SimpleContainer container = new SimpleContainer(size);
		for (int i = 0; i < items.size(); i++) container.setItem(i, items.get(i));
		return container;
	}

	private static ItemContainerContents getToolPouchContents(ItemStack toolPouch) {
		return toolPouch.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
	}

	private static ItemContainerContents getToolPouchContents(Player player) {
		return getToolPouch(player).getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
	}

	private static ItemStack getToolPouch(Player player) {
		ItemStack pouch = ItemStack.EMPTY;
        if (AccessoryUtil.INSTANCE != null) pouch = AccessoryUtil.INSTANCE.tryGetToolPouch(player);
		if (!pouch.isEmpty()) return pouch;
		pouch = player.getItemBySlot(EquipmentSlot.LEGS);
		return GameplayUtil.isValidContainerHolder(pouch) ? pouch : ToolPouch.CONFIG.allowUseFromInventory.get() ?
				player.getInventory().getNonEquipmentItems()
				.stream().filter(stack -> stack.is(GameplayUtil.TOOL_POUCHES))
				.findFirst().orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
	}
}
