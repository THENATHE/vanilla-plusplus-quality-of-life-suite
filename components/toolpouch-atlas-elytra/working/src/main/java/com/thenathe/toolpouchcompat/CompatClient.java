package com.thenathe.toolpouchcompat;
import com.mojang.blaze3d.platform.InputConstants;
import me.pajic.toolpouch.keybind.ModKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
public class CompatClient implements ClientModInitializer {
 public static KeyMapping TOGGLE_ELYTRA;
 public void onInitializeClient() {
  if (FabricLoader.getInstance().isModLoaded("mapstitch")) ClientLifecycleEvents.CLIENT_STARTED.register(client -> HudLayout.register());
  if (Features.NATIVE_ELYTRA) return;
  // The controls screen groups categories by identity, even when their IDs match.
  KeyMapping.Category category = ModKeybinds.MOD_KEYS;
  KeyMapping toggle = TOGGLE_ELYTRA = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.toolpouch.toggle_elytra", InputConstants.UNKNOWN.getValue(), category));
  ClientPlayNetworking.registerGlobalReceiver(Payloads.State.TYPE, (payload, context) -> {
   if (context.player() instanceof ElytraPreference preference) preference.toolpouchCompat$setElytraEnabled(payload.enabled());
  });
  ClientTickEvents.END_CLIENT_TICK.register(client -> {
   while (toggle.consumeClick()) if (client.player != null && ClientPlayNetworking.canSend(Payloads.Toggle.TYPE)) ClientPlayNetworking.send(new Payloads.Toggle());
  });
 }
}
