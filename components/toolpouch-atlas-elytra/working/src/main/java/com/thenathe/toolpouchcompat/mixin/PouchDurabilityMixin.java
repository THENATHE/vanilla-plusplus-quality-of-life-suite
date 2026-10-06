package com.thenathe.toolpouchcompat.mixin;
import com.thenathe.toolpouchcompat.PouchDurability;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=ToolPouchUtil.class,remap=false)
public abstract class PouchDurabilityMixin {
    @Inject(method="updateElytraInToolPouch",at=@At("HEAD"),cancellable=true)
    private static void toolpouchCompat$enchantedFlightWear(Player player,CallbackInfo ci) {
        PouchDurability.damage(player);
        ci.cancel();
    }
}
