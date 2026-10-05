package me.pajic.toolpouch.mixin.client;

import me.pajic.toolpouch.renderer.AvatarRenderStateExtension;
import me.pajic.toolpouch.util.ClientUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@SuppressWarnings("NullableProblems")
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin<T1 extends Avatar & ClientAvatarEntity> extends LivingEntityRenderer<T1, AvatarRenderState, PlayerModel> {

	@Unique private static BlockModelResolver toolpouch$blockModelResolver;

	public AvatarRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadow) {
		super(context, model, shadow);
	}

	@Inject(
			method = "<init>",
			at = @At("TAIL")
	)
	private static void onInit(EntityRendererProvider.Context context, boolean slimSteve, CallbackInfo ci) {
        toolpouch$blockModelResolver = context.getBlockModelResolver();
	}

	@Inject(
			method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
			at = @At("HEAD")
	)
	private <T2 extends Avatar & ClientAvatarEntity> void extendRenderState(
			T2 entity, AvatarRenderState state, float partialTicks, CallbackInfo ci
	) {
		if (entity instanceof Player player) {
			List<ItemStackTemplate> lanterns = ToolPouchUtil.getItemsFromToolPouch(player, ClientUtil.getSupportedLanterns());
			ItemStackTemplate lantern = lanterns.isEmpty() ? null : lanterns.getFirst();
			if (lantern != null) toolpouch$blockModelResolver.update(
					((AvatarRenderStateExtension) state).toolpouch$getLantern(),
					Block.byItem(lantern.item().value()).defaultBlockState(),
					BlockDisplayContext.create()
			);
		}
	}
}
