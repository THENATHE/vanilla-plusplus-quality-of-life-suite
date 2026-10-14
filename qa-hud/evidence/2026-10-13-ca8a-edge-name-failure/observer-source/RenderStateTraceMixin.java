package suitehudqa.mixin;

import suitehudqa.HudObservation;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Native named map markers submit text state directly, bypassing graphics.text. */
@Mixin(GuiRenderState.class)
public abstract class RenderStateTraceMixin {
    @Inject(method="addText",at=@At("HEAD"))
    private void namedMapText(GuiTextRenderState text,CallbackInfo ci){
        if(!HudObservation.inMapCanvas)return;
        var bounds=text.bounds();
        if(bounds!=null && bounds.width()>0 && bounds.height()>0){
            float[] rectangle={bounds.left(),bounds.top(),bounds.right(),bounds.bottom()};
            HudObservation.mapBounds.add(rectangle);
            HudObservation.mapLabelBounds.add(rectangle);
        }
    }
}
