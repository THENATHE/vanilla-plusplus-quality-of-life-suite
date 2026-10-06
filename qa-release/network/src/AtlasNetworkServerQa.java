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
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
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
    private final Map<UUID,ItemStack> tableSources=new HashMap<>();
    public static void packet(ServerPlayer player,Packet<?> packet) {
        JsonObject event=new JsonObject();
        if(packet instanceof ClientboundCustomPayloadPacket custom) {
            event.addProperty("kind","custom-payload");
            event.addProperty("channel",custom.payload().type().id().toString());
            event.addProperty("advertised",ServerPlayNetworking.canSend(player,custom.payload().type()));
        } else if(packet instanceof ClientboundContainerSetContentPacket full) {
            event.addProperty("kind","container-full-snapshot");
            event.addProperty("container_id",full.containerId());
            event.addProperty("state_id",full.stateId());
            event.addProperty("slot_count",full.items().size());
            event.addProperty("contains_atlas",full.items().stream().anyMatch(stack -> stack.is(me.pajic.mapstitch.item.ModItems.ATLAS)));
        } else if(packet instanceof ClientboundMapItemDataPacket map) {
            event.addProperty("kind","vanilla-map-data");event.addProperty("map_id",map.mapId().id());
        } else return;
        events.computeIfAbsent(player.getGameProfile().name(),n->new JsonArray()).add(event);
    }
    private static long fullSnapshots(String name,int start,int containerId) {
        var rows=events.getOrDefault(name,new JsonArray());long count=0;
        for(int i=start;i<rows.size();i++) {
            var event=rows.get(i).getAsJsonObject();
            if(event.get("kind").getAsString().equals("container-full-snapshot") && event.get("container_id").getAsInt()==containerId && event.get("contains_atlas").getAsBoolean())count++;
        }
        return count;
    }
    private static void assertCopy(ItemStack source,ItemStack output,ServerPlayer player) {
        if(output.getCount()!=1 || !output.is(me.pajic.mapstitch.item.ModItems.ATLAS) || AtlasOptions.identity(source).equals(AtlasOptions.identity(output)) || AtlasOptions.identity(output).isEmpty())throw new AssertionError("Copy missing fresh atlas identity");
        var expected=source.get(DataComponents.BUNDLE_CONTENTS).items().stream().filter(entry->entry.get(DataComponents.MAP_ID)!=null).toList();
        if(!output.get(DataComponents.BUNDLE_CONTENTS).items().equals(expected))throw new AssertionError("Copy changed filled-map IDs/components or copied supplies");
        for(var entry:expected) {
            var data=MapItem.getSavedData(entry.get(DataComponents.MAP_ID),player.level());
            if(data==null)throw new AssertionError("Copied saved map record missing");
            for(byte pixel:data.colors)if(pixel!=(byte)20)throw new AssertionError("Copy changed explored pixels");
        }
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
                    var data=MapItem.getSavedData(map,player.level()).locked();
                    player.level().setMapData(map.get(DataComponents.MAP_ID),data);
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
                    int repair=server.getCommands().getDispatcher().execute("atlas fix",player.createCommandSourceStack());
                    if(repair!=1) throw new AssertionError("Expected actual repair of one unique map: "+repair);
                    long firstSnapshots=fullSnapshots(name,start,player.inventoryMenu.containerId);
                    if(firstSnapshots<1)throw new AssertionError("Repair did not send actual inventory full snapshot");
                    var unchanged=book.copy();
                    int checkStart=events.getOrDefault(name,new JsonArray()).size();
                    int check=server.getCommands().getDispatcher().execute("atlas fix check",player.createCommandSourceStack());
                    long checkSnapshots=fullSnapshots(name,checkStart,player.inventoryMenu.containerId);
                    if(check!=1 || checkSnapshots!=0 || !ItemStack.isSameItemSameComponents(unchanged,book))throw new AssertionError("Read-only check mutated atlas or sent full snapshot");
                    int aliasCheck=server.getCommands().getDispatcher().execute("atlas repair check",player.createCommandSourceStack());
                    if(aliasCheck!=1 || fullSnapshots(name,checkStart,player.inventoryMenu.containerId)!=0 || !ItemStack.isSameItemSameComponents(unchanged,book))throw new AssertionError("Repair alias check was not read-only");
                    row.addProperty("alias_check_result",aliasCheck);
                    int repeatStart=events.getOrDefault(name,new JsonArray()).size();
                    int repeat=server.getCommands().getDispatcher().execute("atlas repair",player.createCommandSourceStack());
                    long repeatedSnapshots=fullSnapshots(name,repeatStart,player.inventoryMenu.containerId);
                    if(repeat!=1 || repeatedSnapshots<1 || !ItemStack.isSameItemSameComponents(unchanged,book))throw new AssertionError("Unchanged repair failed to send inventory snapshot or unexpectedly changed atlas components");
                    row.addProperty("first_repair_full_snapshots",firstSnapshots);
                    row.addProperty("check_result",check);row.addProperty("check_full_snapshots",checkSnapshots);
                    row.addProperty("read_only_check_unchanged",true);
                    row.addProperty("repeat_repair_result",repeat);row.addProperty("repeat_repair_full_snapshots",repeatedSnapshots);
                    row.addProperty("repeat_repair_unchanged",true);
                    int dedupe=server.getCommands().getDispatcher().execute("atlas dedupe",player.createCommandSourceStack());
                    if(dedupe!=1) throw new AssertionError("Expected actual removal of one duplicate: "+dedupe);
                    var retained=book.get(DataComponents.BUNDLE_CONTENTS);
                    if(retained.size()!=1 || retained.items().getFirst().get(ModDataComponents.MAP_CENTER)==null) throw new AssertionError("Commands failed to repair and dedupe atlas");
                    if(!Arrays.equals(originalPixels,MapItem.getSavedData(map,player.level()).colors)) throw new AssertionError("Pixels mutated");
                    row.addProperty("repair_result",repair);row.addProperty("dedupe_result",dedupe);
                    row.addProperty("atlas_retained_entries",retained.size());row.addProperty("repaired_center_present",true);
                    var copyContents=new ArrayList<ItemStackTemplate>(retained.items());
                    copyContents.add(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.MAP,2)));
                    copyContents.add(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.PAPER,3)));
                    book.set(DataComponents.BUNDLE_CONTENTS,new BundleContents(copyContents));
                    var sourceBefore=book.copy();
                    player.getInventory().setItem(1,new ItemStack(Items.BOOK,3));
                    int copied=server.getCommands().getDispatcher().execute("atlas makecopy",player.createCommandSourceStack());
                    if(copied!=1 || player.getInventory().getItem(1).getCount()!=2 || !ItemStack.isSameItemSameComponents(sourceBefore,book))throw new AssertionError("makecopy cost or source preservation failed");
                    var outputs=player.getInventory().getNonEquipmentItems().stream().filter(stack->stack.is(me.pajic.mapstitch.item.ModItems.ATLAS) && !AtlasOptions.identity(stack).equals(AtlasOptions.identity(book))).toList();
                    if(outputs.size()!=1)throw new AssertionError("makecopy did not deliver exactly one fresh atlas to inventory");
                    assertCopy(book,outputs.getFirst(),player);
                    if(MapItem.getSavedData(map,player.level())!=data || !Arrays.equals(originalPixels,data.colors))throw new AssertionError("makecopy changed saved map record/pixels");
                    row.addProperty("makecopy_result",copied);row.addProperty("makecopy_books_consumed",1);
                    row.addProperty("makecopy_source_unchanged",true);row.addProperty("makecopy_inventory_output",true);
                    row.addProperty("makecopy_fresh_identity",true);row.addProperty("makecopy_ids_pixels_preserved",true);
                    row.addProperty("makecopy_empty_maps_paper_excluded",true);
                    tableSources.put(player.getUUID(),book.copy());
                    row.addProperty("executed_at_age",age);results.put(player.getUUID(),row);
                }
                var open=CONTROL.resolve(name+"-cartography-open");
                if(Files.exists(open)) {
                    Files.delete(open);player.getInventory().clearContent();
                    player.getInventory().setItem(0,tableSources.get(player.getUUID()).copy());
                    player.getInventory().setItem(1,new ItemStack(Items.BOOK));
                    var pos=player.blockPosition().offset(2,0,0);
                    player.level().setBlock(pos,net.minecraft.world.level.block.Blocks.CARTOGRAPHY_TABLE.defaultBlockState(),3);
                    player.inventoryMenu.broadcastFullState();
                    player.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inventory,who)->new net.minecraft.world.inventory.CartographyTableMenu(id,inventory,net.minecraft.world.inventory.ContainerLevelAccess.create(player.level(),pos)),net.minecraft.network.chat.Component.literal("Atlas Copy QA")));
                }
                if(age%10==0) {
                    JsonObject row=results.getOrDefault(player.getUUID(),new JsonObject()).deepCopy();
                    row.addProperty("connected_age_ticks",age);row.addProperty("player_still_connected",player.connection.isAcceptingMessages());
                    if(player.containerMenu instanceof net.minecraft.world.inventory.CartographyTableMenu table) {
                        var status=new JsonObject();status.addProperty("container_id",table.containerId);
                        status.addProperty("top_count",table.getSlot(0).getItem().getCount());
                        status.addProperty("top_atlas",table.getSlot(0).getItem().is(me.pajic.mapstitch.item.ModItems.ATLAS));
                        status.addProperty("book_count",table.getSlot(1).getItem().getCount());
                        status.addProperty("output_atlas",table.getSlot(2).getItem().is(me.pajic.mapstitch.item.ModItems.ATLAS));
                        status.addProperty("carried_count",table.getCarried().getCount());
                        status.addProperty("carried_atlas",table.getCarried().is(me.pajic.mapstitch.item.ModItems.ATLAS));
                        status.addProperty("carried_book",table.getCarried().is(Items.BOOK));
                        var copies=player.getInventory().getNonEquipmentItems().stream().filter(stack->stack.is(me.pajic.mapstitch.item.ModItems.ATLAS)).toList();
                        status.addProperty("inventory_atlas_count",copies.stream().mapToInt(ItemStack::getCount).sum());
                        boolean complete=table.getSlot(0).getItem().is(me.pajic.mapstitch.item.ModItems.ATLAS) && table.getSlot(1).getItem().isEmpty() && !table.getSlot(2).hasItem() && copies.size()==1;
                        if(complete) {
                            var source=tableSources.get(player.getUUID());
                            if(!ItemStack.isSameItemSameComponents(source,table.getSlot(0).getItem()))throw new AssertionError("Network cartography consumed or changed original atlas");
                            assertCopy(source,copies.getFirst(),player);
                            status.addProperty("source_retained",true);status.addProperty("one_book_consumed",true);status.addProperty("one_fresh_complete_copy",true);
                        }
                        status.addProperty("complete",complete);row.add("cartography",status);
                    }
                    row.add("events",events.getOrDefault(name,new JsonArray()).deepCopy());
                    Files.writeString(CONTROL.resolve(name+"-server.json"),new GsonBuilder().setPrettyPrinting().create().toJson(row));
                }
            }
        } catch(Throwable error) {error.printStackTrace();try{Files.writeString(CONTROL.resolve("failure"),error.toString());}catch(Exception ignored){}}});
    }
}
