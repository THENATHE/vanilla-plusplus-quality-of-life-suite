package capacityqa;

import java.nio.file.*;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Screenshot;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.ContainerInput;

/** Native networking and inventory clicks only; no production class replacements. */
public class CapacityClientQa implements ClientModInitializer {
    final Path control=Path.of(System.getProperty("capacity.qa.control"));
    String current="";String[] parts;int ticks;boolean pending;
    public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(c->{
        if(c.player==null||c.level==null)return;
        try{
            if(!Files.exists(control.resolve("command")))return;
            String next=Files.readString(control.resolve("command"));
            if(!next.equals(current)){
                current=next;parts=next.split("\\|",-1);ticks=0;pending=true;
                switch(parts[1]){
                    case "config"->{boolean custom=Boolean.parseBoolean(parts[2]);ToolPouch.CONFIG.toolPouchRows.accept(custom?2:4);ToolPouch.CONFIG.toolPouchColumns.accept(custom?3:4);ToolPouch.CONFIG.netheriteToolPouchRows.accept(5);ToolPouch.CONFIG.netheriteToolPouchColumns.accept(custom?7:5);}
                    case "open"->ClientPlayNetworking.send(new ModPayloads.C2SOpenToolPouchPayload(Integer.parseInt(parts[2])));
                    case "withdraw"->c.gameMode.handleContainerInput(c.player.containerMenu.containerId,Integer.parseInt(parts[2]),0,ContainerInput.QUICK_MOVE,c.player);
                    case "deposit"->{
                        int size=c.player.containerMenu.slots.size()-36,source=-1;
                        for(int i=size;i<c.player.containerMenu.slots.size();i++){
                            var name=c.player.containerMenu.getSlot(i).getItem().get(DataComponents.CUSTOM_NAME);
                            if(name!=null&&name.getString().equals(parts[3])){source=i;break;}
                        }
                        if(source<0)throw new AssertionError("withdrawn item missing on client "+parts[3]);
                        c.gameMode.handleContainerInput(c.player.containerMenu.containerId,source,0,ContainerInput.PICKUP,c.player);
                        c.gameMode.handleContainerInput(c.player.containerMenu.containerId,Integer.parseInt(parts[2]),0,ContainerInput.PICKUP,c.player);
                    }
                    case "close"->c.player.closeContainer();
                    default->throw new AssertionError("unknown command "+current);
                }
            }
            if(!pending||++ticks<15)return;
            String observed="OK";
            if(parts[1].equals("open")){
                if(!(c.player.containerMenu instanceof ToolPouchMenu menu))throw new AssertionError("native ToolPouchMenu missing");
                int size=menu.slots.size()-36;
                observed="size="+size+",rows="+ToolPouchUtil.getToolPouchRows(menu.getToolPouch())+",columns="+ToolPouchUtil.getToolPouchColumns(menu.getToolPouch());
                Screenshot.grab(c.gameDirectory,"capacity-"+parts[0]+".png",c.gameRenderer.mainRenderTarget(),1,message->{});
            }
            if(parts[1].equals("deposit")&&!c.player.containerMenu.getCarried().isEmpty())throw new AssertionError("deposit left carried stack");
            Files.writeString(control.resolve("ack"),parts[0]+"|"+observed);pending=false;
        }catch(Throwable t){t.printStackTrace();try{Files.writeString(control.resolve("result.txt"),"ERROR client "+t);}catch(Exception ignored){}}
    });}
}
