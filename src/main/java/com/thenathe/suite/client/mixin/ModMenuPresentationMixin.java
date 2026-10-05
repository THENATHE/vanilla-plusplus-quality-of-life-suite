package com.thenathe.suite.client.mixin;

import com.thenathe.suite.client.SuiteModPresentation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mod Menu views only: never changes Fabric's containers or bundled metadata. */
@Mixin(targets = "com.terraformersmc.modmenu.util.mod.fabric.FabricMod", remap = false)
public abstract class ModMenuPresentationMixin {
    @Shadow public abstract String getId();

    @Inject(method = "getParent", at = @At("HEAD"), cancellable = true, remap = false)
    private void suite$groupComponents(CallbackInfoReturnable<String> callback) {
        String id = getId();
        if (SuiteModPresentation.isSuiteChild(id)) {
            callback.setReturnValue(SuiteModPresentation.SUITE_ID);
        } else if (SuiteModPresentation.isOriginalFeature(id)) {
            // Some original jars have generated nested-library parents. Keep real features visible.
            callback.setReturnValue(null);
        }
    }

    @Inject(method = "isHidden", at = @At("HEAD"), cancellable = true, remap = false)
    private void suite$hideColorfulAddon(CallbackInfoReturnable<Boolean> callback) {
        if (SuiteModPresentation.isHiddenAddon(getId())) callback.setReturnValue(true);
    }
}
