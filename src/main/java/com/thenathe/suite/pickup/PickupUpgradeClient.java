package com.thenathe.suite.pickup;

import com.mojang.blaze3d.platform.InputConstants;
import me.pajic.toolpouch.keybind.ModKeybinds;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;

public final class PickupUpgradeClient {
    public static final KeyMapping TOGGLE_COLLECTOR = new KeyMapping(
            "key.thenathe_mod_suite.toggle_collector", InputConstants.Type.KEYBOARD,
            InputConstants.UNKNOWN.getValue(), ModKeybinds.MOD_KEYS);

    private PickupUpgradeClient() {}

    public static void initialize() {
        KeyMappingHelper.registerKeyMapping(TOGGLE_COLLECTOR);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_COLLECTOR.consumeClick()) {
                if (client.player != null && client.level != null && client.gui.screen() == null
                        && ClientPlayNetworking.canSend(PickupTogglePayload.TYPE)) {
                    ClientPlayNetworking.send(new PickupTogglePayload());
                }
            }
        });
    }
}
