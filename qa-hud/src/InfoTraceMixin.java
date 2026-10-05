package suitehudqa;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import me.pajic.toolpouch.hud.InfoOverlays;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
/** Trace innermost renderer arguments after production wrappers apply placement. */
@Mixin(value=InfoOverlays.class,remap=false,priority=900)
public abstract class InfoTraceMixin {
    @WrapOperation(method="renderLine",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"))
    private static void observe(GuiGraphicsExtractor g,Font f,Component t,int x,int y,int color,boolean shadow,Operation<Void> original) {
        HudObservation.text(x,y,f.width(t),f.lineHeight);
        original.call(g,f,t,x,y,color,shadow);
    }
}
