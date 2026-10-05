package me.pajic.misctweaks.util;

import me.pajic.misctweaks.MiscTweaksClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ModClientUtil {

	public static int getOrCreateDimensionBrightness(@Nullable Identifier id) {
		if (id != null) {
			String idString = id.toString();
			if (MiscTweaksClient.CONFIG.perDimensionBrightness.get().containsKey(idString)) {
                return MiscTweaksClient.CONFIG.perDimensionBrightness.get().get(idString);
            }
			updateBrightnessMap(idString, -1);
			return -1;
		}
		return -2;
	}

	public static int getCurrentBrightness() {
		return getOrCreateDimensionBrightness(getCurrentDimensionId());
	}

	public static void saveCurrentBrightness(int brightness) {
		Identifier id = getCurrentDimensionId();
		if (id != null) updateBrightnessMap(id.toString(), brightness);
	}

	public static void updateBrightnessMap(String id, int brightness) {
		Map<String, Integer> updated = new HashMap<>(MiscTweaksClient.CONFIG.perDimensionBrightness.get());
		updated.put(id, brightness);
		MiscTweaksClient.CONFIG.perDimensionBrightness.validateAndSet(updated);
	}

	public static boolean shouldRaiseHotbar() {
		return MiscTweaksClient.CONFIG.raiseHotbarPixels.get() > 0 && !MiscTweaksClient.RAISED_LOADED;
	}

	public static int getRaisePixels() {
		return MiscTweaksClient.CONFIG.raiseHotbarPixels.get();
	}

	public static void setRaisePixels(int pixels) {
		MiscTweaksClient.CONFIG.raiseHotbarPixels.validateAndSet(pixels);
	}

	public static boolean getLowerShield() {
		return MiscTweaksClient.CONFIG.lowerShield.get();
	}

	public static void setLowerShield(boolean lowerShield) {
		MiscTweaksClient.CONFIG.lowerShield.validateAndSet(lowerShield);
	}

	public static void saveConfig() {
		MiscTweaksClient.CONFIG.save();
	}

	private @Nullable static Identifier getCurrentDimensionId() {
		ClientLevel level = Minecraft.getInstance().level;
		return level != null ? level.dimension().identifier() : null;
	}
}
