package qa.atlasnetwork;
import java.nio.file.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
/** Counts real connected client world ticks; does not register suite channels. */
public final class AtlasNetworkClientQa implements ClientModInitializer {
    public void onInitializeClient(){final int[] ticks={0};ClientTickEvents.END_CLIENT_TICK.register(mc->{
        if(mc.player!=null&&mc.level!=null&&++ticks[0]%10==0)try{
            Files.writeString(Path.of(System.getProperty("atlas.qa.control"),mc.player.getGameProfile().name()+"-client.json"),"{\"world_ticks\":"+ticks[0]+",\"connected\":"+(mc.getConnection()!=null)+"}");
        }catch(Exception error){throw new RuntimeException(error);}
    });}
}
