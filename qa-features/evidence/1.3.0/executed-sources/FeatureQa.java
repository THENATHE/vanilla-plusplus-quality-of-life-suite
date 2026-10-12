import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** Dedicated mechanics and real asynchronous chunk loading; client acceptance is separate. */
public final class FeatureQa implements ModInitializer {
    private final JsonObject result = new JsonObject();
    private ServerPlayer player;
    private boolean pending;
    private int ticks;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            result.addProperty("passed", false);
            result.addProperty("polymer", FabricLoader.getInstance().isModLoaded("polymer-core"));
            result.addProperty("restart", Files.exists(Path.of("features-initial-complete")));
            try {
                player = createPlayer(server);
                CoreFeatureChecks.run(server, player);
                PetFeatureChecks.run(server, player);
                pending = true;
            } catch (Throwable error) {
                finish(server, error);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!pending) return;
            try {
                if (++ticks > 600) throw new AssertionError("Asynchronous pet checks exceeded 600 server ticks");
                if (PetFeatureChecks.tick(server, player)) finish(server, null);
            } catch (Throwable error) {
                finish(server, error);
            }
        });
    }

    private void finish(MinecraftServer server, Throwable failure) {
        pending = false;
        try {
            if (failure == null) {
                server.saveEverything(true, true, true);
                Files.writeString(Path.of("features-initial-complete"), "initial mechanics and async pet checks saved\n");
                result.addProperty("passed", true);
            } else {
                failure.printStackTrace();
                result.addProperty("error", failure.toString());
            }
            result.addProperty("async_server_ticks", ticks);
            Files.writeString(Path.of("features-result.json"),
                    new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n");
        } catch (Exception error) {
            throw new RuntimeException(error);
        }
        server.halt(false);
    }

    private static ServerPlayer createPlayer(MinecraftServer server) {
        var profile = new GameProfile(UUID.fromString("118164b3-9f78-4c7b-ae02-fde9c1287c21"), "FeatureQa");
        var player = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
        player.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player,
                CommonListenerCookie.createInitial(profile, false)) {
            @Override
            public void send(Packet<?> packet) {
            }
        };
        player.setPos(0, 100, 0);
        player.level().getChunkAt(player.blockPosition());
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }
}
