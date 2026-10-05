package me.pajic.tiered_backpacks.util;

import me.pajic.tiered_backpacks.TieredBackpacks;
import me.pajic.tiered_backpacks.compat.AccessoryUtil;
import me.pajic.tiered_backpacks.component.ModDataComponents;
import me.pajic.tiered_backpacks.item.ModItems;
import me.pajic.tiered_backpacks.platform.MultiLoaderUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;

public class BackpackUtil {

	public static final TagKey<Item> BACKPACKS = TagKey.create(Registries.ITEM, TieredBackpacks.id("backpacks"));

	public static void tryOpenBackpack(int openMethod, ServerPlayer player) {
		ItemStack backpack = switch (openMethod) {
			case 0 -> player.getInventory().getNonEquipmentItems().stream()
					.filter(stack -> stack.is(BACKPACKS))
					.findFirst().orElse(ItemStack.EMPTY);
			case 1 -> player.getItemBySlot(EquipmentSlot.CHEST);
			case 2 -> {
                if (AccessoryUtil.INSTANCE != null) yield AccessoryUtil.INSTANCE.getBackpack(player);
				yield ItemStack.EMPTY;
			}
			default -> ItemStack.EMPTY;
		};
		if (!backpack.isEmpty()) {
			player.playSound(SoundEvents.BUNDLE_INSERT);
			MultiLoaderUtil.INSTANCE.openBackpackScreen(player, backpack);
		}
	}

	public static BackpackDimensions getBackpackDimensions(ItemStack backpack) {
		return switch (backpack.getOrDefault(ModDataComponents.BACKPACK_TIER, BackpackTier.LEATHER)) {
			case LEATHER -> TieredBackpacks.CONFIG.leatherSize.get();
			case COPPER -> TieredBackpacks.CONFIG.copperSize.get();
			case IRON -> TieredBackpacks.CONFIG.ironSize.get();
			case GOLDEN -> TieredBackpacks.CONFIG.goldenSize.get();
			case DIAMOND -> TieredBackpacks.CONFIG.diamondSize.get();
			case NETHERITE -> TieredBackpacks.CONFIG.netheriteSize.get();
		};
	}

	public static Item.Properties createProperties(BackpackTier tier) {
		return tier == BackpackTier.NETHERITE ?
				TieredBackpacks.CONFIG.canEquipInChestSlot.get() ? new Item.Properties()
						.stacksTo(1)
						.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
						.component(ModDataComponents.BACKPACK_TIER, tier)
						.equippable(EquipmentSlot.CHEST)
						.fireResistant()
						.setId(ResourceKey.create(Registries.ITEM, TieredBackpacks.id(tier.getSerializedName() + "_backpack"))) :
						new Item.Properties()
						.stacksTo(1)
						.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
						.component(ModDataComponents.BACKPACK_TIER, tier)
						.fireResistant()
						.setId(ResourceKey.create(Registries.ITEM, TieredBackpacks.id(tier.getSerializedName() + "_backpack")))
				: TieredBackpacks.CONFIG.canEquipInChestSlot.get() ? new Item.Properties()
						.stacksTo(1)
						.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
						.component(ModDataComponents.BACKPACK_TIER, tier)
						.equippable(EquipmentSlot.CHEST)
						.setId(ResourceKey.create(Registries.ITEM, TieredBackpacks.id(tier.getSerializedName() + "_backpack"))) :
						new Item.Properties()
						.stacksTo(1)
						.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
						.component(ModDataComponents.BACKPACK_TIER, tier)
						.setId(ResourceKey.create(Registries.ITEM, TieredBackpacks.id(tier.getSerializedName() + "_backpack")));
	}

	public static boolean canUnequipBackpack(Player player, ItemStack backpack) {
		GameType mode = player.gameMode();
		if (mode != null && mode.isSurvival() && TieredBackpacks.CONFIG.preventUnequipWhenNotEmpty.get() && BackpackUtil.isValidContainerHolder(backpack)) {
			return backpack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY) == ItemContainerContents.EMPTY;
		}
		return true;
	}

	public static boolean isValidContainerHolder(ItemStack stack) {
		return stack.is(BACKPACKS) || isChestplateWithBackpackAttached(stack);
	}

	public static boolean isChestplateWithBackpackAttached(ItemStack stack) {
		return stack.is(ItemTags.CHEST_ARMOR) &&
				stack.has(DataComponents.CONTAINER) &&
				stack.has(ModDataComponents.BACKPACK_TIER);
	}

	public static ItemStack stackByTier(ItemStack input) {
		return switch (input.getOrDefault(ModDataComponents.BACKPACK_TIER, BackpackTier.LEATHER)) {
			case LEATHER -> new ItemStack(ModItems.LEATHER_BACKPACK);
			case COPPER -> new ItemStack(ModItems.COPPER_BACKPACK);
			case IRON -> new ItemStack(ModItems.IRON_BACKPACK);
			case GOLDEN -> new ItemStack(ModItems.GOLDEN_BACKPACK);
			case DIAMOND -> new ItemStack(ModItems.DIAMOND_BACKPACK);
			case NETHERITE -> new ItemStack(ModItems.NETHERITE_BACKPACK);
		};
	}
}
