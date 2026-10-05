package com.thenathe.toolpouchcompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.thenathe.toolpouchcompat.PouchMending;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExperienceOrb.class)
public abstract class PouchMendingExperienceMixin {
    // Inside the original method: SSO's disabled regular-Mending wrapper skips this hook too.
    @ModifyReturnValue(method = "repairPlayerItems", at = @At("RETURN"))
    private int toolpouchCompat$repairStoredElytra(int remaining, ServerPlayer player, int amount) {
        return PouchMending.repair(player, remaining);
    }
}
