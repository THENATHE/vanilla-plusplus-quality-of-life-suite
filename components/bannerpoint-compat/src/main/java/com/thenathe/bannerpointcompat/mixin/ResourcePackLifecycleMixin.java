package com.thenathe.bannerpointcompat.mixin;

import com.thenathe.bannerpointcompat.BannerpointCompat;
import io.netty.channel.ChannelFutureListener;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ResourcePackLifecycleMixin {
    @Shadow @Final protected MinecraftServer server;

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"))
    private void observePackOffer(Packet<?> packet, ChannelFutureListener callback, CallbackInfo ci) {
        var context = ((PacketContextProvider) this).getPacketContext();
        if (packet instanceof ClientboundResourcePackPushPacket push) BannerpointCompat.packOffered(server, context, push);
        else if (packet instanceof ClientboundResourcePackPopPacket pop) BannerpointCompat.packRemoved(server, context, pop);
    }

    // TAIL runs after vanilla's server-thread check, including in the configuration phase.
    @Inject(method = "handleResourcePackResponse", at = @At("TAIL"))
    private void observeLoadedPack(ServerboundResourcePackPacket packet, CallbackInfo ci) {
        BannerpointCompat.packResponse(server, ((PacketContextProvider) this).getPacketContext(), packet);
    }

    @Inject(method = "onDisconnect", at = @At("TAIL"))
    private void forgetPack(DisconnectionDetails details, CallbackInfo ci) {
        BannerpointCompat.disconnected(((PacketContextProvider) this).getPacketContext());
    }
}
