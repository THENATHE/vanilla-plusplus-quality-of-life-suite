package com.thenathe.suite.client;

import com.thenathe.suite.network.SuiteCapabilities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;

public final class SuiteClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        SuiteResources.initialize();
        SuiteCapabilities.initializeClient();
        SuiteSettings.initialize();
        SuiteKeybindings.initialize();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(
            ClientCommands.literal("suite-settings").executes(command -> {
                Minecraft client = Minecraft.getInstance();
                client.execute(() -> client.gui.setScreen(SuiteSettings.create(client.gui.screen())));
                return 1;
            })));
    }
}
