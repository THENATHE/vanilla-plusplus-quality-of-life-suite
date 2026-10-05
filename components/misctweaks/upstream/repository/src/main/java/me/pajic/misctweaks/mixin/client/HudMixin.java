package me.pajic.misctweaks.mixin.client;

//? >26.1.2 {

import me.pajic.misctweaks.util.ModClientUtil;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

//? fabric {
import me.pajic.misctweaks.MiscTweaksClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}

@Mixin(Hud.class)
public class HudMixin {

    //? fabric {
	@Inject(
			method = "extractHotbarAndDecorations",
			at = @At("HEAD")
	)
	private void raiseHotbarStart(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (ModClientUtil.shouldRaiseHotbar()) {
			graphics.pose().pushMatrix();
			graphics.pose().translate(0, -MiscTweaksClient.CONFIG.raiseHotbarPixels.get());
		}
	}

	@Inject(
			method = "extractHotbarAndDecorations",
			at = @At("TAIL")
	)
	private void raiseHotbarEnd(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (ModClientUtil.shouldRaiseHotbar()) graphics.pose().popMatrix();
	}
    //?}

	@ModifyArg(
			method = "extractItemHotbar",
			at = @At(
					value = "INVOKE",
                    //~ if >26.2 'blaze3d' -> 'renderpearl/api'
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
					ordinal = 1
			),
			index = 5
	)
	private int fixHotbarSelector(int height) {
		return ModClientUtil.shouldRaiseHotbar() ? height + 1 : height;
	}
}
//?}
