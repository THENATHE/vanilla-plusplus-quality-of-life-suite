package com.thenathe.suite.network;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.CommonPacketsImpl;
import net.fabricmc.fabric.impl.networking.CommonRegisterPayload;
import net.fabricmc.fabric.impl.networking.CommonVersionPayload;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.mixin.networking.accessor.ServerCommonPacketListenerImplAccessor;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/** One connection-local capability decision, independent of optional original mod classes. */
public final class SuiteCapabilities {
    public static final List<String> MODULES = List.of("mapstitch", "simple_smithing_overhaul", "tiered_backpacks", "toolpouch", "chalk", "misctweaks", "simple_death_improvements", "shared_region_maps", "amethyst_curse_cleanser", "toolpouch_atlas_elytra_compat", "sensible_stackables");
    private static final List<java.util.function.Consumer<PacketContext>> RESETS = new java.util.ArrayList<>();
    private static boolean initialized, clientInitialized;
    public static void onConnectionReset(java.util.function.Consumer<PacketContext> reset) { RESETS.add(reset); }
    public static void initialize() {
        if (initialized) return;
        initialized = true;
        initializePayloads(); initializeServer();
    }
    public static void initializeClient() {
        if (clientInitialized) return;
        clientInitialized = true;
        initialize();
        net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking.registerGlobalReceiver(Offer.TYPE, (offer, context) -> {
            context.packetContext().set(MODULES_KEY, Set.of());
            RESETS.forEach(reset -> reset.accept(context.packetContext()));
            context.responseSender().sendPacket(new Reply(offer.nonce(), fingerprints()));
        });
        net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking.registerGlobalReceiver(Decision.TYPE, (decision, context) -> context.packetContext().set(MODULES_KEY, Set.copyOf(decision.modules())));
    }
    private static final PacketContext.Key<Set<String>> MODULES_KEY = PacketContext.key(
            Identifier.parse("thenathe_mod_suite:native_modules"));
    private static final PacketContext.Key<Boolean> QUERY_PENDING = PacketContext.key(
            Identifier.parse("thenathe_mod_suite:querying_play_channels"));
    private static final Set<Identifier> TOOLPOUCH_CHANNELS = Set.of(
            Identifier.parse("toolpouch:s2c_sync_shulker_slot"),
            Identifier.parse("toolpouch:s2c_sync_arrow_slot"));
    private static final Set<Identifier> MAPSTITCH_CHANNELS = Set.of(
            Identifier.parse("mapstitch:dimension_ids"),
            Identifier.parse("mapstitch:open_world_map_screen"),
            Identifier.parse("mapstitch:sync_world_map"),
            Identifier.parse("mapstitch:s2c_play_sound"));

    private static final PacketContext.Key<List<String>> FINGERPRINTS = PacketContext.key(
            Identifier.parse("thenathe_mod_suite:module_fingerprints"));
    private static final PacketContext.Key<String> NONCE = PacketContext.key(
            Identifier.parse("thenathe_mod_suite:negotiation_nonce"));
    private static final ConfigurationTask.Type NEGOTIATE = new ConfigurationTask.Type("thenathe_mod_suite:negotiate");
    private SuiteCapabilities() {}

