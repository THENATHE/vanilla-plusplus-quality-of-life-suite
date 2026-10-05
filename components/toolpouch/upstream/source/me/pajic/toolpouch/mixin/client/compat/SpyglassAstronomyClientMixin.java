package me.pajic.toolpouch.mixin.client.compat;

//? if fabric {

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import me.pajic.toolpouch.util.ClientUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded("spyglass_astronomy")
@Mixin(SpyglassAstronomyClient.class)
public class SpyglassAstronomyClientMixin {

	@Shadow
	public static Minecraft client;

	@ModifyExpressionValue(
			method = "update",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"
			)
	)
	private static boolean checkAccessoryScoping(boolean original) {
		return original || ClientUtil.shouldScope;
	}

	@ModifyReturnValue(
			method = "isntHoldingSpyglass",
			at = @At(
					value = "RETURN",
					ordinal = 1
			)
	)
	private static boolean checkAccessorySlot(boolean original) {
		return original && !ToolPouchUtil.toolPouchHasItem(client.player, stack -> stack.is(Items.SPYGLASS));
	}
}
//?}
