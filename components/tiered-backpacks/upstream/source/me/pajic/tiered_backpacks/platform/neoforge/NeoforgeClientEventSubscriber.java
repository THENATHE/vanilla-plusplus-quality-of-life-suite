package me.pajic.tiered_backpacks.platform.neoforge;

//? neoforge {

/*import me.pajic.tiered_backpacks.TieredBackpacks;
import me.pajic.tiered_backpacks.keybind.ModKeybinds;
import me.pajic.tiered_backpacks.menu.ModMenuTypes;
import me.pajic.tiered_backpacks.ui.BackpackScreen;
import me.pajic.tiered_backpacks.util.BackpackClientUtil;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TieredBackpacks.MOD_ID, value = Dist.CLIENT)
public class NeoforgeClientEventSubscriber {

	@SubscribeEvent
	public static void onClientSetup(final FMLCommonSetupEvent event) {
        ModKeybinds.init();
	}

    @SubscribeEvent
    private static void initMenuTypes(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.BACKPACK_MENU, BackpackScreen::new);
    }

    @SubscribeEvent
    private static void initKeybinds(RegisterKeyMappingsEvent event) {
        event.registerCategory(ModKeybinds.MOD_KEYS);
        event.register(ModKeybinds.OPEN_BACKPACK);
    }

    @SubscribeEvent
    private static void onClientTick(ClientTickEvent.Post event) {
        BackpackClientUtil.onClientTick(Minecraft.getInstance());
    }
}
*///?}
