package me.pajic.tiered_backpacks.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Equippable.class)
public class EquippableMixin {

    @WrapMethod(method = "swapWithEquipmentSlot")
    private InteractionResult preventSwappingFilledBackpack(ItemStack inHand, Player player, Operation<InteractionResult> original) {
        ItemStack equipped = player.getItemBySlot(((Equippable) (Object) this).slot());
        return BackpackUtil.canUnequipBackpack(player, equipped) ? original.call(inHand, player) : InteractionResult.FAIL;
    }
}
