package me.pajic.tiered_backpacks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShulkerBoxSlot.class)
public class ShulkerBoxSlotMixin {

    @ModifyReturnValue(
            method = "mayPlace",
            at = @At("RETURN")
    )
    private boolean preventBackpackInsertion(boolean original, @Local(argsOnly = true) ItemStack itemStack) {
        return original && !BackpackUtil.isValidContainerHolder(itemStack);
    }
}
