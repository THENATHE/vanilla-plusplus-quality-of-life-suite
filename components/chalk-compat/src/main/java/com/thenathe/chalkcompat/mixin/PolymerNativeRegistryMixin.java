package com.thenathe.chalkcompat.mixin;

import java.util.List;

import com.thenathe.chalkcompat.NativeClients;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocol;
import eu.pb4.polymer.core.impl.networking.S2CPackets;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PolymerServerProtocol.class, remap = false)
public abstract class PolymerNativeRegistryMixin {
    @Inject(
            method = "sendSync(Lnet/minecraft/server/network/ServerGamePacketListenerImpl;Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$Type;Ljava/util/List;)V",
            at = @At("HEAD"), cancellable = true
    )
    private static void chalkcompat$preserveNativeRegistryIds(
            ServerGamePacketListenerImpl handler, CustomPacketPayload.Type<?> packetId,
            List<?> entries, CallbackInfo ci) {
        if (NativeClients.nativeBlocks(handler.getPacketContext())
                && (packetId.equals(S2CPackets.SYNC_ITEM_ID)
                || packetId.equals(S2CPackets.SYNC_DATA_COMPONENT_TYPE_ID)
                || packetId.equals(S2CPackets.SYNC_BLOCK_ID)
                || packetId.equals(S2CPackets.SYNC_BLOCKSTATE_ID))) {
            // Fabric's negotiated native item/component IDs are authoritative here. Polymer's
            // enhanced caches use original server IDs and would override those client mappings.
            // Its normal sync-clear packet still clears old caches; all other sync continues.
            // The original helper drains its batch, so cancellation must preserve that behavior.
            entries.clear();
            ci.cancel();
        }
    }
}
