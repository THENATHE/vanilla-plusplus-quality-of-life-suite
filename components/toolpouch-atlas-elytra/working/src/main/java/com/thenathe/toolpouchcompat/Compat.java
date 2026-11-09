package com.thenathe.toolpouchcompat;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.util.AllowedItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import java.nio.file.*;
import java.util.*;
public class Compat implements ModInitializer {
 public void onInitialize() {
  migrateAtlasRule();
  LegacyElytraToggle.initialize();
  // Players without the client mod cannot use Tool Pouch's slot-toggle binds; this toggles the same disabled slots.
  CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> dispatcher.register(
   Commands.literal("toolpouch-elytra").executes(context -> { toggle(context.getSource().getPlayerOrException()); return 1; })
    .then(Commands.literal("on").executes(context -> PouchElytraToggle.set(context.getSource().getPlayerOrException(), true)))
    .then(Commands.literal("off").executes(context -> PouchElytraToggle.set(context.getSource().getPlayerOrException(), false)))
  ));
 }
 public static void toggle(ServerPlayer player) { PouchElytraToggle.set(player, !PouchElytraToggle.enabled(player)); }
 private static void migrateAtlasRule() {
  Path marker = FabricLoader.getInstance().getConfigDir().resolve("toolpouch-atlas-elytra-compat-migrated-v1");
  if (Files.exists(marker)) return;
  if (!Features.ATLAS_ALREADY_MIGRATED) { migrateAtlasRule(ToolPouch.CONFIG); ToolPouch.CONFIG.save(); }
  try { Files.createDirectories(marker.getParent()); Files.writeString(marker, "One-time atlas rule migration completed. Later administrator removals are preserved.\n"); }
  catch (java.io.IOException e) { throw new IllegalStateException("Could not record atlas migration", e); }
 }
 public static void migrateAtlasRule(me.pajic.toolpouch.config.ModConfig config) {
  var allowed = config.allowedItems;
  if (allowed.stream().noneMatch(a -> a.id.get().equals("mapstitch:atlas"))) {
   allowed.stream().filter(a -> a.id.get().equals("improved-maps:atlas")).findFirst().ifPresent(old -> {
    var updated = new LinkedHashSet<AllowedItem>(allowed.get());
    updated.add(new AllowedItem("mapstitch:atlas", old.maxStackSize.get(), old.maxStackCount.get()));
    allowed.accept(updated);
   });
  }
 }

}
