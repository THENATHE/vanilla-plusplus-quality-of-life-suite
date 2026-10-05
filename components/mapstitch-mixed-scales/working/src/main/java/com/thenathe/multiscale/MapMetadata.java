package com.thenathe.multiscale;

import com.thenathe.suite.network.SuiteCapabilities;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/** Vanilla map updates omit their dimension and center. Native atlas clients need both. */
public record MapMetadata(int mapId, Identifier dimension, int centerX, int centerZ, byte scale, boolean locked)
        implements CustomPacketPayload {
    public static final Type<MapMetadata> TYPE = new Type<>(Identifier.parse("mapstitch_mixed_scales:map_metadata_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MapMetadata> CODEC = StreamCodec.of((buf, value) -> {
        buf.writeVarInt(value.mapId); buf.writeIdentifier(value.dimension);
        buf.writeInt(value.centerX); buf.writeInt(value.centerZ);
        buf.writeByte(value.scale); buf.writeBoolean(value.locked);
    }, buf -> new MapMetadata(buf.readVarInt(), buf.readIdentifier(), buf.readInt(), buf.readInt(), buf.readByte(), buf.readBoolean()));

    public static void register() { PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC); }

    public static void send(MapId id, MapItemSavedData data, ServerPlayer player) {
        // The payload is never sent to vanilla, Fabric-only, or mismatched suite clients.
        if (SuiteCapabilities.isNative(player.connection.getPacketContext(), "mapstitch")
                && ServerPlayNetworking.canSend(player, TYPE))
            ServerPlayNetworking.send(player, new MapMetadata(id.id(), data.dimension.identifier(),
                    data.centerX, data.centerZ, data.scale, data.locked));
    }

    @Override public Type<MapMetadata> type() { return TYPE; }
}
