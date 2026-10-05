package me.pajic.toolpouch.mixin.compat;

//? fabric {

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.lodestar.aileron.Aileron;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.pajic.toolpouch.util.ItemStackTemplateUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded("aileron")
@Mixin(Aileron.class)
public class AileronMixin {

    @ModifyReturnValue(
            method = "getElytra",
            at = @At("RETURN")
    )
    private static ItemStack useToolPouchElytra(
			ItemStack original,
			@Local(argsOnly = true, name = "entity") LivingEntity entity
	) {
		if (entity instanceof Player player) {
			ItemStackTemplate elytra = ToolPouchUtil.getElytraFromToolPouch(player, false);
			if (elytra != null && !ItemStackTemplateUtil.isEmpty(elytra)) return elytra.create();
		}
        return original;
    }
}
//?}
