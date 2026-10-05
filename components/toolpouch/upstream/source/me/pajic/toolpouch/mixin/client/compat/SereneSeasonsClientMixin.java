package me.pajic.toolpouch.mixin.client.compat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.pajic.toolpouch.ToolPouch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import sereneseasons.init.ModClient;

@IfModLoaded("sereneseasons")
@Mixin(ModClient.class)
public class SereneSeasonsClientMixin {

	@ModifyExpressionValue(
			method = "onItemTooltip",
			at = @At(
					value = "INVOKE",
					target = "Lsereneseasons/config/SeasonsConfig;isDimensionWhitelisted(Lnet/minecraft/resources/ResourceKey;)Z"
			)
	)
	private static boolean hideCalendarTooltip(boolean original) {
		return !ToolPouch.CONFIG.infoOverlaySettings.hideCalendarTooltip.get() && original;
	}
}
