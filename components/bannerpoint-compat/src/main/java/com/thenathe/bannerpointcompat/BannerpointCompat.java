package com.thenathe.bannerpointcompat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ClientboundPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipFile;

/** Bannerpoint has its own native channel; keep the suite's established protocol unchanged. */
public final class BannerpointCompat implements ModInitializer {
    public static final Identifier NAME_CHANNEL = Identifier.fromNamespaceAndPath("bannerpoint", "banner_name");
    private static final PacketContext.Key<PackState> PACK_STATE = PacketContext.key(
            Identifier.fromNamespaceAndPath("bannerpoint_polymer_compat", "loaded_artwork"));
    private static boolean registeredArtwork;

    /** The context belongs to the physical connection, including configuration and play. */
    private record PackState(UUID offeredPack, boolean loaded) { }

    @Override
    public void onInitialize() {
        com.thenathe.multiscale.AtlasBannerEvents.AFTER_EDIT.register((atlas, level, pos, retained) ->
                me.pajic.bannerpoint.waypoint.BannerWaypointUtil.setMapTracking(retained, level, pos));
        if (FabricLoader.getInstance().isModLoaded("polymer-resource-pack")) {
            registeredArtwork = PolymerArtwork.register();
        }
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> refresh(server, handler.getPacketContext()));
        ClientboundPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
            if (channels.contains(NAME_CHANNEL)) refresh(server, handler.getPacketContext());
        });
        ClientboundPlayChannelEvents.UNREGISTER.register((handler, sender, server, channels) -> {
            if (channels.contains(NAME_CHANNEL)) refresh(server, handler.getPacketContext());
        });
    }

    public static boolean nativeClient(ServerPlayer player) {
        return player.connection != null && ServerPlayNetworking.canSend(player, NAME_CHANNEL);
    }

    public static boolean canRenderWaypoints(ServerPlayer player) {
        if (nativeClient(player)) return true;
        if (!registeredArtwork || player.connection == null) return false;
        PackState state = player.connection.getPacketContext().get(PACK_STATE);
        return state != null && state.loaded();
    }

    /** A re-offered main pack invalidates the old acknowledgement before it is sent. */
    public static void packOffered(MinecraftServer server, PacketContext context, ClientboundResourcePackPushPacket packet) {
        if (!registeredArtwork || !PolymerArtwork.mainPack().equals(packet.id())) return;
        boolean changed;
        synchronized (context) {
            PackState previous = context.get(PACK_STATE);
            changed = previous != null && previous.loaded();
            // A stale Polymer ZIP can share the current main UUID while lacking
            // the Bannerpoint artwork. A UUID acknowledgement alone is unsafe.
            context.set(PACK_STATE, PolymerArtwork.verifiedOffer(packet.hash())
                    ? new PackState(packet.id(), false) : null);
        }
        if (changed) refresh(server, context);
    }

    /** Merely accepting or downloading a pack does not guarantee that its sprites exist yet. */
    public static void packResponse(MinecraftServer server, PacketContext context, ServerboundResourcePackPacket packet) {
        if (!registeredArtwork || !PolymerArtwork.mainPack().equals(packet.id())) return;
        boolean changed;
        synchronized (context) {
            PackState previous = context.get(PACK_STATE);
            if (previous == null || !previous.offeredPack().equals(packet.id())) return;
            boolean loaded = switch (packet.action()) {
                case SUCCESSFULLY_LOADED -> true;
                case ACCEPTED, DOWNLOADED -> previous.loaded();
                case DECLINED, FAILED_DOWNLOAD, INVALID_URL, FAILED_RELOAD, DISCARDED -> false;
            };
            changed = previous.loaded() != loaded;
            context.set(PACK_STATE, new PackState(previous.offeredPack(), loaded));
        }
        if (changed) refresh(server, context);
    }

    public static void packRemoved(MinecraftServer server, PacketContext context, ClientboundResourcePackPopPacket packet) {
        if (!registeredArtwork) return;
        if (packet.id().isPresent() && !PolymerArtwork.mainPack().equals(packet.id().get())) return;
        boolean changed;
        synchronized (context) {
            PackState previous = context.get(PACK_STATE);
            changed = previous != null && previous.loaded();
            context.set(PACK_STATE, null);
        }
        if (changed) refresh(server, context);
    }

    public static void disconnected(PacketContext context) {
        context.set(PACK_STATE, null);
    }

    /** Missing connections are created; unsupported existing banner connections become broken. */
    private static void refresh(MinecraftServer server, PacketContext context) {
        Runnable action = () -> {
            var connection = context.orElseThrow(PacketContext.CONNECTION);
            if (connection.getPacketListener() instanceof ServerPlayerConnection session) {
                ServerPlayer player = session.getPlayer();
                if (player.connection != null && player.connection.getPacketContext() == context) {
                    player.level().getWaypointManager().updatePlayer(player);
                }
            }
        };
        if (server.isSameThread()) action.run();
        else server.execute(action);
    }

    /** Nested behind the loader guard so normal native play also works without Polymer. */
    private static final class PolymerArtwork {
        private static final String[] ASSETS = {
                "assets/bannerpoint/waypoint_style/banner.json",
                "assets/bannerpoint/textures/gui/sprites/hud/locator_bar_dot/banner_0.png",
                "assets/bannerpoint/textures/gui/sprites/hud/locator_bar_dot/banner_1.png",
                "assets/bannerpoint/textures/gui/sprites/hud/locator_bar_dot/banner_2.png",
                "assets/bannerpoint/textures/gui/sprites/hud/locator_bar_dot/banner_3.png"
        };
        private static Map<String, byte[]> originalArtwork = Map.of();
        private static volatile VerifiedPack cachedPack;

        private record FileVersion(Path path, long size, FileTime modified, Object fileKey) { }
        private record VerifiedPack(FileVersion version, String sha1) { }

        static boolean register() {
            boolean copied = eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils.addModAssetsWithoutCopy("bannerpoint");
            if (copied) {
                var mod = FabricLoader.getInstance().getModContainer("bannerpoint").orElseThrow();
                Map<String, byte[]> artwork = new HashMap<>();
                try {
                    for (String asset : ASSETS) artwork.put(asset, Files.readAllBytes(mod.findPath(asset).orElseThrow()));
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
                originalArtwork = Map.copyOf(artwork);
                ServerLifecycleEvents.SERVER_STARTING.register(server -> verifiedHash());
                eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils.RESOURCE_PACK_FINISHED_EVENT.register(result -> {
                    // Pack generation can run asynchronously. Invalidate only;
                    // the next offer performs one cached verification of the file.
                    synchronized (PolymerArtwork.class) { cachedPack = null; }
                });
                eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(builder -> {
                    builder.addModToCredits("bannerpoint");
                    // Keep the suite's translated settings labels; only the five
                    // vanilla-readable waypoint resources belong in this pack.
                    for (String asset : ASSETS) {
                        byte[] data = Objects.requireNonNull(builder.getDataOrSource(asset), "Missing Bannerpoint artwork: " + asset);
                        builder.addData(asset, data);
                    }
                    try (var input = Objects.requireNonNull(BannerpointCompat.class.getResourceAsStream(
                            "/bannerpoint-ARTWORK-NOTICE.txt"), "Bannerpoint artwork notice is missing")) {
                        builder.addData("licenses/bannerpoint-ARTWORK-NOTICE.txt", input.readAllBytes());
                    } catch (IOException exception) {
                        throw new UncheckedIOException(exception);
                    }
                });
            }
            return copied;
        }

        static boolean verifiedOffer(String offeredHash) {
            if (offeredHash == null || !offeredHash.matches("(?i)[0-9a-f]{40}")) return false;
            String expectedHash = verifiedHash();
            return expectedHash != null && expectedHash.equalsIgnoreCase(offeredHash);
        }

        /** Only archive changes trigger the five-entry comparison and full-file SHA-1 read. */
        private static synchronized String verifiedHash() {
            Path path = eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils.getMainPath().toAbsolutePath().normalize();
            FileVersion version;
            try {
                version = fileVersion(path);
                VerifiedPack cached = cachedPack;
                if (cached != null && cached.version().equals(version)) return cached.sha1();
                String hash = null;
                try (ZipFile zip = new ZipFile(path.toFile())) {
                    boolean valid = true;
                    for (String asset : ASSETS) {
                        byte[] expected = originalArtwork.get(asset);
                        var entry = zip.getEntry(asset);
                        if (entry == null || entry.isDirectory() || entry.getSize() != expected.length) {
                            valid = false;
                            break;
                        }
                        try (var input = zip.getInputStream(entry)) {
                            if (!Arrays.equals(expected, input.readNBytes(expected.length + 1))) {
                                valid = false;
                                break;
                            }
                        }
                    }
                    if (valid) {
                        MessageDigest digest = MessageDigest.getInstance("SHA-1");
                        try (var input = new DigestInputStream(Files.newInputStream(path), digest)) {
                            byte[] buffer = new byte[65536];
                            while (input.read(buffer) != -1) { }
                        }
                        hash = HexFormat.of().formatHex(digest.digest());
                    }
                }
                // Do not acknowledge a file replaced while its entries/hash were read.
                if (!version.equals(fileVersion(path))) {
                    cachedPack = null;
                    return null;
                }
                cachedPack = new VerifiedPack(version, hash);
                return hash;
            } catch (IOException exception) {
                cachedPack = null;
                return null;
            } catch (NoSuchAlgorithmException exception) {
                throw new IllegalStateException("The Java runtime lacks SHA-1", exception);
            }
        }

        private static FileVersion fileVersion(Path path) throws IOException {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            if (!attributes.isRegularFile()) throw new IOException("Polymer main pack is not a regular file");
            return new FileVersion(path, attributes.size(), attributes.lastModifiedTime(), attributes.fileKey());
        }

        static UUID mainPack() {
            return eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils.getMainUuid();
        }
    }
}
