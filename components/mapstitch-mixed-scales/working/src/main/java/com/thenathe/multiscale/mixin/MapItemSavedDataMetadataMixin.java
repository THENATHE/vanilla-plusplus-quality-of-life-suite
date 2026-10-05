package com.thenathe.multiscale.mixin;

import com.thenathe.multiscale.MapMetadata;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Include ordinary held/pouch maps, which vanilla can first send in a different dimension. */
@Mixin(MapItemSavedData.class)
public abstract class MapItemSavedDataMetadataMixin {
    @Inject(method = "getUpdatePacket", at = @At("RETURN"))
    private void mixedScales$metadataBeforeUpdate(MapId id, Player player, CallbackInfoReturnable<Packet<?>> cir) {
        if (cir.getReturnValue() != null && player instanceof ServerPlayer serverPlayer)
            MapMetadata.send(id, (MapItemSavedData) (Object) this, serverPlayer);
    }
}
