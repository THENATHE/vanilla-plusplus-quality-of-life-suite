package me.pajic.toolpouch.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.pajic.toolpouch.keybind.SlotToggleKeys;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {

	@Inject(method = "set", at = @At("HEAD"))
	private static void onKeySet(InputConstants.Key key, boolean state, CallbackInfo ci) {
		SlotToggleKeys.onKeySet(key, state);
	}

	@Inject(method = "releaseAll", at = @At("HEAD"))
	private static void onReleaseAll(CallbackInfo ci) {
		SlotToggleKeys.releaseAll();
	}
}
