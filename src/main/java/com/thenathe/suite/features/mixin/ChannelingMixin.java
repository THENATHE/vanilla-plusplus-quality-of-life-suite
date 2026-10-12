package com.thenathe.suite.features.mixin;

import com.thenathe.suite.features.AllWeatherChanneling;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
public abstract class ChannelingMixin {
    @Unique private boolean suite$acceptedHit;

    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void suite$beginHit(EntityHitResult hit, CallbackInfo callback) {
        suite$acceptedHit = false;
    }

    @ModifyExpressionValue(method = "onHitEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean suite$rememberAcceptedHit(boolean accepted) {
        suite$acceptedHit = accepted;
        return accepted;
    }

    @ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;doPostAttackEffectsWithItemSourceOnBreak(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)V"), index = 3)
    private ItemStack suite$ownChanneling(ItemStack weapon) {
        return AllWeatherChanneling.vanillaAttackWeapon(weapon);
    }

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void suite$channelSurvivingTarget(EntityHitResult hit, CallbackInfo callback) {
        if (suite$acceptedHit) AllWeatherChanneling.strike((ThrownTrident) (Object) this, hit.getEntity());
    }
}
