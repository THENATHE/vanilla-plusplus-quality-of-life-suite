package com.thenathe.toolpouchcompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.pajic.toolpouch.component.ModDataComponents;
import me.pajic.toolpouch.item.ModItems;
import me.pajic.toolpouch.recipe.AttachToolPouchRecipe;
import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AttachToolPouchRecipe.class, remap = false)
public abstract class PouchAttachmentTierMixin {
    @ModifyReturnValue(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"))
    private ItemStack toolpouchCompat$useActualPouchTier(ItemStack result, CraftingInput input) {
        if (!GameplayUtil.isLeggingsWithPouchAttached(result)) return result;
        ItemStack pouch = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack ingredient = input.getItem(i);
            if (!ingredient.is(GameplayUtil.TOOL_POUCHES)) continue;
            if (!pouch.isEmpty()) return result;
            pouch = ingredient;
        }
        if (pouch.isEmpty()) return result;
        // Old detached leggings can retain the previous pouch's tier. The current ingredient wins.
        if (pouch.is(ModItems.NETHERITE_TOOL_POUCH)) result.set(ModDataComponents.IS_NETHERITE_POUCH, true);
        else result.remove(ModDataComponents.IS_NETHERITE_POUCH);
        return result;
    }
}
