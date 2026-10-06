package qa;

import com.mojang.authlib.GameProfile;
import java.util.*;
import me.pajic.simple_smithing_overhaul.SSO;
import me.pajic.simple_smithing_overhaul.repair.RepairableOverrides;
import me.pajic.simple_smithing_overhaul.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

/** Actual menu and live override checks, with packet-stub players rather than real connections. */
public final class SsoChecks {
 private static int checks;
 private static void check(boolean condition,String label){checks++;if(!condition)throw new AssertionError(label);System.out.println("SSO QA PASS: "+label);}
 @SuppressWarnings("unchecked")
 private static void nativeDecision(ServerPlayer player,boolean supported)throws Exception{
  var field=com.thenathe.suite.network.SuiteCapabilities.class.getDeclaredField("MODULES_KEY");field.setAccessible(true);
  player.connection.getPacketContext().set((net.fabricmc.fabric.api.networking.v1.context.PacketContext.Key<Set<String>>)field.get(null),supported?Set.of("simple_smithing_overhaul"):Set.of());
 }
 private static void materialMenu(ServerPlayer player,BlockPos pos,Item item,Item material,int damage,String label){
  var menu=new AnvilMenu(10,player.getInventory(),ContainerLevelAccess.create(player.level(),pos));
  var source=new ItemStack(item);source.setDamageValue(damage);
  menu.getSlot(0).set(source.copy());menu.getSlot(1).set(new ItemStack(material,2));menu.createResult();
  var result=menu.getSlot(2).getItem();check(result.is(item)&&result.getDamageValue()<damage,label+" actual anvil repair result");
  check(source.getDamageValue()==damage,label+" source copy unchanged");
  check(menu.getSlot(2).mayPickup(player),label+" output can be taken");
  var taken=menu.getSlot(2).remove(1);menu.getSlot(2).onTake(player,taken);
  check(menu.getSlot(0).getItem().isEmpty(),label+" gear consumed exactly once");
  check(menu.getSlot(1).getItem().getCount()<2,label+" repair material consumed");
 }
 public static int runChecks(MinecraftServer server)throws Exception{
  checks=0;var config=SSO.CONFIG.streamlinedRepairs;boolean oldVanilla=config.vanillaRepairables.get();Map<String,String> oldCustom=new HashMap<>(config.modRepairableItems.get());
  try{
   config.vanillaRepairables.accept(true);ModUtil.onUpdateConfig(server);
   check(Items.BOW.components().get(DataComponents.REPAIRABLE)==null,"raw Bow prototype remains unchanged without Defaulted");
   var bow=new ItemStack(Items.BOW);var string=new ItemStack(Items.STRING);
   check(RepairableOverrides.get(Items.BOW)!=null,"original Bow override table populated");
   check(bow.get(DataComponents.REPAIRABLE)!=null&&bow.get(DataComponents.REPAIRABLE).isValidRepairItem(string),"effective Bow getter accepts string");
   check(!bow.get(DataComponents.REPAIRABLE).isValidRepairItem(new ItemStack(Items.REDSTONE)),"wrong Bow material rejected");
   var wings=new ItemStack(Items.ELYTRA);check(wings.get(DataComponents.REPAIRABLE).isValidRepairItem(new ItemStack(Items.PHANTOM_MEMBRANE)),"ordinary Elytra repair material retained");
   Map<String,String> changed=new HashMap<>(oldCustom);changed.put("minecraft:bow","minecraft:redstone");config.modRepairableItems.accept(changed);ModUtil.onUpdateConfig(server);
   check(bow.get(DataComponents.REPAIRABLE).isValidRepairItem(new ItemStack(Items.REDSTONE)),"live custom override applies to existing stack");
   check(!bow.get(DataComponents.REPAIRABLE).isValidRepairItem(string),"live custom override replaces prior material");
   config.modRepairableItems.accept(oldCustom);config.vanillaRepairables.accept(false);ModUtil.onUpdateConfig(server);
   check(RepairableOverrides.get(Items.BOW)==null&&bow.get(DataComponents.REPAIRABLE)==null,"live clear removes obsolete override from existing stack");
   config.vanillaRepairables.accept(true);ModUtil.onUpdateConfig(server);
   check(bow.get(DataComponents.REPAIRABLE).isValidRepairItem(string),"live reset restores original repair rules");
   var player=new ServerPlayer(server,server.overworld(),new GameProfile(UUID.randomUUID(),"SmithingQA"),ClientInformation.createDefault());
   player.connection=new ServerGamePacketListenerImpl(server,new Connection(PacketFlow.SERVERBOUND),player,CommonListenerCookie.createInitial(player.getGameProfile(),false)){@Override public void send(Packet<?> packet){}};
   player.experienceLevel=100;var pos=new BlockPos(20,90,0);player.level().getChunkAt(pos);player.level().setBlock(pos,Blocks.ANVIL.defaultBlockState(),3);
   for(boolean nativeClient:new boolean[]{true,false}){
    nativeDecision(player,nativeClient);String label=nativeClient?"native decision":"fallback decision";
    materialMenu(player,pos,Items.BOW,Items.STRING,100,label+" Bow/string");
    materialMenu(player,pos,Items.ELYTRA,Items.PHANTOM_MEMBRANE,200,label+" Elytra/membrane");
    var menu=new AnvilMenu(11,player.getInventory(),ContainerLevelAccess.create(player.level(),pos));menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));menu.setItemName("QA sword name");menu.createResult();
    check(!menu.getSlot(2).getItem().isEmpty()&&menu.getSlot(2).getItem().getHoverName().getString().equals("QA sword name"),label+" actual anvil rename result");
    check(menu.getCost()==0,label+" default free rename has zero XP cost");
   }
   player.level().removeBlock(pos,false);
  }finally{config.modRepairableItems.accept(oldCustom);config.vanillaRepairables.accept(oldVanilla);ModUtil.onUpdateConfig(server);}
  return checks;
 }
}
