package pouchmendingqa;
import java.nio.file.*;
import java.util.*;
import me.pajic.toolpouch.network.ModPayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
public class MendingClientQa implements ClientModInitializer{
    final Path control=Path.of(System.getProperty("mending.qa.control"));String action="",request="";int ticks;
    void collect(ItemStack s,Map<String,Integer> state,int depth){if(s.isEmpty())return;if(depth>8)throw new AssertionError("client depth");var n=s.get(DataComponents.CUSTOM_NAME);if(n!=null&&n.getString().startsWith("QA-"))state.put(n.getString(),s.getDamageValue());s.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).itemCopies().forEach(c->collect(c,state,depth+1));}
    public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(c->{if(c.player==null||c.level==null)return;try{
        if(Files.exists(control.resolve("action"))){String next=Files.readString(control.resolve("action"));if(!next.equals(action)){action=next;if(next.endsWith(" open"))ClientPlayNetworking.send(new ModPayloads.C2SOpenToolPouchPayload(0));else if(next.endsWith(" close"))c.player.closeContainer();}}
        if(!Files.exists(control.resolve("state-request")))return;String next=Files.readString(control.resolve("state-request"));if(!next.equals(request)){request=next;ticks=0;}if(++ticks<10)return;
        String[] parts=request.split("\n",2);Map<String,Integer> state=new TreeMap<>();for(int i=0;i<c.player.getInventory().getContainerSize();i++)collect(c.player.getInventory().getItem(i),state,0);String actual=state+"|xp="+c.player.totalExperience;
        if(parts[1].equals(actual))Files.writeString(control.resolve("client-ack"),parts[0]+"\nMATCH "+actual);else if(ticks>120)Files.writeString(control.resolve("client-ack"),parts[0]+"\nMISMATCH expected="+parts[1]+" actual="+actual);
    }catch(Throwable t){t.printStackTrace();try{Files.writeString(control.resolve("result.txt"),"ERROR client "+t);}catch(Exception ignored){}}});}
}
