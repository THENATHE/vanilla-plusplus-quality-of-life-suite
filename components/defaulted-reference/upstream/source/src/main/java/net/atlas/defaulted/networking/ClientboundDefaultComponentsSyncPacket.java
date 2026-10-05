package net.atlas.defaulted.networking;

import net.atlas.defaulted.component.ItemPatches;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;

public record ClientboundDefaultComponentsSyncPacket(ArrayList<ItemPatches> list) implements CustomPacketPayload {
    public static final Type<ClientboundDefaultComponentsSyncPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("defaulted", "update_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDefaultComponentsSyncPacket> CODEC = ByteBufCodecs.collection(ArrayList::new, ItemPatches.STREAM_CODEC).map(ClientboundDefaultComponentsSyncPacket::new, ClientboundDefaultComponentsSyncPacket::list);

    @Override
    public @NonNull Type<?> type() {
        return TYPE;
    }
}