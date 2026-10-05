package clientsortqa;

import java.nio.file.*;
import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.GameType;

/** Seeds controlled inventory contents, then observes actual ClientSort-generated packets. */
public class SortServerQa implements ModInitializer {
    final Path control=Path.of(System.getProperty("clientsort.qa.control"));
    final boolean candidate=Boolean.getBoolean("clientsort.qa.candidate");
    final String[] operations={"sort|false","sort|true","fill|true","fill|false","match|true","match|false","transfer|true","sort|false|occupied","transfer|false"};
    int variant,phase,ticks,sequence,operation,size,checks;boolean done;
    ItemStack holder;Map<String,Integer> before,saved;List<ItemStack> sorted;
    void check(boolean ok,String why)throws Exception{if(!ok)throw new AssertionError(why);checks++;}
    void record(String text)throws Exception{Files.writeString(control.resolve("observations.txt"),text+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}
    void command(String action)throws Exception{sequence++;ticks=0;Files.writeString(control.resolve("command"),sequence+"|"+action);}
    boolean ack()throws Exception{return Files.exists(control.resolve("ack"))&&Files.readString(control.resolve("ack")).startsWith(sequence+"|");}
    ItemStack item(Item type,int count){return new ItemStack(type,count);}
    boolean pouch(){return variant<4;}boolean attached(){return variant==2||variant==3;}
    void add(Map<String,Integer> map,ItemStack item){if(!item.isEmpty())map.merge(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(),item.getCount(),Integer::sum);}
    Map<String,Integer> state(ServerPlayer p){var result=new TreeMap<String,Integer>();for(int i=0;i<size;i++)add(result,p.containerMenu.getSlot(i).getItem());for(int i=0;i<p.getInventory().getContainerSize();i++){var stack=p.getInventory().getItem(i);if(stack!=holder)add(result,stack);}add(result,p.containerMenu.getCarried());return result;}
    int bag(ServerPlayer p,Item item){int n=0;for(int i=0;i<size;i++){var s=p.containerMenu.getSlot(i).getItem();if(s.is(item))n+=s.getCount();}return n;}
    int inv(ServerPlayer p,Item item){int n=0;for(var s:p.getInventory().getNonEquipmentItems())if(s!=holder&&s.is(item))n+=s.getCount();return n;}
    void bagSet(ServerPlayer p,int slot,Item item,int count){p.containerMenu.getSlot(slot).set(item(item,count));}
    void seedHolder(ServerPlayer p)throws Exception{
        p.closeContainer();p.getInventory().clearContent();p.inventoryMenu.setCarried(ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.LEGS,ItemStack.EMPTY);p.setGameMode(GameType.SURVIVAL);p.setPermanentlyInvulnerable(true);
        if(pouch()){
            holder=item(variant%2==0?me.pajic.toolpouch.item.ModItems.TOOL_POUCH:me.pajic.toolpouch.item.ModItems.NETHERITE_TOOL_POUCH,1);holder.set(DataComponents.CONTAINER,ItemContainerContents.EMPTY);
            if(attached()){var input=CraftingInput.of(2,1,List.of(item(Items.NETHERITE_LEGGINGS,1),holder));var recipe=new me.pajic.toolpouch.recipe.AttachToolPouchRecipe();check(recipe.matches(input,p.level()),"attachment recipe match");holder=recipe.assemble(input);}
        }else{
            Item[] types={me.pajic.tiered_backpacks.item.ModItems.LEATHER_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.COPPER_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.IRON_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.GOLDEN_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.DIAMOND_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.NETHERITE_BACKPACK};
            holder=item(types[variant-4],1);holder.set(me.pajic.tiered_backpacks.component.ModDataComponents.BACKPACK_TIER,me.pajic.tiered_backpacks.util.BackpackTier.values()[variant-4]);holder.set(DataComponents.CONTAINER,ItemContainerContents.EMPTY);
        }
        holder.set(DataComponents.CUSTOM_NAME,Component.literal("QA-owner-"+variant));if(attached())p.setItemSlot(EquipmentSlot.LEGS,holder);else p.getInventory().setItem(20,holder);p.inventoryMenu.broadcastFullState();record("VARIANT="+variant+" owner="+holder.getItem());command("open|"+variant);
    }
    void seedOperation(ServerPlayer p)throws Exception{
        p.containerMenu.setCarried(ItemStack.EMPTY);
        for(int i=0;i<size;i++)p.containerMenu.getSlot(i).set(ItemStack.EMPTY);
        for(int i=0;i<36;i++)if(p.getInventory().getItem(i)!=holder)p.getInventory().setItem(i,ItemStack.EMPTY);
        String op=operations[operation];
        switch(operation){
            case 0->{bagSet(p,0,Items.FIREWORK_ROCKET,8);bagSet(p,2,Items.ARROW,10);bagSet(p,4,Items.COMPASS,1);bagSet(p,size-1,Items.ARROW,5);}
            case 1->{p.getInventory().setItem(9,item(Items.FIREWORK_ROCKET,8));p.getInventory().setItem(11,item(Items.ARROW,10));p.getInventory().setItem(14,item(Items.COMPASS,1));p.getInventory().setItem(25,item(Items.ARROW,5));}
            case 2->{bagSet(p,0,Items.ARROW,10);bagSet(p,1,Items.FIREWORK_ROCKET,8);bagSet(p,2,Items.COMPASS,1);p.getInventory().setItem(9,item(Items.ARROW,20));p.getInventory().setItem(10,item(Items.FIREWORK_ROCKET,10));p.getInventory().setItem(11,item(Items.COMPASS,10));p.getInventory().setItem(12,item(Items.CLOCK,1));p.getInventory().setItem(13,item(Items.STONE,7));}
            case 3->{bagSet(p,0,Items.ARROW,20);bagSet(p,1,Items.FIREWORK_ROCKET,10);bagSet(p,2,Items.COMPASS,1);p.getInventory().setItem(9,item(Items.ARROW,10));p.getInventory().setItem(10,item(Items.FIREWORK_ROCKET,8));p.getInventory().setItem(11,item(Items.COMPASS,10));}
            case 4->{bagSet(p,0,Items.ARROW,10);bagSet(p,1,Items.COMPASS,1);p.getInventory().setItem(9,item(Items.ARROW,20));p.getInventory().setItem(10,item(Items.FIREWORK_ROCKET,10));p.getInventory().setItem(11,item(Items.COMPASS,10));p.getInventory().setItem(12,item(Items.STONE,7));}
            case 5->{bagSet(p,0,Items.ARROW,20);bagSet(p,1,Items.FIREWORK_ROCKET,10);bagSet(p,2,Items.COMPASS,1);p.getInventory().setItem(9,item(Items.ARROW,10));p.getInventory().setItem(10,item(Items.COMPASS,10));}
            case 6->{p.getInventory().setItem(9,item(Items.ARROW,20));p.getInventory().setItem(10,item(Items.FIREWORK_ROCKET,10));p.getInventory().setItem(11,item(Items.COMPASS,3));p.getInventory().setItem(12,item(Items.STONE,7));p.getInventory().setItem(13,item(Items.CLOCK,2));p.getInventory().setItem(17,item(Items.SHULKER_BOX,1));}
            case 7->{bagSet(p,0,Items.FIREWORK_ROCKET,8);bagSet(p,2,Items.ARROW,10);p.containerMenu.setCarried(item(Items.DIAMOND,1));}
            case 8->{bagSet(p,0,Items.ARROW,20);bagSet(p,1,Items.FIREWORK_ROCKET,10);bagSet(p,2,Items.COMPASS,1);}
        }
        p.containerMenu.broadcastFullState();before=state(p);command("op|"+op);
    }
    void validate(ServerPlayer p)throws Exception{
        check(before.equals(state(p)),"total item conservation expected="+before+" actual="+state(p));check(operation==7?p.containerMenu.getCarried().is(Items.DIAMOND)&&p.containerMenu.getCarried().getCount()==1:p.containerMenu.getCarried().isEmpty(),"server cursor state changed");
        check(holder.getCount()==1&&(attached()?p.getItemBySlot(EquipmentSlot.LEGS):p.getInventory().getItem(20))==holder,"owning bag moved or changed");
        for(int i=0;i<size;i++){Slot slot=p.containerMenu.getSlot(i);var s=slot.getItem();check(s.isEmpty()||!s.is(holder.getItem()),"owning bag nested into itself");check(s.isEmpty()||s.getCount()<=slot.getMaxStackSize(s),"slot cap exceeded");}
        switch(operation){
            case 0->{check(p.containerMenu.getSlot(0).getItem().is(Items.ARROW)&&p.containerMenu.getSlot(0).getItem().getCount()==15,"bag sort must consolidate arrows at firstslot");check(p.containerMenu.getSlot(1).getItem().is(Items.COMPASS)&&p.containerMenu.getSlot(2).getItem().is(Items.FIREWORK_ROCKET),"bag sort alphabetical ordering");}
            case 1->{check(p.getInventory().getItem(9).is(Items.ARROW)&&p.getInventory().getItem(9).getCount()==15,"inventory sort consolidates arrows");}
            case 2->{check(bag(p,Items.ARROW)==30&&bag(p,Items.FIREWORK_ROCKET)==18,"refill into bag");check(bag(p,Items.COMPASS)==(pouch()?1:11),"refill honors custom compass slotcap");check(inv(p,Items.CLOCK)==1&&inv(p,Items.STONE)==7,"refill must not transfer unmatched types");}
            case 3->{check(inv(p,Items.ARROW)==30&&inv(p,Items.FIREWORK_ROCKET)==18&&inv(p,Items.COMPASS)==11,"refill into inventory");check(bag(p,Items.ARROW)==0&&bag(p,Items.FIREWORK_ROCKET)==0,"refill source consumed exactly");}
            case 4->{check(bag(p,Items.ARROW)==30&&bag(p,Items.COMPASS)==11,"matching transfer into bag");check(inv(p,Items.FIREWORK_ROCKET)==10&&inv(p,Items.STONE)==7,"matching transfer must exclude unmatched types");}
            case 5->{check(inv(p,Items.ARROW)==30&&inv(p,Items.COMPASS)==11&&bag(p,Items.FIREWORK_ROCKET)==10,"matching transfer into inventory");}
            case 6->{check(bag(p,Items.ARROW)==20&&bag(p,Items.FIREWORK_ROCKET)==10&&bag(p,Items.COMPASS)==3,"bulk transfer into bag");check(bag(p,Items.CLOCK)==(pouch()?1:2),"clock quota");check(bag(p,Items.STONE)==(pouch()?0:7),"forbidden stone restriction");check(bag(p,Items.SHULKER_BOX)==(pouch()?1:0),"native shulker permission preserved");}
            case 7->{check(p.containerMenu.getSlot(0).getItem().is(Boolean.getBoolean("clientsort.qa.serverInstalled")?Items.ARROW:Items.FIREWORK_ROCKET),"cursor sort behavior: client skips, accelerated safely sorts without touching cursor");}
            case 8->{check(bag(p,Items.ARROW)==0&&bag(p,Items.FIREWORK_ROCKET)==0&&bag(p,Items.COMPASS)==0,"bulk transfer out empties bag");check(inv(p,Items.ARROW)==20&&inv(p,Items.FIREWORK_ROCKET)==10&&inv(p,Items.COMPASS)==1,"bulk transfer out to inventory");}
        }
        record("PASS variant="+variant+" operation="+operations[operation]+" transport="+Files.readString(control.resolve("ack"))+" conservation="+state(p));
    }
    public void onInitialize(){me.pajic.toolpouch.ToolPouch.CONFIG.allowedItems.stream().filter(ai->ai.id.get().equals("minecraft:firework_rocket")).forEach(ai->ai.maxStackCount.accept(1));ServerTickEvents.END_SERVER_TICK.register(server->{if(done||server.getPlayerList().getPlayers().isEmpty())return;var p=server.getPlayerList().getPlayers().getFirst();try{
        if(++ticks>1000)throw new AssertionError("timeout variant="+variant+" phase="+phase+" operation="+operation);
        if(phase==0){if(ticks<60)return;
            if(candidate&&Boolean.getBoolean("clientsort.qa.serverInstalled")){
                var boundary=Class.forName("clientsortqa.ClientSortServerBoundaryQa");
                record("SERVER_BOUNDARIES "+boundary.getMethod("run",ServerPlayer.class).invoke(null,p));
            }
            seedHolder(p);phase=1;return;}
        if(!ack())return;
        switch(phase){
            case 1->{size=p.containerMenu.slots.size()-36;check(size==(pouch()?(variant%2==0?16:25):new int[]{27,36,45,54,66,78}[variant-4]),"native bag capacity");record("OPEN variant="+variant+" "+Files.readString(control.resolve("ack")));if(candidate){operation=0;seedOperation(p);phase=2;}else{command("close|");phase=5;}}
            case 2->{validate(p);if(operation==0){saved=state(p);sorted=new ArrayList<>();for(int i=0;i<size;i++)sorted.add(p.containerMenu.getSlot(i).getItem().copy());command("close|");phase=6;break;}operation++;if(operation<operations.length)seedOperation(p);else{saved=state(p);command("close|");phase=3;}}
            case 3->{command("open|"+variant);phase=4;}
            case 4->{check(saved.equals(state(p)),"close/reopen changed total contents");check(bag(p,Items.ARROW)==0,"withdrawn contents returned on reopen");command("close|");phase=5;}
            case 6->{command("open|"+variant);phase=7;}
            case 7->{check(saved.equals(state(p)),"sorted contents changed on reopen");for(int i=0;i<size;i++)check(ItemStack.matches(sorted.get(i),p.containerMenu.getSlot(i).getItem()),"sorted physical position changed on reopen");operation=1;seedOperation(p);phase=2;}
            case 5->{variant++;if(variant==10){Files.writeString(control.resolve("result.txt"),"COMPLETE "+(candidate?"OPERATIONS_PASS":"BASELINE_MISSING_BUTTONS")+" variants=10 checks="+checks);done=true;}else{seedHolder(p);phase=1;}}
        }
    }catch(Throwable t){done=true;t.printStackTrace();try{Files.writeString(control.resolve("result.txt"),"ERROR server variant="+variant+" operation="+operation+" phase="+phase+" "+t);}catch(Exception ignored){}}});}
}
