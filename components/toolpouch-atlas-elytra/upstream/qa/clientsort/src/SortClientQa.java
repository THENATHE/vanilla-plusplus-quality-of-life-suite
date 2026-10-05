package clientsortqa;

import java.nio.file.*;
import java.util.*;
import dev.terminalmc.clientsort.client.config.*;
import dev.terminalmc.clientsort.client.gui.TriggerButtonManager;
import dev.terminalmc.clientsort.client.gui.widget.*;
import dev.terminalmc.clientsort.client.inventory.operator.SingleUseOperator;
import dev.terminalmc.clientsort.client.order.SortOrder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.entity.player.Inventory;

/** Calls genuine ClientSort widgets; no addon implementation entry points. */
public class SortClientQa implements ClientModInitializer {
    public static int serverOperatorCount;
    final Path control=Path.of(System.getProperty("clientsort.qa.control"));
    final boolean candidate=Boolean.getBoolean("clientsort.qa.candidate");
    boolean configured,pending,dispatched;String current="";String[] parts;int ticks;
    void policyChecks()throws Exception{
        var policies=Config.options().classPolicies;var original=new HashMap<>(policies);int checks=0;
        String chestKey=ClassPolicy.getKey(net.minecraft.world.inventory.ChestMenu.class.getName(),null);var chest=policies.get(chestKey);
        try{
            for(Class<?> bag:List.of(me.pajic.toolpouch.menu.ToolPouchMenu.class,me.pajic.tiered_backpacks.ui.BackpackMenu.class)){
                String key=ClassPolicy.getKey(bag.getName(),null);
                var disabled=new ClassPolicy(bag.getName(),null,chest.buttonOffset(),chest.offsetFromSlot(),Policy.NONE,Policy.NONE,Policy.NONE,Policy.NONE,null,false,new TreeSet<>());
                policies.put(key,disabled);dev.terminalmc.clientsort.client.util.PolicyManager.reloadPolicyClasses(policies.keySet());
                var before=new HashMap<>(policies);var actual=dev.terminalmc.clientsort.client.util.PolicyManager.getPolicy(bag,"QA policy");
                if(!disabled.equals(actual)||!before.equals(policies))throw new AssertionError("explicit bag policy overwritten "+bag);checks++;
                policies.remove(key);
                for(boolean enabled:List.of(false,true)){
                    policies.put(chestKey,enabled?chest:new ClassPolicy(chest.className(),null,chest.buttonOffset(),chest.offsetFromSlot(),Policy.NONE,Policy.NONE,Policy.NONE,Policy.NONE,null,false,new TreeSet<>()));
                    dev.terminalmc.clientsort.client.util.PolicyManager.reloadPolicyClasses(policies.keySet());before=new HashMap<>(policies);actual=dev.terminalmc.clientsort.client.util.PolicyManager.getPolicy(bag,"QA policy");var expected=policies.get(chestKey);
                    if(actual==null||actual.sortPolicy()!=expected.sortPolicy()||actual.stackFillPolicy()!=expected.stackFillPolicy()||actual.matchTransferPolicy()!=expected.matchTransferPolicy()||actual.transferPolicy()!=expected.transferPolicy()||!before.equals(policies))throw new AssertionError("fallback differs from chest or mutates saved policies "+bag+" enabled="+enabled);checks++;
                }
            }
            Files.writeString(control.resolve("client-policy.txt"),"PASS policy checks="+checks+" explicitNONE preserved; absent bag follows disabled/enabled chest; lookups preserve saved maps");
        }finally{policies.clear();policies.putAll(original);dev.terminalmc.clientsort.client.util.PolicyManager.reloadPolicyClasses(policies.keySet());}
    }
    void configure()throws Exception{
        if(candidate)policyChecks();
        me.pajic.toolpouch.ToolPouch.CONFIG.allowedItems.stream().filter(ai->ai.id.get().equals("minecraft:firework_rocket")).forEach(ai->ai.maxStackCount.accept(1));
        Config.options().interactionInterval=1;
        if(Boolean.getBoolean("clientsort.qa.refillButtons")){
            var key=ClassPolicy.getKey(Inventory.class.getName(),null);var p=Config.options().classPolicies.get(key);
            Config.options().classPolicies.put(key,new ClassPolicy(p.className(),p.invTitle(),p.buttonOffset(),p.offsetFromSlot(),p.sortPolicy(),Policy.KEYBIND_BUTTON,p.matchTransferPolicy(),p.transferPolicy(),p.autoOp(),p.autoOpOther(),p.ignoredSlots()));
        }
        Config.save();Config.options().sortOrder=SortOrder.ALPHABET;configured=true;
    }
    List<TriggerButton> buttons(Minecraft c){return c.gui.screen().children().stream().filter(e->e instanceof TriggerButton).map(e->(TriggerButton)e).filter(b->b.active&&b.visible).toList();}
    void operation(Minecraft c){
        serverOperatorCount=0;String op=parts[2];boolean player=Boolean.parseBoolean(parts[3]);Class<?> type=switch(op){case "sort"->SortButton.class;case "fill"->StackFillButton.class;case "match"->MatchTransferButton.class;default->TransferButton.class;};
        var found=buttons(c).stream().filter(b->type.isInstance(b)&&b.isPlayerInv==player).findFirst();
        if(found.isPresent()){
            TriggerButton b=found.get();if(!b.operationAllowed)throw new AssertionError("operation policy disabled "+op);
            if(!b.mouseClicked(new MouseButtonEvent(b.getX()+3,b.getY()+3,new MouseButtonInfo(com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT,0)),false))throw new AssertionError("button rejected click "+op);
        }else if(op.equals("fill")&&!Boolean.getBoolean("clientsort.qa.refillButtons")){
            // ClientSort's default inventory policy deliberately keeps refill keybind-only.
            var slot=player?TriggerButtonManager.getPlayerRefSlot(Operation.STACK_FILL):TriggerButtonManager.getContainerRefSlot(Operation.STACK_FILL);
            if(slot==null||!SingleUseOperator.fillStacks((AbstractContainerScreen<?>)c.gui.screen(),slot,false))throw new AssertionError("refill entry point rejected");
        }else throw new AssertionError("missing visible "+op+" button player="+player);
    }
    public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(c->{if(c.player==null||c.level==null)return;try{
        if(!configured)configure();if(!Files.exists(control.resolve("command")))return;
        String next=Files.readString(control.resolve("command"));
        if(!next.equals(current)){current=next;parts=next.split("\\|",-1);ticks=0;pending=true;dispatched=false;
            if(parts[1].equals("open")){
                int variant=Integer.parseInt(parts[2]);
                if(variant<4)ClientPlayNetworking.send(new me.pajic.toolpouch.network.ModPayloads.C2SOpenToolPouchPayload(variant>=2?1:0));
                else ClientPlayNetworking.send(new me.pajic.tiered_backpacks.network.ModNetworking.C2SOpenBackpackPayload(0));
            }else if(parts[1].equals("close"))c.player.closeContainer();
        }
        if(!pending)return;if(++ticks>500)throw new AssertionError("operation timeout "+current);
        if(parts[1].equals("op")&&!dispatched){if(ticks<15)return;operation(c);dispatched=true;ticks=0;return;}
        if(ticks<20||dev.terminalmc.clientsort.client.ClientSort.operatingClient)return;
        String observed="OK";
        if(parts[1].equals("op")){observed="serverOperators="+serverOperatorCount;if(Boolean.getBoolean("clientsort.qa.serverInstalled")&&serverOperatorCount==0)throw new AssertionError("accelerated operation did not use ServerOperator");}
        if(parts[1].equals("open")){
            if(!(c.gui.screen() instanceof AbstractContainerScreen<?>))throw new AssertionError("native screen missing");
            var b=buttons(c);long bag=b.stream().filter(x->!x.isPlayerInv).count();
            if(candidate&&bag<3)throw new AssertionError("expected visible native container buttons; actual="+bag);
            if(!candidate&&bag!=0)throw new AssertionError("baseline unexpectedly supports buttons: "+bag);
            observed="bagButtons="+bag+",playerButtons="+b.stream().filter(x->x.isPlayerInv).count()+",screen="+c.gui.screen().getClass().getName();
            Screenshot.grab(c.gameDirectory,"clientsort-"+parts[2]+"-"+parts[0]+".png",c.gameRenderer.mainRenderTarget(),1,message->{});
        }
        if(parts[1].equals("op")&&parts.length<5&&!c.player.containerMenu.getCarried().isEmpty())throw new AssertionError("operation left cursor stack "+current+" "+c.player.containerMenu.getCarried());
        Files.writeString(control.resolve("ack"),parts[0]+"|"+observed);pending=false;
    }catch(Throwable t){t.printStackTrace();try{Files.writeString(control.resolve("result.txt"),"ERROR client "+t);}catch(Exception ignored){}}});}
}
