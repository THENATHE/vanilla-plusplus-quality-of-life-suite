package com.thenathe.bannerpointcompat.mixin;

import me.pajic.bannerpoint.networking.NetworkingUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetworkingUtil.class, remap = false)
public abstract class NetworkingUtilMixin {
    @Inject(method = "s2c", at = @At("HEAD"), cancellable = true)
    private static void sendOnlySupportedNames(ServerPlayer receiver, CustomPacketPayload payload, CallbackInfo ci) {
        if (receiver.connection == null || !ServerPlayNetworking.canSend(receiver, payload.type())) ci.cancel();
    }
}
