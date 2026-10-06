package qa;

import com.thenathe.multiscale.AtlasOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2i;

/** Actual command execution and cold-client full-pixel reconstruction; never shipped. */
public final class AtlasRepairQa {
    private int checks;
    private void check(boolean ok,String message) { checks++; if(!ok)throw new AssertionError(message); }

    public static int run(MinecraftServer server, ServerPlayer player, ArrayList<Packet<?>> packets,
            ArrayList<net.minecraft.world.entity.item.ItemEntity> drops) throws Exception {
        var qa=new AtlasRepairQa();qa.verify(server,player,packets);qa.cleanup(server,player,drops);
        qa.ejectionAndGeneration(server,player,drops);return qa.checks;
    }

    private void cleanup(MinecraftServer server, ServerPlayer player,
            ArrayList<net.minecraft.world.entity.item.ItemEntity> drops) throws Exception {
        player.setPos(-80,100,-80);var level=server.overworld();level.getChunkAt(player.blockPosition());
        var firstAllocated=MapItem.create(level,-80,-80,(byte)0,true,false);
        var first=new ItemStack(Items.BURIED_TREASURE_MAP);
        first.set(DataComponents.MAP_ID,firstAllocated.get(DataComponents.MAP_ID));
        var other=MapItem.create(level,-80,-80,(byte)0,true,false);
        var firstId=first.get(DataComponents.MAP_ID);var otherId=other.get(DataComponents.MAP_ID);
        var firstData=MapItem.getSavedData(first,level);var otherData=MapItem.getSavedData(other,level);
        java.util.Arrays.fill(firstData.colors,(byte)20);java.util.Arrays.fill(otherData.colors,(byte)28);
        check(!firstId.equals(otherId)&&firstData.centerX==otherData.centerX&&firstData.centerZ==otherData.centerZ,"cleanup fixture has distinct IDs at identical grid/scale/dimension");
        var firstColors=firstData.colors.clone();var otherColors=otherData.colors.clone();
        first.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("First copy"));
        other.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Separate artwork"));
        var duplicate=first.copy();duplicate.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Later copy"));duplicate.setCount(3);
        first.setCount(3);
        var missing=new ItemStack(Items.FILLED_MAP);missing.set(DataComponents.MAP_ID,new MapId(Integer.MAX_VALUE));
        for(int mode=0;mode<3;mode++) {
            player.getInventory().clearContent();
            var atlas=new ItemStack(ModItems.ATLAS);atlas.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Cleanup QA atlas"));
            AtlasOptions.setGenerationMask(atlas,0);AtlasOptions.ensureIdentity(atlas);atlas.set(ModDataComponents.ATLAS_SCALE,0);
            var entries=List.of(ItemStackTemplate.fromNonEmptyStack(first),ItemStackTemplate.fromNonEmptyStack(other),
                    ItemStackTemplate.fromNonEmptyStack(duplicate),ItemStackTemplate.fromNonEmptyStack(missing),
                    ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.FILLED_MAP)),ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.MAP,3)),
                    ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.PAPER,4)));
            var mutable=new BundleContents(entries).asMutable();mutable.toggleSelectedItem(mode==0?4:mode==1?2:0);
            atlas.set(DataComponents.BUNDLE_CONTENTS,mutable.toImmutable());var untouched=atlas.copy();
            int beforeCount=entries.stream().mapToInt(ItemStackTemplate::count).sum();
            boolean pouch=mode==2;
            if(pouch) {
                var bag=new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
                bag.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(atlas)));player.getInventory().setItem(0,bag);
                for(int slot=1;slot<36;slot++)player.getInventory().setItem(slot,new ItemStack(Items.STONE,64));
            } else player.getInventory().setItem(mode==1?40:0,atlas);
            player.getInventory().setItem(2,untouched);
            int beforeDrops=drops.size();
            check(server.getCommands().getDispatcher().execute("atlas dedupe",player.createCommandSourceStack())==5,"cleanup returns5 redundant copies "+mode);
            var after=pouch?com.thenathe.toolpouchcompat.AtlasBridge.atlases(player).getFirst():player.getInventory().getItem(mode==1?40:0);
            var contents=after.get(DataComponents.BUNDLE_CONTENTS);
            check(contents.items().stream().mapToInt(ItemStackTemplate::count).sum()==beforeCount-5,"cleanup conserves retained quantity "+mode);
            check(contents.size()==6,"cleanup removes only later same-ID entry "+mode);
            check(contents.items().getFirst().count()==1&&contents.items().getFirst().get(DataComponents.MAP_ID).equals(firstId),"cleanup reduces first stacked entry to one "+mode);
            check(contents.items().getFirst().is(Items.BURIED_TREASURE_MAP),"cleanup preserves first explorer-map item type "+mode);
            check(contents.items().getFirst().get(DataComponents.CUSTOM_NAME).getString().equals("First copy"),"cleanup preserves first copy components "+mode);
            check(contents.items().get(1).get(DataComponents.MAP_ID).equals(otherId)&&contents.items().get(1).get(DataComponents.CUSTOM_NAME).getString().equals("Separate artwork"),"cleanup preserves distinct same-grid artwork "+mode);
            check(contents.items().stream().filter(e->e.get(DataComponents.MAP_ID)!=null).map(e->e.get(DataComponents.MAP_ID)).collect(java.util.stream.Collectors.toSet()).equals(java.util.Set.of(firstId,otherId,new MapId(Integer.MAX_VALUE))),"cleanup preserves unknown-record identity "+mode);
            check(contents.items().stream().filter(e->e.is(Items.MAP)).mapToInt(ItemStackTemplate::count).sum()==3&&contents.items().stream().filter(e->e.is(Items.PAPER)).mapToInt(ItemStackTemplate::count).sum()==4,"cleanup leaves stored empty maps/paper unchanged "+mode);
            check(contents.getSelectedItemIndex()==(mode==0?3:mode==1?BundleContents.NO_SELECTED_ITEM_INDEX:0),"cleanup remaps or clears selected entry coherently "+mode);
            check(after.get(DataComponents.CUSTOM_NAME).equals(untouched.get(DataComponents.CUSTOM_NAME))&&AtlasOptions.identity(after).equals(AtlasOptions.identity(untouched))&&AtlasOptions.generationMask(after)==0,"cleanup preserves atlas identity/name/options "+mode);
            check(ItemStack.isSameItemSameComponents(player.getInventory().getItem(2),untouched),"cleanup leaves unselected atlas unchanged "+mode);
            check(MapItem.getSavedData(firstId,level)==firstData&&MapItem.getSavedData(otherId,level)==otherData&&java.util.Arrays.equals(firstData.colors,firstColors)&&java.util.Arrays.equals(otherData.colors,otherColors),"cleanup preserves saved record identities and explored pixels "+mode);
            int returned=0;for(int slot=0;slot<player.getInventory().getContainerSize();slot++)if(player.getInventory().getItem(slot).is(Items.MAP))returned+=player.getInventory().getItem(slot).getCount();
            int dropped=0;for(var entity:drops.subList(beforeDrops,drops.size())) {check(entity.getItem().is(Items.MAP),"cleanup only drops ordinary blanks "+mode);dropped+=entity.getItem().getCount();}
            check(returned+dropped==5,"cleanup returns exactly one blank per removed copy "+mode);
            check(pouch?dropped==5:returned==5&&dropped==0,"cleanup inventory-first/overflow delivery "+mode);
            check(server.getCommands().getDispatcher().execute("atlas dedupe",player.createCommandSourceStack())==0,"repeated cleanup has no additional duplicates "+mode);
        }
        player.getInventory().clearContent();
        check(server.getCommands().getDispatcher().execute("atlas dedupe",player.createCommandSourceStack())==0,"cleanup without atlas returns zero");
    }

    private void verify(MinecraftServer server, ServerPlayer player, ArrayList<Packet<?>> packets) throws Exception {
        player.getInventory().clearContent();player.setPos(-80,100,-80);
        // Match a joined player's real inventory synchronizer so packet assertions
        // exercise vanilla remote-slot equality, rather than an uninitialized menu.
        player.initInventoryMenu();
        var book=new ItemStack(ModItems.ATLAS);
        book.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Repair QA atlas"));
        AtlasOptions.setGenerationMask(book,0);book.set(ModDataComponents.ATLAS_SCALE,2);
        book.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID,Integer.MAX_VALUE);
        var entries=new ArrayList<ItemStackTemplate>();
        var snapshots=new HashMap<MapId,byte[]>();
        var originals=new HashMap<MapId,MapItemSavedData>();
        var banners=new HashMap<MapId,List<net.minecraft.world.level.saveddata.maps.MapBanner>>();
        for(var dimension:List.of(Level.OVERWORLD,Level.NETHER,Level.END)) {
            // Keep the banner inside vanilla's decoration bounds even at 1:16.
            var level=server.getLevel(dimension);var pos=new BlockPos(-100,60,-100);
            level.getChunkAt(pos);level.setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3);
            level.setBlock(pos,net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(
                    net.minecraft.resources.Identifier.withDefaultNamespace("red_banner")).defaultBlockState(),3);
            for(byte scale=0;scale<5;scale++) {
                var map=MapItem.create(level,-80,-80,scale,true,false);
                var id=map.get(DataComponents.MAP_ID);var data=MapItem.getSavedData(map,level).locked();
                if(!(dimension.equals(Level.OVERWORLD)&&scale==2))java.util.Arrays.fill(data.colors,(byte)(12+scale));
                level.setMapData(id,data);
                check(data.toggleBanner(level,pos),"fixture stores banner for repair "+dimension+"/"+scale);
                snapshots.put(id,data.colors.clone());originals.put(id,data);banners.put(id,List.copyOf(data.getBanners()));
                map.set(ModDataComponents.MAP_CENTER,new Vector2i(123456,-123456));
                map.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Map "+dimension.identifier()+" "+scale));
                entries.add(ItemStackTemplate.fromNonEmptyStack(map));
            }
        }
        var treasureAllocated=MapItem.create(server.overworld(),-80,-80,(byte)0,true,false);
        var treasureId=treasureAllocated.get(DataComponents.MAP_ID);var treasureData=MapItem.getSavedData(treasureAllocated,server.overworld()).locked();
        java.util.Arrays.fill(treasureData.colors,(byte)48);server.overworld().setMapData(treasureId,treasureData);
        var addDecoration=MapItemSavedData.class.getDeclaredMethod("addDecoration",net.minecraft.core.Holder.class,
                net.minecraft.world.level.LevelAccessor.class,String.class,double.class,double.class,double.class,
                net.minecraft.network.chat.Component.class);addDecoration.setAccessible(true);
        addDecoration.invoke(treasureData,net.minecraft.world.level.saveddata.maps.MapDecorationTypes.RED_X,
                server.overworld(),"repair-treasure",-100.0,-100.0,180.0,null);
        snapshots.put(treasureId,treasureData.colors.clone());originals.put(treasureId,treasureData);banners.put(treasureId,List.copyOf(treasureData.getBanners()));
        var treasure=new ItemStack(Items.BURIED_TREASURE_MAP);treasure.set(DataComponents.MAP_ID,treasureId);
        treasure.set(ModDataComponents.MAP_CENTER,new Vector2i(123456,-123456));
        treasure.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Treasure marker"));
        entries.add(ItemStackTemplate.fromNonEmptyStack(treasure));
        var duplicate=entries.getFirst().create();duplicate.setCount(3);entries.add(ItemStackTemplate.fromNonEmptyStack(duplicate));
        var missing=new ItemStack(Items.FILLED_MAP);missing.set(DataComponents.MAP_ID,new MapId(Integer.MAX_VALUE));
        entries.add(ItemStackTemplate.fromNonEmptyStack(missing));entries.add(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.FILLED_MAP)));
        entries.add(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.MAP,3)));
        entries.add(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.PAPER,4)));
        var contents=new BundleContents(entries).asMutable();contents.toggleSelectedItem(18);book.set(DataComponents.BUNDLE_CONTENTS,contents.toImmutable());
        AtlasOptions.ensureIdentity(book);
        var before=book.copy();player.getInventory().setItem(0,book);
        packets.clear();
        check(server.getCommands().getDispatcher().execute("atlas fix check",player.createCommandSourceStack())==16,"check reports all unique ordinary/explorer records");
        check(ItemStack.isSameItemSameComponents(book,before),"check changes no atlas component");
        check(packets.stream().noneMatch(p->p instanceof ClientboundMapItemDataPacket),"check sends no color snapshots");
        packets.clear();
        check(server.getCommands().getDispatcher().execute("atlas fix",player.createCommandSourceStack())==16,"repair executes actual command");
        assertRepaired(book,before,originals,snapshots,banners,packets);
        assertZeroCenterResend(server,player,book,player.getInventory().getItem(0),packets);

        // Offhand and pouch saveback target only the selected book, never another atlas.
        player.getInventory().clearContent();var offhand=before.copy();player.getInventory().setItem(40,offhand);
        var untouched=before.copy();player.getInventory().setItem(2,untouched);
        packets.clear();check(server.getCommands().getDispatcher().execute("atlas fix",player.createCommandSourceStack())==16,"offhand repair succeeds");
        assertRepaired(offhand,before,originals,snapshots,banners,packets);
        check(ItemStack.isSameItemSameComponents(untouched,before),"offhand repair leaves unselected inventory atlas unchanged");
        assertZeroCenterResend(server,player,offhand,player.getInventory().getItem(40),packets);
        player.getInventory().clearContent();me.pajic.toolpouch.ToolPouch.CONFIG.allowUseFromInventory.accept(true);
        var pouch=new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
        pouch.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(before.copy())));player.getInventory().setItem(0,pouch);
        player.getInventory().setItem(2,untouched);packets.clear();
        check(server.getCommands().getDispatcher().execute("atlas fix",player.createCommandSourceStack())==16,"pouch repair succeeds");
        var repairedPouch=com.thenathe.toolpouchcompat.AtlasBridge.atlases(player).getFirst();
        assertRepaired(repairedPouch,before,originals,snapshots,banners,packets);
        check(ItemStack.isSameItemSameComponents(untouched,before),"pouch repair leaves unselected inventory atlas unchanged");
        assertZeroCenterResend(server,player,repairedPouch,player.getInventory().getItem(0),packets);
        player.getInventory().clearContent();packets.clear();
        check(server.getCommands().getDispatcher().execute("atlas fix",player.createCommandSourceStack())==0,"no atlas repair fails without mutation");
        check(packets.stream().noneMatch(p->p instanceof ClientboundMapItemDataPacket),"no atlas sends no snapshots");
    }

    private void assertZeroCenterResend(MinecraftServer server, ServerPlayer player, ItemStack selected,
            ItemStack transmittedItem, ArrayList<Packet<?>> packets) throws Exception {
        // Establish remote cache equality. A client can still hold corrupt/missing
        // nested metadata independently; ordinary broadcastChanges then sends nothing.
        player.containerMenu.broadcastChanges();packets.clear();
        var before=selected.copy();
        check(server.getCommands().getDispatcher().execute("atlas fix check",player.createCommandSourceStack())==16,"already-correct check reports available records");
        check(ItemStack.isSameItemSameComponents(selected,before),"already-correct check remains read-only");
        check(packets.stream().noneMatch(AtlasRepairQa::inventoryPacket),"check never forces an inventory snapshot");
        check(packets.stream().noneMatch(p->p instanceof ClientboundMapItemDataPacket),"already-correct check never sends map colors");
        check(server.getCommands().getDispatcher().execute("atlas repair check",player.createCommandSourceStack())==16,"repair alias provides read-only atlas check");
        check(ItemStack.isSameItemSameComponents(selected,before)&&packets.stream().noneMatch(AtlasRepairQa::inventoryPacket),"repair check alias never changes or resends inventory");
        packets.clear();
        check(server.getCommands().getDispatcher().execute("atlas fix",player.createCommandSourceStack())==16,"already-correct repair succeeds");
        check(ItemStack.isSameItemSameComponents(selected,before),"already-correct repair preserves exact atlas state");
        boolean found=false;
        for(var packet:packets) {
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket all)
                found|=all.items().stream().anyMatch(stack->ItemStack.isSameItemSameComponents(stack,transmittedItem));
            else if(packet instanceof net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket slot)
                found|=ItemStack.isSameItemSameComponents(slot.getItem(),transmittedItem);
            else if(packet instanceof net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket slot)
                found|=ItemStack.isSameItemSameComponents(slot.contents(),transmittedItem);
        }
        check(found,"zero-center repair resends actual held/offhand/pouch inventory item metadata despite remote equality");
        check(packets.stream().filter(p->p instanceof ClientboundMapItemDataPacket).count()==16,"zero-center repair resends all unique full color records");
    }

    private static boolean inventoryPacket(Packet<?> packet) {
        return packet instanceof net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
                ||packet instanceof net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
                ||packet instanceof net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
    }

    private void ejectionAndGeneration(MinecraftServer server, ServerPlayer player,
            ArrayList<net.minecraft.world.entity.item.ItemEntity> drops) {
        var level=server.overworld();
        for(int mode=0;mode<2;mode++) {
            player.getInventory().clearContent();player.setPos(65536+mode*8192,100,65536);
            level.getChunkAt(player.blockPosition());
            var map=org.sharedregionmaps.SharedMaps.create(level,player.getBlockX(),player.getBlockZ(),(byte)2,true,false);
            var id=map.get(DataComponents.MAP_ID);var saved=MapItem.getSavedData(map,level);
            var neighbor=MapItem.create(level,player.getBlockX()+8192,player.getBlockZ(),(byte)2,true,false);
            var neighborId=neighbor.get(DataComponents.MAP_ID);
            var book=new ItemStack(ModItems.ATLAS);AtlasOptions.setGenerationMask(book,4);
            book.set(ModDataComponents.ATLAS_SCALE,2);AtlasOptions.ensureIdentity(book);
            book.set(DataComponents.BUNDLE_CONTENTS,new BundleContents(List.of(ItemStackTemplate.fromNonEmptyStack(neighbor),
                    ItemStackTemplate.fromNonEmptyStack(map),ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.MAP)))));
            var otherBook=new ItemStack(ModItems.ATLAS);AtlasOptions.setGenerationMask(otherBook,0);
            otherBook.set(DataComponents.BUNDLE_CONTENTS,new BundleContents(List.of(ItemStackTemplate.fromNonEmptyStack(neighbor))));
            var otherBefore=otherBook.copy();player.getInventory().setItem(2,otherBook);
            if(mode==0)player.getInventory().setItem(0,book);
            else {
                var pouch=new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
                pouch.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(book)));player.getInventory().setItem(0,pouch);
            }
            int beforeDrops=drops.size();
            me.pajic.mapstitch.networking.ServerNetworkEvents.ejectMap(
                    new me.pajic.mapstitch.networking.payload.C2SEjectMap(id),player);
            var after=mode==0?player.getInventory().getItem(0):com.thenathe.toolpouchcompat.AtlasBridge.atlases(player).getFirst();
            check(drops.size()==beforeDrops+1,"real C2SEjectMap handler drops one map "+mode);
            var dropped=drops.getLast().getItem();
            check(dropped.getCount()==1&&id.equals(dropped.get(DataComponents.MAP_ID)),"ejection drop identifies exact requested map ID "+mode);
            check(after.get(DataComponents.BUNDLE_CONTENTS).items().stream().noneMatch(entry->id.equals(entry.get(DataComponents.MAP_ID))),"requested map removed before next exploration tick "+mode);
            check(after.get(DataComponents.BUNDLE_CONTENTS).items().stream().anyMatch(entry->neighborId.equals(entry.get(DataComponents.MAP_ID))),"different region map remains in selected atlas "+mode);
            check(ItemStack.isSameItemSameComponents(otherBook,otherBefore),"ejection leaves unrelated atlas unchanged "+mode);
            check(MapItem.getSavedData(id,level)==saved,"ejection preserves world map record "+mode);
            check(after.get(DataComponents.BUNDLE_CONTENTS).items().stream().filter(entry->entry.is(Items.MAP)).mapToInt(ItemStackTemplate::count).sum()==1,"ejection does not consume atlas blank "+mode);
            com.thenathe.multiscale.MixedScaleMaps.selectActive(after,level,player,true);
            if(mode==1)com.thenathe.toolpouchcompat.AtlasBridge.save(player,after,0);
            var regenerated=mode==0?player.getInventory().getItem(0):com.thenathe.toolpouchcompat.AtlasBridge.atlases(player).getFirst();
            check(regenerated.get(ModDataComponents.ATLAS_ACTIVE_MAP_ID)==id.id(),"enabled generation reuses same shared region ID after ejection "+mode);
            check(MapItem.getSavedData(id,level)==saved,"generation reuses saved map object rather than resetting artwork "+mode);
            check(regenerated.get(DataComponents.BUNDLE_CONTENTS).items().stream().filter(entry->id.equals(entry.get(DataComponents.MAP_ID))).mapToInt(ItemStackTemplate::count).sum()==1,"regeneration inserts one atlas copy "+mode);
            check(regenerated.get(DataComponents.BUNDLE_CONTENTS).items().stream().noneMatch(entry->entry.is(Items.MAP)),"regeneration consumes exactly stored blank "+mode);
            check(regenerated.get(DataComponents.BUNDLE_CONTENTS).items().stream().mapToInt(ItemStackTemplate::count).sum()+dropped.getCount()==3,"drop plus regenerated atlas preserves total item quantity "+mode);
            check(AtlasOptions.generationMask(regenerated)==4&&regenerated.get(ModDataComponents.ATLAS_SCALE)==2,"ejection/regeneration preserves generation and display choices "+mode);
        }
        player.getInventory().clearContent();
    }

    private void assertRepaired(ItemStack atlas, ItemStack before, HashMap<MapId,MapItemSavedData> originals,
            HashMap<MapId,byte[]> pixels, HashMap<MapId,List<net.minecraft.world.level.saveddata.maps.MapBanner>> banners,
            ArrayList<Packet<?>> packets) {
        var current=atlas.get(DataComponents.BUNDLE_CONTENTS);var previous=before.get(DataComponents.BUNDLE_CONTENTS);
        check(current.size()==previous.size()&&current.getSelectedItemIndex()==18,"repair preserves entry count and selected index");
        check(atlas.get(ModDataComponents.ATLAS_SCALE)==2&&AtlasOptions.generationMask(atlas)==0,"repair preserves minimap scale and generation toggles");
        check(AtlasOptions.identity(atlas).equals(AtlasOptions.identity(before))&&atlas.get(DataComponents.CUSTOM_NAME).equals(before.get(DataComponents.CUSTOM_NAME)),"repair preserves atlas identity and name");
        for(int i=0;i<current.size();i++) {
            var entry=current.items().get(i);var old=previous.items().get(i);var id=entry.get(DataComponents.MAP_ID);
            check(entry.count()==old.count()&&java.util.Objects.equals(id,old.get(DataComponents.MAP_ID)),"repair preserves exact entry count/ID "+i);
            var data=originals.get(id);
            if(data==null) {check(entry.equals(old),"repair preserves blank, paper or unavailable entry "+i);continue;}
            var center=entry.get(ModDataComponents.MAP_CENTER);
            check(center!=null&&center.x==data.centerX&&center.y==data.centerZ,"repair restores center "+i);
            check(java.util.Objects.equals(entry.get(DataComponents.CUSTOM_NAME),old.get(DataComponents.CUSTOM_NAME)),"repair preserves map name "+i);
            check(data.locked&&java.util.Arrays.equals(data.colors,pixels.get(id)),"repair keeps locked saved artwork "+i);
            check(List.copyOf(data.getBanners()).equals(banners.get(id)),"repair preserves banners "+i);
        }
        var received=new HashSet<MapId>();
        for(var packet:packets)if(packet instanceof ClientboundMapItemDataPacket map) {
            check(received.add(map.mapId()),"repair resends each unique ID once");
            var data=originals.get(map.mapId());var patch=map.colorPatch().orElseThrow();
            check(patch.startX()==0&&patch.startY()==0&&patch.width()==128&&patch.height()==128,"repair sends full128x128 patch "+map.mapId());
            check(patch.mapColors()!=data.colors&&java.util.Arrays.equals(patch.mapColors(),pixels.get(map.mapId())),"snapshot owns exact independent saved pixel buffer "+map.mapId());
            var cold=MapItemSavedData.createForClient(map.scale(),map.locked(),data.dimension);map.applyToMap(cold);
            check(java.util.Arrays.equals(cold.colors,pixels.get(map.mapId())),"packet reconstructs cold client colors "+map.mapId());
            var sent=new ArrayList<net.minecraft.world.level.saveddata.maps.MapDecoration>();cold.getDecorations().forEach(sent::add);
            var expected=new ArrayList<net.minecraft.world.level.saveddata.maps.MapDecoration>();data.getDecorations().forEach(expected::add);
            check(sent.equals(expected),"packet reconstructs existing banner decorations "+map.mapId());
        }
        check(received.equals(originals.keySet()),"repair covers all16ordinary/explorer dim/scale records including disabled generation and other dimensions");
    }
}
