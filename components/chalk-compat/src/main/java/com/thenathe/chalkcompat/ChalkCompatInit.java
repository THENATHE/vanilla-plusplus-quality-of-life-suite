package com.thenathe.chalkcompat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/** Conversion recipes remain available without Polymer; suite capabilities are shared. */
public final class ChalkCompatInit implements ModInitializer {
    @Override public void onInitialize() {
        NativeClients.initialize();
    }
    public static boolean serverAvailable() {
        var loader = FabricLoader.getInstance();
        return loader.isModLoaded("chalk") && loader.isModLoaded("polymer-core")
                && loader.isModLoaded("polymer-resource-pack") && loader.isModLoaded("polymer-virtual-entity");
    }
}
