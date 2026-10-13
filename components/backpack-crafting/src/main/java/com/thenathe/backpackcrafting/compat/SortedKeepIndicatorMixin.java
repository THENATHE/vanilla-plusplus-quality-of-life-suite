package com.thenathe.backpackcrafting.compat;

import com.thenathe.backpackcrafting.client.SortedRuleContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.sorted.client.KeepIndicator", remap = false)
public abstract class SortedKeepIndicatorMixin {
    @Inject(method = {"revealing", "altHeld"}, at = @At("HEAD"), cancellable = true)
    private static void backpackCrafting$craftingRules(CallbackInfoReturnable<Boolean> cir) {
        if (SortedRuleContext.isSuppressed()) {
            cir.setReturnValue(false);
        }
    }
}
