package me.pajic.misctweaks;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.pajic.misctweaks.config.ModClientConfig;
import me.pajic.misctweaks.mixson.AssetPatches;
import me.pajic.misctweaks.platform.MultiLoaderUtil;

public class MiscTweaksClient {

	public static ModClientConfig CONFIG = ConfigApiJava.registerAndLoadConfig(ModClientConfig::new, RegisterType.CLIENT);
	public static boolean RAISED_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("raised");

    public static void onInitialize() {
        AssetPatches.init();
    }
}
