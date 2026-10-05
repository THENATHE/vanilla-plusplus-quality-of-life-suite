package me.pajic.misctweaks.mixin.client;

//? <=26.1.2 {

/*import me.pajic.misctweaks.util.ModClientUtil;
import net.minecraft.client.gui.Gui;
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

@Mixin(Gui.class)
public class GuiMixin {

    //? fabric {
	@Inject(
			method = {"extractHotbarAndDecorations", "renderHotbarAndDecorations"},
			require = 1,
			at = @At("HEAD")
	)
	private void raiseHotbarStart(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (ModClientUtil.shouldRaiseHotbar()) {
            //~ if <26.1 'pushMatrix' -> 'pushPose'
			graphics.pose().pushMatrix();
			graphics.pose().translate(0, -MiscTweaksClient.CONFIG.raiseHotbarPixels.get()/^? <26.1 {^//^, 0^//^?}^/);
		}
	}

	@Inject(
			method = {"extractHotbarAndDecorations", "renderHotbarAndDecorations"},
			require = 1,
			at = @At("TAIL")
	)
	private void raiseHotbarEnd(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        //~ if <26.1 'popMatrix' -> 'popPose'
		if (ModClientUtil.shouldRaiseHotbar()) graphics.pose().popMatrix();
	}
    //?}

	@ModifyArg(
			method = {"extractItemHotbar", "renderItemHotbar"},
			at = @At(
					value = "INVOKE",
                    //? >=26.1
					target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
                    //? <26.1
                    //target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lnet/minecraft/resources/Identifier;IIII)V",
					ordinal = 1
			),
            //~ if <26.1 '5' -> '4'
			index = 5
	)
	private int fixHotbarSelector(int height) {
		return ModClientUtil.shouldRaiseHotbar() ? height + 1 : height;
	}
}
*///?}
