package me.pajic.toolpouch;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.pajic.toolpouch.compat.OkZoomerCompat;
import me.pajic.toolpouch.config.ModClientConfig;
import me.pajic.toolpouch.util.CompatFlags;

public class ToolPouchClient {

	public static ModClientConfig CONFIG = ConfigApiJava.registerAndLoadConfig(ModClientConfig::new, RegisterType.CLIENT);

    public static void onInitialize() {
        if (CompatFlags.OK_ZOOMER_LOADED) OkZoomerCompat.init();
    }
}
