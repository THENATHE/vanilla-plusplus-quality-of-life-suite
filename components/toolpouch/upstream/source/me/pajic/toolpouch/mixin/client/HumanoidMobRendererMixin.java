package me.pajic.toolpouch.mixin.client;

import me.pajic.toolpouch.renderer.HumanoidRenderStateExtension;
import me.pajic.toolpouch.util.ItemStackTemplateUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidMobRenderer.class)
public class HumanoidMobRendererMixin {

	@Inject(
			method = "extractHumanoidRenderState",
			at = @At("TAIL")
	)
	private static void extractToolPouchElytra(
			LivingEntity entity,
			HumanoidRenderState state,
			float partialTicks,
			ItemModelResolver itemModelResolver,
			CallbackInfo ci
	) {
		if (entity instanceof Player player) {
			ItemStackTemplate elytra = ToolPouchUtil.getElytraFromToolPouch(player, true);
			if (elytra != null && !ItemStackTemplateUtil.isEmpty(elytra)) ((HumanoidRenderStateExtension) state).toolpouch$setElytra(elytra);
		}
	}
}
