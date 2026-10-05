package com.thenathe.suite.client.mixin;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.thenathe.suite.client.SuiteSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Redirect the original Chalk button while retaining Chalk's actual saved configuration. */
@Mixin(targets = "de.dafuqs.chalk.client.ModMenuConfig", remap = false)
public abstract class ChalkModMenuMixin {
    @Inject(method = "getModConfigScreenFactory", at = @At("HEAD"), cancellable = true, remap = false)
    private void suite$chalkSettings(CallbackInfoReturnable<ConfigScreenFactory<?>> callback) {
        callback.setReturnValue(SuiteSettings::createChalk);
    }
}
