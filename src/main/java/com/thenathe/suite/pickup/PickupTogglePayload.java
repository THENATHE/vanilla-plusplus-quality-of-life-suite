package com.thenathe.suite.pickup;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PickupTogglePayload() implements CustomPacketPayload {
    public static final Type<PickupTogglePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("thenathe_mod_suite", "toggle_collector"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PickupTogglePayload> CODEC =
            StreamCodec.unit(new PickupTogglePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
