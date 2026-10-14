package com.thenathe.toolpouchcompat.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Identify the native renderer that owns the current minimap's submissions. */
@Mixin(GuiGraphicsExtractor.class)
public interface HudLayoutGraphicsAccess {
    @Accessor("guiRenderState")
    GuiRenderState suite$renderState();
}
