package me.pajic.misctweaks.mixin;

import me.pajic.misctweaks.MiscTweaks;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? >=26.1 {
import net.minecraft.core.component.DataComponents;
//?} else {
/*import net.minecraft.world.item.ElytraItem;
*///?}

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void cancelElytraFlyingInLiquid(CallbackInfo ci) {
		Player self = (Player) (Object) this;
        if (
				MiscTweaks.CONFIG.elytraSwimTweak.get() &&
                self.isFallFlying() &&
				self.isInLiquid() &&
                //? >=26.1
				self.getItemBySlot(EquipmentSlot.CHEST).has(DataComponents.GLIDER)
                //? <26.1
                //self.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ElytraItem
        ) {
            self.stopFallFlying();
			self.setSwimming(true);
        }
    }
}
