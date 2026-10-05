package pouchmendingqa;

import com.thenathe.toolpouchcompat.ElytraPreference;
import java.nio.file.*;
import java.util.*;
import me.pajic.toolpouch.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/** Natural XP-entity collision with a connected native player; no direct repair call. */
public class MendingServerQa implements ModInitializer {
    final Path control=Path.of(System.getProperty("mending.qa.control"));
    final String[] cases={"inventory","leggings","toggle-off","no-mending","full","loose-elytra","remainder","broken","competition","multiple","open-menu","leggings-restart"};
    int scenario,phase,ticks,orbs,checks;
    ExperienceOrb orb;
    boolean done;
    Map<String,Integer> beforeState;
    ItemStack item(ServerPlayer p,Item type,String name,int damage,boolean mend){
        var stack=new ItemStack(type);stack.set(DataComponents.CUSTOM_NAME,Component.literal(name));stack.setDamageValue(damage);
        if(mend)stack.enchant(p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING),1);
        return stack;
    }
    void record(String text)throws Exception{Files.writeString(control.resolve("observations.txt"),text+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}
    void seed(ServerPlayer p)throws Exception{
        p.closeContainer();p.getInventory().clearContent();p.inventoryMenu.setCarried(ItemStack.EMPTY);p.setGameMode(GameType.SURVIVAL);p.setPermanentlyInvulnerable(true);
        p.totalExperience=0;p.experienceLevel=0;p.experienceProgress=0;p.takeXpDelay=0;p.setExperiencePoints(0);
        for(var old:p.level().getEntitiesOfClass(ExperienceOrb.class,p.getBoundingBox().inflate(64)))old.discard();
        String name=cases[scenario];boolean leggings=name.startsWith("leggings");int damage=name.equals("full")?0:name.equals("remainder")?5:name.equals("broken")?431:40;
        var elytra=item(p,Items.ELYTRA,"QA-elytra-A",damage,!name.equals("no-mending"));
        var holder=item(p,leggings?Items.IRON_LEGGINGS:ModItems.TOOL_POUCH,"QA-holder",0,false);
        var contents=new ArrayList<ItemStack>();contents.add(elytra);contents.add(item(p,Items.CLOCK,"QA-clock",0,false));
        if(name.equals("multiple"))contents.add(item(p,Items.ELYTRA,"QA-elytra-B",40,true));
        holder.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(contents));
        if(name.equals("loose-elytra"))p.getInventory().setItem(12,elytra);
        else if(leggings)p.setItemSlot(EquipmentSlot.LEGS,holder);else p.getInventory().setItem(12,holder);
        if(name.equals("competition"))p.setItemSlot(EquipmentSlot.CHEST,item(p,Items.DIAMOND_CHESTPLATE,"QA-worn",40,true));
        ((ElytraPreference)p).toolpouchCompat$setElytraEnabled(!name.equals("toggle-off"));
        p.getInventory().setSelectedSlot(0);p.inventoryMenu.broadcastFullState();orbs=0;
        record(name+" BEFORE "+snapshot(p));
        beforeState=state(p);
    }
    static void collect(ItemStack stack,Map<String,Integer> result,int depth){
        if(stack.isEmpty())return;if(depth>8)throw new AssertionError("unexpected container depth");
        var name=stack.get(DataComponents.CUSTOM_NAME);if(name!=null&&name.getString().startsWith("QA-"))result.put(name.getString(),stack.getDamageValue());
        stack.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).itemCopies().forEach(child->collect(child,result,depth+1));
    }
    static Map<String,Integer> state(ServerPlayer p){Map<String,Integer> state=new TreeMap<>();for(int i=0;i<p.getInventory().getContainerSize();i++)collect(p.getInventory().getItem(i),state,0);return state;}
    static String snapshot(ServerPlayer p){return state(p)+"|xp="+p.totalExperience;}
    void validate(ServerPlayer p)throws Exception{
        var expected=new TreeMap<>(beforeState);String name=cases[scenario];int xp=orbs*7;
        boolean vanilla=Boolean.getBoolean("mending.qa.vanillaRepair");boolean pouch=Boolean.getBoolean("mending.qa.expectPouchRepair");
        if(name.equals("competition")&&vanilla){expected.put("QA-worn",0);xp-=20;}
        if(pouch&&!List.of("no-mending","full","loose-elytra").contains(name)){
            if(name.equals("multiple")){expected.put("QA-elytra-A",0);expected.put("QA-elytra-B",0);xp-=40;}
            else if(name.equals("competition")){expected.put("QA-elytra-A",0);xp-=20;}
            else if(name.equals("remainder")){expected.put("QA-elytra-A",0);xp-=2;}
            else{expected.put("QA-elytra-A",expected.get("QA-elytra-A")-14);xp-=7;}
        }
        if(!expected.equals(state(p))||xp!=p.totalExperience)throw new AssertionError(name+" expected="+expected+"|xp="+xp+" actual="+snapshot(p));
        record(name+" EXPECTED_BEHAVIOR_PASS");checks++;
    }
    void configure()throws Exception{
        if(!Boolean.getBoolean("mending.qa.regular"))return;
        Object config=Class.forName("me.pajic.simple_smithing_overhaul.SSO").getField("CONFIG").get(null);
        Object mending=config.getClass().getField("mendingRework").get(config);
        Object regular=mending.getClass().getField("enableRegularMendingBehavior").get(mending);
        ((me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean)regular).accept(true);
        record("FIXTURE CONFIG enableRegularMendingBehavior=true (memory only)");
    }
    void spawn(ServerPlayer p)throws Exception{
        orb=new ExperienceOrb(p.level(),p.getX(),p.getY()+0.15,p.getZ(),7);orb.setDeltaMovement(Vec3.ZERO);orb.setNoGravity(true);
        if(!p.level().addFreshEntity(orb))throw new AssertionError("XP orb spawn failed");orbs++;ticks=0;
        record(cases[scenario]+" SPAWN orb="+orb.getId()+" value="+orb.getValue()+" xpBefore="+p.totalExperience);
    }
    public void onInitialize(){try{configure();}catch(Exception e){throw new IllegalStateException(e);}ServerTickEvents.END_SERVER_TICK.register(server->{
        if(done||server.getPlayerList().getPlayers().isEmpty())return;
        var p=server.getPlayerList().getPlayers().getFirst();
        try{
            if(++ticks>500)throw new AssertionError("timeout scenario="+scenario+" phase="+phase);
            switch(phase){
                case 0->{if(ticks<40)return;
                    if(Boolean.getBoolean("mending.qa.reconnect")){String expected=Files.readString(control.resolve("persisted-state.txt"));String actual=snapshot(p);if(!expected.equals(actual))throw new AssertionError("restart differs expected="+expected+" actual="+actual);record("RESTART "+actual);Files.writeString(control.resolve("state-request"),"restart\n"+actual);phase=7;ticks=0;}
                    else{seed(p);Files.deleteIfExists(control.resolve("client-ack"));Files.writeString(control.resolve("action"),cases[scenario]+" "+(cases[scenario].equals("open-menu")?"open":"wait"));phase=1;ticks=0;}
                }
                case 1->{if(ticks<20)return;if(cases[scenario].equals("open-menu")&&p.containerMenu==p.inventoryMenu)return;spawn(p);phase=2;}
                case 2->{if(!orb.isRemoved())return;record(cases[scenario]+" PICKED orb="+orb.getId()+" state="+snapshot(p));checks++;ticks=0;phase=3;}
                case 3->{if(ticks<6)return;int target=cases[scenario].equals("competition")||cases[scenario].equals("multiple")?6:1;if(orbs<target){spawn(p);phase=2;}else{Files.writeString(control.resolve("action"),cases[scenario]+" close");ticks=0;phase=4;}}
                case 4->{if(ticks<20||p.containerMenu!=p.inventoryMenu)return;String state=snapshot(p);record(cases[scenario]+" AFTER "+state);validate(p);Files.writeString(control.resolve("state-request"),cases[scenario]+"\n"+state);Files.writeString(control.resolve("persisted-state.txt"),state);ticks=0;phase=5;}
                case 5->{if(!Files.exists(control.resolve("client-ack")))return;String ack=Files.readString(control.resolve("client-ack"));if(!ack.startsWith(cases[scenario]+"\n"))return;if(!ack.contains("MATCH"))throw new AssertionError("client mismatch "+ack);record(cases[scenario]+" CLIENT_MATCH");checks++;
                    if(++scenario==cases.length){Files.writeString(control.resolve("result.txt"),"COMPLETE "+checks+" actual pickup/client synchronization checks\n");done=true;}else{ticks=20;phase=0;}
                }
                case 7->{if(!Files.exists(control.resolve("client-ack")))return;String ack=Files.readString(control.resolve("client-ack"));if(!ack.startsWith("restart\n"))return;if(!ack.contains("MATCH"))throw new AssertionError("restart client mismatch "+ack);Files.writeString(control.resolve("result.txt"),"COMPLETE restart server/client state preserved\n");done=true;}
            }
        }catch(Throwable failure){failure.printStackTrace();done=true;try{Files.writeString(control.resolve("result.txt"),"ERROR "+failure);}catch(Exception ignored){}}
    });}
}
