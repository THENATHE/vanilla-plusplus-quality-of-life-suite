package me.pajic.toolpouch.platform.neoforge;

//? neoforge {

/*import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.ToolPouchClient;
import me.pajic.toolpouch.hud.ContextualSelectionWidget;
import me.pajic.toolpouch.hud.InfoOverlays;
import me.pajic.toolpouch.hud.MinimapOverlay;
import me.pajic.toolpouch.keybind.ModKeybinds;
import me.pajic.toolpouch.menu.ModMenuTypes;
import me.pajic.toolpouch.menu.ToolPouchScreen;
import me.pajic.toolpouch.renderer.PlayerLanternLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = ToolPouch.MOD_ID, value = Dist.CLIENT)
public class NeoforgeClientEventSubscriber {

	@SubscribeEvent
	public static void onClientSetup(final FMLCommonSetupEvent event) {
        ToolPouchClient.onInitialize();
	}

    @SubscribeEvent
    private static void initToolPouchMenuScreen(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.TOOL_POUCH_MENU, ToolPouchScreen::new);
    }

    @SubscribeEvent
    private static void initKeybinds(RegisterKeyMappingsEvent event) {
        event.registerCategory(ModKeybinds.MOD_KEYS);
        event.register(ModKeybinds.OPEN_TOOL_POUCH);
        event.register(ModKeybinds.OPEN_ENDER_CHEST);
        event.register(ModKeybinds.OPEN_WIDGET);
        event.register(ModKeybinds.USE_SPYGLASS);
        event.register(ModKeybinds.TOGGLE_MINIMAP);
    }

    @SubscribeEvent
    private static void initOnClientTick(ClientTickEvent.Post event) {
        ModKeybinds.onClientTick(Minecraft.getInstance());
    }

    @SubscribeEvent
    private static void initOnClientStart(ClientStartedEvent event) {
        ModKeybinds.onClientStarted(Minecraft.getInstance());
    }

    @SubscribeEvent
    private static void initHudLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(
                VanillaGuiLayers.EFFECTS,
                ToolPouch.id("info_overlay"),
                (context, _) -> InfoOverlays.render(context)
        );
        event.registerAboveAll(
                ToolPouch.id("contextual_widget"),
                (context, _) -> ContextualSelectionWidget.render(context)
        );
        event.registerBelow(
                VanillaGuiLayers.EFFECTS,
                ToolPouch.id("minimap_overlay"),
                (context, _) -> MinimapOverlay.render(context)
        );
    }

    @SuppressWarnings("DataFlowIssue")
    @SubscribeEvent
    private static void initPlayerLanternRenderer(EntityRenderersEvent.AddLayers event) {
        event.getSkins().forEach(skin -> {
            AvatarRenderer<?> renderer = event.getPlayerRenderer(skin);
            renderer.addLayer(new PlayerLanternLayer(renderer));
        });
    }
}
*///?}
