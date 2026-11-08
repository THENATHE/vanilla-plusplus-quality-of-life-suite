package me.pajic.tiered_backpacks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BundleContents.class)
public class BundleContentsMixin {

    @ModifyReturnValue(
            method = "canItemBeInBundle",
            at = @At("RETURN")
    )
    private static boolean preventBackpackInsertion(boolean original, @Local(argsOnly = true) ItemStack itemToAdd) {
        return original && !BackpackUtil.isValidContainerHolder(itemToAdd);
    }
}
