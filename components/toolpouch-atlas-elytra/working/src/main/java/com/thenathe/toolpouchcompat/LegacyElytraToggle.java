package com.thenathe.toolpouchcompat;

import me.pajic.toolpouch.util.GameplayUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Suites up to 1.5.1 kept a per-player "pouch Elytra off" flag in player data
 * (ToolPouchElytraEnabled = false). It is carried over to Tool Pouch's disabled slots the first
 * time the player has a pouch with an Elytra, and kept in player data until then, so nobody who
 * had turned it off starts gliding after the update.
 */
public final class LegacyElytraToggle {
    public static final String TAG = "ToolPouchElytraEnabled";

    /** Implemented on players by LegacyElytraToggleMixin. */
    public interface Pending {
        boolean toolpouchCompat$legacyElytraOff();
        void toolpouchCompat$setLegacyElytraOff(boolean off);
    }

    private LegacyElytraToggle() {}

    public static void initialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> apply(handler.player));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) apply(player);
        });
    }

    public static void apply(ServerPlayer player) {
        if (!(player instanceof Pending pending) || !pending.toolpouchCompat$legacyElytraOff()) return;
        // The old flag covered every pouch the player carried, attached or loose.
        int disabled = PouchElytraToggle.setSlots(PouchElytraToggle.activePouch(player), false);
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);
            if (GameplayUtil.isValidContainerHolder(stack)) disabled += PouchElytraToggle.setSlots(stack, false);
        }
        var legs = player.getItemBySlot(EquipmentSlot.LEGS);
        if (GameplayUtil.isValidContainerHolder(legs)) disabled += PouchElytraToggle.setSlots(legs, false);
        boolean anyGlider = !PouchElytraToggle.gliderSlots(PouchElytraToggle.activePouch(player)).isEmpty();
        if (disabled > 0 || anyGlider) pending.toolpouchCompat$setLegacyElytraOff(false);
    }
}
