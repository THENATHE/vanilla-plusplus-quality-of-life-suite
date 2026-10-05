package com.thenathe.toolpouchcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.thenathe.toolpouchcompat.HudLayout;
import me.pajic.mapstitch.minimap.MinimapOverlay;
import me.pajic.mapstitch.platform.MultiVersionUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinimapOverlay.class, remap = false)
public abstract class AtlasHudLayoutMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private static void suite$startFrame(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        HudLayout.beginMap(graphics);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lme/pajic/mapstitch/platform/MultiVersionUtil;translatePose(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFF)V"))
    private static void suite$origin(MultiVersionUtil util, GuiGraphicsExtractor graphics, float x, float y, float z, Operation<Void> original) {
        float originX = HudLayout.atlasOriginX(x);
        float originY = HudLayout.atlasOriginY(y);
        HudLayout.translated(originX, originY);
        original.call(util, graphics, originX, originY, z);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lme/pajic/mapstitch/platform/MultiVersionUtil;scalePose(Lnet/minecraft/client/gui/GuiGraphicsExtractor;FFF)V"))
    private static void suite$scale(MultiVersionUtil util, GuiGraphicsExtractor graphics, float x, float y, float z, Operation<Void> original) {
        HudLayout.scaled(y);
        original.call(util, graphics, x, y, z);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lme/pajic/mapstitch/platform/MultiVersionUtil;blitSprite(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;IIII)V"))
    private static void suite$background(MultiVersionUtil util, GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        HudLayout.background(y, y + height);
        original.call(util, graphics, texture, x, y, width, height);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private static void suite$clearBackground(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
        HudLayout.background(y1, y2);
        original.call(graphics, x1, y1, x2, y2, color);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
    private static void suite$textBounds(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color, Operation<Void> original) {
        HudLayout.drawnTo(y + font.lineHeight);
        original.call(graphics, font, text, x, y, color);
    }
}
