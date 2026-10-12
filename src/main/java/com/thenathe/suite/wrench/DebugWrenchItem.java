package com.thenathe.suite.wrench;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Native behavior has no Polymer dependency, including singleplayer without Polymer. */
public final class DebugWrenchItem extends Item {
    public DebugWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
        return false;
    }
}
