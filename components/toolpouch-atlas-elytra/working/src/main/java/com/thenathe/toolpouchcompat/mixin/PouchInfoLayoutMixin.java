package com.thenathe.toolpouchcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.thenathe.toolpouchcompat.HudLayout;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import me.pajic.toolpouch.hud.InfoOverlays;
import net.minecraft.client.gui.Font;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

/** Offset original text and background together; retain content, alignment and user settings. */
@Mixin(value = InfoOverlays.class, remap = false)
public abstract class PouchInfoLayoutMixin {
    @Shadow @Final private static List<ObjectIntImmutablePair<Component>> renderList;

    @ModifyExpressionValue(method = "renderLines", at = @At(value = "FIELD", target = "Lme/pajic/toolpouch/hud/MinimapOverlay;minimapActive:Z"))
    private static boolean suite$useActualMapBounds(boolean active) {
        // Both map renderers now use the same measured reservation below.
        return false;
    }

    @Inject(method = "renderLines", at = @At("HEAD"))
    private static void suite$beginDetails(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        Font font = Minecraft.getInstance().font;
        int width = renderList.stream().mapToInt(line -> font.width(line.left())).max().orElse(0);
        HudLayout.beginDetails(renderList.size(), width);
    }

    @WrapOperation(method = "renderLine", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private static void suite$background(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
        int shift = HudLayout.shift(graphics, x1, y1, x2, y2, x2 - x1 - 4);
        original.call(graphics, x1, y1 + shift, x2, y2 + shift, color);
    }

    @WrapOperation(method = "renderLine", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"))
    private static void suite$text(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color, boolean shadow, Operation<Void> original) {
        int width = font.width(text);
        int shadowSize = shadow ? 1 : 0;
        int shift = HudLayout.shift(graphics, x, y, x + width + shadowSize,
                y + font.lineHeight + shadowSize, width);
        original.call(graphics, font, text, x, y + shift, color, shadow);
    }
}
