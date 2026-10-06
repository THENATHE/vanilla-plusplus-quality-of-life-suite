package com.thenathe.multiscale;

import com.thenathe.suite.network.SuiteCapabilities;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/** Refresh native atlas view caches without discarding synchronized map data. */
public record MapRefresh() implements CustomPacketPayload {
    public static final Type<MapRefresh> TYPE = new Type<>(Identifier.parse("mapstitch_mixed_scales:refresh_maps_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MapRefresh> CODEC = StreamCodec.unit(new MapRefresh());

    public static void send(ServerPlayer player) {
        if (SuiteCapabilities.isNative(player.connection.getPacketContext(), "mapstitch")
                && ServerPlayNetworking.canSend(player, TYPE)) ServerPlayNetworking.send(player, new MapRefresh());
    }

    @Override public Type<MapRefresh> type() { return TYPE; }
}
