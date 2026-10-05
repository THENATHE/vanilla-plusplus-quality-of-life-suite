package me.pajic.toolpouch.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.util.ClientUtil;
import me.pajic.toolpouch.util.ItemStackTemplateUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends Avatar {

	protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Shadow public abstract boolean isCreative();
	@Shadow public abstract boolean isSpectator();
	@Shadow public abstract void setReducedDebugInfo(boolean reducedDebugInfo);

	@Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void setReducedDebugInfo(CallbackInfo ci) {
        if (ToolPouch.CONFIG.hideDebugInfoInSurvival.get()) {
            setReducedDebugInfo(!isCreative() && !isSpectator());
        }
    }

    @WrapMethod(method = "isScoping")
    private boolean modifyScopingCondition(Operation<Boolean> original) {
        if (ClientUtil.shouldScope) return true;
        return original.call();
    }

	@Inject(
			method = "aiStep",
			at = @At("HEAD")
	)
	private void tickMapsInToolPouch(CallbackInfo ci) {
		Player self = (Player) (Object) this;
		ToolPouchUtil.getItemsFromToolPouch(self, stack -> ItemStackTemplateUtil.has(stack, DataComponents.MAP_ID))
				.forEach(map -> map.create().inventoryTick(level(), self, EquipmentSlot.MAINHAND));
	}
}