    /** Payload codecs are neutral: client loading needs neither originals nor Polymer. */
    private static void initializePayloads() {
        PayloadTypeRegistry.clientboundConfiguration().register(Offer.TYPE, Offer.CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(Reply.TYPE, Reply.CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(Decision.TYPE, Decision.CODEC);
    }

    private static void initializeServer() {
        ServerConfigurationNetworking.registerGlobalReceiver(Reply.TYPE, (reply, context) -> {
            var packetContext = context.packetContext();
            String expected = packetContext.get(NONCE);
            if (expected == null || !expected.equals(reply.nonce())) return;
            packetContext.set(NONCE, null); // Reject stale, duplicate and unsolicited replies.
            packetContext.set(FINGERPRINTS, reply.fingerprints());
            context.packetListener().completeTask(NEGOTIATE);
        });
    }

    public static List<String> fingerprints() {
        return MODULES.stream().map(SuiteCapabilities::fingerprint).toList();
    }

    private static String fingerprint(String modId) {
        if (!loaded(modId)) return "";
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            for (String id : dependencies(modId)) {
                String version = FabricLoader.getInstance().getModContainer(id)
                        .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("absent");
                digest.update((id + "=" + version + "\n").getBytes(StandardCharsets.UTF_8));
            }
            BuiltInRegistries.REGISTRY.keySet().stream().sorted().forEach(registryId -> {
                var registry = BuiltInRegistries.REGISTRY.getValue(registryId);
                registry.keySet().stream().filter(id -> owns(modId, id)).sorted().forEach(id -> {
                    digest.update((registryId + "=" + id + "\n").getBytes(StandardCharsets.UTF_8));
                    if (registry == BuiltInRegistries.BLOCK) {
                        for (var state : BuiltInRegistries.BLOCK.getValue(id).getStateDefinition().getPossibleStates()) digest.update((state.toString()+"\n").getBytes(StandardCharsets.UTF_8));
                    }
                });
            });
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }

    private static boolean confirmed(PacketContext context, String modId) {
        var values = context.get(FINGERPRINTS);
        int index = MODULES.indexOf(modId);
        return values != null && loaded(modId) && fingerprint(modId).equals(values.get(index));
    }

    public record Offer(String nonce) implements CustomPacketPayload {
        public static final Type<Offer> TYPE = new Type<>(Identifier.parse("thenathe_mod_suite:offer_v2"));
        public static final StreamCodec<FriendlyByteBuf, Offer> CODEC = StreamCodec.of(
                (buf, value) -> buf.writeUtf(value.nonce(), 36), buf -> new Offer(buf.readUtf(36)));
        public Type<Offer> type() { return TYPE; }
    }

    public record Reply(String nonce, List<String> fingerprints) implements CustomPacketPayload {
        public static final Type<Reply> TYPE = new Type<>(Identifier.parse("thenathe_mod_suite:reply_v2"));
        public static final StreamCodec<FriendlyByteBuf, Reply> CODEC = StreamCodec.of((buf, value) -> {
            buf.writeUtf(value.nonce(), 36);
            for (String fingerprint : value.fingerprints()) buf.writeUtf(fingerprint, 64);
        }, buf -> {
            String nonce = buf.readUtf(36);
            var values = new java.util.ArrayList<String>(MODULES.size());
            for (int i = 0; i < MODULES.size(); i++) values.add(buf.readUtf(64));
            return new Reply(nonce, List.copyOf(values));
        });
        public Type<Reply> type() { return TYPE; }
    }

    public static boolean isNative(PacketContext context, String modId) {
        var modules = context == null ? null : context.get(MODULES_KEY);
        return modules != null && modules.contains(modId);
    }

    public static boolean isNativeEntry(PacketContext context, Identifier id) {
        return id != null && isNative(context, id.getNamespace());
    }

    public static boolean hasRegistryReceiver(ServerConfigurationPacketListenerImpl listener) {
        return ServerConfigurationNetworking.canSend(listener, Identifier.parse("fabric:registry/sync"));
    }

    public static void classify(ServerConfigurationPacketListenerImpl listener) {
        var selected = new HashSet<String>();
        var context = listener.getPacketContext();
        if (hasRegistryReceiver(listener)) {
            for (String mod : MODULES) if (confirmed(context, mod)) selected.add(mod);
            // Original clients do advertise these channels, so they need no suite handshake.
            // An explicit suite reply is authoritative, including version/registry mismatches.
            if (context.get(FINGERPRINTS) == null) {
                var connection = ((ServerCommonPacketListenerImplAccessor) listener).getConnection();
                if (connection instanceof ChannelInfoHolder holder) {
                    var channels = holder.fabric_getPendingChannelsNames(ConnectionProtocol.PLAY);
                    if (loaded("toolpouch") && channels.containsAll(TOOLPOUCH_CHANNELS)) selected.add("toolpouch");
                    if (loaded("mapstitch") && channels.containsAll(MAPSTITCH_CHANNELS)) selected.add("mapstitch");
                }
            }
        }
        org.slf4j.LoggerFactory.getLogger("Thenathe Mod Suite").info("Suite negotiated native modules {} (Polymer {})", selected, loaded("polymer-core") ? "available" : "absent");
        context.set(MODULES_KEY, Set.copyOf(selected));
        if (context.get(FINGERPRINTS) != null) ServerConfigurationNetworking.send(listener, new Decision(MODULES.stream().filter(selected::contains).toList()));
    }

    private static List<String> dependencies(String mod) {
        if (mod.equals("sensible_stackables")) return List.of(mod, "sensible_stackables_polymer_compat", "defaulted", "fzzy_config", "thenathe_mod_suite");
        if (mod.equals("chalk")) return List.of(mod, "chalk-colorful-addon", "chalk_polymer_compat", "thenathe_mod_suite");
        if (mod.equals("toolpouch")) return List.of(mod, "toolpouch_atlas_elytra_compat", "sso_backpack_toolpouch_mapstitch_shim", "thenathe_mod_suite");
        return List.of(mod, "sso_backpack_toolpouch_mapstitch_shim", "thenathe_mod_suite");
    }
    private static boolean owns(String mod, Identifier id) {
        return id.getNamespace().equals(mod) || (mod.equals("chalk") && id.getNamespace().equals("chalk_polymer_compat"));
    }
    public record Decision(List<String> modules) implements CustomPacketPayload {
        public static final Type<Decision> TYPE = new Type<>(Identifier.parse("thenathe_mod_suite:decision_v2"));
        public static final StreamCodec<FriendlyByteBuf, Decision> CODEC = StreamCodec.of((buf, value) -> {
            for (String mod : MODULES) buf.writeBoolean(value.modules().contains(mod));
        }, buf -> {
            var values = new java.util.ArrayList<String>();
            for (String mod : MODULES) if (buf.readBoolean()) values.add(mod);
            return new Decision(List.copyOf(values));
        });
        public Type<Decision> type() { return TYPE; }
    }

    private static boolean loaded(String modId) { return FabricLoader.getInstance().isModLoaded(modId); }

    /** Reuse Fabric's existing common protocol once, before native registry selection. */
    public static void beforeRegistrySync(ServerConfigurationPacketListenerImpl listener, Runnable configure) {
        var context = listener.getPacketContext();
        context.set(MODULES_KEY, Set.of());
        RESETS.forEach(reset -> reset.accept(context));
        context.set(FINGERPRINTS, null);
        context.set(NONCE, null);
        context.set(QUERY_PENDING, true);
        if ((loaded("toolpouch") || loaded("mapstitch"))
                && ServerConfigurationNetworking.canSend(listener, CommonVersionPayload.TYPE)
                && ServerConfigurationNetworking.canSend(listener, CommonRegisterPayload.TYPE)) {
            listener.addTask(new CommonVersionTask(listener));
            listener.addTask(new CommonPlayChannelsTask(listener));
        }
        if (ServerConfigurationNetworking.canSend(listener, Offer.TYPE) && ServerConfigurationNetworking.canSend(listener, Decision.TYPE)) {
            listener.addTask(new ConfigurationTask() {
                public void start(Consumer<Packet<?>> sender) {
                    String nonce = UUID.randomUUID().toString();
                    context.set(NONCE, nonce);
                    ServerConfigurationNetworking.send(listener, new Offer(nonce));
                }
                public Type type() { return NEGOTIATE; }
            });
        }
        listener.addTask(new DeferredRegistryTask(listener, configure));
    }

    private record CommonVersionTask(ServerConfigurationPacketListenerImpl listener) implements ConfigurationTask {
        @Override public void start(Consumer<Packet<?>> sender) {
            ServerConfigurationNetworking.send(listener, new CommonVersionPayload(CommonPacketsImpl.SUPPORTED_COMMON_PACKET_VERSIONS));
        }
        @Override public Type type() { return new Type(CommonVersionPayload.TYPE.id().toString()); }
    }

    private record CommonPlayChannelsTask(ServerConfigurationPacketListenerImpl listener) implements ConfigurationTask {
        @Override public void start(Consumer<Packet<?>> sender) {
            ServerConfigurationNetworking.send(listener,
                    new CommonRegisterPayload(ServerNetworkingImpl.getAddon(listener).getNegotiatedVersion(),
                            CommonRegisterPayload.PLAY_PROTOCOL, ServerPlayNetworking.getGlobalReceivers()));
        }
        @Override public Type type() { return new Type(CommonRegisterPayload.TYPE.id().toString()); }
    }

    private record DeferredRegistryTask(ServerConfigurationPacketListenerImpl listener, Runnable configure) implements ConfigurationTask {
        private static final Type TYPE = new Type("thenathe_mod_suite:prepare_registry_sync");
        @Override public void start(Consumer<Packet<?>> sender) {
            classify(listener);
            configure.run();
            // Configuration can repeat on the same connection; query fresh PLAY capabilities then.
            listener.getPacketContext().set(QUERY_PENDING, false);
            listener.completeTask(TYPE);
        }
        @Override public Type type() { return TYPE; }
    }
}
