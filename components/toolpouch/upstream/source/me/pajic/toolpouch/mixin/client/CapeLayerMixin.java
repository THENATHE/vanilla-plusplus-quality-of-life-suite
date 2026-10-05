package me.pajic.toolpouch.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.toolpouch.renderer.HumanoidRenderStateExtension;
import me.pajic.toolpouch.util.ItemStackTemplateUtil;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.ItemStackTemplate;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CapeLayer.class)
public class CapeLayerMixin {

	@ModifyExpressionValue(
			method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;showCape:Z",
					opcode = Opcodes.GETFIELD
			)
	)
	private boolean hideCapeIfPouchElytraDisplayed(
			boolean original,
			@Local(argsOnly = true, name = "state") final AvatarRenderState state
	) {
		ItemStackTemplate elytra = ((HumanoidRenderStateExtension) state).toolpouch$getElytra();
		return original && (elytra == null || ItemStackTemplateUtil.isEmpty(elytra));
	}
}
