package me.pajic.toolpouch.util;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.util.Objects;

public class ItemStackTemplateUtil {

	public static boolean isEmpty(ItemStackTemplate stack) {
		return stack == null || stack.item().value() == Items.AIR || stack.count() <= 0;
	}

	public static boolean has(ItemStackTemplate stack, DataComponentType<?> dataType) {
		return stack != null && stack.get(dataType) != null;
	}

	public static boolean isSameItemSameComponents(final ItemStackTemplate a, final ItemStackTemplate b) {
		if (a == null || b == null) return false;
		if (!a.is(b.item().value())) return false;
		else return (isEmpty(a) && isEmpty(b)) || Objects.equals(a.components(), b.components());
	}

	public static boolean nextDamageWillBreak(ItemStackTemplate stack) {
		return stack != null && isDamageableItem(stack) && getDamageValue(stack) >= getMaxDamage(stack) - 1;
	}

	private static boolean isDamageableItem(ItemStackTemplate stack) {
		return has(stack, DataComponents.MAX_DAMAGE) && !has(stack, DataComponents.UNBREAKABLE) && has(stack, DataComponents.DAMAGE);
	}

	private static int getDamageValue(ItemStackTemplate stack) {
		return Mth.clamp(stack.getOrDefault(DataComponents.DAMAGE, 0), 0, getMaxDamage(stack));
	}

	private static int getMaxDamage(ItemStackTemplate stack) {
		return stack.getOrDefault(DataComponents.MAX_DAMAGE, 0);
	}
}
