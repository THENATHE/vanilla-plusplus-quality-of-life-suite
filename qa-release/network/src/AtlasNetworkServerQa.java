package qa.atlasnetwork;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import com.thenathe.multiscale.*;
import me.pajic.mapstitch.component.ModDataComponents;

/** Actual connected players and real commands; no capability overrides. */
public final class AtlasNetworkServerQa implements ModInitializer {
    private static final Path CONTROL=Path.of(System.getProperty("atlas.qa.control"));
    private static final Map<String,JsonArray> events=new HashMap<>();
    private final Map<UUID,Integer> ages=new HashMap<>();
    private final Set<UUID> executed=new HashSet<>();
    private final Map<UUID,JsonObject> results=new HashMap<>();
    public static void packet(ServerPlayer player,Packet<?> packet) {
        JsonObject event=new JsonObject();
        if(packet instanceof ClientboundCustomPayloadPacket custom) {
            event.addProperty("kind","custom-payload");
            event.addProperty("channel",custom.payload().type().id().toString());
            event.addProperty("advertised",ServerPlayNetworking.canSend(player,custom.payload().type()));
        } else if(packet instanceof ClientboundMapItemDataPacket map) {
            event.addProperty("kind","vanilla-map-data");event.addProperty("map_id",map.mapId().id());
        } else return;
        events.computeIfAbsent(player.getGameProfile().name(),n->new JsonArray()).add(event);
    }
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server->{try{
            for(var player:server.getPlayerList().getPlayers()) {
                var name=player.getGameProfile().name();
                int age=ages.merge(player.getUUID(),1,Integer::sum);
                if(age>=40 && Files.exists(CONTROL.resolve(name+"-execute")) && executed.add(player.getUUID())) {
                    Files.delete(CONTROL.resolve(name+"-execute"));
                    player.getInventory().clearContent();
                    var map=MapItem.create(player.level(),player.getBlockX(),player.getBlockZ(),(byte)0,true,false);
                    var data=MapItem.getSavedData(map,player.level());
                    java.util.Arrays.fill(data.colors,(byte)20);
                    byte[] originalPixels=data.colors.clone();
                    map.remove(ModDataComponents.MAP_CENTER);
                    var book=new ItemStack(me.pajic.mapstitch.item.ModItems.ATLAS);
                    AtlasOptions.setGenerationMask(book,0);AtlasOptions.ensureIdentity(book);book.set(ModDataComponents.ATLAS_SCALE,0);
                    book.set(DataComponents.BUNDLE_CONTENTS,new BundleContents(List.of(ItemStackTemplate.fromNonEmptyStack(map),ItemStackTemplate.fromNonEmptyStack(map.copy()))));
                    player.getInventory().setItem(0,book);
                    player.getInventory().setSelectedSlot(0);
                    player.containerMenu.broadcastChanges();
                    var row=new JsonObject();
                    row.addProperty("native_mapstitch",com.thenathe.suite.network.SuiteCapabilities.isNative(player.connection.getPacketContext(),"mapstitch"));
                    row.addProperty("refresh_advertised",ServerPlayNetworking.canSend(player,MapRefresh.TYPE));
                    int start=events.getOrDefault(name,new JsonArray()).size();
                    row.addProperty("packet_event_start",start);
                    int repair=server.getCommands().getDispatcher().execute("repairmaps",player.createCommandSourceStack());
                    if(repair!=1) throw new AssertionError("Expected actual repair of one unique map: "+repair);
                    int dedupe=server.getCommands().getDispatcher().execute("dedupemaps",player.createCommandSourceStack());
                    if(dedupe!=1) throw new AssertionError("Expected actual removal of one duplicate: "+dedupe);
                    var retained=book.get(DataComponents.BUNDLE_CONTENTS);
                    if(retained.size()!=1 || retained.items().getFirst().get(ModDataComponents.MAP_CENTER)==null) throw new AssertionError("Commands failed to repair and dedupe atlas");
                    if(!Arrays.equals(originalPixels,MapItem.getSavedData(map,player.level()).colors)) throw new AssertionError("Pixels mutated");
                    row.addProperty("repair_result",repair);row.addProperty("dedupe_result",dedupe);
                    row.addProperty("atlas_retained_entries",retained.size());row.addProperty("repaired_center_present",true);
                    row.addProperty("executed_at_age",age);results.put(player.getUUID(),row);
                }
                if(age%10==0) {
                    JsonObject row=results.getOrDefault(player.getUUID(),new JsonObject()).deepCopy();
                    row.addProperty("connected_age_ticks",age);row.addProperty("player_still_connected",player.connection.isAcceptingMessages());
                    row.add("events",events.getOrDefault(name,new JsonArray()).deepCopy());
                    Files.writeString(CONTROL.resolve(name+"-server.json"),new GsonBuilder().setPrettyPrinting().create().toJson(row));
                }
            }
        } catch(Throwable error) {error.printStackTrace();try{Files.writeString(CONTROL.resolve("failure"),error.toString());}catch(Exception ignored){}}});
    }
}
