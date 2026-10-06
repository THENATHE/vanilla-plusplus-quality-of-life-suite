package com.thenathe.multiscale.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.thenathe.multiscale.client.MixedScalesClient;
import me.pajic.mapstitch.minimap.MinimapOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinimapOverlay.class, remap = false)
public abstract class MinimapDimensionMixin {
    @Shadow private static ItemStack lastAtlas;
    @Shadow @Final private static java.util.Map<MapId, Vector2i> CACHED_CENTERS;
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

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE",
            target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object mixedScales$authoritativeCenter(Object cached) {
        var level = Minecraft.getInstance().level;
        int active = lastAtlas.getOrDefault(me.pajic.mapstitch.component.ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
        if (level == null || active < 0) return cached;
        var id = new MapId(active);
        var data = level.getMapData(id);
        if (data == null || !data.dimension.equals(level.dimension())) return cached;
        // Old, shared or externally edited atlas entries can omit MAP_CENTER or
        // retain a stale item value. The synchronized saved map is authoritative.
        if (cached instanceof Vector2i center && center.x == data.centerX && center.y == data.centerZ) return center;
        var center = new Vector2i(data.centerX, data.centerZ);
        CACHED_CENTERS.put(id, center);
        return center;
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/MapItem;getSavedData(Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;"))
    private static MapItemSavedData mixedScales$activeCurrentDimension(MapItemSavedData data) {
        var level = Minecraft.getInstance().level;
        return data != null && level != null && data.dimension.equals(level.dimension()) ? data : null;
    }

    @ModifyExpressionValue(method = "updateMarkers", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMapData(Lnet/minecraft/world/level/saveddata/maps/MapId;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;"))
    private static MapItemSavedData mixedScales$onlyCurrentDimension(MapItemSavedData data) {
        var level = Minecraft.getInstance().level;
        return data != null && level != null && data.dimension.equals(level.dimension()) ? data : null;
    }
}
