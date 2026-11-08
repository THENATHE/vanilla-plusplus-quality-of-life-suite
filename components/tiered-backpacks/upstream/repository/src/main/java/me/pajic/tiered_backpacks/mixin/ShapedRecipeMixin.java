package me.pajic.tiered_backpacks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {

    @ModifyReturnValue(
            method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN")
    )
    private ItemStack handleBackpackUpgrade(ItemStack original, @Local(argsOnly = true) CraftingInput input) {
        if (original.is(BackpackUtil.BACKPACKS)) {
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
