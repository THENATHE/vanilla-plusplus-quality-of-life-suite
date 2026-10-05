package com.thenathe.multiscale.client;

import com.thenathe.multiscale.AtlasTarget;
import com.thenathe.multiscale.MixedScales;
import com.thenathe.suite.network.SuiteCapabilities;
import me.pajic.mapstitch.MapStitch;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public final class MixedScalesClient {
    private static AtlasTarget pendingUse;
    private static long pendingUseTime;
    private MixedScalesClient() {}

    public static void rememberUse(ItemStack atlas) {
        var player = Minecraft.getInstance().player;
        pendingUse = player == null ? null : AtlasTarget.find(player, atlas);
        pendingUseTime = System.nanoTime();
    }

    public static AtlasTarget openTarget(int scaleOverride) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        var used = pendingUse;
        pendingUse = null;
        if (scaleOverride >= 0 && used != null && System.nanoTime() - pendingUseTime < 5_000_000_000L && used.resolve(mc.player) != null) return used;
        var minimap = AtlasTarget.firstForScan(mc.player, MapStitch.CONFIG.itemRequirements.minimapAtlasScan);
        return minimap != null ? minimap
                : AtlasTarget.firstForScan(mc.player, MapStitch.CONFIG.itemRequirements.worldMapAtlasScan);
    }

    public static void select(AtlasTarget target, int scale) {
        var mc = Minecraft.getInstance();
        if (target != null && mc.player != null && mc.getConnection() != null
                && SuiteCapabilities.isNative(mc.getConnection().getPacketContext(), "mapstitch")
                && ClientPlayNetworking.canSend(MixedScales.SelectScale.TYPE)) {
            // Ejection or first-map generation can change the anchor while this screen stays open.
            // Keep the screen bound to its original location, but validate its current contents.
            for (var current : AtlasTarget.all(mc.player)) {
                if (current.location() == target.location() && current.index() == target.index()) {
                    ClientPlayNetworking.send(new MixedScales.SelectScale(current, scale));
                    return;
                }
            }
        }
    }
}
