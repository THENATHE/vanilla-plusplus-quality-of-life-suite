package com.thenathe.toolpouchcompat.mixin;

import com.thenathe.toolpouchcompat.LegacyElytraToggle;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Death and dimension changes create a new player object; keep a not-yet-applied Elytra flag. */
@Mixin(ServerPlayer.class)
public abstract class LegacyElytraRespawnMixin {
    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void toolpouchCompat$copyPending(ServerPlayer oldPlayer, boolean keepEverything, CallbackInfo ci) {
        ((LegacyElytraToggle.Pending) this).toolpouchCompat$setLegacyElytraOff(
                ((LegacyElytraToggle.Pending) oldPlayer).toolpouchCompat$legacyElytraOff());
    }
}
