package qa.bannerpoint;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.*;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.waypoints.Waypoint;
import me.pajic.bannerpoint.waypoint.BannerWaypointUtil;
import me.pajic.bannerpoint.extension.BannerBlockEntityExtension;
import me.pajic.bannerpoint.networking.S2CBannerNamePayload;

/** Disposable real world; packet/status observations do not override compatibility decisions. */
public final class BannerpointServerQa implements ModInitializer {
    public static final Path CONTROL = Path.of(System.getProperty("banner.qa.control"));
    private static final Map<String, JsonArray> events = new HashMap<>();
    private static final UUID REGULAR = UUID.fromString("aca2b810-9353-4ae5-8c72-1eb6540f147f");
    private final Set<UUID> seeded = new HashSet<>();
    private final List<BannerBlockEntity> banners = new ArrayList<>();
    private int ticks;
    public static synchronized void observe(ServerPlayer player, String kind, Object detail) {
        observe(player.getGameProfile().name(), kind, detail);
    }
    public static synchronized void observe(String name, String kind, Object detail) {
        var row = new JsonObject(); row.addProperty("kind", kind); row.addProperty("detail", String.valueOf(detail));
        events.computeIfAbsent(name, n -> new JsonArray()).add(row);
    }
    public static void packet(ServerPlayer player, Packet<?> packet) {
        if (packet instanceof ClientboundTrackedWaypointPacket waypoint) {
            observe(player, "waypoint", waypoint.operation() + " " + waypoint.waypoint().id() + " " + waypoint.waypoint().icon().style.identifier());
        } else if (packet instanceof ClientboundResourcePackPushPacket pack) observe(player, "pack-push", pack.id() + " " + pack.url());
        else if (packet instanceof ClientboundResourcePackPopPacket pack) observe(player, "pack-pop", pack.id());
        else if (packet instanceof ClientboundCustomPayloadPacket payload && payload.payload().type().id().toString().equals("bannerpoint:banner_name")) observe(player, "banner-name", payload.payload());
    }
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            try {
                ticks++;
                var ready = CONTROL.resolve("pack-ready.json");
                if (Files.exists(ready)) {
                    var information = JsonParser.parseString(Files.readString(ready)).getAsJsonObject();
                    var properties = ((net.minecraft.server.dedicated.DedicatedServer) server).getProperties();
                    var field = properties.getClass().getDeclaredField("serverResourcePackInfo"); field.setAccessible(true);
                    field.set(properties, Optional.of(new net.minecraft.server.MinecraftServer.ServerResourcePackInfo(UUID.fromString(information.get("uuid").getAsString()), information.get("url").getAsString(), information.get("sha1").getAsString(), false, Component.literal("Bannerpoint real-pack QA"))));
                    Files.delete(ready); Files.writeString(CONTROL.resolve("pack-installed.txt"), "Exact generated pack hash configured\n");
                }
                for (var player : server.getPlayerList().getPlayers()) {
                    var name = player.getGameProfile().name();
                    if (seeded.add(player.getUUID())) {
                        player.teleportTo(player.level(), 0.5, -62, 0.5, Set.of(), 0, 0, true);
                        if (banners.isEmpty()) {
                            for (int i = 0; i < 2; i++) {
                                var pos = new BlockPos(i == 0 ? -3 : 3, -63, 18);
                                player.level().getChunkAt(pos);
                                player.level().setBlock(pos, Blocks.BANNER.pick(i == 0 ? net.minecraft.world.item.DyeColor.RED : net.minecraft.world.item.DyeColor.BLUE).defaultBlockState(), 3);
                                var banner = (BannerBlockEntity) player.level().getBlockEntity(pos);
                                if (i == 0) {
                                    var item = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BANNER.pick(net.minecraft.world.item.DyeColor.RED));
                                    item.set(DataComponents.CUSTOM_NAME, Component.literal("QA Red Named Banner"));
                                    banner.applyComponentsFromItemStack(item);
                                } else ((BannerBlockEntityExtension) banner).bannerpoint$setTiedToMap(true);
                                BannerWaypointUtil.startTracking(player.level(), banner, true); banners.add(banner);
                            }
                            var ids = new JsonArray(); for (var banner : banners) ids.add(((BannerBlockEntityExtension) banner).bannerpoint$getUUID().toString());
                            Files.writeString(CONTROL.resolve("banner-ids.json"), ids.toString());
                        }
                        // A default, non-banner locator waypoint must survive every compatibility state.
                        player.connection.send(ClientboundTrackedWaypointPacket.addWaypointPosition(REGULAR, new Waypoint.Icon(), new BlockPos(0, -63, 24)));
                        observe(player, "joined", "native-banner-channel=" + ServerPlayNetworking.canSend(player, S2CBannerNamePayload.TYPE));
                    }
                    var action = CONTROL.resolve(name + "-action.txt");
                    if (Files.exists(action)) {
                        var content = Files.readString(action).trim(); Files.delete(action);
                        if (content.equals("push") || content.equals("stale")) {
                            String file = content.equals("stale") ? "stale-pack.zip" : "resource_pack.zip";
                            String hash = HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-1").digest(Files.readAllBytes(Path.of("polymer", file))));
                            String url = System.getProperty("banner.qa.pack.url").replace("resource_pack.zip", file);
                            player.connection.send(new ClientboundResourcePackPushPacket(eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils.getMainUuid(), url, hash, false, Optional.empty()));
                        } else if (content.equals("pop")) player.connection.send(new ClientboundResourcePackPopPacket(Optional.of(eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils.getMainUuid())));
                        else if (content.equals("states")) {
                            for (var state : ServerboundResourcePackPacket.Action.values()) {
                                if (state == ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED) continue;
                                com.thenathe.bannerpointcompat.BannerpointCompat.packResponse(server, player.connection.getPacketContext(), new ServerboundResourcePackPacket(eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils.getMainUuid(), state));
                                boolean allowed = com.thenathe.bannerpointcompat.BannerpointCompat.canRenderWaypoints(player);
                                if (allowed) throw new AssertionError("Non-success state enabled banner artwork: " + state);
                                observe(player, "strict-state", state + " can_render=false");
                            }
                        }
                    }
                    if (ticks % 10 == 0) {
                        var row = new JsonObject(); row.addProperty("name", name);
                        row.addProperty("native_banner_channel", ServerPlayNetworking.canSend(player, S2CBannerNamePayload.TYPE));
                        row.addProperty("polymer_main_pack", net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("polymer-resource-pack") && eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils.hasMainPack(player));
                        row.addProperty("can_render", com.thenathe.bannerpointcompat.BannerpointCompat.canRenderWaypoints(player));
                        synchronized (BannerpointServerQa.class) { row.add("events", events.getOrDefault(name, new JsonArray()).deepCopy()); }
                        Files.writeString(CONTROL.resolve(name + "-server.json"), new GsonBuilder().setPrettyPrinting().create().toJson(row));
                    }
                }
            } catch (Throwable error) {
                try { Files.writeString(CONTROL.resolve("failure"), error.toString()); } catch (Exception ignored) {}
                error.printStackTrace();
            }
        });
    }
}
