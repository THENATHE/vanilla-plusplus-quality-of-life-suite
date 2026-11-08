package suitehudqa.mixin;

import suitehudqa.HudObservation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** MapStitch 1.2 draws minimap markers and their names itself, outside the vanilla map canvas. */
@Mixin(targets = "me.pajic.mapstitch.util.ModClientUtil", remap = false)
public abstract class DecorationTraceMixin {
    @Inject(method = "renderDecorations", at = @At("HEAD"))
    private static void begin(Minecraft client, GuiGraphicsExtractor graphics, java.util.List<?> decorations, float scale, CallbackInfo ci) {
        HudObservation.inMapCanvas = HudObservation.inMap;
    }

    @Inject(method = "renderDecorations", at = @At("RETURN"))
    private static void end(Minecraft client, GuiGraphicsExtractor graphics, java.util.List<?> decorations, float scale, CallbackInfo ci) {
        HudObservation.inMapCanvas = false;
    }
}
