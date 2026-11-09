package com.thenathe.toolpouchcompat.mixin;

import com.thenathe.toolpouchcompat.LegacyElytraToggle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Reads the pre-1.5.2 Elytra flag and keeps it saved only until it has been carried over. */
@Mixin(Player.class)
public abstract class LegacyElytraToggleMixin implements LegacyElytraToggle.Pending {
    @Unique private boolean toolpouchCompat$legacyOff;

    @Override public boolean toolpouchCompat$legacyElytraOff() { return toolpouchCompat$legacyOff; }
    @Override public void toolpouchCompat$setLegacyElytraOff(boolean off) { toolpouchCompat$legacyOff = off; }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void toolpouchCompat$readLegacy(ValueInput input, CallbackInfo ci) {
        toolpouchCompat$legacyOff = !input.getBooleanOr(LegacyElytraToggle.TAG, true);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void toolpouchCompat$keepPending(ValueOutput output, CallbackInfo ci) {
        if (toolpouchCompat$legacyOff) output.putBoolean(LegacyElytraToggle.TAG, false);
    }

}
