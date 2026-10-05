package suitehudqa.mixin;
import suitehudqa.HudObservation;
import me.pajic.toolpouch.hud.InfoOverlays;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Scope the observer to original pouch detail rendering. */
@Mixin(value=InfoOverlays.class,remap=false)
public abstract class InfoTraceMixin {
    @Inject(method="renderLines",at=@At("HEAD"))
    private static void start(GuiGraphicsExtractor g,CallbackInfo ci){HudObservation.inDetails=true;}
    @Inject(method="renderLines",at=@At("RETURN"))
    private static void stop(GuiGraphicsExtractor g,CallbackInfo ci){HudObservation.inDetails=false;}
}
