package me.pajic.misctweaks.platform.fabric;

//? fabric {

import me.pajic.misctweaks.platform.MultiLoaderUtil;
import net.fabricmc.loader.api.FabricLoader;

public class FabricLoaderUtil implements MultiLoaderUtil {

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
//?}
