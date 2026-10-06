package com.thenathe.stackablescompat.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.thenathe.stackablescompat.StackablesCompat;
import com.thenathe.stackablescompat.StackSizePayloadProjection;
import me.pajic.sensible_stackables.handler.StackSizeSyncPayload;
import me.pajic.sensible_stackables.platform.fabric.FabricLoaderUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

/** Native clients receive the full override table; fallback clients receive item metadata. */
@Mixin(value = FabricLoaderUtil.class, remap = false)
public abstract class StackablesNetworkingMixin {
    @WrapMethod(method = "s2c")
    private void suite$sendSupportedStackTable(ServerPlayer player, CustomPacketPayload payload,
                                              Operation<Void> original) {
        var context = player.connection.getPacketContext();
        boolean stackTable = payload instanceof StackSizeSyncPayload;
        if (ServerPlayNetworking.canSend(player, payload.type())
                && (!stackTable || StackablesCompat.nativeClient(context))) {
            original.call(player, stackTable
                    ? StackSizePayloadProjection.project((StackSizeSyncPayload) payload, context) : payload);
        }
        // A full snapshot replaces any previous client-side explicit limits after
        // hot updates, including an empty table that restores vanilla defaults.
        if (stackTable) {
            player.inventoryMenu.broadcastFullState();
            if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastFullState();
        }
    }
}
