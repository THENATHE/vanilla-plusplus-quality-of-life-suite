package me.pajic.tiered_backpacks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.tiered_backpacks.TieredBackpacks;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin {

    @ModifyExpressionValue(
            method = "slotChangedCraftingGrid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/CraftingRecipe;assemble(Lnet/minecraft/world/item/crafting/RecipeInput;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private static ItemStack handleBackpackUpgrade(
            ItemStack original,
            @Local(name = "input") CraftingInput input,
            @Local(name = "craftingRecipe") CraftingRecipe craftingRecipe,
            @Local(name = "recipeHolder") RecipeHolder<CraftingRecipe> recipeHolder
    ) {
        if (
                original.is(BackpackUtil.BACKPACKS) &&
                recipeHolder.id().identifier().getNamespace().equals(TieredBackpacks.MOD_ID) &&
				craftingRecipe instanceof ShapedRecipe
        ) {
            input.items().stream().filter(stack -> stack.is(BackpackUtil.BACKPACKS))
                    .findFirst().ifPresent(stack -> {
                        original.set(
                                DataComponents.CONTAINER,
                                stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                        );
                        original.set(
                                DataComponents.DYED_COLOR,
                                stack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(DyedItemColor.LEATHER_COLOR))
                        );
                    });
        }
        return original;
    }
}
