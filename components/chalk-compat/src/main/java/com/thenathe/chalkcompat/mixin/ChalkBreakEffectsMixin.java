package com.thenathe.chalkcompat.mixin;

import de.dafuqs.chalk.common.blocks.ChalkMarkBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Minecraft 26.3 split the destroy effect into a player-break hook and a destroyBlock hook.
 * Official Chalk 3.2.0+26.3 only overrides the second, so breaking a mark by hand fell back to
 * the generic block-break effect. Route both through Chalk's own sound and cloud effect, as
 * Chalk did on 26.2 and the suite's earlier 26.3 port did.
 */
@Mixin(ChalkMarkBlock.class)
public abstract class ChalkBreakEffectsMixin extends Block {
    private ChalkBreakEffectsMixin(Properties properties) {
        super(properties);
    }

    @Override
    public void spawnDestroyByEntityParticles(Level level, Entity entity, BlockPos pos, BlockState state) {
        spawnDestroyParticles(level, pos, state);
    }
}
