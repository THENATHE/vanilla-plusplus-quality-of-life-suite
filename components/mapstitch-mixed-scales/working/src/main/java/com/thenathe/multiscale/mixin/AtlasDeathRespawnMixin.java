package com.thenathe.multiscale.mixin;

import com.thenathe.multiscale.AtlasDeathRetention;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class AtlasDeathRespawnMixin {
    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void suite$restoreNestedAtlases(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        AtlasDeathRetention.transfer(oldPlayer, (ServerPlayer) (Object) this);
    }
}
