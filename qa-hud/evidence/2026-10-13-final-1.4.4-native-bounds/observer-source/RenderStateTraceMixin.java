package suitehudqa.mixin;

import suitehudqa.HudObservation;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Native named map markers submit text state directly, bypassing graphics.text. */
@Mixin(GuiRenderState.class)
public abstract class RenderStateTraceMixin {
    @Inject(method="addGuiElement",at=@At("HEAD"))
    private void nativeElement(GuiElementRenderState element,CallbackInfo ci){
        if(HudObservation.inMap||HudObservation.inDetails)record(element.bounds(),false);
    }
    @Inject(method="addText",at=@At("HEAD"))
    private void nativeText(GuiTextRenderState text,CallbackInfo ci){
        if(HudObservation.inMap||HudObservation.inDetails)record(text.bounds(),HudObservation.inMapCanvas);
    }
    private void record(ScreenRectangle bounds,boolean mapName){
        if(bounds==null||bounds.width()<=0||bounds.height()<=0)return;
        float[] rectangle={bounds.left(),bounds.top(),bounds.right(),bounds.bottom()};
        if(HudObservation.inMap)HudObservation.mapBounds.add(rectangle);
        if(HudObservation.inDetails)HudObservation.detailDrawBounds.add(rectangle);
        if(mapName)HudObservation.mapLabelBounds.add(rectangle);
    }
}
