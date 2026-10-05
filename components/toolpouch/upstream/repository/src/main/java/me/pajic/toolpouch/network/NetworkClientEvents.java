package me.pajic.toolpouch.network;

import me.pajic.toolpouch.keybind.ScrollHandler;

public class NetworkClientEvents {

	public static void syncShulkerSlotToClient(int slot) {
		ScrollHandler.selectedShulkerSlot = slot;
	}

	public static void syncArrowSlotToClient(int slot) {
		ScrollHandler.selectedArrowSlot = slot;
	}
}
