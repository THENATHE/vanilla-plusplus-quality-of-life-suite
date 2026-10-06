package qa.pouchmending;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.*;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import me.pajic.simple_smithing_overhaul.SSO;
import me.pajic.simple_smithing_overhaul.util.ModUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.GameType;
/** Actual orb pickups, Inventory.tick and LivingEntity.updateFallFlying in a dedicated world. */
public final class PouchMendingQa implements ModInitializer {
 private final JsonArray cases=new JsonArray();private int checks;private boolean sso;private boolean netheriteStandalone;
 private void check(boolean ok,String message){checks++;if(!ok&&!Boolean.getBoolean("pouch.qa.observe"))throw new AssertionError(message);}
 private JsonObject row(String name){var row=new JsonObject();row.addProperty("case",name);cases.add(row);return row;}
 private void policy(boolean regular,boolean auto){if(sso){var c=SSO.CONFIG.mendingRework;c.enabled.accept(true);c.enableRegularMendingBehavior.accept(regular);c.autoRepairOnBreak.accept(auto);c.repairOnShiftUse.accept(true);}}
 private ItemStack wing(ServerPlayer p,int damage,boolean mending,int unbreaking){var item=new ItemStack(Items.ELYTRA);item.setDamageValue(damage);var r=p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);if(mending)item.enchant(r.getOrThrow(Enchantments.MENDING),1);if(unbreaking>0)item.enchant(r.getOrThrow(Enchantments.UNBREAKING),unbreaking);return item;}
 private ItemStack holder(ServerPlayer p,boolean attached,List<ItemStack> items,int armorDamage,boolean armorMending){
  var pouch=new ItemStack(attached||netheriteStandalone?me.pajic.toolpouch.item.ModItems.NETHERITE_TOOL_POUCH:me.pajic.toolpouch.item.ModItems.TOOL_POUCH);pouch.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(items));
  if(!attached)return pouch;
  var armor=new ItemStack(Items.NETHERITE_LEGGINGS);armor.setDamageValue(armorDamage);if(armorMending)armor.enchant(p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING),1);
  var recipe=new me.pajic.toolpouch.recipe.AttachToolPouchRecipe();var input=CraftingInput.of(2,1,List.of(armor,pouch));check(recipe.matches(input,p.level()),"Actual netherite attachment recipe must match");var result=recipe.assemble(input);
  check(result.getOrDefault(me.pajic.toolpouch.component.ModDataComponents.IS_NETHERITE_POUCH,false),"Attached netherite tier retained");return result;
 }
 private void seed(ServerPlayer p,ItemStack holder,boolean attached){p.containerMenu=p.inventoryMenu;p.getInventory().clearContent();p.inventoryMenu.setCarried(ItemStack.EMPTY);p.setGameMode(GameType.SURVIVAL);p.totalExperience=0;p.experienceLevel=0;p.experienceProgress=0;p.takeXpDelay=0;
  com.thenathe.toolpouchcompat.Compat.set(p,true);if(attached)p.setItemSlot(EquipmentSlot.LEGS,holder);else p.getInventory().setItem(12,holder);}
 private int damage(ItemStack holder,int index){return holder.get(DataComponents.CONTAINER).itemCopies().toList().get(index).getDamageValue();}
 private void orb(ServerPlayer p,int xp){p.takeXpDelay=0;var orb=new ExperienceOrb(p.level(),p.getX(),p.getY(),p.getZ(),xp);p.level().addFreshEntity(orb);orb.playerTouch(p);check(orb.isRemoved(),"Actual XP orb was collected");}
 private void xp(ServerPlayer p,String name,boolean attached,int armorDamage,boolean armorMending,int wingDamage,boolean wingMending,boolean regular,boolean toggle,int expectedArmor,int expectedWing,int expectedXp){
  policy(regular,true);var clock=new ItemStack(Items.CLOCK);var h=holder(p,attached,List.of(wing(p,wingDamage,wingMending,0),clock),armorDamage,armorMending);seed(p,h,attached);com.thenathe.toolpouchcompat.Compat.set(p,toggle);
  var r=row(name);r.addProperty("actual_attachment_recipe",attached);r.addProperty("active_owner_same_reference",com.thenathe.toolpouchcompat.mixin.PouchMendingAccess.toolpouchCompat$activePouch(p)==h);orb(p,7);
  r.addProperty("armor_damage",h.getDamageValue());r.addProperty("elytra_damage",damage(h,0));r.addProperty("player_xp",p.totalExperience);r.addProperty("expected_armor_damage",expectedArmor);r.addProperty("expected_elytra_damage",expectedWing);r.addProperty("expected_player_xp",expectedXp);
  check(h.getDamageValue()==expectedArmor&&damage(h,0)==expectedWing&&p.totalExperience==expectedXp,"XP Mending mismatch "+r);check(ItemStack.matches(clock,h.get(DataComponents.CONTAINER).itemCopies().toList().get(1)),"Unrelated pouch contents preserved");
 }
 private void nativeCapability(ServerPlayer p,boolean nativeClient)throws Exception{
  if(!FabricLoader.getInstance().isModLoaded("thenathe_mod_suite"))return;
  var key=com.thenathe.suite.network.SuiteCapabilities.class.getDeclaredField("MODULES_KEY");key.setAccessible(true);p.connection.getPacketContext().set((net.fabricmc.fabric.api.networking.v1.context.PacketContext.Key<Set<String>>)key.get(null),nativeClient?Set.of("toolpouch","simple_smithing_overhaul"):Set.of());
 }
 private void fly(ServerPlayer p)throws Exception{p.setOnGround(false);p.startFallFlying();var ticks=LivingEntity.class.getDeclaredField("fallFlyTicks");ticks.setAccessible(true);ticks.setInt(p,19);var method=LivingEntity.class.getDeclaredMethod("updateFallFlying");method.setAccessible(true);method.invoke(p);}
 private void flightOracle(ServerPlayer p,boolean unbreaking)throws Exception{
  policy(true,false);var h=holder(p,false,List.of(wing(p,40,false,unbreaking?3:0)),0,false);seed(p,h,false);var oracle=h.get(DataComponents.CONTAINER).itemCopies().findFirst().orElseThrow();
  int mismatch=0;for(int i=0;i<128;i++){long seed=Long.rotateLeft(39117L+i*0x9E3779B97F4A7C15L,17);p.getRandom().setSeed(seed);p.level().getRandom().setSeed(seed);oracle.hurtAndBreak(1,p.level(),p,stack->{});p.getRandom().setSeed(seed);p.level().getRandom().setSeed(seed);fly(p);if(damage(h,0)!=oracle.getDamageValue())mismatch++;}
  var r=row(unbreaking?"actual-flight-Unbreaking-III-oracle":"actual-flight-unenchanted-oracle");r.addProperty("wear_intervals",128);r.addProperty("damage_after",damage(h,0));r.addProperty("standard_damage_oracle",oracle.getDamageValue());r.addProperty("interval_mismatches",mismatch);check(mismatch==0,"Flight durability bypasses standard enchantment damage "+r);if(unbreaking)check(damage(h,0)<168,"Unbreaking reduced real flight wear");
 }
 private void liveXp(ServerPlayer p){policy(true,true);var h=holder(p,true,List.of(wing(p,40,true,0),new ItemStack(Items.CLOCK)),0,true);seed(p,h,true);var menu=new me.pajic.toolpouch.menu.ToolPouchMenu(8,p.getInventory(),h);p.containerMenu=menu;orb(p,7);var live=((com.thenathe.toolpouchcompat.mixin.PouchMendingMenuAccess)menu).toolpouchCompat$liveContents();check(live.getItem(0).getDamageValue()==26,"XP repairs live attached menu");menu.removed(p);p.containerMenu=p.inventoryMenu;check(damage(h,0)==26,"XP live repair survives close/attached saveback");var r=row("netherite-attached-open-menu-XP-saveback");r.addProperty("elytra_damage",damage(h,0));r.addProperty("live_then_saved",true);}
 private void clumpedXp(ServerPlayer p,boolean regular)throws Exception{
  if(!FabricLoader.getInstance().isModLoaded("clumps"))return;
  policy(regular,true);netheriteStandalone=true;var h=holder(p,false,List.of(wing(p,80,true,3)),0,false);netheriteStandalone=false;seed(p,h,false);
  var orb=new ExperienceOrb(p.level(),p.getX(),p.getY(),p.getZ(),7);p.level().addFreshEntity(orb);
  var type=Class.forName("com.blamejared.clumps.helper.IClumpedOrb");type.getMethod("clumps$setClumpedMap",Map.class).invoke(orb,Map.of(3,2,7,2));
  orb.playerTouch(p);check(orb.isRemoved(),"Merged Clumps orb collected");var r=row(regular?"Clumps-merged-orb-netherite-pouch":"Clumps-merged-orb-SSO-regular-disabled");r.addProperty("orb_entries",4);r.addProperty("total_orb_xp",20);r.addProperty("elytra_damage",damage(h,0));r.addProperty("player_xp",p.totalExperience);
  check(damage(h,0)==(regular?40:80)&&p.totalExperience==(regular?0:20),"Merged Clumps orb respects leftover XP and SSO policy "+r);
 }
 private void armoredXp(ServerPlayer p)throws Exception{
  if(!FabricLoader.getInstance().isModLoaded("armored-elytra"))return;
  policy(true,true);var armor=new ItemStack(Items.NETHERITE_CHESTPLATE);var wings=wing(p,40,true,3);
  var type=Class.forName("dorkix.armored.elytra.ArmoredElytra");
  var combined=(ItemStack)type.getMethod("createArmoredElytra",ItemStack.class,ItemStack.class,net.minecraft.world.inventory.ContainerLevelAccess.class,String.class).invoke(null,wings,armor,net.minecraft.world.inventory.ContainerLevelAccess.create(p.level(),p.blockPosition()),null);
  check(combined.is(Items.ELYTRA)&&type.getMethod("isArmoredElytra",ItemStack.class).invoke(null,combined).equals(true),"Actual Armored Elytra forging retained Elytra item and armor metadata");
  // Forging chooses the chestplate's durability when it exceeds the wings'. Damage the forged result.
  combined.setDamageValue(40);
  var custom=combined.get(DataComponents.CUSTOM_DATA);var attributes=combined.get(DataComponents.ATTRIBUTE_MODIFIERS);netheriteStandalone=true;var h=holder(p,false,List.of(combined),0,false);netheriteStandalone=false;seed(p,h,false);orb(p,7);
  var repaired=h.get(DataComponents.CONTAINER).itemCopies().findFirst().orElseThrow();var r=row("actual-forged-armored-elytra-netherite-pouch-XP");r.addProperty("elytra_damage",repaired.getDamageValue());r.addProperty("player_xp",p.totalExperience);
  check(repaired.getDamageValue()==26&&p.totalExperience==0,"Armored Elytra repairs from XP");check(Objects.equals(custom,repaired.get(DataComponents.CUSTOM_DATA))&&Objects.equals(attributes,repaired.get(DataComponents.ATTRIBUTE_MODIFIERS)),"Armored Elytra armor/custom metadata preserved");
 }
 private void inventoryMendingXp(ServerPlayer p){
  if(!FabricLoader.getInstance().isModLoaded("inventorymending"))return;
  policy(true,true);var h=holder(p,false,List.of(wing(p,40,true,0)),0,false);seed(p,h,false);
  var sword=new ItemStack(Items.DIAMOND_SWORD);sword.setDamageValue(40);sword.enchant(p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING),1);p.getInventory().setItem(13,sword);orb(p,7);
  var r=row("Inventory-Mending-inventory-item-before-stored-wings");r.addProperty("inventory_sword_damage",sword.getDamageValue());r.addProperty("elytra_damage",damage(h,0));r.addProperty("player_xp",p.totalExperience);
  check(sword.getDamageValue()==26&&damage(h,0)==40&&p.totalExperience==0,"Original Inventory Mending repair runs before stored-wings leftover pass "+r);
 }
 private void autoRepair(ServerPlayer p,String name,boolean attached,boolean enabled,boolean resources,boolean mending,boolean open,boolean toggle){
  policy(true,enabled);if(name.equals("SSO-auto-rework-off"))SSO.CONFIG.mendingRework.enabled.accept(false);var h=holder(p,attached,List.of(wing(p,name.contains("BROKEN-flag")?40:431,mending,0),new ItemStack(Items.CLOCK)),0,false);seed(p,h,attached);com.thenathe.toolpouchcompat.Compat.set(p,toggle);
  if(name.contains("BROKEN-flag")){var contents=h.get(DataComponents.CONTAINER).itemCopies().toList();var flagged=contents.getFirst().copy();flagged.set(me.pajic.simple_smithing_overhaul.util.ModDataComponents.BROKEN,true);h.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(flagged,contents.get(1))));}
  int beforeDamage=damage(h,0);
  if(resources){var whetstone=new ItemStack(me.pajic.simple_smithing_overhaul.items.ModItems.WHETSTONE);var stored=new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);stored.upgrade(p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING),1);whetstone.set(DataComponents.STORED_ENCHANTMENTS,stored.toImmutable());p.getInventory().setItem(1,whetstone);p.getInventory().setItem(2,new ItemStack(Items.PHANTOM_MEMBRANE,3));}
  me.pajic.toolpouch.menu.ToolPouchMenu menu=null;if(open){menu=new me.pajic.toolpouch.menu.ToolPouchMenu(9,p.getInventory(),h);p.containerMenu=menu;}
  p.getInventory().tick();if(menu!=null){menu.removed(p);p.containerMenu=p.inventoryMenu;}
  int after=damage(h,0);int materials=p.getInventory().getItem(2).getCount();boolean expect=enabled&&resources&&mending&&SSO.CONFIG.mendingRework.enabled.get()&&!name.contains("unsupported");var r=row(name);r.addProperty("actual_attachment_recipe",attached);r.addProperty("damage_before",beforeDamage);r.addProperty("damage_after",after);r.addProperty("repair_material_remaining",materials);r.addProperty("automatic_enabled",enabled);r.addProperty("resources",resources);r.addProperty("mending",mending);r.addProperty("flight_toggle",toggle);r.addProperty("open_menu",open);r.addProperty("expected_repaired",expect);
  check(expect?after<beforeDamage&&materials==2:after==beforeDamage&&materials==(resources?3:0),"SSO stored material repair mismatch "+r);
 }
 private void selectedFlight(ServerPlayer p,boolean live)throws Exception{
  policy(true,false);var h=holder(p,true,List.of(wing(p,431,false,0),wing(p,40,false,0),new ItemStack(Items.CLOCK)),0,false);seed(p,h,true);me.pajic.toolpouch.menu.ToolPouchMenu menu=null;if(live){menu=new me.pajic.toolpouch.menu.ToolPouchMenu(10,p.getInventory(),h);p.containerMenu=menu;}
  fly(p);if(menu!=null){menu.removed(p);p.containerMenu=p.inventoryMenu;}var r=row(live?"flight-live-menu-correct-eligible-slot":"flight-broken-first-correct-eligible-slot");r.addProperty("first_exhausted_damage",damage(h,0));r.addProperty("second_eligible_damage",damage(h,1));check(damage(h,0)==431&&damage(h,1)==41,"Flight replaced the wrong same-item glider slot "+r);
 }
 public void onInitialize(){ServerLifecycleEvents.SERVER_STARTED.register(server->{var result=new JsonObject();result.addProperty("passed",false);try{
  var modules=new JsonObject();for(String id:List.of("thenathe_mod_suite","simple_smithing_overhaul","toolpouch","toolpouch_atlas_elytra_compat","mapstitch","sensible_stackables","clumps","inventorymending","betterthanmending","armored-elytra","collective","fzzy_config","defaulted"))FabricLoader.getInstance().getModContainer(id).ifPresent(mod->modules.addProperty(id,mod.getMetadata().getVersion().getFriendlyString()));result.add("loaded_modules",modules);result.addProperty("defaulted_loaded",FabricLoader.getInstance().isModLoaded("defaulted"));
  sso=FabricLoader.getInstance().isModLoaded("simple_smithing_overhaul");result.addProperty("original_sso_present",sso);me.pajic.toolpouch.ToolPouch.CONFIG.allowUseFromInventory.accept(true);me.pajic.toolpouch.ToolPouch.CONFIG.canAttachToLeggings.accept(true);
  var player=new ServerPlayer(server,server.overworld(),new GameProfile(UUID.randomUUID(),"PouchXpQA"),ClientInformation.createDefault());player.connection=new ServerGamePacketListenerImpl(server,new Connection(PacketFlow.SERVERBOUND),player,CommonListenerCookie.createInitial(player.getGameProfile(),false)) {@Override public void send(Packet<?> packet){}};player.setPos(0,100,0);player.level().getChunkAt(player.blockPosition());nativeCapability(player,true);
  xp(player,"inventory-XP-all-SSO-enabled",false,0,false,40,true,true,true,0,26,0);
  netheriteStandalone=true;xp(player,"standalone-netherite-XP-all-SSO-enabled",false,0,false,40,true,true,true,0,26,0);netheriteStandalone=false;
  xp(player,"netherite-attached-repaired-Mending-legs",true,0,true,40,true,true,true,0,26,0);
  xp(player,"netherite-attached-damaged-Mending-legs-first",true,40,true,40,true,true,true,26,40,0);
  xp(player,"netherite-attached-partial-Mending-legs-leftover",true,5,true,40,true,true,true,0,30,0);
  xp(player,"netherite-attached-flight-disabled-XP",true,0,true,40,true,true,false,0,26,0);
  xp(player,"no-Mending-XP-unchanged",false,0,false,40,false,true,true,0,40,7);
  if(sso)xp(player,"SSO-regular-XP-disabled",false,0,false,40,true,false,true,0,40,7);
  liveXp(player);
  clumpedXp(player,true);if(sso)clumpedXp(player,false);
  armoredXp(player);
  inventoryMendingXp(player);
  flightOracle(player,false);flightOracle(player,true);selectedFlight(player,false);selectedFlight(player,true);
  var h=holder(player,false,List.of(wing(player,40,false,0)),0,false);seed(player,h,false);com.thenathe.toolpouchcompat.Compat.set(player,false);fly(player);check(damage(h,0)==40,"Disabled pouch flight must not wear stored Elytra");var toggle=row("flight-toggle-disabled-no-wear");toggle.addProperty("elytra_damage",damage(h,0));
  if(sso){
   autoRepair(player,"SSO-auto-inventory-enabled",false,true,true,true,false,true);
   autoRepair(player,"SSO-auto-netherite-attached-enabled",true,true,true,true,false,true);
   autoRepair(player,"SSO-auto-netherite-attached-live-saveback",true,true,true,true,true,true);
   autoRepair(player,"SSO-auto-flight-disabled-still-repairs",true,true,true,true,false,false);
   autoRepair(player,"SSO-auto-disabled-keeps-resources",false,false,true,true,false,true);
   autoRepair(player,"SSO-auto-no-materials",false,true,false,true,false,true);
   autoRepair(player,"SSO-auto-no-Mending",false,true,true,false,false,true);
   autoRepair(player,"SSO-auto-known-BROKEN-flag",false,true,true,true,false,true);
   autoRepair(player,"SSO-auto-rework-off",false,true,true,true,false,true);
  }
  if(FabricLoader.getInstance().isModLoaded("polymer-core")){nativeCapability(player,false);policy(true,true);h=holder(player,false,List.of(wing(player,40,true,3)),0,false);seed(player,h,false);orb(player,7);check(damage(h,0)==40&&player.totalExperience==7,"Unsupported client guard remains intact");var fallback=row("unsupported-Polymer-client-passive-guard");fallback.addProperty("elytra_damage",damage(h,0));fallback.addProperty("player_xp",player.totalExperience);if(sso)autoRepair(player,"SSO-auto-unsupported-client-guard",false,true,true,true,false,true);nativeCapability(player,true);}
  result.addProperty("passed",true);
 }catch(Throwable error){error.printStackTrace();result.addProperty("error",error.toString());}
 result.addProperty("checks",checks);result.add("cases",cases);try{Files.writeString(Path.of("pouch-mending-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception error){throw new RuntimeException(error);}server.halt(false);});}
}
