package com.thenathe.toolpouchcompat.mixin;

import com.thenathe.toolpouchcompat.HudLayout;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Measure only submissions owned by the native minimap being rendered. */
@Mixin(GuiRenderState.class)
public abstract class HudLayoutRenderStateMixin {
    @Inject(method = "addGuiElement", at = @At("HEAD"))
    private void suite$mapElement(GuiElementRenderState element, CallbackInfo ci) {
        if (HudLayout.measuringMap((GuiRenderState) (Object) this)) {
            HudLayout.mapBounds(element.bounds());
        }
    }

    @Inject(method = "addText", at = @At("HEAD"))
    private void suite$mapText(GuiTextRenderState text, CallbackInfo ci) {
        if (HudLayout.measuringMap((GuiRenderState) (Object) this)) {
            // bounds() reuses the native prepared text, including glyph backgrounds.
            HudLayout.mapBounds(text.bounds());
        }
    }
}
