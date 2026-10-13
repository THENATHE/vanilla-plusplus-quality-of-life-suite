package suitesettingsqa.mixin;

import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLVideo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Supplies normal focus events to the isolated input fixture; the wheel's guard still runs. */
@Mixin(targets = "com.thenathe.hotkeywheel.HotkeyWheelClient", remap = false)
public abstract class WheelFocusQaMixin {
    @Inject(method = "tick", at = @At("HEAD"), remap = false)
    private static void focusedInputFixture(Minecraft client, CallbackInfo callback) {
        if (!Boolean.getBoolean("settings.qa.wheel") || client.player == null) return;
        try (SDL_Event event = SDL_Event.calloc()) {
            event.window().type(SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED)
                    .windowID(SDLVideo.SDL_GetWindowID(client.getWindow().handle()));
            client.getWindow().handleEvent(event);
        }
    }
}
