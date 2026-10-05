package me.pajic.toolpouch.renderer;

import net.minecraft.world.item.ItemStackTemplate;

public interface HumanoidRenderStateExtension {
	ItemStackTemplate toolpouch$getElytra();
	void toolpouch$setElytra(ItemStackTemplate stack);
}
