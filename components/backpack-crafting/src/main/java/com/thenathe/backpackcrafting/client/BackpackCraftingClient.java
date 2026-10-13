package com.thenathe.backpackcrafting.client;

import com.thenathe.backpackcrafting.BackpackCrafting;
import com.thenathe.backpackcrafting.compat.SortedCompat;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public final class BackpackCraftingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(BackpackCrafting.MENU_TYPE, BackpackCraftingScreen::new);
        SortedCompat.initialize();
    }
}
