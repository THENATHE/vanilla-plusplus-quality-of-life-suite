package com.thenathe.toolpouchcompat.mixin;
import com.thenathe.toolpouchcompat.AtlasBridge;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.util.ModClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import me.pajic.mapstitch.compat.AccessoryUtil;
import java.util.List;
@Mixin(value=ModClientUtil.class,remap=false)
public class AtlasClientLookupMixin {
 @ModifyReturnValue(method="hasItem",at=@At("RETURN"))
 private static boolean item(boolean original, Minecraft mc, Item item, String context, List<String> reqs) {
  return original || (mc.player != null && MapStitch.CONFIG.itemRequirements.compassAndClockScan.contains("accessories") && AtlasBridge.hasItem(mc.player,item));
 }
 @Inject(method="getFirstItem",at=@At("HEAD"),cancellable=true)
 private static void atlas(Minecraft mc, Item item, CallbackInfoReturnable<ItemStack> cir) {
  if (mc.player == null || !MapStitch.CONFIG.itemRequirements.minimapAtlasScan.contains("accessories")) return;
  // Existing accessory API has priority, followed by the pouch, then inventory.
  if (AccessoryUtil.INSTANCE != null && AccessoryUtil.INSTANCE.getFirstItem(item,mc.player).is(item)) return;
  for (ItemStack atlas:AtlasBridge.atlases(mc.player)) if(atlas.is(item)) { cir.setReturnValue(atlas); return; }
 }
}
