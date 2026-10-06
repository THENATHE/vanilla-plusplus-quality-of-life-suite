package com.thenathe.multiscale;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** Optional integrations observe the final result, rather than intermediate map toggles. */
public final class AtlasBannerEvents {
    private AtlasBannerEvents() {}

    public static final Event<AfterEdit> AFTER_EDIT = EventFactory.createArrayBacked(AfterEdit.class,
            listeners -> (atlas, level, pos, retained) -> {
                for (var listener : listeners) listener.afterEdit(atlas, level, pos, retained);
            });

    @FunctionalInterface
    public interface AfterEdit {
        void afterEdit(ItemStack atlas, ServerLevel level, BlockPos pos, boolean retained);
    }
}
