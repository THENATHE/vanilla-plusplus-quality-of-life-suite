package me.pajic.tiered_backpacks.util;

import me.pajic.tiered_backpacks.platform.MultiLoaderUtil;

public class CompatFlags {
	public static final boolean TRINKETS_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("trinkets_updated");
	public static final boolean OHMEGA_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("ohmega");
    public static final boolean CURIOS_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("ohmega");
	public static final boolean SHULKER_BOX_TOOLTIP_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("shulkerboxtooltip");
}
