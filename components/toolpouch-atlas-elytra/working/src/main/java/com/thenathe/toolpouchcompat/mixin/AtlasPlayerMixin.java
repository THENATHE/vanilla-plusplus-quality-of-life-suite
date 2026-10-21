package com.thenathe.toolpouchcompat.mixin;
import com.thenathe.toolpouchcompat.AtlasBridge;
import com.thenathe.toolpouchcompat.AtlasCacheOwner;
import com.thenathe.toolpouchcompat.AtlasStackCache;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Player.class)
public class AtlasPlayerMixin implements AtlasCacheOwner {
 @Unique private AtlasStackCache toolpouchCompat$atlasCache;
 public AtlasStackCache toolpouchCompat$atlasCache() {
  if (toolpouchCompat$atlasCache == null) toolpouchCompat$atlasCache = new AtlasStackCache();
  return toolpouchCompat$atlasCache;
 }
 @Inject(method="aiStep",at=@At("TAIL"))
 private void tickPouch(CallbackInfo ci) { AtlasBridge.tick((Player)(Object)this); }
}
