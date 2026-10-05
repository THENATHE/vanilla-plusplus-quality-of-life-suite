package me.pajic.toolpouch.mixin.client;

import me.pajic.toolpouch.renderer.HumanoidRenderStateExtension;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HumanoidRenderState.class)
public class HumanoidRenderStateMixin implements HumanoidRenderStateExtension {

	@Unique private ItemStackTemplate toolpouch$elytra = null;

	@Override
	public ItemStackTemplate toolpouch$getElytra() {
		return toolpouch$elytra;
	}

	@Override
	public void toolpouch$setElytra(ItemStackTemplate stack) {
		toolpouch$elytra = stack;
	}
}
