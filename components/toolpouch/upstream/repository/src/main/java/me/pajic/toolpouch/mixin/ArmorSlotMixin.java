package me.pajic.toolpouch.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorSlot.class)
public class ArmorSlotMixin {

    @ModifyReturnValue(
            method = "mayPickup",
            at = @At("RETURN")
    )
    private boolean modifyMayPickup(
			boolean original,
			@Local(name = "itemStack") ItemStack itemStack,
			@Local(argsOnly = true, name = "player") Player player
	) {
        return GameplayUtil.canUnequipToolPouch(player, itemStack) && original;
    }
}
