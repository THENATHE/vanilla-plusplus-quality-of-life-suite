package qa.atlasnetwork;
import java.nio.file.*;
import com.google.gson.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
/** Real client menu clicks; registers no suite channels and never overrides local slot rules. */
public final class AtlasNetworkClientQa implements ClientModInitializer {
    public void onInitializeClient(){final int[] ticks={0};final JsonArray actions=new JsonArray();ClientTickEvents.END_CLIENT_TICK.register(mc->{
        if(mc.player!=null&&mc.level!=null&&++ticks[0]%10==0)try{
            Path control=Path.of(System.getProperty("atlas.qa.control"));String name=mc.player.getGameProfile().name();
            var action=control.resolve(name+"-client-click.json");
            if(Files.exists(action) && mc.player.containerMenu instanceof CartographyTableMenu table) {
                var request=JsonParser.parseString(Files.readString(action)).getAsJsonObject();Files.delete(action);
                var observation=new JsonObject();observation.addProperty("stage",request.get("stage").getAsInt());
                observation.addProperty("slot",request.get("slot").getAsInt());
                observation.addProperty("local_book_may_place",table.getSlot(1).mayPlace(new ItemStack(Items.BOOK)));
                mc.gameMode.handleContainerInput(table.containerId,request.get("slot").getAsInt(),0,request.get("quick").getAsBoolean()?ContainerInput.QUICK_MOVE:ContainerInput.PICKUP,mc.player);
                actions.add(observation);
            }
            var row=new JsonObject();row.addProperty("world_ticks",ticks[0]);row.addProperty("connected",mc.getConnection()!=null);
            row.add("clicks",actions.deepCopy());
            if(mc.player.containerMenu instanceof CartographyTableMenu table) {
                var menu=new JsonObject();menu.addProperty("top_count",table.getSlot(0).getItem().getCount());
                menu.addProperty("book_count",table.getSlot(1).getItem().getCount());
                menu.addProperty("output_count",table.getSlot(2).getItem().getCount());
                menu.addProperty("carried_count",table.getCarried().getCount());
                menu.addProperty("local_book_may_place",table.getSlot(1).mayPlace(new ItemStack(Items.BOOK)));
                row.add("cartography",menu);
            }
            Files.writeString(control.resolve(name+"-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(row));
        }catch(Exception error){throw new RuntimeException(error);}
    });}
}
