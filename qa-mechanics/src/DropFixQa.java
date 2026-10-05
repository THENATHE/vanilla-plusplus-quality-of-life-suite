package qa;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import com.mojang.authlib.GameProfile;
import net.atlas.defaulted.utils.ReferentialDataComponentMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public final class DropFixQa implements ModInitializer {
 int passed,failed; final List<String> results=new ArrayList<>();
 interface Check {void run() throws Exception;}
 void test(String name,Check action) {try {action.run();passed++;results.add("PASS "+name);}catch(Throwable t){failed++;results.add("FAIL "+name+" "+t);t.printStackTrace();}System.out.println("DROP_QA "+results.getLast());}
 static void require(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static String name(ItemStack s){var n=s.get(DataComponents.CUSTOM_NAME);return n==null?null:n.getString();}
 public void onInitialize(){ServerLifecycleEvents.SERVER_STARTED.register(this::run);}
 void run(MinecraftServer server){
  var level=server.overworld();
  test("frame-null-name",()->new ItemStack(Items.ITEM_FRAME).set(DataComponents.CUSTOM_NAME,null));
  test("cushion-null-name",()->new ItemStack(Items.CUSHION.pick(DyeColor.WHITE)).set(DataComponents.CUSTOM_NAME,null));
  test("frame-name-return-values",()->{var s=new ItemStack(Items.ITEM_FRAME);require(s.set(DataComponents.CUSTOM_NAME,Component.literal("A"))==null,"initial old value");require(s.set(DataComponents.CUSTOM_NAME,Component.literal("B")).getString().equals("A"),"replacement old value");require(s.remove(DataComponents.CUSTOM_NAME).getString().equals("B"),"removed old value");require(s.get(DataComponents.CUSTOM_NAME)==null,"removed name");});
  test("copy-isolation-and-codec-persistence",()->{var s=new ItemStack(Items.ELYTRA);s.set(DataComponents.CUSTOM_NAME,Component.literal("Ship wings"));s.setDamageValue(12);var copy=s.copy();copy.set(DataComponents.CUSTOM_NAME,Component.literal("Copy"));require(name(s).equals("Ship wings"),"copy mutated original");var ops=server.registryAccess().createSerializationContext(NbtOps.INSTANCE);var tag=ItemStack.CODEC.encodeStart(ops,s).getOrThrow();var loaded=ItemStack.CODEC.parse(ops,tag).getOrThrow();require(loaded.is(Items.ELYTRA)&&name(loaded).equals("Ship wings")&&loaded.getDamageValue()==12,"serialized stack changed");});
  test("dynamic-prototype-retains-overrides",()->{
   var supplier=new AtomicReference<DataComponentMap>(DataComponentMap.builder().set(DataComponents.CUSTOM_NAME,Component.literal("default-a")).build());
   var ref=new ReferentialDataComponentMap(supplier::get);var map=new PatchedDataComponentMap(ref);ref.setOriginal(map);
   require(map.get(DataComponents.CUSTOM_NAME).getString().equals("default-a"),"first prototype");
   map.set(DataComponents.CUSTOM_NAME,Component.literal("explicit"));
   supplier.set(DataComponentMap.builder().set(DataComponents.CUSTOM_NAME,Component.literal("default-b")).set(DataComponents.MAX_STACK_SIZE,16).build());
   require(map.get(DataComponents.MAX_STACK_SIZE)==16,"updated prototype");require(map.get(DataComponents.CUSTOM_NAME).getString().equals("explicit"),"override lost");
   map.remove(DataComponents.CUSTOM_NAME);supplier.set(DataComponentMap.builder().set(DataComponents.CUSTOM_NAME,Component.literal("default-c")).set(DataComponents.MAX_STACK_SIZE,8).build());
   require(map.get(DataComponents.MAX_STACK_SIZE)==8,"second prototype refresh");require(map.get(DataComponents.CUSTOM_NAME)==null,"removal lost");
  });
  test("null-first-write-after-prototype-refresh",()->{var supplier=new AtomicReference<DataComponentMap>(DataComponentMap.EMPTY);var ref=new ReferentialDataComponentMap(supplier::get);var map=new PatchedDataComponentMap(ref);ref.setOriginal(map);map.get(DataComponents.CUSTOM_NAME);supplier.set(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,16).build());require(map.set(DataComponents.CUSTOM_NAME,null)==null,"null old value");require(map.get(DataComponents.MAX_STACK_SIZE)==16,"refreshed default");});
  test("repeated-null-setters",()->{var stack=new ItemStack(Items.ITEM_FRAME);for(int i=0;i<4;i++)require(stack.set(DataComponents.CUSTOM_NAME,null)==null,"repeat null old value");require(stack.get(DataComponents.CUSTOM_NAME)==null,"repeat null changed value");});
  test("removed-name-survives-absent-present-absent-prototype",()->{
   var supplier=new AtomicReference<DataComponentMap>(DataComponentMap.EMPTY);var ref=new ReferentialDataComponentMap(supplier::get);var map=new PatchedDataComponentMap(ref);ref.setOriginal(map);
   map.set(DataComponents.CUSTOM_NAME,null);
   supplier.set(DataComponentMap.builder().set(DataComponents.CUSTOM_NAME,Component.literal("new default")).set(DataComponents.MAX_STACK_SIZE,16).build());
   require(map.get(DataComponents.MAX_STACK_SIZE)==16,"present refresh");require(map.get(DataComponents.CUSTOM_NAME)==null,"explicit removed name reappeared");
   supplier.set(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,8).build());require(map.get(DataComponents.MAX_STACK_SIZE)==8,"absent refresh");
   require(map.set(DataComponents.CUSTOM_NAME,null)==null,"null after removal+reload");require(map.get(DataComponents.CUSTOM_NAME)==null,"name returned after reload");
  });
  test("snapshot-and-copy-ownership",()->{var s=new ItemStack(Items.ELYTRA);s.set(DataComponents.CUSTOM_NAME,Component.literal("original"));s.setDamageValue(11);var patch=s.getComponentsPatch();var copy=s.copy();s.set(DataComponents.CUSTOM_NAME,Component.literal("changed"));s.setDamageValue(22);require(name(copy).equals("original")&&copy.getDamageValue()==11,"copy shares mutations");var replay=new ItemStack(Items.ELYTRA);replay.applyComponents(patch);require(name(replay).equals("original")&&replay.getDamageValue()==11,"snapshot shares mutations");});
  var player=new ServerPlayer(server,level,new GameProfile(UUID.randomUUID(),"DropQa"),ClientInformation.createDefault());
  for(boolean named:new boolean[]{false,true}) {
   test("cushion-player-attack-"+named,()->cushion(level,player,named,false));
   test("cushion-support-loss-"+named,()->cushion(level,player,named,true));
   test("frame-player-attack-elytra-"+named,()->frame(level,player,named,false));
   test("frame-support-loss-elytra-"+named,()->frame(level,player,named,true));
   test("frame-shulker-bullet-elytra-"+named,()->projectileFrame(level,named));
  }
  results.add("SUMMARY passed="+passed+" failed="+failed);
  try {Files.write(Path.of("result.txt"),results);}catch(Exception e){e.printStackTrace();}
  System.out.println(results.getLast());
 }
 static final class TestBullet extends net.minecraft.world.entity.projectile.ShulkerBullet {
  TestBullet(ServerLevel l){super(EntityTypes.SHULKER_BULLET,l);}
  void impact(Entity e){super.onHitEntity(new net.minecraft.world.phys.EntityHitResult(e));}
 }
 void projectileFrame(ServerLevel l,boolean named){
  var pos=new BlockPos(8,81,0);var support=pos.north();var box=new AABB(5,77,-3,12,85,4);clean(l,box);l.setBlock(support,Blocks.STONE.defaultBlockState(),3);
  var f=new ItemFrame(l,pos,Direction.SOUTH);if(named)f.setCustomName(Component.literal("Frame"));var wings=new ItemStack(Items.ELYTRA);wings.set(DataComponents.CUSTOM_NAME,Component.literal("Ship wings"));wings.setDamageValue(12);f.setItem(wings);require(l.addFreshEntity(f),"add projectile frame");
  try {new TestBullet(l).impact(f);require(f.getItem().isEmpty(),"projectile did not release elytra");new TestBullet(l).impact(f);require(f.isRemoved(),"projectile did not break empty frame");var drops=l.getEntitiesOfClass(ItemEntity.class,box);require(drops.size()==2,"projectile drop count "+drops.size());var frame=drops.stream().map(ItemEntity::getItem).filter(v->v.is(Items.ITEM_FRAME)).findFirst().orElseThrow();var elytra=drops.stream().map(ItemEntity::getItem).filter(v->v.is(Items.ELYTRA)).findFirst().orElseThrow();require(Objects.equals(name(frame),named?"Frame":null),"projectile frame name");require(name(elytra).equals("Ship wings")&&elytra.getDamageValue()==12,"projectile elytra data");}
  finally{clean(l,box);l.removeBlock(support,false);}
 }
 void clean(ServerLevel l,AABB box){for(var e:l.getEntitiesOfClass(ItemEntity.class,box))e.discard();for(var e:l.getEntitiesOfClass(BlockAttachedEntity.class,box))e.discard();}
 void cushion(ServerLevel l,ServerPlayer player,boolean named,boolean supportLoss){
  var pos=new BlockPos(0,80,0);var box=new AABB(-3,77,-3,4,85,4);clean(l,box);l.getChunk(0,0);l.setBlock(pos,Blocks.STONE.defaultBlockState(),3);
  var c=new Cushion(EntityTypes.CUSHION,l);c.setColor(DyeColor.BLUE);c.setPos(.5,81,.5);if(named)c.setCustomName(Component.literal("Seat"));require(c.survives(),"cushion setup lacks support");require(l.addFreshEntity(c),"add cushion");
  try {
   if(supportLoss){l.removeBlock(pos,false);require(!c.survives(),"support removal");for(int i=0;i<102&&!c.isRemoved();i++)c.tick();}
   else require(c.hurtServer(l,l.damageSources().playerAttack(player),1),"attack rejected");
   require(c.isRemoved(),"cushion remains");var drops=l.getEntitiesOfClass(ItemEntity.class,box);require(drops.size()==1,"cushion drop count "+drops.size());var s=drops.getFirst().getItem();require(s.is(Items.CUSHION.pick(DyeColor.BLUE)),"cushion color changed");require(Objects.equals(name(s),named?"Seat":null),"cushion name changed");
  }finally{clean(l,box);l.removeBlock(pos,false);}
 }
 void frame(ServerLevel l,ServerPlayer player,boolean named,boolean supportLoss){
  var pos=new BlockPos(8,81,0);var support=pos.north();var box=new AABB(5,77,-3,12,85,4);clean(l,box);l.setBlock(support,Blocks.STONE.defaultBlockState(),3);
  var f=new ItemFrame(l,pos,Direction.SOUTH);if(named)f.setCustomName(Component.literal("Frame"));var wings=new ItemStack(Items.ELYTRA);wings.set(DataComponents.CUSTOM_NAME,Component.literal("Ship wings"));wings.setDamageValue(12);f.setItem(wings);require(f.survives(),"frame setup lacks support");require(l.addFreshEntity(f),"add frame");
  try{
   if(supportLoss){l.removeBlock(support,false);for(int i=0;i<102&&!f.isRemoved();i++)f.tick();}
   else {require(f.hurtServer(l,l.damageSources().playerAttack(player),1),"first frame attack");require(f.getItem().isEmpty(),"elytra not removed");require(f.hurtServer(l,l.damageSources().playerAttack(player),1),"second frame attack");}
   require(f.isRemoved(),"frame remains");var drops=l.getEntitiesOfClass(ItemEntity.class,box);require(drops.size()==2,"frame drop count "+drops.size());
   var frame=drops.stream().map(ItemEntity::getItem).filter(s->s.is(Items.ITEM_FRAME)).findFirst().orElseThrow();var elytra=drops.stream().map(ItemEntity::getItem).filter(s->s.is(Items.ELYTRA)).findFirst().orElseThrow();require(Objects.equals(name(frame),named?"Frame":null),"frame name changed");require(name(elytra).equals("Ship wings")&&elytra.getDamageValue()==12,"elytra components lost");
  }finally{clean(l,box);l.removeBlock(support,false);}
 }
}
