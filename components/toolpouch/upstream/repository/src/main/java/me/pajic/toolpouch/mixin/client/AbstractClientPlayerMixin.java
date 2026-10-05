package me.pajic.toolpouch.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.pajic.toolpouch.ToolPouchClient;
import me.pajic.toolpouch.util.ClientUtil;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {

    @WrapMethod(method = "getFieldOfViewModifier")
    private float modifyFOV(boolean firstPerson, float effectScale, Operation<Float> original) {
        if (ClientUtil.shouldScope) {
            return 0.1F * ClientUtil.zoomModifier;
        }
        else if (
				ClientUtil.zoomModifier != 1.0F &&
				(!ToolPouchClient.CONFIG.rememberZoomLevel.get() || !ToolPouchClient.CONFIG.scrollableZoom.get())
		) {
			ClientUtil.zoomModifier = 1.0F;
        }
        return original.call(firstPerson, effectScale);
    }
}
