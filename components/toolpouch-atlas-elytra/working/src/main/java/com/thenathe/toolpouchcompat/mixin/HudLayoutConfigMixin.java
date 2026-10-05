package com.thenathe.toolpouchcompat.mixin;

import com.thenathe.toolpouchcompat.HudLayout;
import me.fzzyhmstrs.fzzy_config.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Synchronize the other native file when a user applies either original config. */
@Mixin(value = Config.class, remap = false)
public abstract class HudLayoutConfigMixin {
    @Inject(method = "onUpdateClient", at = @At("TAIL"))
    private void suite$appliedPreferences(CallbackInfo ci) {
        HudLayout.configApplied((Config) (Object) this);
    }
}
