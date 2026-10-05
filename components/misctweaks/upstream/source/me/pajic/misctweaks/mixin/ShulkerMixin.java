package me.pajic.misctweaks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.pajic.misctweaks.MiscTweaks;
import net.minecraft.world.entity.monster.Shulker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Shulker.class)
public class ShulkerMixin {

    @WrapWithCondition(
            //~ if <26.1 'hurtServer' -> 'hurt'
            method = "hurtServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Shulker;hitByShulkerBullet()V"
            )
    )
    private boolean preventDuplication(Shulker instance) {
        return !MiscTweaks.CONFIG.preventShulkerDuplication.get();
    }
}
