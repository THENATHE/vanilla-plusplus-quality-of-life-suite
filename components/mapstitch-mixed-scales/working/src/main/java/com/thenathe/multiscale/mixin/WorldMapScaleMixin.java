package com.thenathe.multiscale.mixin;

import com.thenathe.multiscale.AtlasTarget;
import com.thenathe.multiscale.MixedScaleMaps;
import com.thenathe.multiscale.client.MixedScalesClient;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WorldMapScreen.class, remap = false)
public abstract class WorldMapScaleMixin {
    @Shadow private int scale;
    @Unique private int mixedScales$previous;
    @Unique private AtlasTarget mixedScales$target;

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Tooltip;create(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/client/gui/components/Tooltip;", ordinal = 3), index = 0)
    private Component mixedScales$scaleHelp(Component original) {
        return mixedScales$target == null ? original : Component.translatable("mapstitch_mixed_scales.screen.scale_help");
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mixedScales$bindBook(int scaleOverride, CallbackInfo ci) {
        mixedScales$target = MixedScalesClient.openTarget(scaleOverride);
        var player = Minecraft.getInstance().player;
        if (scaleOverride < 0 && player != null && mixedScales$target != null) {
            var handle = mixedScales$target.resolve(player);
            if (handle != null) scale = MixedScaleMaps.activeScale(handle.atlas());
        }
        mixedScales$previous = scale;
    }

    @Unique private void mixedScales$changed() {
        if (scale == mixedScales$previous) return;
        mixedScales$previous = scale;
        // Upstream caches decorations across renders. Switching layers must not
        // leave an old scale's banner/player markers at the new grid positions.
        WorldMapScreen.clearMaps();
        MixedScalesClient.select(mixedScales$target, scale);
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void mixedScales$renderScale(GuiGraphicsExtractor graphics, int x, int y, float delta, CallbackInfo ci) { mixedScales$changed(); }

    @Inject(method = {"keyPressed", "mouseClicked"}, at = @At("RETURN"))
    private void mixedScales$inputScale(CallbackInfoReturnable<Boolean> cir) { mixedScales$changed(); }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void mixedScales$lastScale(CallbackInfo ci) { mixedScales$changed(); }
}
