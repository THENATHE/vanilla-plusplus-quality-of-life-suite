package capacityqa;

import java.nio.file.*;
import java.util.*;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.component.ModDataComponents;
import me.pajic.toolpouch.item.ModItems;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.recipe.AttachToolPouchRecipe;
import me.pajic.toolpouch.recipe.DetachToolPouchRecipe;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.GameType;

/** Real native menus; original recipe APIs produce and detach the exact holder under test. */
public class CapacityServerQa implements ModInitializer {
    final Path control=Path.of(System.getProperty("capacity.qa.control"));
    final boolean fixed=Boolean.getBoolean("capacity.qa.candidate");
    int scenario,phase,ticks,sequence,checks,actual,expected;boolean done,attached,netherite,custom;
    ItemStack holder;Map<String,Integer> original;String moved;
    void check(boolean value,String description)throws Exception{if(!value)throw new AssertionError(description);checks++;}
    void record(String text)throws Exception{Files.writeString(control.resolve("observations.txt"),text+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}
    void command(String op,String args)throws Exception{sequence++;ticks=0;Files.writeString(control.resolve("command"),sequence+"|"+op+"|"+args);}
    boolean ack()throws Exception{return Files.exists(control.resolve("ack"))&&Files.readString(control.resolve("ack")).startsWith(sequence+"|");}
    ItemStack token(int slot){ItemStack item=new ItemStack(Items.COMPASS);item.set(DataComponents.CUSTOM_NAME,Component.literal("QA-"+scenario+"-"+slot));return item;}
    Map<String,Integer> contents(ItemStack stack){Map<String,Integer> result=new TreeMap<>();stack.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).itemCopies().filter(s->!s.isEmpty()).forEach(s->result.merge(s.getHoverName().getString(),s.getCount(),Integer::sum));return result;}
    ItemStack attach(ServerPlayer p,ItemStack leggings,ItemStack pouch)throws Exception{
        var recipe=new AttachToolPouchRecipe();var input=CraftingInput.of(2,1,List.of(leggings,pouch));check(recipe.matches(input,p.level()),"attach recipe must match");return recipe.assemble(input);
    }
    void recipeLifecycle(ServerPlayer p)throws Exception{
        var pouch=new ItemStack(ModItems.NETHERITE_TOOL_POUCH);pouch.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(token(101))));pouch.set(DataComponents.DYED_COLOR,new DyedItemColor(0x345678));
        var leggings=new ItemStack(Items.NETHERITE_LEGGINGS);leggings.setDamageValue(7);
        var armor=attach(p,leggings,pouch);var detach=new DetachToolPouchRecipe();var input=CraftingInput.of(1,1,List.of(armor));check(detach.matches(input,p.level()),"detach recipe match");
        var extracted=detach.assemble(input);var bare=detach.getRemainingItems(input).getFirst();
        check(extracted.is(ModItems.NETHERITE_TOOL_POUCH),"first detach keeps netherite item");check(contents(extracted).equals(contents(pouch)),"first detach contents");check(extracted.get(DataComponents.DYED_COLOR).rgb()==0x345678,"detach dye");check(bare.getDamageValue()==7&&!bare.has(DataComponents.CONTAINER),"bare armor damage/container state");
        boolean stale=bare.getOrDefault(ModDataComponents.IS_NETHERITE_POUCH,false);check(stale!=fixed,"expected marker cleanup state fixed="+fixed+" stale="+stale);record("DETACH_BARE_MARKER="+stale);
        // Include armor left over from the old buggy recipe even when testing the fix.
        bare.set(ModDataComponents.IS_NETHERITE_POUCH,true);
        var ordinary=new ItemStack(ModItems.TOOL_POUCH);ordinary.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(token(102))));
        var normalArmor=attach(p,bare,ordinary);var normalInput=CraftingInput.of(1,1,List.of(normalArmor));
        var detachedNormal=detach.assemble(normalInput);
        check(detachedNormal.is(fixed?ModItems.TOOL_POUCH:ModItems.NETHERITE_TOOL_POUCH),"normal reattach must not gain netherite tier");check(contents(detachedNormal).equals(contents(ordinary)),"normal reattach contents conserved");record("NORMAL_REATTACH_DETACH="+detachedNormal.getItem()+" fixed="+fixed);
        check(ToolPouchUtil.getToolPouchSize(bare)==16,"bare stale marker must not grant attached capacity");
        var unrelated=new ItemStack(Items.STICK);unrelated.set(ModDataComponents.IS_NETHERITE_POUCH,true);check(ToolPouchUtil.getToolPouchSize(unrelated)==16,"unrelated stale marker must not grant capacity");
        var standalone=new ItemStack(ModItems.NETHERITE_TOOL_POUCH);standalone.set(ModDataComponents.IS_NETHERITE_POUCH,false);check(ToolPouchUtil.getToolPouchSize(standalone)==25,"standalone netherite item keeps tier despite false marker");
        var explicitFalse=normalArmor.copy();explicitFalse.set(ModDataComponents.IS_NETHERITE_POUCH,false);check(ToolPouchUtil.getToolPouchSize(explicitFalse)==16,"false attached marker uses ordinary dimensions");
    }
    void seed(ServerPlayer p)throws Exception{
        p.closeContainer();p.getInventory().clearContent();p.inventoryMenu.setCarried(ItemStack.EMPTY);p.setGameMode(GameType.SURVIVAL);p.setPermanentlyInvulnerable(true);p.setItemSlot(EquipmentSlot.LEGS,ItemStack.EMPTY);
        custom=scenario>=4;attached=scenario%4>=2;netherite=scenario%2==1;
        ToolPouch.CONFIG.toolPouchRows.accept(custom?2:4);ToolPouch.CONFIG.toolPouchColumns.accept(custom?3:4);ToolPouch.CONFIG.netheriteToolPouchRows.accept(5);ToolPouch.CONFIG.netheriteToolPouchColumns.accept(custom?7:5);
        expected=netherite?(custom?35:25):(custom?6:16);var pouch=new ItemStack(netherite?ModItems.NETHERITE_TOOL_POUCH:ModItems.TOOL_POUCH);var items=new ArrayList<ItemStack>();for(int i=0;i<expected;i++)items.add(token(i));pouch.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(items));pouch.set(DataComponents.DYED_COLOR,new DyedItemColor(0x123456));
        holder=attached?attach(p,new ItemStack(Items.NETHERITE_LEGGINGS),pouch):pouch;original=contents(holder);
        if(attached)p.setItemSlot(EquipmentSlot.LEGS,holder);else p.getInventory().setItem(12,holder);
        p.getInventory().setSelectedSlot(0);p.inventoryMenu.broadcastFullState();
        record("SCENARIO "+scenario+" attached="+attached+" netherite="+netherite+" custom="+custom+" desired="+expected+" stored="+original.size());
        command("config",Boolean.toString(custom));
    }
    void verifyDetached(ServerPlayer p)throws Exception{
        if(!attached)return;var recipe=new DetachToolPouchRecipe();var input=CraftingInput.of(1,1,List.of(holder));check(recipe.matches(input,p.level()),"final detach match");var result=recipe.assemble(input);check(result.is(netherite?ModItems.NETHERITE_TOOL_POUCH:ModItems.TOOL_POUCH),"final detach tier");check(contents(result).equals(contents(holder)),"final detach exact contents");check(result.get(DataComponents.DYED_COLOR).rgb()==0x123456,"final detach dye");
        record("FINAL_DETACH contents="+contents(result).size()+" desired="+expected);
    }
    public void onInitialize(){ServerTickEvents.END_SERVER_TICK.register(server->{
        if(done||server.getPlayerList().getPlayers().isEmpty())return;var p=server.getPlayerList().getPlayers().getFirst();
        try{
            if(++ticks>600)throw new AssertionError("timeout scenario="+scenario+" phase="+phase);
            if(phase==0){if(ticks<60)return;recipeLifecycle(p);seed(p);phase=1;return;}
            if(!ack())return;
            switch(phase){
                case 1->{
                    boolean rejected=false;
                    try{
                        ToolPouchUtil.replaceItemInToolPouch(p,token(1000),stack->stack.is(Items.COMPASS),expected-1);
                        check(contents(holder).size()==expected&&contents(holder).containsKey("QA-"+scenario+"-1000"),"utility lastslot replacement missing");
                        ToolPouchUtil.replaceItemInToolPouch(p,token(expected-1),stack->stack.is(Items.COMPASS),expected-1);
                        check(contents(holder).equals(original),"utility replacement lost sibling data");
                    }catch(IndexOutOfBoundsException oldCapacityBug){rejected=true;}
                    check(rejected==(!fixed&&attached&&netherite),"utility lastslot acceptance mismatch");record("UTILITY_LAST_SLOT scenario="+scenario+" rejected="+rejected);
                    command("open",attached?"1":"0");phase=2;
                }
                case 2->{
                    check(p.containerMenu instanceof ToolPouchMenu,"native server menu missing");actual=p.containerMenu.slots.size()-36;
                    int wanted=!fixed&&attached&&netherite?(custom?6:16):expected;
                    check(actual==wanted,"server slot count actual="+actual+" wanted="+wanted);check(Files.readString(control.resolve("ack")).contains("size="+wanted+","),"client/server slots differ");check(ToolPouchUtil.getToolPouchSize(holder)==wanted,"utility capacity mismatch");
                    record("OPEN scenario="+scenario+" server="+actual+" client="+Files.readString(control.resolve("ack"))+" desired="+expected);
                    moved="QA-"+scenario+"-"+(actual-1);check(p.containerMenu.getSlot(actual-1).getItem().getHoverName().getString().equals(moved),"last accessible slot data");command("withdraw",Integer.toString(actual-1));phase=3;
                }
                case 3->{check(p.containerMenu.getSlot(actual-1).getItem().isEmpty(),"withdraw did not empty lastslot");long count=p.getInventory().getNonEquipmentItems().stream().filter(s->!s.isEmpty()&&s.getHoverName().getString().equals(moved)).count();check(count==1,"withdraw not present exactlyonce");command("deposit",(actual-1)+"|"+moved);phase=4;}
                case 4->{check(p.containerMenu.getSlot(actual-1).getItem().getHoverName().getString().equals(moved),"lastslot deposit missing");check(p.containerMenu.getCarried().isEmpty(),"server cursor notempty");check(p.getInventory().getNonEquipmentItems().stream().noneMatch(stack->!stack.isEmpty()&&stack.getHoverName().getString().equals(moved)),"deposit left duplicate in inventory");command("close","");phase=5;}
                case 5->{
                    var saved=contents(holder);Map<String,Integer> wanted=new TreeMap<>(original);if(!fixed&&attached&&netherite)for(int i=actual;i<expected;i++)wanted.remove("QA-"+scenario+"-"+i);
                    check(saved.equals(wanted),"close contents differ actual="+saved+" expected="+wanted);record("CLOSE scenario="+scenario+" retained="+saved.size()+" lost="+(original.size()-saved.size()));
                    var ops=p.registryAccess().createSerializationContext(NbtOps.INSTANCE);var serialized=ItemStack.CODEC.encodeStart(ops,holder).getOrThrow();var decoded=ItemStack.CODEC.parse(ops,serialized).getOrThrow();check(ItemStack.isSameItemSameComponents(holder,decoded),"NBT roundtrip changed holder components");check(contents(decoded).equals(saved),"NBT roundtrip lost items");
                    command("open",attached?"1":"0");phase=6;
                }
                case 6->{check(p.containerMenu.slots.size()-36==actual,"reopen capacity changed");check(p.containerMenu.getSlot(actual-1).getItem().getHoverName().getString().equals(moved),"reopen lastslot lost");command("close","");phase=7;}
                case 7->{verifyDetached(p);scenario++;if(scenario==8){record("CHECKS="+checks);Files.writeString(control.resolve("result.txt"),"COMPLETE "+(fixed?"FIXED_PASS":"BASELINE_BUGS_REPRODUCED")+" scenarios=8 checks="+checks);done=true;}else{seed(p);phase=1;}}
            }
        }catch(Throwable t){done=true;t.printStackTrace();try{Files.writeString(control.resolve("result.txt"),"ERROR server scenario="+scenario+" phase="+phase+" "+t);}catch(Exception ignored){}}
    });}
}
