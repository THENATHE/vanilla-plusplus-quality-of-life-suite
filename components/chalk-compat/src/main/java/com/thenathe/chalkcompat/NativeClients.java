package com.thenathe.chalkcompat;

import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import java.util.function.Consumer;

/** Explicit, connection-scoped negotiation; the decision is fixed before registry sync. */
public final class NativeClients {
    public static final PacketContext.Key<Integer> STATE_BITS = PacketContext.key(id("state_bits"));
    private static final PacketContext.Key<Boolean> WAITING_STATES = PacketContext.key(id("waiting_states"));
    private static final ConfigurationTask.Type STATE_TASK = new ConfigurationTask.Type("chalk_polymer_compat:state_ids");
    private static boolean initialized;
    private NativeClients() {}
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath("chalk_polymer_compat", path); }
    public static boolean nativeClient(PacketContext context) {
        return com.thenathe.suite.network.SuiteCapabilities.isNative(context, "chalk");
    }
    public static boolean nativeClient(ServerPlayer player) {
        return player != null && nativeClient(player.connection.getPacketContext());
    }
    public static boolean ownEntry(Identifier id) {
        return id != null && (id.getNamespace().equals("chalk") || id.getNamespace().equals("chalk-colorful-addon")
                || id.getNamespace().equals("chalk_polymer_compat"));
    }
    public static void initialize() {
        if (initialized) return;
        initialized = true;
        com.thenathe.suite.network.SuiteCapabilities.initialize();
        com.thenathe.suite.network.SuiteCapabilities.onConnectionReset(context -> {
            context.set(STATE_BITS, null);
            context.set(WAITING_STATES, null);
            WireRegistries.clear(context);
        });
        PayloadTypeRegistry.clientboundConfiguration().register(StateRequest.TYPE, StateRequest.CODEC);
        PayloadTypeRegistry.serverboundConfiguration().registerLarge(StateReply.TYPE, StateReply.CODEC, 8 * 1024 * 1024);
        ServerConfigurationNetworking.registerGlobalReceiver(StateReply.TYPE, (reply, context) -> {
            if (!Boolean.TRUE.equals(context.packetContext().get(WAITING_STATES))) return;
            context.packetContext().set(WAITING_STATES, null);
            try {
                if (reply.bits() < 1 || reply.bits() > 30) throw new IllegalArgumentException("Invalid state bit width");
                for (var block : reply.blocks()) for (int value : block.ids()) {
                    if (value < 0 || value >= (1L << reply.bits())) throw new IllegalArgumentException("State ID exceeds negotiated bit width");
                }
                WireRegistries.applyClientStates(reply.blocks(), context.packetContext());
                context.packetContext().set(STATE_BITS, reply.bits());
                context.packetListener().completeTask(STATE_TASK);
            } catch (IllegalArgumentException | IllegalStateException error) {
                context.packetListener().disconnect(net.minecraft.network.chat.Component.literal("Chalk native state negotiation failed: " + error.getMessage()));
            }
        });
    }
    /** Queued from Fabric's actual sync task, after any other shim's deferred scheduling. */
    public static void afterRegistrySyncStarted(ServerConfigurationPacketListenerImpl listener) {
        var context = listener.getPacketContext();
        if (!nativeClient(context)) return;
        listener.addTask(new ConfigurationTask() {
            public void start(Consumer<Packet<?>> sender) {
                context.set(WAITING_STATES, true);
                ServerConfigurationNetworking.send(listener, new StateRequest());
            }
            public Type type() { return STATE_TASK; }
        });
    }
    public record BlockIds(Identifier block, int[] ids) {}
    public record StateRequest() implements CustomPacketPayload {
        public static final Type<StateRequest> TYPE = new Type<>(id("state_request_v1"));
        public static final StreamCodec<FriendlyByteBuf, StateRequest> CODEC = StreamCodec.of(
                (buf, value) -> {}, buf -> new StateRequest());
        public Type<StateRequest> type() { return TYPE; }
    }
    public record StateReply(int bits, java.util.List<BlockIds> blocks) implements CustomPacketPayload {
        public static final Type<StateReply> TYPE = new Type<>(id("state_reply_v1"));
        public static final StreamCodec<FriendlyByteBuf, StateReply> CODEC = StreamCodec.of((buf, value) -> {
            buf.writeVarInt(value.bits());
            buf.writeVarInt(value.blocks().size());
            for (var block : value.blocks()) {
                buf.writeIdentifier(block.block());
                buf.writeVarIntArray(block.ids());
            }
        }, buf -> {
            int bits = buf.readVarInt();
            int count = buf.readVarInt();
            if (count < 0 || count > 100000) throw new IllegalArgumentException("Invalid block count");
            var blocks = new java.util.ArrayList<BlockIds>(count);
            for (int i = 0; i < count; i++) blocks.add(new BlockIds(buf.readIdentifier(), buf.readVarIntArray(100000)));
            return new StateReply(bits, java.util.List.copyOf(blocks));
        });
        public Type<StateReply> type() { return TYPE; }
    }
}
