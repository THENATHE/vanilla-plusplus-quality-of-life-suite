package com.thenathe.suite.features.mixin;

import com.thenathe.suite.features.FeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.Enderman$EndermanTakeBlockGoal")
public abstract class EndermanPickupMixin {
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void suite$preventNewPickup(CallbackInfoReturnable<Boolean> callback) {
        if (FeatureConfig.CONFIG.noEndermanGrief.get()) callback.setReturnValue(false);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void suite$stopPendingPickup(CallbackInfo callback) {
        if (FeatureConfig.CONFIG.noEndermanGrief.get()) callback.cancel();
    }
}
