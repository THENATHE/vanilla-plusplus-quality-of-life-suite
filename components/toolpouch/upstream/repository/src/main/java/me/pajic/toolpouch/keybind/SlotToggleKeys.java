package me.pajic.toolpouch.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.pajic.toolpouch.ToolPouchClient;
import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.platform.MultiLoaderUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SlotToggleKeys {

	private static final Set<InputConstants.Key> heldKeys = new HashSet<>();

	public static @Nullable InputConstants.Key getKey(int slot) {
		String name = ToolPouchClient.CONFIG.slotToggleKeys.get().get(slot);
		if (name == null) return null;
		try {
			InputConstants.Key key = InputConstants.getKey(name);
			return key.equals(InputConstants.UNKNOWN) ? null : key;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static void bind(int slot, @Nullable InputConstants.Key key) {
		Map<Integer, String> keys = new HashMap<>(ToolPouchClient.CONFIG.slotToggleKeys.get());
		if (key == null) keys.remove(slot);
		else keys.put(slot, key.getName());
		ToolPouchClient.CONFIG.slotToggleKeys.validateAndSet(keys);
		ToolPouchClient.CONFIG.save();
	}

	public static void unbind(int slot, ItemStack toolPouch) {
		bind(slot, null);
		if (ToolPouchUtil.isSlotDisabled(toolPouch, slot)) {
			ToolPouchUtil.toggleSlot(toolPouch, slot);
			MultiLoaderUtil.INSTANCE.sendToServer(new ModPayloads.C2SToggleSlotPayload(slot));
		}
	}

	public static IntList getSlots(InputConstants.Key key, ItemStack toolPouch) {
		IntList slots = new IntArrayList();
		int size = ToolPouchUtil.getToolPouchSize(toolPouch);
		for (Map.Entry<Integer, ? extends String> entry : ToolPouchClient.CONFIG.slotToggleKeys.get().entrySet()) {
			int slot = entry.getKey();
			if (entry.getValue().equals(key.getName()) && slot >= 0 && slot < size) slots.add(slot);
		}
		slots.sort(null);
		return slots;
	}

	public static boolean toggleSlots(LocalPlayer player, ItemStack toolPouch, InputConstants.Key key, boolean showMessage) {
		if (toolPouch.isEmpty()) return false;
		IntList slots = getSlots(key, toolPouch);
		if (slots.isEmpty()) return false;
		for (int slot : slots) {
			ToolPouchUtil.toggleSlot(toolPouch, slot);
			MultiLoaderUtil.INSTANCE.sendToServer(new ModPayloads.C2SToggleSlotPayload(slot));
		}
		if (showMessage) {
			if (slots.size() == 1) {
				int slot = slots.getInt(0);
				ItemStack stack = ToolPouchUtil.getItemInToolPouch(toolPouch, slot);
				Component name = stack.isEmpty() ? Component.translatable("text.toolpouch.slot", slot + 1) : stack.getHoverName();
				player.sendOverlayMessage(Component.translatable(
						ToolPouchUtil.isSlotDisabled(toolPouch, slot) ? "text.toolpouch.slot_disabled" : "text.toolpouch.slot_enabled", name
				));
			} else {
				player.sendOverlayMessage(Component.translatable("text.toolpouch.slots_toggled", slots.size()));
			}
		}
		return true;
	}

	public static void onKeySet(InputConstants.Key key, boolean down) {
		if (!down) {
			heldKeys.remove(key);
			return;
		}
		if (!heldKeys.add(key)) return;
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		//~ if >=26.2 'client.screen' -> 'client.gui.screen()'
		if (player == null || client.gui.screen() != null || player.isSpectator()) return;
		toggleSlots(player, ToolPouchUtil.getToolPouch(player), key, true);
	}

	public static void releaseAll() {
		heldKeys.clear();
	}
}
