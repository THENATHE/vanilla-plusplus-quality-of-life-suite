package com.thenathe.backpackcrafting.mixin.client;

import com.thenathe.backpackcrafting.client.BackpackCraftingScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractRecipeBookScreen.class)
abstract class RecipeBookScreenMixin {
    @ModifyConstant(method = "init", constant = @Constant(intValue = 379))
    private int backpackCrafting$narrowScreenThreshold(int original) {
        if ((Object) this instanceof BackpackCraftingScreen screen) {
            return Math.max(original, screen.getMenu().getWidth() + 203);
        }
        return original;
    }
}
