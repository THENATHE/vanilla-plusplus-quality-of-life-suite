package net.atlas.defaulted.networking;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.enchantment.EnchantmentPatches;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;

public record ClientboundEnchantmentsSyncPacket(ArrayList<EnchantmentPatches> list) implements CustomPacketPayload {
    public static final Type<ClientboundEnchantmentsSyncPacket> TYPE = new Type<>(Defaulted.id("update_enchantments"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundEnchantmentsSyncPacket> CODEC = ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.fromCodecWithRegistriesTrusted(EnchantmentPatches.CODEC)).map(ClientboundEnchantmentsSyncPacket::new, ClientboundEnchantmentsSyncPacket::list);

    @Override
    public @NonNull Type<?> type() {
        return TYPE;
    }
}