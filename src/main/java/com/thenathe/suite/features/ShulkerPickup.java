package com.thenathe.suite.features;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.core.BlockPos;

/** Offhand storage commits first; the caller retains ownership of any remainder. */
public final class ShulkerPickup {
    private ShulkerPickup() {}

    public static int collect(ServerPlayer player, ItemStack incoming) {
        if (!FeatureConfig.CONFIG.dropsIntoShulker.get() || incoming.isEmpty()
                || !player.isAlive() || player.isSpectator()
                || !incoming.getItem().canFitInsideContainerItems()) return 0;
        ItemStack holder = player.getOffhandItem();
        if (holder == incoming || holder.getCount() != 1
                || !(holder.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof ShulkerBoxBlock shulkerBlock)) return 0;
        // Ask the block's actual entity for its capacity, including shulker subclasses.
        var entity = shulkerBlock.newBlockEntity(BlockPos.ZERO, shulkerBlock.defaultBlockState());
        if (!(entity instanceof ShulkerBoxBlockEntity shulker)) return 0;
        int capacity = shulker.getContainerSize();
        ItemContainerContents original = holder.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        if (capacity <= 0 || original.size() > capacity) return 0;
        NonNullList<ItemStack> slots = NonNullList.withSize(capacity, ItemStack.EMPTY);
        original.copyInto(slots);
        int saved = 0;
        for (var ignored : original.nonEmptyItems()) saved++;
        if (saved != slots.stream().filter(stack -> !stack.isEmpty()).count()) return 0;

        int remaining = incoming.getCount();
        for (ItemStack stored : slots) {
            if (stored.isEmpty() || !ItemStack.isSameItemSameComponents(stored, incoming)) continue;
            int moved = Math.min(remaining, Math.max(0, shulker.getMaxStackSize(stored) - stored.getCount()));
            stored.grow(moved);
            remaining -= moved;
            if (remaining == 0) break;
        }
        for (int index = 0; index < capacity && remaining > 0; index++) {
            if (!slots.get(index).isEmpty()) continue;
            int moved = Math.min(remaining, shulker.getMaxStackSize(incoming));
            if (moved <= 0) break;
            slots.set(index, incoming.copyWithCount(moved));
            remaining -= moved;
        }
        int inserted = incoming.getCount() - remaining;
        if (inserted > 0) {
            holder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(slots));
            incoming.shrink(inserted);
            player.getInventory().setChanged();
        }
        return inserted;
    }
}
