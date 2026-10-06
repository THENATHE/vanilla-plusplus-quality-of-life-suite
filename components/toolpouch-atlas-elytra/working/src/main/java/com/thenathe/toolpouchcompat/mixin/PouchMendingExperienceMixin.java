package com.thenathe.toolpouchcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.thenathe.toolpouchcompat.PouchMending;
import com.thenathe.toolpouchcompat.PouchSsoMending;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ExperienceOrb.class)
public abstract class PouchMendingExperienceMixin {
    // Wrap the whole repair pass: Clumps adds a cancellable early return that skips RETURN injections.
    @WrapMethod(method = "repairPlayerItems")
    private int toolpouchCompat$repairStoredElytra(ServerPlayer player, int amount, Operation<Integer> original) {
        int remaining = original.call(player, amount);
        // Keep SSO's opt-out independent of wrapper application order, without requiring SSO to load.
        if (FabricLoader.getInstance().isModLoaded("simple_smithing_overhaul")
                && !PouchSsoMending.regularXpEnabled()) return remaining;
        return PouchMending.repair(player, remaining);
    }
}
