package com.thenathe.toolpouchcompat.mixin;
import com.thenathe.toolpouchcompat.ElytraPreference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Player.class)
public class PlayerPreferenceMixin implements ElytraPreference {
 @Unique private boolean toolpouchCompat$enabled = true;
 public boolean toolpouchCompat$elytraEnabled() { return toolpouchCompat$enabled; }
 public void toolpouchCompat$setElytraEnabled(boolean value) { toolpouchCompat$enabled = value; }
 @Inject(method="addAdditionalSaveData",at=@At("TAIL"))
 private void save(ValueOutput output, CallbackInfo ci) { output.putBoolean("ToolPouchElytraEnabled", toolpouchCompat$enabled); }
 @Inject(method="readAdditionalSaveData",at=@At("TAIL"))
 private void load(ValueInput input, CallbackInfo ci) { toolpouchCompat$enabled = input.getBooleanOr("ToolPouchElytraEnabled", true); }
}
