package me.pajic.simple_death_improvements.mixin.compat;

//? >=26.1 {

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.swacky.ohmega.event.CommonCallbacks;
import me.pajic.simple_death_improvements.SDI;
import me.pajic.simple_death_improvements.util.ModUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded("ohmega")
@Mixin(CommonCallbacks.class)
public class OhmegaCommonCallbacksMixin {

    @ModifyExpressionValue(
            method = "onPlayerDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            )
    )
    private static boolean keepAccessories(boolean original, @Local ItemStack stack, @Local(argsOnly = true) Player player) {
        return original ||
                SDI.CONFIG.keepAccessoriesOnDeath.get() && !SDI.CONFIG.accessoryDropList.contains(SDI.getItemId(stack)) ||
                ModUtil.tryKeepDamageableItem(player, stack);
    }
}
//?}
