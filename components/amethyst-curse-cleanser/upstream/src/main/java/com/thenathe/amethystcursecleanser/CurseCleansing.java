package com.thenathe.amethystcursecleanser;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Prediction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class CurseCleansing {
	private CurseCleansing() {
	}

	public static boolean isAmethystShard(final ItemStack stack) {
		return stack.is(Items.AMETHYST_SHARD);
	}

	public static boolean isEligibleCursedGear(final ItemStack stack) {
		if (stack.isEmpty() || !isGear(stack)) {
			return false;
		}

		return EnchantmentHelper.getEnchantmentsForCrafting(stack)
			.entrySet()
			.stream()
			.anyMatch(entry -> isRemovableCurse(entry.getKey()));
	}

	public static ItemStack createCleansedCopy(final ItemStack stack) {
		if (!isEligibleCursedGear(stack)) {
			return ItemStack.EMPTY;
		}

		ItemStack result = stack.copyWithCount(1);
		EnchantmentHelper.updateEnchantments(result, enchantments -> enchantments.removeIf(CurseCleansing::isRemovableCurse));
		return result;
	}

	public static Match findGrindstoneMatch(final ItemStack first, final ItemStack second) {
		if (isEligibleCursedGear(first) && isAmethystShard(second)) {
			return new Match(0, 1);
		}

		if (isAmethystShard(first) && isEligibleCursedGear(second)) {
			return new Match(1, 0);
		}

		return null;
	}

	public static boolean matchesSmithingInputs(final Container inputs) {
		return inputs.getItem(0).isEmpty()
			&& isEligibleCursedGear(inputs.getItem(1))
			&& isAmethystShard(inputs.getItem(2));
	}

	public static void consumeAndReward(final Container inputs, final int gearSlot, final int shardSlot, final Player player) {
		inputs.removeItem(gearSlot, 1);
		inputs.removeItem(shardSlot, 1);
		player.getInventory().placeItemBackInInventory(new ItemStack(Items.ECHO_SHARD), Prediction.SERVER_ONLY);
	}

	private static boolean isGear(final ItemStack stack) {
		return stack.isDamageableItem()
			|| stack.has(DataComponents.TOOL)
			|| stack.has(DataComponents.WEAPON)
			|| stack.has(DataComponents.EQUIPPABLE);
	}

	private static boolean isRemovableCurse(final Holder<Enchantment> enchantment) {
		return enchantment.is(Enchantments.BINDING_CURSE) || enchantment.is(Enchantments.VANISHING_CURSE);
	}

	public record Match(int gearSlot, int shardSlot) {
	}
}
