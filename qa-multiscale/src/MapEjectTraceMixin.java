package qa.mixin;

import qa.MixedScaleClientQa;
import me.pajic.mapstitch.networking.payload.C2SEjectMap;
import me.pajic.mapstitch.platform.fabric.FabricLoaderUtil;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Observe the original client's actual payload; never substitute an ejection. */
@Mixin(value = FabricLoaderUtil.class, remap = false)
public abstract class MapEjectTraceMixin {
    @Inject(method = "c2s", at = @At("HEAD"))
    private void observe(CustomPacketPayload payload, CallbackInfo ci) {
        if (payload instanceof C2SEjectMap eject) MixedScaleClientQa.ejected.add(eject.mapId().id());
    }
}
