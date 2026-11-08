package me.pajic.tiered_backpacks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShulkerBoxBlockEntity.class)
public class ShulkerBoxBlockEntityMixin {

    @ModifyReturnValue(
            method = "canPlaceItemThroughFace",
            at = @At("RETURN")
    )
    private boolean preventBackpackInsertion(boolean original, @Local(argsOnly = true) ItemStack itemStack) {
        return original && !BackpackUtil.isValidContainerHolder(itemStack);
    }
}
