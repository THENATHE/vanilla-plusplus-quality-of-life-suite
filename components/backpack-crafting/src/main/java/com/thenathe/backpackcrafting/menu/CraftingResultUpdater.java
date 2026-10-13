package com.thenathe.backpackcrafting.menu;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Keeps vanilla result synchronization and upstream crafting-result mixins in one path. */
final class CraftingResultUpdater extends CraftingMenu {
    private CraftingResultUpdater(Inventory inventory) {
        super(0, inventory);
    }

    static void update(AbstractContainerMenu menu, ServerLevel level, Player player,
                       CraftingContainer input, ResultContainer result, RecipeHolder<CraftingRecipe> hint) {
        slotChangedCraftingGrid(menu, level, player, input, result, hint);
    }
}
