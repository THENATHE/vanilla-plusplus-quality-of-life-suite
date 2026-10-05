package com.thenathe.toolpouchcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.thenathe.toolpouchcompat.HudLayout;
import me.pajic.toolpouch.hud.MinimapOverlay;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinimapOverlay.class, remap = false)
public abstract class PouchHudLayoutMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private static void suite$beginMap(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        HudLayout.beginPouchMap(graphics);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;"))
    private static Matrix3x2f suite$origin(Matrix3x2fStack pose, float x, float y, Operation<Matrix3x2f> original) {
        float originX = HudLayout.pouchOriginX(x);
        float originY = HudLayout.pouchOriginY(y);
        HudLayout.translated(originX, originY);
        return original.call(pose, originX, originY);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;scale(FF)Lorg/joml/Matrix3x2f;"))
    private static Matrix3x2f suite$scale(Matrix3x2fStack pose, float x, float y, Operation<Matrix3x2f> original) {
        HudLayout.scaled(y);
        return original.call(pose, x, y);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private static void suite$textureBounds(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        HudLayout.background(y, y + height);
        original.call(graphics, pipeline, texture, x, y, width, height);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private static void suite$backgroundBounds(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
        HudLayout.background(y1, y2);
        original.call(graphics, x1, y1, x2, y2, color);
    }
}
