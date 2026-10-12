package com.thenathe.suite.features;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public final class CoreFeatures {
    private CoreFeatures() {}

    public static void initialize() {
        UseBlockCallback.EVENT.register(StructurePlacement::use);
        PlayerBlockBreakEvents.AFTER.register(StructurePlacement::dismantle);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerList().getPlayers()) NetheriteInstamine.update(player);
        });
    }
}
