package com.thenathe.toolpouchcompat;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.util.AllowedItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.nio.file.*;
import java.util.*;
public class Compat implements ModInitializer {
 private static final Map<UUID, Boolean> SENT = new HashMap<>();
 public void onInitialize() {
  migrateAtlasRule();
  if (!Features.NATIVE_ELYTRA) {
   PayloadTypeRegistry.serverboundPlay().register(Payloads.Toggle.TYPE, Payloads.Toggle.CODEC);
   PayloadTypeRegistry.clientboundPlay().register(Payloads.State.TYPE, Payloads.State.CODEC);
   ServerPlayNetworking.registerGlobalReceiver(Payloads.Toggle.TYPE, (payload, context) -> toggle(context.player()));
   ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> SENT.remove(handler.player.getUUID()));
   ServerTickEvents.END_SERVER_TICK.register(server -> {
    for (ServerPlayer p : server.getPlayerList().getPlayers()) {
     boolean enabled = enabled(p);
     if (ServerPlayNetworking.canSend(p, Payloads.State.TYPE) && !Objects.equals(SENT.get(p.getUUID()), enabled)) {
      ServerPlayNetworking.send(p, new Payloads.State(enabled)); SENT.put(p.getUUID(), enabled);
     }
    }
   });
   net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> SENT.remove(player.getUUID()));
   net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, player, alive) -> SENT.remove(player.getUUID()));
  }
  CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> dispatcher.register(
   Commands.literal("toolpouch-elytra").executes(context -> { toggle(context.getSource().getPlayerOrException()); return 1; })
    .then(Commands.literal("on").executes(context -> { set(context.getSource().getPlayerOrException(), true); return 1; }))
    .then(Commands.literal("off").executes(context -> { set(context.getSource().getPlayerOrException(), false); return 1; }))
  ));
 }
 public static boolean enabled(net.minecraft.world.entity.player.Player player) {
  if (player instanceof ElytraPreference preference) return preference.toolpouchCompat$elytraEnabled();
  try { return (boolean) player.getClass().getMethod("toolpouch$isElytraEnabled").invoke(player); }
  catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
 }
 public static void toggle(ServerPlayer player) { set(player, !enabled(player)); }
 public static void set(ServerPlayer player, boolean value) {
  if (player instanceof ElytraPreference preference) preference.toolpouchCompat$setElytraEnabled(value);
  else try { player.getClass().getMethod("toolpouch$setElytraEnabled", boolean.class).invoke(player, value); }
  catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
  player.sendSystemMessage(Component.literal("Tool Pouch Elytra " + (value ? "enabled" : "disabled")), true);
 }
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
