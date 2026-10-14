package qa;

import com.mojang.authlib.GameProfile;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.*;
import me.pajic.misctweaks.MiscTweaks;
import me.pajic.misctweaks.ai.SearchForFoodGoal;
import me.pajic.simple_death_improvements.SDI;
import me.pajic.simple_death_improvements.access.PlayerAccess;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.phys.*;

/** Real packaged methods; isolated worlds and fixture players only. */
public class MiscSdiQa implements ModInitializer {
 private int checks;private final List<String> observations=new ArrayList<>();
 private void check(boolean pass,String reason){checks++;if(!pass)throw new AssertionError(reason);}
 private void close(double observed,double expected,String label){check(Math.abs(observed-expected)<0.00001,label+": "+observed+" expected "+expected);}
 static class Probe extends ServerPlayer {
  Probe(MinecraftServer server){super(server,server.overworld(),new GameProfile(UUID.randomUUID(),"MiscSdiQA"),ClientInformation.createDefault());
   connection=new ServerGamePacketListenerImpl(server,new net.minecraft.network.Connection(PacketFlow.SERVERBOUND),this,CommonListenerCookie.createInitial(getGameProfile(),false)){
    @Override public void send(Packet<?> packet){} };
  }
  int reward(){return super.getBaseExperienceReward(level());}
  void spawnXp(){super.dropExperience(level(),this);}
  void dropEquipmentNow(){super.dropEquipment(level());}
 }
 private List<ItemEntity> items(ServerLevel level,BlockPos pos,double radius){return level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(radius));}
 private void clearItems(ServerLevel level,BlockPos pos,double radius){items(level,pos,radius).forEach(Entity::discard);}
 private ItemEntity blockDrop(ServerLevel level,BlockPos pos) {
  clearItems(level,pos,8);Block.popResource(level,pos,new ItemStack(Items.STONE));var found=items(level,pos,8);
  check(found.size()==1,"popResource produces one actual dropped stack; found="+found.size());return found.getFirst();
 }
 private void misc(MinecraftServer server)throws Exception {
  var level=server.overworld();var managerField=ServerLevel.class.getDeclaredField("entityManager");managerField.setAccessible(true);var manager=(net.minecraft.world.level.entity.PersistentEntitySectionManager<Entity>)managerField.get(level);for(int x=-1;x<5;x++)for(int z=-1;z<5;z++){level.getChunk(x,z);manager.updateChunkStatus(new net.minecraft.world.level.ChunkPos(x,z),net.minecraft.world.level.entity.Visibility.TRACKED);}var conf=MiscTweaks.CONFIG;var stable=conf.stableBlockDrops;var pos=new BlockPos(20,80,20);level.getChunkAt(pos);
  stable.flingTowardsPlayer.accept(false);stable.alwaysSpawnDropInBlockCenter.accept(true);stable.noRandomHorizontalMovement.accept(true);
  var centered=blockDrop(level,pos);close(centered.getX(),20.5,"centered drop X");close(centered.getZ(),20.5,"centered drop Z");
  close(centered.getDeltaMovement().x,0,"stable horizontal X");close(centered.getDeltaMovement().z,0,"stable horizontal Z");
  stable.alwaysSpawnDropInBlockCenter.accept(false);stable.noRandomHorizontalMovement.accept(false);
  boolean displaced=false,moving=false;
  for(int i=0;i<12;i++){var drop=blockDrop(level,pos);displaced|=Math.abs(drop.getX()-20.5)>0.00001||Math.abs(drop.getZ()-20.5)>0.00001;
   moving|=Math.abs(drop.getDeltaMovement().x)>0.00001||Math.abs(drop.getDeltaMovement().z)>0.00001;}
  check(displaced,"disabling center option restores vanilla random drop position");check(moving,"disabling stable velocity restores vanilla horizontal fling");
  var player=new Probe(server);player.setPos(23.5,80.5,20.5);level.addNewPlayer(player);
  stable.alwaysSpawnDropInBlockCenter.accept(true);stable.flingTowardsPlayer.accept(true);stable.requireCrouchForFling.accept(true);stable.noRandomHorizontalMovement.accept(true);
  player.setShiftKeyDown(false);var standing=blockDrop(level,pos);close(standing.getDeltaMovement().x,0,"crouch-required fling does not affect standing player");
  player.setShiftKeyDown(true);var crouching=blockDrop(level,pos);close(crouching.getDeltaMovement().x,0.3,"crouching player receives targeted drop fling");close(crouching.getDeltaMovement().z,0,"targeted fling follows player Z");
  stable.requireCrouchForFling.accept(false);player.setShiftKeyDown(false);var uncrouched=blockDrop(level,pos);close(uncrouched.getDeltaMovement().x,0.3,"disabled crouch requirement flings to standing player");
  player.setPos(40,80,20);var distant=blockDrop(level,pos);close(distant.getDeltaMovement().x,0,"outside fling range uses stable fallback");level.removePlayerImmediately(player,Entity.RemovalReason.DISCARDED);
  stable.flingTowardsPlayer.accept(false);
  var pick=new ItemStack(Items.DIAMOND_PICKAXE);var tool=pick.get(DataComponents.TOOL);check(tool!=null,"registered pickaxe has actual Tool component");
  conf.obsidianBlocks.accept(Set.of("minecraft:obsidian","minecraft:crying_obsidian","minecraft:respawn_anchor"));conf.fasterObsidianMining.accept(false);
  var states=List.of(Blocks.OBSIDIAN.defaultBlockState(),Blocks.CRYING_OBSIDIAN.defaultBlockState(),Blocks.RESPAWN_ANCHOR.defaultBlockState());
  var baseline=new ArrayList<Float>();for(var state:states)baseline.add(tool.getMiningSpeed(state));var stone=tool.getMiningSpeed(Blocks.STONE.defaultBlockState());
  conf.obsidianMiningSpeedMultiplier.accept(1.6F);conf.fasterObsidianMining.accept(true);
  for(int i=0;i<states.size();i++)close(tool.getMiningSpeed(states.get(i)),baseline.get(i)*1.6,"configured obsidian/anchor mining boost "+i);
  close(tool.getMiningSpeed(Blocks.STONE.defaultBlockState()),stone,"ordinary stone mining remains unchanged");conf.fasterObsidianMining.accept(false);
  for(int i=0;i<states.size();i++)close(tool.getMiningSpeed(states.get(i)),baseline.get(i),"disabled obsidian boost restores baseline "+i);
  var fire=(FireBlock)Blocks.FIRE;var burn=FireBlock.class.getDeclaredMethod("getBurnOdds",BlockState.class);burn.setAccessible(true);
  var ignite=FireBlock.class.getDeclaredMethod("getIgniteOdds",BlockState.class);ignite.setAccessible(true);
  check((int)burn.invoke(fire,Blocks.COBWEB.defaultBlockState())==60,"loaded custom cobweb burn odds");check((int)ignite.invoke(fire,Blocks.COBWEB.defaultBlockState())==600,"loaded custom cobweb ignition odds");
  check((int)burn.invoke(fire,Blocks.STONE.defaultBlockState())==0,"ordinary stone remains nonflammable");
  var selector=Mob.class.getDeclaredField("goalSelector");selector.setAccessible(true);
  conf.animalsSearchForFood.accept(false);var disabled=new Cow((EntityType<? extends Cow>)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.parse("minecraft:cow")),level);
  check(((GoalSelector)selector.get(disabled)).getAvailableGoals().stream().noneMatch(g->g.getGoal() instanceof SearchForFoodGoal),"animal search disabled omits extra goal");
  conf.animalsSearchForFood.accept(true);var cow=new Cow((EntityType<? extends Cow>)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.parse("minecraft:cow")),level);cow.setPos(24.5,80,24.5);level.setBlock(cow.blockPosition().below(),Blocks.STONE.defaultBlockState(),3);level.addFreshEntity(cow);
  var search=((GoalSelector)selector.get(cow)).getAvailableGoals().stream().filter(g->g.getGoal() instanceof SearchForFoodGoal).findFirst();check(search.isPresent(),"eligible adult cow receives food-search goal");
  check(cow.canHoldItem(new ItemStack(Items.WHEAT)),"adult cow can hold breeding food");check(!cow.canHoldItem(new ItemStack(Items.STONE)),"adult cow rejects nonfood");
  cow.setAge(-200);check(!cow.canHoldItem(new ItemStack(Items.WHEAT)),"baby cow cannot pick up breeding food");cow.setAge(0);
  var food=new ItemEntity(level,24.5,80,24.5,new ItemStack(Items.WHEAT,2));food.setNoPickUpDelay();level.addFreshEntity(food);
  var goal=search.get().getGoal();check(goal.canUse(),"food search finds real dropped wheat");goal.start();check(cow.canPickUpLoot(),"search temporarily enables food pickup");cow.aiStep();
  check(cow.isInLove(),"actual animal AI pickup enters love mode");check(food.getItem().getCount()==1,"animal consumes one wheat only");goal.stop();check(!cow.canPickUpLoot(),"stopping search restores original pickup permission");cow.discard();food.discard();
  observations.add("MiscTweaks: block positions/velocities, targeted fling/crouch/range controls, obsidian+anchor mining controls, configured cobweb fire odds, adult/baby cow goal and actual food consumption");
 }
 private int expectedXp(Probe p){int total=Map.of(0,0,1,7,10,160,30,1395,50,5345).get(p.experienceLevel);return total+(int)(p.experienceProgress*p.getXpNeededForNextLevel());}
 private void sdi(MinecraftServer server)throws Exception {
  var conf=SDI.CONFIG;var level=server.overworld();level.getGameRules().set(GameRules.KEEP_INVENTORY,false,server);var player=new Probe(server);player.setPos(8.5,81,8.5);level.setBlock(new BlockPos(8,80,8),Blocks.STONE.defaultBlockState(),3);player.setOnGround(true);
  var delay=Player.class.getDeclaredField("sdi$delayBeforeTracking");delay.setAccessible(true);delay.setInt(player,0);player.doTick();
  var safe=((PlayerAccess)player).sdi$getLastSafeBlockPosition();check(safe.equals(new BlockPos(8,80,8)),"actual player tick records safe block; safe="+safe+" on="+player.getOnPos());
  var save=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,server.registryAccess());player.saveWithoutId(save);var restored=new Probe(server);restored.load(TagValueInput.create(ProblemReporter.DISCARDING,server.registryAccess(),save.buildResult()));check(((PlayerAccess)restored).sdi$getLastSafeBlockPosition().equals(safe),"safe block survives player save/load");
  for(int xpLevel:new int[]{0,1,10,30,50}){player.experienceLevel=xpLevel;player.experienceProgress=0.4F;
   for(int percent:new int[]{1,80,100}){conf.playerDropMoreXpOnDeath.accept(true);conf.droppedExperiencePercent.accept(percent);int reward=player.reward();check(reward==(int)(expectedXp(player)*(float)percent/100),"configured death XP percentage "+xpLevel+"/"+percent);check(player.experienceLevel==xpLevel,"reward computation restores player's level");}
   conf.playerDropMoreXpOnDeath.accept(false);check(player.reward()==Math.min(100,xpLevel*7),"disabled expanded XP restores vanilla cap "+xpLevel);
  }
  conf.playerDropMoreXpOnDeath.accept(true);conf.droppedExperiencePercent.accept(80);player.experienceLevel=30;level.getGameRules().set(GameRules.KEEP_INVENTORY,true,server);check(player.reward()==0,"keepInventory emits no death XP");level.getGameRules().set(GameRules.KEEP_INVENTORY,false,server);
  player.setHealth(0);conf.noItemSplatterOnDeath.accept(true);conf.noDeathItemDespawn.accept(true);
  var eternal=player.createItemStackToDrop(new ItemStack(Items.DIAMOND,2),true,false);check(eternal!=null,"dead player creates actual item entity");close(eternal.getDeltaMovement().lengthSqr(),0,"death item splatter suppression");check(eternal.getAge()<0,"death item unlimited lifetime is applied");
  var age=ItemEntity.class.getDeclaredField("age");age.setAccessible(true);for(int i=0;i<25;i++)eternal.tick();check(!eternal.isRemoved(),"unlimited death item survives actual ticks");
  conf.noDeathItemDespawn.accept(false);conf.itemDespawnTimer.accept(1);var timed=player.createItemStackToDrop(new ItemStack(Items.DIAMOND),true,false);age.setInt(timed,19);timed.tick();check(timed.isRemoved(),"configured death lifetime actually expires at 20 ticks");
  var ordinary=new ItemEntity(level,8.5,81,8.5,new ItemStack(Items.STONE));age.setInt(ordinary,19);ordinary.tick();check(!ordinary.isRemoved(),"ordinary items retain vanilla lifetime");
  conf.noItemSplatterOnDeath.accept(false);var scattered=player.createItemStackToDrop(new ItemStack(Items.DIAMOND),true,false);check(scattered.getDeltaMovement().lengthSqr()>0,"disabled death splatter suppression restores velocity");conf.noItemSplatterOnDeath.accept(true);
  var explosion=new net.minecraft.world.level.ServerExplosion(level,null,null,null,new Vec3(8.5,81,8.5),2,false,net.minecraft.world.level.Explosion.BlockInteraction.DESTROY);conf.explosionResistantItems.accept(true);check(ordinary.ignoreExplosion(explosion),"explosion resistance enabled for item entities");conf.explosionResistantItems.accept(false);check(!ordinary.ignoreExplosion(explosion),"explosion resistance disabled restores item behavior");
  conf.tryItemVoidSaveOnDeath.accept(true);player.setPos(100,-200,100);var voidSaved=player.createItemStackToDrop(new ItemStack(Items.DIAMOND),true,false);check(voidSaved.position().equals(new Vec3(8.5,81,8.5)),"void death drop returns to safe block");conf.tryItemVoidSaveOnDeath.accept(false);var voidUnsafe=player.createItemStackToDrop(new ItemStack(Items.DIAMOND),true,false);check(voidUnsafe.getY()<-100,"disabled void rescue leaves original drop position");
  level.setBlock(new BlockPos(40,70,40),Blocks.LAVA.defaultBlockState(),3);player.setPos(40.5,90,40.5);player.setOnGround(false);player.mainSupportingBlockPos=Optional.empty();conf.tryItemLavaSaveOnDeath.accept(true);var lavaSaved=player.createItemStackToDrop(new ItemStack(Items.DIAMOND),true,false);check(lavaSaved.position().equals(new Vec3(8.5,81,8.5)),"airborne death over lava returns items to safe block; got="+lavaSaved.position()+" on="+player.getOnPos());conf.tryItemLavaSaveOnDeath.accept(false);var lavaUnsafe=player.createItemStackToDrop(new ItemStack(Items.DIAMOND),true,false);check(lavaUnsafe.getX()==40.5,"disabled lava rescue leaves original drop position");
  player.setPos(8.5,81,8.5);conf.noXpSplatterOnDeath.accept(true);player.experienceProgress=0.4F;int reward=player.reward();var area=new AABB(new BlockPos(8,80,8)).inflate(16);level.getEntitiesOfClass(ExperienceOrb.class,area).forEach(Entity::discard);player.spawnXp();var xp=level.getEntitiesOfClass(ExperienceOrb.class,area);check(xp.size()==1,"death XP suppression emits one actual orb");check(xp.getFirst().getValue()==reward,"single death orb retains full configured reward");check(xp.getFirst().position().equals(new Vec3(8.5,81,8.5)),"XP orb spawns at saved safe block");xp.forEach(Entity::discard);
  conf.noXpSplatterOnDeath.accept(false);player.spawnXp();xp=level.getEntitiesOfClass(ExperienceOrb.class,area);check(!xp.isEmpty(),"disabled XP suppression emits vanilla orbs");check(orbTotal(xp)==reward,"vanilla orb split conserves death reward");xp.forEach(Entity::discard);
  keepCases(server);
  observations.add("SDI: saved safe position, XP percentages and vanilla/keepInventory controls, actual orb spawning, item lifetime/splatter/explosion toggles, airborne lava and void rescue, armor/hotbar/offhand death retention and exclusion lists");
 }
 private int orbTotal(List<ExperienceOrb> orbs)throws Exception {var count=ExperienceOrb.class.getDeclaredField("count");count.setAccessible(true);int total=0;for(var orb:orbs)total+=orb.getValue()*count.getInt(orb);return total;}
 private void keepCases(MinecraftServer server){
  var conf=SDI.CONFIG;me.pajic.mapstitch.MapStitch.CONFIG.keepAtlasOnDeath.accept(false);
  conf.armorDropList.accept(List.of(net.minecraft.resources.Identifier.parse("minecraft:diamond_chestplate")));conf.hotbarDropList.accept(List.of(net.minecraft.resources.Identifier.parse("minecraft:diamond_pickaxe")));conf.offhandDropList.accept(List.of(net.minecraft.resources.Identifier.parse("minecraft:diamond_pickaxe")));
  for(int mask=0;mask<8;mask++) {
   conf.keepArmorOnDeath.accept((mask&1)!=0);conf.keepHotbarOnDeath.accept((mask&2)!=0);conf.keepOffhandOnDeath.accept((mask&4)!=0);
   var old=new Probe(server);old.setHealth(0);old.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));old.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));old.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));old.getInventory().setItem(0,new ItemStack(Items.STONE,2));old.getInventory().setItem(1,new ItemStack(Items.DIAMOND_PICKAXE));old.getInventory().setItem(15,new ItemStack(Items.DIRT,3));old.dropEquipmentNow();var fresh=new Probe(server);fresh.restoreFrom(old,false);
   check(fresh.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET)==((mask&1)!=0),"armor keep toggle "+mask);check(fresh.getItemBySlot(EquipmentSlot.CHEST).isEmpty(),"armor exclusion always drops "+mask);
   check(fresh.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.SHIELD)==((mask&4)!=0),"offhand keep toggle "+mask);check(fresh.getInventory().getItem(0).is(Items.STONE)==((mask&2)!=0),"hotbar keep toggle "+mask);
   check(fresh.getInventory().getItem(1).isEmpty(),"hotbar exclusion always drops "+mask);check(fresh.getInventory().getItem(15).isEmpty(),"main inventory still drops "+mask);
  }
  conf.keepOffhandOnDeath.accept(true);var old=new Probe(server);old.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.DIAMOND_PICKAXE));old.dropEquipmentNow();var fresh=new Probe(server);fresh.restoreFrom(old,false);check(fresh.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty(),"offhand exclusion always drops");
 }
 public void onInitialize(){ServerLifecycleEvents.SERVER_STARTED.register(server->{String result;try{misc(server);sdi(server);result="PASS "+checks+" assertions: "+String.join("; ",observations);}catch(Throwable error){error.printStackTrace();result="FAIL "+error;}
  try{Files.writeString(Path.of("misc-sdi-result.txt"),result+"\n");}catch(Exception error){throw new RuntimeException(error);}finally{server.halt(false);}});}
}
