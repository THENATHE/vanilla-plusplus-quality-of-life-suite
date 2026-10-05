package me.pajic.tiered_backpacks.menu;

import me.pajic.tiered_backpacks.platform.MultiLoaderUtil;
import me.pajic.tiered_backpacks.ui.BackpackMenu;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {

	public static final MenuType<BackpackMenu> BACKPACK_MENU = MultiLoaderUtil.INSTANCE.constructMenu();

	public static void init() {}
}
