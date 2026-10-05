package com.thenathe.toolpouchcompat.mixin;
import com.thenathe.toolpouchcompat.ElytraPreference;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ServerPlayer.class)
public class ServerPlayerPreferenceMixin {
 @Inject(method="restoreFrom",at=@At("TAIL"))
 private void copy(ServerPlayer oldPlayer, boolean keepEverything, CallbackInfo ci) {
  ((ElytraPreference)this).toolpouchCompat$setElytraEnabled(((ElytraPreference)oldPlayer).toolpouchCompat$elytraEnabled());
 }
}
