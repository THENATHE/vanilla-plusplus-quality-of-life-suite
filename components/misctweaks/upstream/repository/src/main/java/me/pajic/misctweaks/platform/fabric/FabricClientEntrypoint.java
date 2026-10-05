package me.pajic.misctweaks.platform.fabric;

//? fabric {

//~ if <26.1 'ClientLevelEvents' -> 'ClientWorldEvents' {
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.misctweaks.MiscTweaksClient;
import me.pajic.misctweaks.util.ModUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		MiscTweaksClient.onInitialize();
        //~ if <26.1 'AFTER_CLIENT_LEVEL_CHANGE' -> 'AFTER_CLIENT_WORLD_CHANGE'
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> ModUtil.onLevelLoad(level));
	}
}
//~}
//?}
