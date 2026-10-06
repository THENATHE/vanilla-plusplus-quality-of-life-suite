package qa;

import com.mojang.authlib.GameProfile;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.AtlasItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.maps.MapId;
import java.nio.file.*;
import java.util.*;

/** Runs against the packaged addon in disposable worlds; never included in the mod. */
public final class MixedScaleQa implements ModInitializer {
 private int checks;
 private void check(boolean ok,String message) { checks++;if(!ok)throw new AssertionError(message); }
 private Set<Integer> ids(ItemStack atlas) {
  Set<Integer> result=new HashSet<>();
  for(var item:atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS,BundleContents.EMPTY).items()) {
   MapId id=item.get(DataComponents.MAP_ID);if(id!=null)result.add(id.id());
  }
  return result;
 }
 public void onInitialize() { ServerLifecycleEvents.SERVER_STARTED.register(server -> {
  String result;
  try {
   ServerLevel level=server.overworld();
   var profile=new GameProfile(UUID.randomUUID(),"MixedScaleQA");
   var extractionDrops=new ArrayList<net.minecraft.world.entity.item.ItemEntity>();
   var player=new ServerPlayer(server,level,profile,ClientInformation.createDefault()) {
    @Override public net.minecraft.world.entity.item.ItemEntity drop(ItemStack stack,boolean randomThrow,net.minecraft.util.Prediction prediction) {
     var entity=super.drop(stack,randomThrow,prediction);
     if(entity!=null)extractionDrops.add(entity);
     return entity;
    }
   };
   var creationSounds=new java.util.concurrent.atomic.AtomicInteger();
   var observedPackets=new ArrayList<Packet<?>>();
   player.connection=new ServerGamePacketListenerImpl(server,new Connection(PacketFlow.SERVERBOUND),player,CommonListenerCookie.createInitial(profile,false)) {
    @Override public void send(Packet<?> packet) {
     observedPackets.add(packet);
     if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound && sound.getSound().value()==net.minecraft.sounds.SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT)creationSounds.incrementAndGet();
    }
   };
   var key=com.thenathe.suite.network.SuiteCapabilities.class.getDeclaredField("MODULES_KEY");key.setAccessible(true);
   player.connection.getPacketContext().set((net.fabricmc.fabric.api.networking.v1.context.PacketContext.Key<java.util.Set<String>>)key.get(null),java.util.Set.of("toolpouch","mapstitch"));
   var ops=server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
   Path saved=Path.of("mixedscale-atlas.dat");
   if(Files.exists(saved)) {
    ItemStack loaded=ItemStack.CODEC.parse(ops,NbtIo.readCompressed(saved,net.minecraft.nbt.NbtAccounter.unlimitedHeap())).getOrThrow();
    check(ids(loaded).size()==5,"restart retains five map identities");
    Set<Integer> scales=new HashSet<>();
    for(var item:loaded.get(DataComponents.BUNDLE_CONTENTS).items())scales.add((int)MapItem.getSavedData(item.get(DataComponents.MAP_ID),level).scale);
    check(scales.equals(Set.of(0,1,2,3,4)),"restart retains all five scales and saved map data");
    check(loaded.get(ModDataComponents.ATLAS_SCALE)==3,"restart retains selected scale");
    check(com.thenathe.multiscale.AtlasOptions.generationMask(loaded)==21,"restart retains independent generation choices");
    check(loaded.get(DataComponents.CUSTOM_DATA).copyTag().getStringOr("qa-marker", "").equals("keep"),"restart preserves unrelated custom data");
   } else {
    ItemStack atlas=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("mapstitch","atlas")));
    check(atlas.getItem() instanceof AtlasItem,"real atlas registry item");
    AtlasItem item=(AtlasItem)atlas.getItem();
    var container=new SimpleContainer(1);var slot=new Slot(container,0,0,0);
    Set<Integer> expected=new HashSet<>();
    for(int scale=0;scale<5;scale++) {
     ItemStack map=MapItem.create(level,0,0,(byte)scale,true,false);expected.add(map.get(DataComponents.MAP_ID).id());
     container.setItem(0,map);
     check(item.overrideStackedOnOther(atlas,slot,ClickAction.PRIMARY,player),"slot insertion accepts scale "+scale+"; atlas="+atlas.getComponents()+"; map="+map.getComponents()+"; data="+MapItem.getSavedData(map,level)+"; contents="+atlas.get(DataComponents.BUNDLE_CONTENTS));
     check(container.getItem(0).isEmpty(),"slot insertion consumes map once "+scale);
     check(ids(atlas).equals(expected),"slot insertion preserves every identity "+scale);
    }
    // Actual secondary-click extraction followed by carried-stack insertion.
    var carried=new ItemStack[]{ItemStack.EMPTY};
    SlotAccess access=new SlotAccess(){public ItemStack get(){return carried[0];}public boolean set(ItemStack s){carried[0]=s;return true;}};
    check(item.overrideOtherStackedOnMe(atlas,ItemStack.EMPTY,slot,ClickAction.SECONDARY,player,access),"extract to cursor");
    check(carried[0].is(Items.FILLED_MAP)&&ids(atlas).size()==4,"extract exactly one map");
    check(item.overrideOtherStackedOnMe(atlas,carried[0],slot,ClickAction.PRIMARY,player,access),"reinsert cursor map");
    check(carried[0].isEmpty()&&ids(atlas).equals(expected),"cursor insertion conserves all map IDs");
    atlas.set(ModDataComponents.ATLAS_SCALE,3);
    check(com.thenathe.multiscale.AtlasOptions.generationMask(atlas)==8,"old atlas defaults generation to its selected scale");
    net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA,atlas,tag->tag.putString("qa-marker","keep"));
    com.thenathe.multiscale.AtlasOptions.setGenerationMask(atlas,21);
    var encoded=ItemStack.CODEC.encodeStart(ops,atlas).getOrThrow();
    ItemStack loaded=ItemStack.CODEC.parse(ops,encoded).getOrThrow();
    check(ids(loaded).equals(expected),"codec retains mixed-scale contents");
    check(loaded.get(ModDataComponents.ATLAS_SCALE)==3,"codec retains selected scale");
    check(com.thenathe.multiscale.AtlasOptions.generationMask(loaded)==21,"codec retains independent generation choices");
    NbtIo.writeCompressed((CompoundTag)encoded,saved);
    for(int scale=0;scale<5;scale++) {
     atlas.set(ModDataComponents.ATLAS_SCALE,scale);
     com.thenathe.multiscale.MixedScaleMaps.selectActive(atlas,level,player,false);
     var active=MapItem.getSavedData(new MapId(atlas.get(ModDataComponents.ATLAS_ACTIVE_MAP_ID)),level);
     check(active!=null&&active.scale==scale,"active selection follows chosen layer "+scale);
     check(ids(atlas).equals(expected),"layer switching preserves map IDs "+scale);
    }
    // A distant region requires one new map for each requested layer.
    player.setPos(16384,100,16384);
    container.setItem(0,new ItemStack(Items.MAP,5));
    check(item.overrideStackedOnOther(atlas,slot,ClickAction.PRIMARY,player),"insert blank maps");
    Set<Integer> generated=new HashSet<>();
    for(int scale=0;scale<5;scale++) {
     atlas.set(ModDataComponents.ATLAS_SCALE,scale);
     com.thenathe.multiscale.AtlasOptions.setGenerationMask(atlas,1<<scale);
     com.thenathe.multiscale.MixedScaleMaps.selectActive(atlas,level,player,true);
     int id=atlas.get(ModDataComponents.ATLAS_ACTIVE_MAP_ID);generated.add(id);
     var active=MapItem.getSavedData(new MapId(id),level);
     check(active!=null&&active.scale==scale,"generated map has chosen scale "+scale);
     check(ids(atlas).containsAll(expected)&&ids(atlas).size()==6+scale,"generation consumes exactly one blank "+scale);
     var copy=org.sharedregionmaps.SharedMaps.create(level,16384,16384,(byte)scale,true,false);
     check(copy.get(DataComponents.MAP_ID).id()==id,"shared regions retain scale identity "+scale);
    }
    check(generated.size()==5,"overlapping layers retain five distinct map IDs");
    check(atlas.get(DataComponents.BUNDLE_CONTENTS).items().stream().mapToInt(net.minecraft.world.item.ItemStackTemplate::count).sum()==10,"all ten maps conserved");
    check(creationSounds.get()==5,"each one-map generation batch sends its explorer one vanilla sound");
    var multi=new ItemStack(item);
    multi.set(ModDataComponents.ATLAS_SCALE,4);
    multi.set(DataComponents.BUNDLE_CONTENTS,new BundleContents(java.util.List.of(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.MAP,5)))));
    com.thenathe.multiscale.AtlasOptions.setGenerationMask(multi,5);
    com.thenathe.multiscale.MixedScaleMaps.selectActive(multi,level,player,true);
    var layerSet=new HashSet<Integer>();
    for(var entry:multi.get(DataComponents.BUNDLE_CONTENTS).items())if(entry.get(DataComponents.MAP_ID)!=null)layerSet.add((int)MapItem.getSavedData(entry.get(DataComponents.MAP_ID),level).scale);
    check(layerSet.equals(Set.of(0,2)),"only enabled layers generate even when minimap differs");
    check(multi.get(ModDataComponents.ATLAS_ACTIVE_MAP_ID)==-1,"unavailable minimap layer does not choose another scale");
    check(multi.get(DataComponents.BUNDLE_CONTENTS).items().stream().mapToInt(ItemStackTemplate::count).sum()==5,"multi-layer generation preserves total item count");
    check(multi.get(DataComponents.BUNDLE_CONTENTS).items().stream().filter(e->e.is(Items.MAP)).mapToInt(ItemStackTemplate::count).sum()==3,"two enabled layers consume exactly two blanks");
    check(creationSounds.get()==6,"multi-map generation batch plays one chime");
    com.thenathe.multiscale.MixedScaleMaps.selectActive(multi,level,player,true);
    check(ids(multi).size()==2&&creationSounds.get()==6,"existing layer coverage consumes no blanks and plays no sound");
    player.getInventory().setItem(5,multi);
    var target=new com.thenathe.multiscale.AtlasTarget(com.thenathe.multiscale.AtlasTarget.INVENTORY,5,com.thenathe.multiscale.AtlasTarget.anchor(multi),com.thenathe.multiscale.AtlasOptions.identity(multi));
    check(com.thenathe.multiscale.MixedScales.select(player,new com.thenathe.multiscale.MixedScales.SelectScale(target,2)),"server accepts independent minimap selection");
    check(com.thenathe.multiscale.AtlasOptions.generationMask(multi)==5,"minimap selection preserves generation toggles");
    check(com.thenathe.multiscale.MixedScales.selectGeneration(player,new com.thenathe.multiscale.MixedScales.SelectGeneration(target,0)),"all generation can be disabled");
    check(multi.get(ModDataComponents.ATLAS_SCALE)==2,"generation changes preserve minimap scale");
    check(!com.thenathe.multiscale.MixedScales.selectGeneration(player,new com.thenathe.multiscale.MixedScales.SelectGeneration(target,32)),"invalid generation mask rejected");
    check(!com.thenathe.multiscale.MixedScales.selectGeneration(player,new com.thenathe.multiscale.MixedScales.SelectGeneration(new com.thenathe.multiscale.AtlasTarget(0,5,999999),1)),"stale atlas anchor rejected");
    var modules=player.connection.getPacketContext();
    modules.set((net.fabricmc.fabric.api.networking.v1.context.PacketContext.Key<java.util.Set<String>>)key.get(null),java.util.Set.of());
    check(!com.thenathe.multiscale.MixedScales.selectGeneration(player,new com.thenathe.multiscale.MixedScales.SelectGeneration(target,1)),"non-native generation request rejected");
    modules.set((net.fabricmc.fabric.api.networking.v1.context.PacketContext.Key<java.util.Set<String>>)key.get(null),java.util.Set.of("toolpouch","mapstitch"));
    var replacement=multi.copy();
    net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA,replacement,tag->tag.remove(com.thenathe.multiscale.AtlasOptions.IDENTITY));
    com.thenathe.multiscale.AtlasOptions.ensureIdentity(replacement);
    player.getInventory().setItem(5,replacement);
    check(!com.thenathe.multiscale.MixedScales.selectGeneration(player,new com.thenathe.multiscale.MixedScales.SelectGeneration(target,1)),"replacing the same slot with a different atlas rejects the bound target");
    player.getInventory().setItem(5,multi);
    player.setPos(32768,100,32768);
    com.thenathe.multiscale.MixedScaleMaps.selectActive(multi,level,player,true);
    check(ids(multi).size()==2&&creationSounds.get()==6,"all-off generation does not create maps in a new region");
    player.setPos(16384,100,16384);
    me.pajic.toolpouch.ToolPouch.CONFIG.allowUseFromInventory.accept(true);
    var pouch=new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
    pouch.set(DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.fromItems(java.util.List.of(atlas)));
    player.getInventory().setItem(0,pouch);
    atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID,-1);
    pouch.set(DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.fromItems(java.util.List.of(atlas)));
    com.thenathe.toolpouchcompat.AtlasBridge.tick(player);
    var fromPouch=com.thenathe.toolpouchcompat.AtlasBridge.atlases(player);
    check(fromPouch.size()==1&&ids(fromPouch.getFirst()).equals(ids(atlas)),"pouch tick conserves mixed atlas maps; atlases="+fromPouch.size()+"; pouch="+player.getInventory().getItem(0)+"; inventoryAllowed="+me.pajic.toolpouch.ToolPouch.CONFIG.allowUseFromInventory.get());
    check(fromPouch.getFirst().get(ModDataComponents.ATLAS_ACTIVE_MAP_ID)!=-1,"pouch tick saves selected active map");
    var duplicate=fromPouch.getFirst().copy();
    player.getInventory().setItem(1,duplicate);
    var preferred=com.thenathe.multiscale.AtlasTarget.firstForScan(player,java.util.List.of("accessories","hotbar","inventory"));
    check(preferred!=null&&preferred.location()==com.thenathe.multiscale.AtlasTarget.POUCH&&preferred.index()==0,"identical inventory atlas does not steal pouch minimap target");
    check(com.thenathe.multiscale.MixedScales.select(player,new com.thenathe.multiscale.MixedScales.SelectScale(preferred,2)),"select scale on preferred pouch atlas");
    check(com.thenathe.toolpouchcompat.AtlasBridge.atlases(player).getFirst().get(ModDataComponents.ATLAS_SCALE)==2&&duplicate.get(ModDataComponents.ATLAS_SCALE)==4,"pouch scale selection leaves identical inventory book unchanged");
    var inventoryOnly=com.thenathe.multiscale.AtlasTarget.firstForScan(player,java.util.List.of("hotbar","inventory"));
    check(inventoryOnly!=null&&inventoryOnly.location()==com.thenathe.multiscale.AtlasTarget.INVENTORY&&inventoryOnly.index()==1,"disabled accessories scan selects inventory book");
   }
   extractionChecks(server,player,extractionDrops);
   checks += AtlasBannerQa.run(server,player);
   mapIntegrityChecks(server,player,observedPackets);
   checks += AtlasRepairQa.run(server,player,observedPackets,extractionDrops);
   result="PASS "+checks+" mixed-scale insertion, extraction commands, banner edits, map integrity, codec and restart checks";
  } catch(Throwable error) {error.printStackTrace();result="FAIL "+error;}
  try {Files.writeString(Path.of("mixedscale-qa-result.txt"),result+"\n");}catch(Exception e){throw new RuntimeException(e);}
  finally {server.halt(false);}
 }); } private void extractionChecks(net.minecraft.server.MinecraftServer server, ServerPlayer player, ArrayList<net.minecraft.world.entity.item.ItemEntity> extractionDrops) throws Exception {
  me.pajic.toolpouch.ToolPouch.CONFIG.allowUseFromInventory.accept(true);
  String[] commands={"extractmap 1:1 minecraft:the_end", "extractmap 1:1", "extractmap minecraft:the_end", "extractmap empty", "extractmap scale 1:16 minecraft:the_nether", "extractmap dimension minecraft:the_end 1:1", "extractmap scale 1:1", "extractmap dimension minecraft:the_end", "extractmap 1:16 minecraft:the_end", "extractmap 1:1 minecraft:the_end", "extractmap minecraft:missing", "extractmap scale 1:3"};
  int[] expected={1,3,2,8,1,1,3,2,1,1,0,0};
  for(int test=0;test<commands.length;test++) {
   player.getInventory().clearContent();
   var book=new ItemStack(me.pajic.mapstitch.item.ModItems.ATLAS);
   book.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Extraction QA book"));
   com.thenathe.multiscale.AtlasOptions.setGenerationMask(book,21);
   book.set(ModDataComponents.ATLAS_SCALE,4);
   var entries=new ArrayList<ItemStackTemplate>();
   var original=new HashMap<Integer,ItemStack>();
   for(var dim:java.util.List.of(net.minecraft.world.level.Level.OVERWORLD,net.minecraft.world.level.Level.NETHER,net.minecraft.world.level.Level.END)) {
    var origin=server.getLevel(dim);check(origin!=null,"extraction fixture dimension loaded "+dim.identifier());
    for(byte scale:new byte[]{0,4}) {
     var map=MapItem.create(origin,1234,5678,scale,true,false);
     map.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal(dim.identifier()+" scale "+scale));
     original.put(map.get(DataComponents.MAP_ID).id(),map.copy());entries.add(ItemStackTemplate.fromNonEmptyStack(map));
    }
   }
   var unknown=new ItemStack(Items.FILLED_MAP);unknown.set(DataComponents.MAP_ID,new MapId(Integer.MAX_VALUE));
   entries.add(ItemStackTemplate.fromNonEmptyStack(unknown));
   entries.add(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.MAP,3)));
   entries.add(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.PAPER,5)));
   var initial=new BundleContents(entries);var mutable=initial.asMutable();mutable.toggleSelectedItem(8);book.set(DataComponents.BUNDLE_CONTENTS,mutable.toImmutable());
   com.thenathe.multiscale.AtlasOptions.ensureIdentity(book);
   var identity=com.thenathe.multiscale.AtlasOptions.identity(book);
   boolean pouch=test==8,offhand=test==9,overflow=test==4;
   var untouched=book.copy();
   if(pouch) {
    var bag=new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
    bag.set(DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.fromItems(java.util.List.of(book)));
    player.getInventory().setItem(0,bag);
   } else player.getInventory().setItem(offhand?40:0,book);
   // Another unselected atlas must not be modified, even if its contents match.
   player.getInventory().setItem(2,untouched);
   if(overflow)for(int slot=1;slot<36;slot++)if(slot!=2)player.getInventory().setItem(slot,new ItemStack(Items.STONE,64));
   var level=player.level();level.getChunkAt(player.blockPosition());
   // Observe real drop() results, not a chunk query that can include asynchronously
   // reloaded item entities from the previous restart phase.
   int beforeDrops=extractionDrops.size();
   int returned;
   try {returned=server.getCommands().getDispatcher().execute(commands[test],player.createCommandSourceStack());}
   catch(com.mojang.brigadier.exceptions.CommandSyntaxException error) {if(test!=11)throw error;returned=0;}
   check(returned==expected[test],"actual command "+commands[test]+" extracts expected quantity "+expected[test]);
   var after=pouch?com.thenathe.toolpouchcompat.AtlasBridge.atlases(player).getFirst():player.getInventory().getItem(offhand?40:0);
   int remaining=after.get(DataComponents.BUNDLE_CONTENTS).items().stream().mapToInt(ItemStackTemplate::count).sum();
   check(remaining==15-expected[test],"command conserves atlas contents quantity "+commands[test]);
   check(com.thenathe.multiscale.AtlasOptions.identity(after).equals(identity)&&after.get(DataComponents.CUSTOM_NAME).getString().equals("Extraction QA book"),"command preserves book identity and name "+test);
   check(com.thenathe.multiscale.AtlasOptions.generationMask(after)==21&&after.get(ModDataComponents.ATLAS_SCALE)==4,"command preserves generation and minimap choices "+test);
   check(ItemStack.isSameItemSameComponents(player.getInventory().getItem(2),untouched),"command leaves other atlas unchanged "+test);
   check(ids(after).contains(Integer.MAX_VALUE),"unavailable map data remains untouched "+test);
   var outputs=new ArrayList<ItemStack>();
   for(int slot=0;slot<player.getInventory().getContainerSize();slot++) {
    var stack=player.getInventory().getItem(slot);if(stack.is(Items.FILLED_MAP)||stack.is(Items.MAP)||stack.is(Items.PAPER))outputs.add(stack);
   }
   int dropped=0;
   for(var entity:extractionDrops.subList(beforeDrops,extractionDrops.size())){outputs.add(entity.getItem());dropped+=entity.getItem().getCount();}
   check(outputs.stream().mapToInt(ItemStack::getCount).sum()==expected[test],"actual inventory plus drops conserve extracted quantity "+test);
   if(overflow)check(dropped==expected[test],"full inventory drops matching contents once");
   for(var output:outputs)if(output.is(Items.FILLED_MAP)) {
    var id=output.get(DataComponents.MAP_ID).id();check(original.containsKey(id)&&ItemStack.isSameItemSameComponents(output,original.get(id)),"extracted map preserves identity/custom components "+id);
    check(!ids(after).contains(id),"extracted map removed from selected atlas only "+id);
   }
   if(test==3)check(ids(after).size()==7&&after.get(DataComponents.BUNDLE_CONTENTS).items().stream().noneMatch(e->e.is(Items.MAP)||e.is(Items.PAPER)),"empty filter removes all paper/blanks and retains all filled maps");
  }
  player.getInventory().clearContent();
  check(server.getCommands().getDispatcher().execute("extractmap empty",player.createCommandSourceStack())==0,"no atlas reports failure without extracting another inventory");
 }

 private void mapIntegrityChecks(net.minecraft.server.MinecraftServer server, ServerPlayer player,
                                 ArrayList<Packet<?>> packets) {
  // Saved coordinates are authoritative. A stale or absent item center must not
  // strand a persisted region after reconnect, switching scale, or ticking in a pouch.
  player.getInventory().clearContent();
  player.setPos(-65,100,-65);
  var book=new ItemStack(me.pajic.mapstitch.item.ModItems.ATLAS);
  com.thenathe.multiscale.AtlasOptions.setGenerationMask(book,0);
  var maps=new ArrayList<ItemStackTemplate>();
  var originals=new HashMap<Integer,byte[]>();
  var dimensions=java.util.List.of(net.minecraft.world.level.Level.OVERWORLD,
          net.minecraft.world.level.Level.NETHER,net.minecraft.world.level.Level.END);
  for(var dimension:dimensions) {
   var level=server.getLevel(dimension);
   for(byte scale=0;scale<5;scale++) {
    var map=MapItem.create(level,-65,-65,scale,true,false);
    var id=map.get(DataComponents.MAP_ID);
    var data=MapItem.getSavedData(map,level).locked();
    java.util.Arrays.fill(data.colors,(byte)(4+scale));
    level.setMapData(id,data);originals.put(id.id(),data.colors.clone());
    // Deliberately preserve a wrong existing value as well as one missing value.
    if(scale!=0)map.set(ModDataComponents.MAP_CENTER,new org.joml.Vector2i(999999,-999999));
    map.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Integrity "+dimension.identifier()+" "+scale));
    maps.add(ItemStackTemplate.fromNonEmptyStack(map));
    int radius=64<<scale;
    check(com.thenathe.multiscale.MixedScaleMaps.covers(data,data.centerX-radius,data.centerZ-radius),"coverage includes low corner "+dimension+"/"+scale);
    check(com.thenathe.multiscale.MixedScaleMaps.covers(data,data.centerX+radius-1,data.centerZ+radius-1),"coverage includes last high pixel "+dimension+"/"+scale);
    check(!com.thenathe.multiscale.MixedScaleMaps.covers(data,data.centerX+radius,data.centerZ),"coverage excludes next east cell "+dimension+"/"+scale);
    check(!com.thenathe.multiscale.MixedScaleMaps.covers(data,data.centerX,data.centerZ+radius),"coverage excludes next south cell "+dimension+"/"+scale);
    check(!com.thenathe.multiscale.MixedScaleMaps.covers(data,data.centerX-radius-1,data.centerZ),"coverage excludes previous west cell "+dimension+"/"+scale);
   }
  }
  var treasureSource=MapItem.create(server.overworld(),-65,-65,(byte)0,true,false);
  var treasureId=treasureSource.get(DataComponents.MAP_ID);
  var treasureData=MapItem.getSavedData(treasureId,server.overworld()).locked();
  java.util.Arrays.fill(treasureData.colors,(byte)32);server.overworld().setMapData(treasureId,treasureData);
  originals.put(treasureId.id(),treasureData.colors.clone());
  var treasure=new ItemStack(Items.BURIED_TREASURE_MAP);treasure.set(DataComponents.MAP_ID,treasureId);
  treasure.set(ModDataComponents.MAP_CENTER,new org.joml.Vector2i(999999,-999999));
  treasure.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Integrity explorer"));
  maps.add(ItemStackTemplate.fromNonEmptyStack(treasure));
  var unavailable=new ItemStack(Items.FILLED_MAP);unavailable.set(DataComponents.MAP_ID,new MapId(Integer.MAX_VALUE));
  maps.add(ItemStackTemplate.fromNonEmptyStack(unavailable));
  maps.add(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.MAP,3)));
  maps.add(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.PAPER,4)));
  var contents=new BundleContents(maps).asMutable();contents.toggleSelectedItem(7);book.set(DataComponents.BUNDLE_CONTENTS,contents.toImmutable());
  player.getInventory().setItem(0,book);
  var expectedIds=ids(book);
  int count=book.get(DataComponents.BUNDLE_CONTENTS).items().stream().mapToInt(ItemStackTemplate::count).sum();
  for(var dimension:dimensions)for(int scale=0;scale<5;scale++) {
   var level=server.getLevel(dimension);book.set(ModDataComponents.ATLAS_SCALE,scale);
   ((AtlasItem)book.getItem()).inventoryTick(book,level,player,null);
   var id=new MapId(book.get(ModDataComponents.ATLAS_ACTIVE_MAP_ID));
   var active=MapItem.getSavedData(id,level);
   check(active!=null&&active.scale==scale&&active.dimension.equals(dimension),"real tick selects requested region/dimension/scale "+dimension+"/"+scale);
   check(active.locked&&java.util.Arrays.equals(active.colors,originals.get(id.id())),"tick preserves locked artwork "+dimension+"/"+scale);
   check(ids(book).equals(expectedIds),"tick preserves map IDs including unavailable saved record "+dimension+"/"+scale);
   check(book.get(DataComponents.BUNDLE_CONTENTS).items().stream().mapToInt(ItemStackTemplate::count).sum()==count,"all-off tick consumes no blank maps or paper "+dimension+"/"+scale);
   check(book.get(DataComponents.BUNDLE_CONTENTS).getSelectedItemIndex()==7,"tick preserves selected bundle item "+dimension+"/"+scale);
  }
  for(var template:book.get(DataComponents.BUNDLE_CONTENTS).items()) {
   var id=template.get(DataComponents.MAP_ID);if(id==null||id.id()==Integer.MAX_VALUE)continue;
   var data=MapItem.getSavedData(id,server.overworld());
   var center=template.get(ModDataComponents.MAP_CENTER);
   check(center!=null&&center.x==data.centerX&&center.y==data.centerZ,"real tick reconciles absent and stale MAP_CENTER "+id.id());
   check(java.util.Arrays.equals(data.colors,originals.get(id.id())),"center repair never changes saved pixels "+id.id());
   check(template.get(DataComponents.CUSTOM_NAME).getString().startsWith("Integrity "),"center repair preserves custom names "+id.id());
  }
  // Upstream's force=true uses the last dirty rectangle, not a guaranteed full
  // snapshot. This proves why repair must explicitly send all 128 x 128 pixels.
  var id=maps.getFirst().get(DataComponents.MAP_ID);
  var data=MapItem.getSavedData(id,server.overworld());
  data.getHoldingPlayer(player);data.getUpdatePacket(id,player);
  data.setColor(3,4,(byte)40);
  var incremental=(net.minecraft.network.protocol.game.ClientboundMapItemDataPacket)data.getUpdatePacket(id,player);
  check(incremental!=null&&incremental.colorPatch().orElseThrow().width()==1&&incremental.colorPatch().orElseThrow().height()==1,"ordinary update consumes a one-pixel dirty rectangle");
  packets.clear();me.pajic.mapstitch.util.ModUtil.sendVanillaMapPacket(id,data,player,true);
  var forced=packets.stream().filter(p->p instanceof net.minecraft.network.protocol.game.ClientboundMapItemDataPacket).map(p->(net.minecraft.network.protocol.game.ClientboundMapItemDataPacket)p).findFirst().orElseThrow();
  check(forced.colorPatch().orElseThrow().width()==1&&forced.colorPatch().orElseThrow().height()==1,"upstream force retains one-pixel patch rather than restoring a cold client cache");
  player.getInventory().clearContent();
 }

}
