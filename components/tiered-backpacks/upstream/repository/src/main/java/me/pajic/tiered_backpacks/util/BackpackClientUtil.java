package me.pajic.tiered_backpacks.util;

import me.pajic.tiered_backpacks.TieredBackpacks;
import me.pajic.tiered_backpacks.compat.AccessoryUtil;
import me.pajic.tiered_backpacks.keybind.ModKeybinds;
import me.pajic.tiered_backpacks.network.ModNetworking;
import me.pajic.tiered_backpacks.platform.MultiLoaderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class BackpackClientUtil {

	public static void onClientTick(Minecraft client) {
		if (ModKeybinds.OPEN_BACKPACK.consumeClick() && client.player != null && client.level != null) {
			ItemStack chestItem = ItemStack.EMPTY;
            if (AccessoryUtil.INSTANCE != null) chestItem = AccessoryUtil.INSTANCE.getBackpack(client.player);
			if (!chestItem.isEmpty()) {
				client.player.playSound(SoundEvents.BUNDLE_INSERT);
				MultiLoaderUtil.INSTANCE.sendToServer(new ModNetworking.C2SOpenBackpackPayload(2));
			}
			else {
				chestItem = client.player.getItemBySlot(EquipmentSlot.CHEST);
				if (BackpackUtil.isValidContainerHolder(chestItem)) {
					client.player.playSound(SoundEvents.BUNDLE_INSERT);
                    MultiLoaderUtil.INSTANCE.sendToServer(new ModNetworking.C2SOpenBackpackPayload(1));
				} else if (TieredBackpacks.CONFIG.canOpenFromInventory.get()) {
					if (client.player.getInventory().getNonEquipmentItems().stream().anyMatch(stack -> stack.is(BackpackUtil.BACKPACKS))) {
						client.player.playSound(SoundEvents.BUNDLE_INSERT);
                        MultiLoaderUtil.INSTANCE.sendToServer(new ModNetworking.C2SOpenBackpackPayload(0));
					}
				}
			}
		}
	}
}
