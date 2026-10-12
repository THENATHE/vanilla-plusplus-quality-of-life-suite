package com.thenathe.suite.client.mixin;

import com.thenathe.suite.client.SuiteFeatureEntries;
import com.terraformersmc.modmenu.ModMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds feature descriptions after Mod Menu has built its real loader tree. */
@Mixin(value = ModMenu.class, remap = false)
public abstract class ModMenuFeatureEntriesMixin {
    @Inject(method = "onInitializeClient", at = @At("TAIL"), remap = false)
    private void suite$registerFeatures(CallbackInfo callback) {
        SuiteFeatureEntries.register();
    }
}
