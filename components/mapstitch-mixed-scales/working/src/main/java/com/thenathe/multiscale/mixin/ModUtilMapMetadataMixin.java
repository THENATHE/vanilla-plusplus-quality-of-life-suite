package com.thenathe.multiscale.mixin;

import com.thenathe.multiscale.MapMetadata;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModUtil.class, remap = false)
public abstract class ModUtilMapMetadataMixin {
    @Inject(method = "sendVanillaMapPacket", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private static void mixedScales$metadataBeforeColors(MapId id, MapItemSavedData data, ServerPlayer player,
                                                        boolean force, CallbackInfo ci) {
        // Send only when there is an actual vanilla map update, and before that update.
        // Normal getUpdatePacket is covered globally, including ordinary held maps.
        if (force) MapMetadata.send(id, data, player);
    }
}
