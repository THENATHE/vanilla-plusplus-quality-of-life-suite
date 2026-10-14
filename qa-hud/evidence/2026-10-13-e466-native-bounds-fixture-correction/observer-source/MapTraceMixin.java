package suitehudqa.mixin;

import suitehudqa.HudObservation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Scope measurements to the actual native atlas and ordinary-map draw calls. */
@Mixin(targets = {"me.pajic.mapstitch.minimap.MinimapOverlay", "me.pajic.toolpouch.hud.MinimapOverlay"}, remap = false)
public abstract class MapTraceMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private static void begin(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        HudObservation.inMap = true;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private static void end(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        HudObservation.inMap = false;
    }
}
