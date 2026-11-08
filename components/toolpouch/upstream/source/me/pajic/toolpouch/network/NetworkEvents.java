package me.pajic.toolpouch.network;

import it.unimi.dsi.fastutil.ints.IntList;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.compat.AccessoryUtil;
import me.pajic.toolpouch.menu.ShulkerBoxContainerMenu;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.platform.MultiLoaderUtil;
import me.pajic.toolpouch.util.GameplayUtil;
import me.pajic.toolpouch.util.PlayerExtension;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.util.List;

public class NetworkEvents {

	public static void tryOpenToolPouch(ServerPlayer player, int openMethod) {
		ItemStack toolPouch = switch (openMethod) {
			// 2 - open from trinket/accessory API slot
			// 1 - open from leg slot
			// 0 - open from inventory
			case 0 -> !ToolPouch.CONFIG.canOpenFromInventory.get() ? ItemStack.EMPTY : player.getInventory().getNonEquipmentItems().stream()
					.filter(stack -> stack.is(GameplayUtil.TOOL_POUCHES))
					.findFirst().orElse(ItemStack.EMPTY);
			case 1 -> player.getItemBySlot(EquipmentSlot.LEGS);
			case 2 -> {
                if (AccessoryUtil.INSTANCE != null) yield AccessoryUtil.INSTANCE.tryGetToolPouch(player);
				yield ItemStack.EMPTY;
			}
			default -> ItemStack.EMPTY;
		};
		if (!toolPouch.isEmpty()) {
			player.playSound(SoundEvents.BUNDLE_INSERT);
			MultiLoaderUtil.INSTANCE.openToolPouchScreen(player, toolPouch);
		}
	}

	public static void openShulkerBox(ServerPlayer player, int index) {
		ItemStack toolPouch = ToolPouchUtil.getToolPouch(player);
		IntList slots = ToolPouchUtil.findSlotsInToolPouch(toolPouch, stack -> stack.is(ItemTags.SHULKER_BOXES));
		if (index < 0 || index >= slots.size()) return;
		int slot = slots.getInt(index);
		player.playSound(SoundEvents.SHULKER_BOX_OPEN);
		player.openMenu(new ShulkerBoxContainerMenu(toolPouch, slot, ToolPouchUtil.getItemInToolPouch(toolPouch, slot), 27));
		player.awardStat(Stats.OPEN_SHULKER_BOX);
	}

	public static void openEnderContainer(ServerPlayer player) {
		if (!ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(Items.ENDER_CHEST))) return;
		player.playSound(SoundEvents.ENDER_CHEST_OPEN);
		PlayerEnderChestContainer container = player.getEnderChestInventory();
		player.openMenu(new SimpleMenuProvider((i, inventory, _) ->
				ChestMenu.threeRows(i, inventory, container), Component.translatable("container.enderchest")
		));
		player.awardStat(Stats.OPEN_ENDERCHEST);
	}

	public static void playSound(ServerPlayer player, Holder<SoundEvent> sound) {
		if (
				(sound.is(SoundEvents.SPYGLASS_USE.location()) || sound.is(SoundEvents.SPYGLASS_STOP_USING.location())) &&
				ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(Items.SPYGLASS))
		) player.playSound(sound.value());
	}

	public static void toggleSlot(ServerPlayer player, int slot) {
		ItemStack toolPouch = player.containerMenu instanceof ToolPouchMenu menu ? menu.getToolPouch() : ToolPouchUtil.getToolPouch(player);
		if (GameplayUtil.isValidContainerHolder(toolPouch)) ToolPouchUtil.toggleSlot(toolPouch, slot);
	}

	public static void syncShulkerSlotToServer(ServerPlayer player, int slot) {
		((PlayerExtension) player).toolpouch$setShulkerSlot(slot);
	}

	public static void syncArrowSlotToServer(ServerPlayer player, int slot) {
		((PlayerExtension) player).toolpouch$setArrowSlot(slot);
	}

	public static void elytraBoostFromPouch(ServerPlayer player) {
		List<ItemStackTemplate> fireworks = ToolPouchUtil.getItemsFromToolPouch(player, stack -> stack.is(Items.FIREWORK_ROCKET));
		if (!fireworks.isEmpty() && player.isFallFlying()) {
			ItemStackTemplate firework = fireworks.getFirst();
			ItemStack rocket = firework.create();
			Projectile.spawnProjectile(new FireworkRocketEntity(player.level(), rocket, player), player.level(), rocket);
			ToolPouchUtil.removeItemFromToolPouch(player, firework, 1);
			player.awardStat(Stats.ITEM_USED.get(rocket.getItem()));
		}
	}
}
