package me.pajic.misctweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.misctweaks.MiscTweaks;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

	@WrapOperation(
            //? >=26.1
			method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V",
            //? <26.1
            //method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;DDD)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/item/ItemEntity;setDeltaMovement(DDD)V"
			)
	)
	private void stabilizeDrop(ItemEntity instance, double x, double y, double z, Operation<Void> original, @Local(argsOnly = true) Level level) {
		if (MiscTweaks.CONFIG.stableBlockDrops.flingTowardsPlayer.get()) {
			double maxRange = MiscTweaks.CONFIG.stableBlockDrops.flingMaxRange.get();
			Player player = level.getNearestPlayer((ItemEntity) (Object) this, maxRange);
			if (player != null && (!MiscTweaks.CONFIG.stableBlockDrops.requireCrouchForFling.get() || player.isShiftKeyDown())) {
				double xa = player.getX() - instance.getX();
				double ya = player.getY() - instance.getY();
				double za = player.getZ() - instance.getZ();
				original.call(instance, xa * 0.1, y + Mth.lerp(ya / maxRange, 0, maxRange / 2.5) * 0.25, za * 0.1);
				return;
			}
		}
		if (MiscTweaks.CONFIG.stableBlockDrops.noRandomHorizontalMovement.get()) original.call(instance, 0.0, y, 0.0);
		else original.call(instance, x, y, z);
	}
}
