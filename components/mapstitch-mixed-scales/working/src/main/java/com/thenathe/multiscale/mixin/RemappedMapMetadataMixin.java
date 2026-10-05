package com.thenathe.multiscale.mixin;

import com.thenathe.multiscale.MapMetadata;
import me.pajic.mapstitch.compat.RemappedCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Preserve MapStitch's existing Remapped packet path when that optional integration is installed. */
@Mixin(value = RemappedCompat.class, remap = false)
public abstract class RemappedMapMetadataMixin {
    @Inject(method = "sendMapPackets", at = @At("HEAD"))
    private static void mixedScales$metadataBeforeRemapped(MapId id, MapItemSavedData data, ServerPlayer player, CallbackInfo ci) {
        MapMetadata.send(id, data, player);
    }
}
