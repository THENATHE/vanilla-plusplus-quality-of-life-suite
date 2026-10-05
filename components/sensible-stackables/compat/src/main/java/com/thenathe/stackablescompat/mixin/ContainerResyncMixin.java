package com.thenathe.stackablescompat.mixin;

import com.thenathe.stackablescompat.StackablesCompat;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Vanilla predicts slot caps and upstream menu behavior differently; server results stay authoritative. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ContainerResyncMixin {
    @Shadow public ServerPlayer player;
    @Inject(method = "handleContainerClick", at = @At("TAIL"))
    private void stackables$confirmAuthoritativeResult(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        if (StackablesCompat.nativeClient(player.connection.getPacketContext())
                || packet.containerId() != player.containerMenu.containerId) return;
        // Do not interrupt the start/add-slot phases of a drag distribution gesture.
        if (packet.containerInput() == ContainerInput.QUICK_CRAFT && (packet.buttonNum() & 3) != 2) return;
        player.containerMenu.broadcastFullState();
    }
}
