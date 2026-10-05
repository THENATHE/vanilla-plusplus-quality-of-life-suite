package com.thenathe.bannerpointcompat.mixin;

import com.thenathe.bannerpointcompat.BannerpointCompat;
import me.pajic.bannerpoint.waypoint.BannerWaypointUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = BannerWaypointUtil.class, remap = false)
public abstract class BannerWaypointUtilMixin {
    @Inject(method = "createConnection", at = @At("HEAD"), cancellable = true)
    private static void requireArtwork(BannerBlockEntity banner, ServerPlayer receiver, Waypoint.Icon icon,
                                       CallbackInfoReturnable<Optional<WaypointTransmitter.Connection>> cir) {
        if (!BannerpointCompat.canRenderWaypoints(receiver)) cir.setReturnValue(Optional.empty());
    }

    // All three original connection implementations consult this method from isBroken().
    @Inject(method = "doesSourceIgnoreReceiver", at = @At("HEAD"), cancellable = true)
    private static void revokeWithoutArtwork(BannerBlockEntity banner, ServerPlayer receiver,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (!BannerpointCompat.canRenderWaypoints(receiver)) cir.setReturnValue(true);
    }
}
