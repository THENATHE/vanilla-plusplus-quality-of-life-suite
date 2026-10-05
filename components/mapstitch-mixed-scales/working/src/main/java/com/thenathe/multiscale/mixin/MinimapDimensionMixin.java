package com.thenathe.multiscale.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.thenathe.multiscale.client.MixedScalesClient;
import me.pajic.mapstitch.minimap.MinimapOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinimapOverlay.class, remap = false)
public abstract class MinimapDimensionMixin {
    @Shadow private static ItemStack lastAtlas;
    @Unique private static Identifier mixedScales$markerDimension;
    @Unique private static long mixedScales$markerRevision;

    @Inject(method = "render", at = @At("HEAD"))
    private static void mixedScales$refreshDimension(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        var level = Minecraft.getInstance().level;
        var dimension = level == null ? null : level.dimension().identifier();
        long revision = MixedScalesClient.mapMetadataRevision();
        if (!java.util.Objects.equals(dimension, mixedScales$markerDimension) || revision != mixedScales$markerRevision) {
            mixedScales$markerDimension = dimension;
            mixedScales$markerRevision = revision;
            lastAtlas = ItemStack.EMPTY;
        }
    }

    @ModifyExpressionValue(method = "updateMarkers", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMapData(Lnet/minecraft/world/level/saveddata/maps/MapId;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;"))
    private static MapItemSavedData mixedScales$onlyCurrentDimension(MapItemSavedData data) {
        var level = Minecraft.getInstance().level;
        return data != null && level != null && data.dimension.equals(level.dimension()) ? data : null;
    }
}
