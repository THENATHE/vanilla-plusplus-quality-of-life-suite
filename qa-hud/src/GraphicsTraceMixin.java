package suitehudqa.mixin;
import suitehudqa.HudObservation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Observe final renderer inputs after all caller-side placement wrappers. */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GraphicsTraceMixin {
    @Inject(method="text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",at=@At("HEAD"))
    private void observe(Font font,Component text,int x,int y,int color,boolean shadow,CallbackInfo ci){
        if(HudObservation.inDetails)HudObservation.text(x,y,font.width(text),font.lineHeight);
    }
}
