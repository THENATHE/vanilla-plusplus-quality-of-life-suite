package com.thenathe.toolpouchcompat.mixin;
import com.thenathe.toolpouchcompat.ElytraPreference;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.Predicate;
@Mixin(value=ToolPouchUtil.class,remap=false)
public class PouchFlightMixin {
 @Inject(method="getElytraFromToolPouch",at=@At("HEAD"),cancellable=true)
 private static void flight(Player player, boolean allowBroken, CallbackInfoReturnable<ItemStackTemplate> cir) {
  if (!allowBroken && !((ElytraPreference)player).toolpouchCompat$elytraEnabled()) cir.setReturnValue(null);
 }
 @Inject(method="toolPouchHasItem",at=@At("HEAD"),cancellable=true)
 private static void activeItems(Player player, Predicate<ItemStackTemplate> predicate, CallbackInfoReturnable<Boolean> cir) {
  // Stock durability handling checks the generic item lookup. Excluding disabled
  // gliders here lets vanilla chest Elytra durability proceed normally.
  if (!((ElytraPreference)player).toolpouchCompat$elytraEnabled())
   cir.setReturnValue(!ToolPouchUtil.getItemsFromToolPouch(player, s -> s.get(DataComponents.GLIDER)==null && predicate.test(s)).isEmpty());
 }
}
