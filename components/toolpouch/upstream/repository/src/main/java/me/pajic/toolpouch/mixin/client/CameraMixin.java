package me.pajic.toolpouch.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.pajic.toolpouch.ToolPouchClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin {

	@Shadow @Final private Minecraft minecraft;
	@Shadow private float fovModifier;

	@ModifyExpressionValue(
			method = "tickFov",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/Mth;clamp(FFF)F"
			)
	)
	private float uncapSpyglassZoomLevel(float original) {
		if (ToolPouchClient.CONFIG.scrollableZoom.get() && minecraft.player != null && minecraft.player.isScoping()) {
			return fovModifier;
		}
		return original;
	}
}
