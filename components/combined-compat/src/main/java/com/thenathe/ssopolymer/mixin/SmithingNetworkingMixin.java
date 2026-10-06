package com.thenathe.ssopolymer.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.thenathe.combinedshim.NativeClients;
import com.thenathe.ssopolymer.RepairablePayloadProjection;
import me.pajic.simple_smithing_overhaul.platform.fabric.FabricLoaderUtil;
import me.pajic.simple_smithing_overhaul.repair.RepairableSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

/** Upstream custom repair tables are useful only to a compatible native receiver. */
@Mixin(value = FabricLoaderUtil.class, remap = false)
public abstract class SmithingNetworkingMixin {
    @WrapMethod(method = "s2c")
    private void suite$sendSupportedRepairTable(ServerPlayer player, CustomPacketPayload payload,
                                                Operation<Void> original) {
        var context = player.connection.getPacketContext();
        boolean repairTable = payload instanceof RepairableSyncPayload;
        if (ServerPlayNetworking.canSend(player, payload.type())
                && (!repairTable || NativeClients.isNative(context, "simple_smithing_overhaul"))) {
            original.call(player, repairTable
                    ? RepairablePayloadProjection.project((RepairableSyncPayload) payload, context) : payload);
        }
        // Effective defaults now live in the upstream registry, not Defaulted. Send
        // a fresh item snapshot after both populated and cleared config updates.
        if (repairTable) {
            player.inventoryMenu.broadcastFullState();
            if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastFullState();
        }
    }
}
