package me.pajic.toolpouch.util;

import me.pajic.toolpouch.platform.MultiLoaderUtil;

public class CompatFlags {
	public static final boolean IMMERSIVE_OVERLAYS_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("immersiveoverlays");
	public static final boolean RAISED_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("raised");
	public static final boolean SBT_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("shulkerboxtooltip");
	public static final boolean SEASONS_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("sereneseasons");
	public static final boolean TRINKETS_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("trinkets_updated");
	public static final boolean OHMEGA_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("ohmega");
    public static final boolean CURIOS_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("curios");
	public static final boolean OK_ZOOMER_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("ok_zoomer");
    public static final boolean ZOOMIFY_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("zoomify");

    public static boolean zoomModPresent() {
        return ZOOMIFY_LOADED || OK_ZOOMER_LOADED;
    }
}
