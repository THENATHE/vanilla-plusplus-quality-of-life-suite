package com.thenathe.toolpouchcompat.mixin;
import com.thenathe.toolpouchcompat.PouchSsoMending;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Applied only when the original SSO mod is present. */
@Mixin(Inventory.class)
public abstract class PouchSsoMendingMixin {
    @Shadow @Final public Player player;
    @Inject(method="tick",at=@At("TAIL"))
    private void toolpouchCompat$repairStoredBrokenWings(CallbackInfo ci) {
        if(player instanceof ServerPlayer serverPlayer) PouchSsoMending.tick(serverPlayer);
    }
}
