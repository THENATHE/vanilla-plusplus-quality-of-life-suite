package com.thenathe.toolpouchcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.thenathe.toolpouchcompat.HudLayout;
import me.pajic.mapstitch.minimap.MinimapOverlay;
import me.pajic.mapstitch.platform.MultiVersionUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = MinimapOverlay.class, remap = false)
public abstract class AtlasHudLayoutMixin {
    @WrapMethod(method = "render")
    private static void suite$startFrame(GuiGraphicsExtractor graphics, Operation<Void> original) {
        HudLayout.beginMap(graphics);
        try {
            original.call(graphics);
        } finally {
            HudLayout.endMap();
        }
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
        HudLayout.scaled(x, y);
        original.call(util, graphics, x, y, z);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lme/pajic/mapstitch/platform/MultiVersionUtil;blitSprite(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;IIII)V"))
    private static void suite$background(MultiVersionUtil util, GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        HudLayout.background(x, y, x + width, y + height);
        original.call(util, graphics, texture, x, y, width, height);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private static void suite$clearBackground(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
        HudLayout.background(x1, y1, x2, y2);
        original.call(graphics, x1, y1, x2, y2, color);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
    private static void suite$textBounds(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color, Operation<Void> original) {
        // This native overload always draws its one-pixel shadow.
        HudLayout.background(x, y, x + font.width(text) + 1, y + font.lineHeight + 1);
        original.call(graphics, font, text, x, y, color);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lme/pajic/mapstitch/platform/MultiVersionUtil;renderMap(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;Lme/pajic/mapstitch/worldmap/WorldMapScreen$GridPos;Z)V"))
    private static void suite$mapBounds(MultiVersionUtil util, Minecraft client, GuiGraphicsExtractor graphics,
            MapId id, MapItemSavedData data, WorldMapScreen.GridPos grid, boolean minimap, Operation<Void> original) {
        HudLayout.drawnMap();
        original.call(util, client, graphics, id, data, grid, minimap);
    }
}
