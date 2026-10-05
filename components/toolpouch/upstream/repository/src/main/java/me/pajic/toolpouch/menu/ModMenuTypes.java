package me.pajic.toolpouch.menu;

import me.pajic.toolpouch.platform.MultiLoaderUtil;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {

	public static final MenuType<ToolPouchMenu> TOOL_POUCH_MENU = MultiLoaderUtil.INSTANCE.constructMenu();

	public static void init() {}
}
