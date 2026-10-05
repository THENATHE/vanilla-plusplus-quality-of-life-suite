package me.pajic.misctweaks.platform.neoforge;

//? neoforge {

/*import me.pajic.misctweaks.MiscTweaks;
import me.pajic.misctweaks.util.ModUtil;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

//? <26.1 {
/^import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.event.AddPackFindersEvent;
^///?}

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

    //? <26.1 {
    /^@SubscribeEvent
    private static void initCommonResources(AddPackFindersEvent event) {
		if (MiscTweaks.CONFIG.craftableSaddleBackport.get()) event.addPackFinders(
                MiscTweaks.id("resourcepacks/craftable_saddle"),
                PackType.SERVER_DATA,
                Component.literal("Craftable saddle"),
                PackSource.BUILT_IN,
                true,
                Pack.Position.TOP
        );
    }
    ^///?}
}
*///?}
