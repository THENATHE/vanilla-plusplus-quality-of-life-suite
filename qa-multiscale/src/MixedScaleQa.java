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
   var player=new ServerPlayer(server,level,profile,ClientInformation.createDefault());
   player.connection=new ServerGamePacketListenerImpl(server,new Connection(PacketFlow.SERVERBOUND),player,CommonListenerCookie.createInitial(profile,false)) {
    @Override public void send(Packet<?> packet) {}
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
   } else {
    ItemStack atlas=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("mapstitch","atlas")));
    check(atlas.getItem() instanceof AtlasItem,"real atlas registry item");
    AtlasItem item=(AtlasItem)atlas.getItem();
    var container=new SimpleContainer(1);var slot=new Slot(container,0,0,0);
    Set<Integer> expected=new HashSet<>();
    for(int scale=0;scale<5;scale++) {
     ItemStack map=MapItem.create(level,0,0,(byte)scale,true,false);expected.add(map.get(DataComponents.MAP_ID).id());
     container.setItem(0,map);
     check(item.overrideStackedOnOther(atlas,slot,ClickAction.PRIMARY,player),"slot insertion accepts scale "+scale);
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
    var encoded=ItemStack.CODEC.encodeStart(ops,atlas).getOrThrow();
    ItemStack loaded=ItemStack.CODEC.parse(ops,encoded).getOrThrow();
    check(ids(loaded).equals(expected),"codec retains mixed-scale contents");
    check(loaded.get(ModDataComponents.ATLAS_SCALE)==3,"codec retains selected scale");
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
   result="PASS "+checks+" mixed-scale insertion, extraction, codec and restart checks";
  } catch(Throwable error) {error.printStackTrace();result="FAIL "+error;}
  try {Files.writeString(Path.of("mixedscale-qa-result.txt"),result+"\n");}catch(Exception e){throw new RuntimeException(e);}
  finally {server.halt(false);}
 }); }
}
