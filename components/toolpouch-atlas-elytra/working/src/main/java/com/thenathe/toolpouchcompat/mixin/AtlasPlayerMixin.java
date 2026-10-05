package com.thenathe.toolpouchcompat.mixin;
import com.thenathe.toolpouchcompat.AtlasBridge;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Player.class)
public class AtlasPlayerMixin {
 @Inject(method="aiStep",at=@At("TAIL"))
 private void tickPouch(CallbackInfo ci) { AtlasBridge.tick((Player)(Object)this); }
}
