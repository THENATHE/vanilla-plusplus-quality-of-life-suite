package me.pajic.simple_death_improvements.mixin.compat;

//? <26.1 {

/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.swacky.ohmega.common.dataattachment.AccessoryContainer;
import me.pajic.simple_death_improvements.SDI;
import me.pajic.simple_death_improvements.util.ModUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded("ohmega")
@Mixin(AccessoryContainer.class)
public class OhmegaAccessoryContainerMixin {

    @ModifyExpressionValue(
            method = "removeStackFromSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean keepAccessories(boolean original, @Local ItemStack stack, @Local(argsOnly = true) Player player, @Local(argsOnly = true) boolean fromDeath) {
        return original || fromDeath && (
                SDI.CONFIG.keepAccessoriesOnDeath.get() && !SDI.CONFIG.accessoryDropList.contains(SDI.getItemId(stack)) ||
                ModUtil.tryKeepDamageableItem(player, stack)
        );
    }
}
*///?}
