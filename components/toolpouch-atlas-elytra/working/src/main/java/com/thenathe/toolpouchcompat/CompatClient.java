package com.thenathe.toolpouchcompat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
public class CompatClient implements ClientModInitializer {
 public void onInitializeClient() {
  if (FabricLoader.getInstance().isModLoaded("mapstitch")) ClientLifecycleEvents.CLIENT_STARTED.register(client -> HudLayout.register());
  // The suite's own Elytra key is gone; Tool Pouch's slot-toggle binds replace it.
  LegacyElytraKey.initialize();
 }
}
