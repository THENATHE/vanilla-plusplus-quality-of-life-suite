package com.thenathe.toolpouchcompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.pajic.toolpouch.component.ModDataComponents;
import me.pajic.toolpouch.recipe.DetachToolPouchRecipe;
import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = DetachToolPouchRecipe.class, remap = false)
public abstract class PouchDetachmentTierMixin {
    @ModifyReturnValue(method = "getRemainingItems(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/core/NonNullList;",
            at = @At("RETURN"))
    private NonNullList<ItemStack> toolpouchCompat$clearDetachedTier(NonNullList<ItemStack> remaining,
                                                                   CraftingInput input) {
        for (int i = 0; i < Math.min(input.size(), remaining.size()); i++) {
            if (GameplayUtil.isLeggingsWithPouchAttached(input.getItem(i)) && remaining.get(i).is(ItemTags.LEG_ARMOR)) {
                remaining.get(i).remove(ModDataComponents.IS_NETHERITE_POUCH);
            }
        }
        return remaining;
    }
}
