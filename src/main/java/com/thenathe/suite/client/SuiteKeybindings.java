package com.thenathe.suite.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/** A standard Controls binding; Minecraft owns the user's chosen key and persistence. */
public final class SuiteKeybindings {
    private static final Identifier CATEGORY_ID = Identifier.fromNamespaceAndPath(SuiteSettings.SCOPE, "settings");
    public static final KeyMapping OPEN_SETTINGS = new KeyMapping(
            "key.thenathe_mod_suite.open_settings", InputConstants.Type.KEYBOARD,
            InputConstants.UNKNOWN.getValue(), new KeyMapping.Category(CATEGORY_ID));
    private static boolean initialized;
    private SuiteKeybindings() {}

    public static void initialize() {
        if (initialized) return;
        initialized = true;
        KeyMapping.Category.register(CATEGORY_ID);
        KeyMappingHelper.registerKeyMapping(OPEN_SETTINGS);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_SETTINGS.consumeClick()) {
                // Do not replace chat, inventories or other screens while users type.
                if (client.player != null && client.level != null && client.gui.screen() == null) {
                    client.gui.setScreen(SuiteSettings.create(null));
                }
            }
        });
    }
}
