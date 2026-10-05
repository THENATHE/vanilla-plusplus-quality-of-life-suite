package me.pajic.misctweaks.platform.neoforge;

//? neoforge {

/*import me.pajic.misctweaks.MiscTweaks;
import me.pajic.misctweaks.util.ModUtil;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

@Mod(MiscTweaks.MOD_ID)
@EventBusSubscriber(modid = MiscTweaks.MOD_ID)
public class NeoforgeEntrypoint {

	@SubscribeEvent
	private static void onCommonSetup(FMLCommonSetupEvent event) {
        MiscTweaks.onInitialize();
	}

    @SubscribeEvent
    private static void onLevelLoad(LevelEvent.Load event) {
        ModUtil.onLevelLoad(event.getLevel());
    }
}
*///?}
