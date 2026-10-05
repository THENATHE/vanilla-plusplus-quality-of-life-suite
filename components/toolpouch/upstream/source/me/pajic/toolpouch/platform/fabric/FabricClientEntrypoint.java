package me.pajic.toolpouch.platform.fabric;

//? fabric {

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.ToolPouchClient;
import me.pajic.toolpouch.hud.ContextualSelectionWidget;
import me.pajic.toolpouch.hud.InfoOverlays;
import me.pajic.toolpouch.hud.MinimapOverlay;
import me.pajic.toolpouch.keybind.ModKeybinds;
import me.pajic.toolpouch.menu.ModMenuTypes;
import me.pajic.toolpouch.menu.ToolPouchScreen;
import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.network.NetworkClientEvents;
import me.pajic.toolpouch.renderer.PlayerLanternLayer;
import me.pajic.toolpouch.util.ItemSuggestions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ToolPouchClient.onInitialize();
        MenuScreens.register(ModMenuTypes.TOOL_POUCH_MENU, ToolPouchScreen::new);
        KeyMapping.Category.register(ToolPouch.id("keys"));
        KeyMappingHelper.registerKeyMapping(ModKeybinds.OPEN_TOOL_POUCH);
        KeyMappingHelper.registerKeyMapping(ModKeybinds.OPEN_WIDGET);
        KeyMappingHelper.registerKeyMapping(ModKeybinds.OPEN_ENDER_CHEST);
        KeyMappingHelper.registerKeyMapping(ModKeybinds.USE_SPYGLASS);
        KeyMappingHelper.registerKeyMapping(ModKeybinds.TOGGLE_MINIMAP);
        ClientTickEvents.END_CLIENT_TICK.register(ModKeybinds::onClientTick);
        ClientLifecycleEvents.CLIENT_STARTED.register(ModKeybinds::onClientStarted);
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((_, level) -> ItemSuggestions.update(level));
        ClientPlayNetworking.registerGlobalReceiver(
                ModPayloads.S2CSyncShulkerSlot.TYPE,
                (payload, _) -> NetworkClientEvents.syncShulkerSlotToClient(payload.slot())
        );
        ClientPlayNetworking.registerGlobalReceiver(
                ModPayloads.S2CSyncArrowSlot.TYPE,
                (payload, _) -> NetworkClientEvents.syncArrowSlotToClient(payload.slot())
        );
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.MOB_EFFECTS,
                ToolPouch.id("info_overlay"),
                (context, _) -> InfoOverlays.render(context)
        );
        HudElementRegistry.addLast(
                ToolPouch.id("contextual_widget"),
                (context, _) -> ContextualSelectionWidget.render(context)
        );
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.MOB_EFFECTS,
                ToolPouch.id("minimap_overlay"),
                (context, _) -> MinimapOverlay.render(context)
        );
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((_, entityRenderer, registrationHelper, _) -> {
            if (entityRenderer instanceof AvatarRenderer<?> avatarRenderer) {
                registrationHelper.register(new PlayerLanternLayer(avatarRenderer));
            }
        });
	}
}
//?}
