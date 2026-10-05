package qa;

import com.mojang.authlib.GameProfile;
import com.thenathe.multiscale.AtlasDeathRetention;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.simple_death_improvements.SDI;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.*;
import net.minecraft.util.ProblemReporter;
import java.nio.file.*;
import java.util.*;

/** One-off requested death coverage, deliberately outside the general QA runner. */
public final class AtlasDeathOnceQa implements ModInitializer {
 private int checks, cases;
 private final List<String> completed=new ArrayList<>();
 private void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static final class Probe extends ServerPlayer {
  final List<ItemStack> dropped=new ArrayList<>();
  Probe(MinecraftServer server){super(server,server.overworld(),new GameProfile(UUID.randomUUID(),"AtlasDeathQA"),ClientInformation.createDefault());
   connection=new ServerGamePacketListenerImpl(server,new net.minecraft.network.Connection(PacketFlow.SERVERBOUND),this,CommonListenerCookie.createInitial(getGameProfile(),false)){
    @Override public void send(Packet<?> packet){} };
  }
  @Override public ItemEntity createItemStackToDrop(ItemStack stack,boolean random,boolean fromHand){
   if(stack.isEmpty())return null;dropped.add(stack.copy());return new ItemEntity(level(),0,100,0,stack.copy());
  }
 }
 private ItemStack atlas(MinecraftServer server){
  var atlas=new ItemStack(ModItems.ATLAS);
  atlas.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Retained Atlas"));
  atlas.set(DataComponents.BUNDLE_CONTENTS,new BundleContents(List.of(ItemStackTemplate.fromNonEmptyStack(MapItem.create(server.overworld(),0,0,(byte)0,true,false)))));
  return atlas;
 }
 private ItemStack box(Item item,ItemStack...contents){var stack=new ItemStack(item);stack.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(contents)));return stack;}
 private ItemStack bundle(ItemStack...contents){var stack=new ItemStack(Items.BUNDLE);stack.set(DataComponents.BUNDLE_CONTENTS,new BundleContents(Arrays.stream(contents).map(ItemStackTemplate::fromNonEmptyStack).toList()));return stack;}
 private List<ItemStack> flat(ItemStack stack){
  var list=new ArrayList<ItemStack>();if(stack.isEmpty())return list;list.add(stack);
  if(stack.is(ModItems.ATLAS))return list;
  var container=stack.get(DataComponents.CONTAINER);if(container!=null)container.itemCopies().forEach(s->list.addAll(flat(s)));
  var bundle=stack.get(DataComponents.BUNDLE_CONTENTS);if(bundle!=null)bundle.itemCopies().forEach(s->list.addAll(flat(s)));
  return list;
 }
 private List<ItemStack> inventory(Player player){var list=new ArrayList<ItemStack>();for(int i=0;i<player.getInventory().getContainerSize();i++)list.addAll(flat(player.getInventory().getItem(i)));return list;}
 private void run(MinecraftServer server,String label,ItemStack root,EquipmentSlot equip,int expectedAtlases,int expectedOthers,boolean enabled,boolean keepAll) throws Exception {
  cases++;MapStitch.CONFIG.keepAtlasOnDeath.accept(enabled);server.overworld().getGameRules().set(GameRules.KEEP_INVENTORY,keepAll,server);
  var old=new Probe(server);if(equip==null)old.getInventory().setItem(12,root);else old.setItemSlot(equip,root);
  old.getInventory().setItem(15,new ItemStack(Items.DIAMOND,3));
  if(label.startsWith("open pouch")) {
   old.containerMenu=new me.pajic.toolpouch.menu.ToolPouchMenu(1,old.getInventory(),root);
   old.containerMenu.getSlot(0).set(atlas(server));
  }
  if(label.startsWith("open backpack")) {
   old.containerMenu=new me.pajic.tiered_backpacks.ui.BackpackMenu(1,old.getInventory(),root);
   old.containerMenu.getSlot(0).set(atlas(server));
  }
  var drop=Player.class.getDeclaredMethod("dropEquipment",net.minecraft.server.level.ServerLevel.class);drop.setAccessible(true);drop.invoke(old,server.overworld());
  var fresh=new Probe(server);fresh.restoreFrom(old,false);
  if(label.startsWith("bundle")) old.dropped.stream().filter(s->s.is(Items.BUNDLE)).forEach(s -> {
   var contents=s.get(DataComponents.BUNDLE_CONTENTS);
   check(contents.getSelectedItem()==null || !contents.getSelectedItem().is(ModItems.ATLAS),label+" removed atlas selection reset safely");
  });
  var kept=inventory(fresh);var lost=old.dropped.stream().flatMap(s->flat(s).stream()).toList();
  check(kept.stream().filter(s->s.is(ModItems.ATLAS)).mapToInt(ItemStack::getCount).sum()==expectedAtlases,label+" atlas retained once");
  check(kept.stream().filter(s->!s.is(ModItems.ATLAS)).mapToInt(ItemStack::getCount).sum()==expectedOthers,label+" unrelated contents are not retained");
  if(enabled&&!keepAll&&expectedAtlases>0){
   check(lost.stream().noneMatch(s->s.is(ModItems.ATLAS)),label+" no atlas duplicated in dropped container");
   check(lost.stream().filter(s->s.is(Items.DIAMOND)).mapToInt(ItemStack::getCount).sum()==3,label+" unrelated inventory still drops");
   int otherRootCount=flat(root).stream().filter(s->!s.is(ModItems.ATLAS)).mapToInt(ItemStack::getCount).sum();
   check(lost.stream().filter(s->!s.is(ModItems.ATLAS)).mapToInt(ItemStack::getCount).sum()==otherRootCount+3,label+" containing item and every unrelated content drop without loss");
   check(kept.stream().filter(s->s.is(ModItems.ATLAS)).allMatch(s->s.get(DataComponents.BUNDLE_CONTENTS).items().stream().anyMatch(t->t.is(Items.FILLED_MAP))),label+" atlas own map contents remain intact");
  }
  completed.add(label);System.out.println("PASS case "+label);
 }
 public void onInitialize(){ServerLifecycleEvents.SERVER_STARTED.register(server->{String result;try{
  SDI.CONFIG.keepArmorOnDeath.accept(false);SDI.CONFIG.keepHotbarOnDeath.accept(false);SDI.CONFIG.keepOffhandOnDeath.accept(false);
  run(server,"loose inventory",atlas(server),null,1,0,true,false);
  run(server,"loose offhand",atlas(server),EquipmentSlot.OFFHAND,1,0,true,false);
  run(server,"pouch inventory",box(me.pajic.toolpouch.item.ModItems.TOOL_POUCH,atlas(server),new ItemStack(Items.COMPASS)),null,1,0,true,false);
  run(server,"open pouch final contents",box(me.pajic.toolpouch.item.ModItems.TOOL_POUCH,ItemStack.EMPTY,new ItemStack(Items.COMPASS)),null,1,0,true,false);
  run(server,"open backpack final contents",box(me.pajic.tiered_backpacks.item.ModItems.LEATHER_BACKPACK,ItemStack.EMPTY,new ItemStack(Items.STONE,7)),null,1,0,true,false);
  run(server,"pouch leggings attached",box(Items.IRON_LEGGINGS,atlas(server),new ItemStack(Items.COMPASS)),EquipmentSlot.LEGS,1,0,true,false);
  for(Item backpack:List.of(me.pajic.tiered_backpacks.item.ModItems.LEATHER_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.COPPER_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.IRON_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.GOLDEN_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.DIAMOND_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.NETHERITE_BACKPACK))
   run(server,"backpack "+backpack,box(backpack,atlas(server),new ItemStack(Items.STONE,7)),null,1,0,true,false);
  run(server,"backpack chestplate attached",box(Items.IRON_CHESTPLATE,atlas(server),new ItemStack(Items.STONE,7)),EquipmentSlot.CHEST,1,0,true,false);
  run(server,"shulker inventory",box(Items.SHULKER_BOX,atlas(server),new ItemStack(Items.STONE,7)),null,1,0,true,false);
  check(BundleContents.canItemBeInBundle(atlas(server)),"atlas permitted by vanilla bundle predicate");
  var contents=new BundleContents.Mutable();var insert=atlas(server);check(contents.tryInsert(insert)==1&&insert.isEmpty(),"actual bundle insertion accepts atlas");
  var selectedBundle=bundle(atlas(server),new ItemStack(Items.STONE,7));
  var selectedContents=selectedBundle.get(DataComponents.BUNDLE_CONTENTS).asMutable();selectedContents.toggleSelectedItem(0);
  selectedBundle.set(DataComponents.BUNDLE_CONTENTS,selectedContents.toImmutable());
  run(server,"bundle selected atlas",selectedBundle,null,1,0,true,false);
  run(server,"nested bundle in shulker in backpack",box(me.pajic.tiered_backpacks.item.ModItems.DIAMOND_BACKPACK,box(Items.SHULKER_BOX,bundle(atlas(server),new ItemStack(Items.STONE,7))),new ItemStack(Items.COMPASS)),null,1,0,true,false);
  run(server,"two atlases with empty middle slot",box(Items.SHULKER_BOX,atlas(server),ItemStack.EMPTY,atlas(server),new ItemStack(Items.STONE,7)),null,2,0,true,false);
  run(server,"disabled retention loose",atlas(server),null,0,0,false,false);
  run(server,"disabled retention nested",box(Items.SHULKER_BOX,atlas(server),new ItemStack(Items.STONE,7)),null,0,0,false,false);
  run(server,"keep inventory gamerule",box(Items.SHULKER_BOX,atlas(server),new ItemStack(Items.STONE,7)),null,1,11,true,true);
  var cursed=atlas(server);cursed.enchant(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.VANISHING_CURSE),1);
  run(server,"loose vanishing atlas",cursed,null,0,0,true,false);
  var cursedBox=box(Items.SHULKER_BOX,atlas(server));cursedBox.enchant(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.VANISHING_CURSE),1);
  run(server,"vanishing ancestor container",cursedBox,null,0,0,true,false);
  server.overworld().getGameRules().set(GameRules.KEEP_INVENTORY,false,server);MapStitch.CONFIG.keepAtlasOnDeath.accept(true);
  for(boolean nested:List.of(false,true)) {
   var cursorPlayer=new Probe(server);
   for(int slot=0;slot<36;slot++)cursorPlayer.getInventory().setItem(slot,new ItemStack(Items.STONE));
   cursorPlayer.containerMenu=net.minecraft.world.inventory.ChestMenu.threeRows(1,cursorPlayer.getInventory(),new net.minecraft.world.SimpleContainer(27));
   cursorPlayer.containerMenu.setCarried(nested?box(Items.SHULKER_BOX,atlas(server),new ItemStack(Items.COMPASS)):atlas(server));
   var cursorDrop=Player.class.getDeclaredMethod("dropEquipment",net.minecraft.server.level.ServerLevel.class);cursorDrop.setAccessible(true);cursorDrop.invoke(cursorPlayer,server.overworld());
   var restored=new Probe(server);restored.restoreFrom(cursorPlayer,false);
   check(inventory(restored).stream().filter(s->s.is(ModItems.ATLAS)).count()==1,"cursor atlas rescued with full inventory, nested="+nested);
   check(inventory(restored).stream().allMatch(s->s.is(ModItems.ATLAS)),"only cursor atlas retained, nested="+nested);
   check(cursorPlayer.dropped.stream().flatMap(s->flat(s).stream()).noneMatch(s->s.is(ModItems.ATLAS)),"cursor atlas not duplicated in drops, nested="+nested);
   check(cursorPlayer.dropped.stream().flatMap(s->flat(s).stream()).mapToInt(ItemStack::getCount).sum()==(nested?38:36),"full-inventory cursor unrelated items conserved, nested="+nested);
   cases++;completed.add("full inventory cursor "+(nested?"container":"atlas"));
  }
  var old=new Probe(server);var many=new ArrayList<ItemStack>();for(int i=0;i<40;i++)many.add(atlas(server));old.getInventory().setItem(12,box(me.pajic.tiered_backpacks.item.ModItems.NETHERITE_BACKPACK,many.toArray(ItemStack[]::new)));
  var drop=Player.class.getDeclaredMethod("dropEquipment",net.minecraft.server.level.ServerLevel.class);drop.setAccessible(true);drop.invoke(old,server.overworld());
  var fresh=new Probe(server);fresh.restoreFrom(old,false);check(inventory(fresh).stream().filter(s->s.is(ModItems.ATLAS)).count()==36,"36 atlases fit main inventory");
  check(((AtlasDeathRetention.State)(Object)fresh).suite$retainedAtlases().size()==4,"overflow atlases retained pending, not dropped");
  var output=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,server.registryAccess());fresh.saveWithoutId(output);var saved=output.buildResult();
  var reloaded=new Probe(server);reloaded.load(TagValueInput.create(ProblemReporter.DISCARDING,server.registryAccess(),saved));
  check(((AtlasDeathRetention.State)(Object)reloaded).suite$retainedAtlases().size()==4,"overflow survives player save/load");
  reloaded.getInventory().setItem(0,ItemStack.EMPTY);AtlasDeathRetention.flush(reloaded);
  check(((AtlasDeathRetention.State)(Object)reloaded).suite$retainedAtlases().size()==3&&reloaded.getInventory().getItem(0).is(ModItems.ATLAS),"overflow returns when inventory space opens");
  completed.add("overflow save/load and room recovery");cases++;
  result="PASS "+checks+" assertions, "+cases+" death cases: "+String.join("; ",completed);
 }catch(Throwable error){error.printStackTrace();result="FAIL "+error;}
 try{Files.writeString(Path.of("atlas-death-once-result.txt"),result+"\n");}catch(Exception error){throw new RuntimeException(error);}finally{server.halt(false);}});}
}
