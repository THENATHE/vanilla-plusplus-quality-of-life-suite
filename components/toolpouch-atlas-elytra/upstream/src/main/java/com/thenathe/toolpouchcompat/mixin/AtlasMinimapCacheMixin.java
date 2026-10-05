package com.thenathe.toolpouchcompat.mixin;

import java.lang.ref.WeakReference;
import java.util.Map;
import me.pajic.mapstitch.minimap.MinimapOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Map IDs are world-local; atlas metadata can also change while a map retains its ID. */
@Mixin(value = MinimapOverlay.class, remap = false)
public abstract class AtlasMinimapCacheMixin {
    @Shadow @Final private static Map<MapId, Vector2i> CACHED_CENTERS;
    @Shadow private static ItemStack lastAtlas;
    @Unique private static WeakReference<ClientLevel> toolpouchCompat$cacheLevel = new WeakReference<>(null);

    @Inject(method = "render", at = @At("HEAD"))
    private static void toolpouchCompat$worldChanged(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (toolpouchCompat$cacheLevel.get() != level) {
            toolpouchCompat$cacheLevel = new WeakReference<>(level);
            CACHED_CENTERS.clear();
            lastAtlas = ItemStack.EMPTY;
        }
    }

    @Inject(method = "updateMarkers", at = @At("HEAD"))
    private static void toolpouchCompat$atlasChanged(ItemStack atlas, CallbackInfo ci) {
        CACHED_CENTERS.clear();
    }
}
