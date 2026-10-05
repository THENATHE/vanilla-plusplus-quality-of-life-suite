package me.pajic.misctweaks.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireBlock.class)
public interface FireBlockInvoker {

	@Invoker("setFlammable")
	void mt$invokeSetFlammable(final Block block, final int igniteOdds, final int burnOdds);
}
