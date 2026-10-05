package me.pajic.toolpouch.util;

import it.unimi.dsi.fastutil.objects.ObjectBooleanImmutablePair;
import me.pajic.toolpouch.ToolPouch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;

import java.util.Optional;
import java.util.function.Predicate;

public class GameplayUtil {

	public static final TagKey<Item> TOOL_POUCHES = TagKey.create(Registries.ITEM, ToolPouch.id("tool_pouches"));

	public static boolean isValidContainerHolder(ItemStack stack) {
		return stack.is(TOOL_POUCHES) || isLeggingsWithPouchAttached(stack);
	}

	public static boolean isLeggingsWithPouchAttached(ItemStack stack) {
		return stack.is(ItemTags.LEG_ARMOR) && stack.has(DataComponents.CONTAINER);
	}

	public static boolean isHoldingProjectileWeapon(Player player) {
		return player.getMainHandItem().getItem() instanceof ProjectileWeaponItem ||
				player.getOffhandItem().getItem() instanceof ProjectileWeaponItem;
	}

	//? neoforge
	//@SuppressWarnings("deprecation")
	public static Predicate<ItemStackTemplate> getSupportedAmmo(Player player) {
		Item mainHand = player.getMainHandItem().getItem();
		Item offHand = player.getOffhandItem().getItem();
		if (mainHand instanceof ProjectileWeaponItem pwi) return itemStackTemplate -> pwi.getSupportedHeldProjectiles().test(itemStackTemplate.create());
		if (offHand instanceof ProjectileWeaponItem pwi) return itemStackTemplate -> pwi.getSupportedHeldProjectiles().test(itemStackTemplate.create());
		return _ -> false;
	}

	public static boolean canUnequipToolPouch(Player player, ItemStack backpack) {
		GameType mode = player.gameMode();
		if (mode != null && mode.isSurvival() && ToolPouch.CONFIG.preventUnequipWhenNotEmpty.get() && isValidContainerHolder(backpack)) {
			return backpack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY) == ItemContainerContents.EMPTY;
		}
		return true;
	}

	public static ObjectBooleanImmutablePair<Optional<TagKey<Item>>> itemMatches(ItemStack stack, String s) {
		if (s.startsWith("#")) {
			Identifier tagId = Identifier.tryParse(s.substring(1));
			if (tagId != null) {
				TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
				return new ObjectBooleanImmutablePair<>(Optional.of(tag), stack.is(tag));
			}
			return new ObjectBooleanImmutablePair<>(Optional.empty(), false);
		}
		else return new ObjectBooleanImmutablePair<>(Optional.empty(), stack.is(BuiltInRegistries.ITEM.getValue(Identifier.tryParse(s))));
	}

}
