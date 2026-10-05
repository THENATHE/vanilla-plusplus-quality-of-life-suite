package me.pajic.misctweaks.platform.fabric;

//? fabric {

//~ if <26.1 'ServerLevelEvents' -> 'ServerWorldEvents' {
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.misctweaks.MiscTweaks;
import me.pajic.misctweaks.util.ModUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		MiscTweaks.onInitialize();
        ServerLevelEvents.LOAD.register((server, level) -> ModUtil.onLevelLoad(level));
	}
}
//~}
//?}
