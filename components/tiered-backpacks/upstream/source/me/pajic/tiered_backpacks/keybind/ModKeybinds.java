package me.pajic.tiered_backpacks.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import me.pajic.tiered_backpacks.TieredBackpacks;
import net.minecraft.client.KeyMapping;

//? >26.2 {
import org.lwjgl.sdl.SDLKeycode;
//?} else {
/*import org.lwjgl.glfw.GLFW;
*///?}

public class ModKeybinds {

    public static final KeyMapping.Category MOD_KEYS = new KeyMapping.Category(TieredBackpacks.id("keys"));

    public static final KeyMapping OPEN_BACKPACK = new KeyMapping(
			"key.tiered_backpacks.open_backpack",
            //~ if >26.2 'KEYSYM' -> 'KEYBOARD'
			InputConstants.Type.KEYBOARD,
            //~ if >26.2 'GLFW.GLFW_KEY_' -> 'SDLKeycode.SDLK_'
            SDLKeycode.SDLK_B,
			MOD_KEYS
	);

    public static void init() {}
}
