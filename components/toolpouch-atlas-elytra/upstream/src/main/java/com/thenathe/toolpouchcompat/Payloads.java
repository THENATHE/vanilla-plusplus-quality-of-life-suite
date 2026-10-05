package com.thenathe.toolpouchcompat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
public final class Payloads {
 public record Toggle() implements CustomPacketPayload {
  public static final Type<Toggle> TYPE = new Type<>(Identifier.fromNamespaceAndPath("toolpouch_atlas_elytra_compat", "toggle"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Toggle> CODEC = StreamCodec.unit(new Toggle());
  public Type<? extends CustomPacketPayload> type() { return TYPE; }
 }
 public record State(boolean enabled) implements CustomPacketPayload {
  public static final Type<State> TYPE = new Type<>(Identifier.fromNamespaceAndPath("toolpouch_atlas_elytra_compat", "state"));
  public static final StreamCodec<RegistryFriendlyByteBuf,State> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, State::enabled, State::new);
  public Type<? extends CustomPacketPayload> type() { return TYPE; }
 }
}
