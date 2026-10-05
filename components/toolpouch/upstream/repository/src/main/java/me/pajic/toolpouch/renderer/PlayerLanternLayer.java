package me.pajic.toolpouch.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class PlayerLanternLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

	public PlayerLanternLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
		super(renderer);
	}

	@Override
	public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector nodeCollector, int packedLight, AvatarRenderState renderState, float yRot, float xRot) {
		poseStack.pushPose();
		Vec3 offset = !renderState.chestEquipment.isEmpty() || !renderState.legsEquipment.isEmpty() ?
				new Vec3(-0.05F, 0.75F, 0F) : new Vec3(-0.05F, 0.75F, 0.05F);
		getParentModel().body.translateAndRotate(poseStack);
		poseStack.translate(offset);
		poseStack.scale(0.5F, 0.5F, 0.5F);
        //~ if >26.2 'mulPose(Axis.XP.rotationDegrees(180))' -> 'rotateDegrees(Axis.XP, 180)'
        poseStack.rotateDegrees(Axis.XP, 180);
		((AvatarRenderStateExtension) renderState).toolpouch$getLantern().submit(poseStack, nodeCollector, packedLight, OverlayTexture.NO_OVERLAY, renderState.outlineColor);
		poseStack.popPose();
	}
}
