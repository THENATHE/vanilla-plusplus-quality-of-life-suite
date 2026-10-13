package com.thenathe.backpackcrafting.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecipeBookComponent.class)
abstract class RecipeBookCountsMixin {
    @Shadow @Final protected RecipeBookMenu menu;
    @Shadow private boolean widthTooNarrow;

    @ModifyReturnValue(method = "getXOrigin", at = @At("RETURN"))
    private int backpackCrafting$alignBesideStorage(int original) {
        return menu instanceof BackpackCraftingMenu backpackMenu && !widthTooNarrow
                ? original - (backpackMenu.getWidth() - 176) / 2 : original;
    }

    @WrapOperation(method = "updateTabs", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookTabButton;setPosition(II)V"))
    private void backpackCrafting$alignTabs(RecipeBookTabButton tab, int x, int y, Operation<Void> original) {
        if (menu instanceof BackpackCraftingMenu backpackMenu && !widthTooNarrow) {
            x -= (backpackMenu.getWidth() - 176) / 2;
        }
        original.call(tab, x, y);
    }

    @WrapOperation(method = {"initVisuals", "updateStackedContents"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;fillStackedContents(Lnet/minecraft/world/entity/player/StackedItemContents;)V"))
    private void backpackCrafting$countUsableInventory(Inventory inventory, StackedItemContents contents,
                                                      Operation<Void> original) {
        if (menu instanceof BackpackCraftingMenu backpackMenu) {
            backpackMenu.fillPlayerStackedContents(inventory, contents);
        } else {
            original.call(inventory, contents);
        }
    }
}
