package me.pajic.toolpouch.mixin.client;

import me.pajic.toolpouch.renderer.AvatarRenderStateExtension;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements AvatarRenderStateExtension {

	@Unique private final BlockModelRenderState toolpouch$lantern = new BlockModelRenderState();

	@Override
	public BlockModelRenderState toolpouch$getLantern() {
		return toolpouch$lantern;
	}
}
