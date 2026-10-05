package me.pajic.misctweaks.platform.neoforge;

//? neoforge {

/*import me.pajic.misctweaks.MiscTweaks;
import me.pajic.misctweaks.MiscTweaksClient;
import me.pajic.misctweaks.util.ModClientUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = MiscTweaks.MOD_ID, value = Dist.CLIENT)
public class NeoforgeClientEventSubscriber {

	@SubscribeEvent
	public static void onClientSetup(final FMLCommonSetupEvent event) {
        MiscTweaksClient.onInitialize();
	}

    @SubscribeEvent
    public static void raiseHotbarStart(RenderGuiLayerEvent.Pre event) {
        if (ModClientUtil.shouldRaiseHotbar() && event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            //~ if <26.1 'pushMatrix' -> 'pushPose'
            event.getGuiGraphics().pose().pushMatrix();
            event.getGuiGraphics().pose().translate(0, -MiscTweaksClient.CONFIG.raiseHotbarPixels.get()/^? <26.1 {^//^, 0^//^?}^/);
        }
    }

    @SubscribeEvent
    public static void raiseHotbarEnd(RenderGuiLayerEvent.Post event) {
        //~ if <26.1 'SPECTATOR_TOOLTIP' -> 'EXPERIENCE_LEVEL'
        if (ModClientUtil.shouldRaiseHotbar() && event.getName().equals(VanillaGuiLayers.SPECTATOR_TOOLTIP)) {
            //~ if <26.1 'popMatrix' -> 'popPose'
            event.getGuiGraphics().pose().popMatrix();
        }
    }
}
*///?}
