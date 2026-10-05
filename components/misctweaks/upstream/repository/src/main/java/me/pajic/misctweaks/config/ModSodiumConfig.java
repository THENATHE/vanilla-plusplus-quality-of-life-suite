package me.pajic.misctweaks.config;

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.misctweaks.MiscTweaksClient;
import me.pajic.misctweaks.util.ModClientUtil;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
//? if neoforge {
/*import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;

@ConfigEntryPointForge("misctweaks")
*///?}
@SuppressWarnings("unused")
@Entrypoint("sodium:config_api_user")
public class ModSodiumConfig implements ConfigEntryPoint {

	@Override
	public void registerConfigLate(ConfigBuilder builder) {
		builder.registerOwnModOptions()
				.setName("MiscTweaks")
				.setColorTheme(builder.createColorTheme().setBaseThemeRGB(0x55834a))
				.setNonTintedIcon(Identifier.parse("misctweaks:textures/config_icon.png"))
				.addPage(builder.createOptionPage()
						.setName(Component.translatable("misctweaks.client_config.settings"))
						.addOption(builder.createIntegerOption(Identifier.parse("misctweaks:current_dimension_brightness"))
								.setName(Component.translatable("misctweaks.client_config.currentDimensionBrightness"))
								.setTooltip(Component.translatable("misctweaks.client_config.currentDimensionBrightness.tooltip"))
								.setRange(-1, 100, 1)
								.setDefaultValue(50)
								.setValueFormatter(i -> {
									if (i == -1) return Component.translatable("misctweaks.client_config.currentDimensionBrightness.disabled");
									if (i == 0) return Component.translatable("options.gamma.min");
									if (i == 50) return Component.translatable("options.gamma.default");
									if (i == 100) return Component.translatable("options.gamma.max");
									return Component.literal(i + "%");
								})
								.setEnabledProvider(cs -> ModClientUtil.getCurrentBrightness() != -2, ConfigState.UPDATE_ON_REBUILD)
								.setBinding(ModClientUtil::saveCurrentBrightness, ModClientUtil::getCurrentBrightness)
								.setStorageHandler(ModClientUtil::saveConfig)
						)
						.addOption(builder.createIntegerOption(Identifier.parse("misctweaks:raise_hotbar"))
								.setName(Component.translatable("misctweaks.client_config.raiseHotbarPixels"))
								.setTooltip(i -> MiscTweaksClient.RAISED_LOADED ?
										Component.translatable("misctweaks.client_config.raiseHotbarPixels.raisedInstalled") :
										Component.translatable("misctweaks.client_config.raiseHotbarPixels.desc"))
								.setRange(0, 24, 1)
								.setDefaultValue(2)
								.setValueFormatter(i -> {
									if (i == 0) return Component.translatable("misctweaks.client_config.raiseHotbarPixels.disabled");
									return Component.translatable("misctweaks.client_config.raiseHotbarPixels.value", i);
								})
								.setEnabledProvider(cs -> !MiscTweaksClient.RAISED_LOADED)
								.setBinding(ModClientUtil::setRaisePixels, ModClientUtil::getRaisePixels)
								.setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
								.setStorageHandler(ModClientUtil::saveConfig)
						)
						.addOption(builder.createBooleanOption(Identifier.parse("misctweaks:lower_shield"))
								.setName(Component.translatable("misctweaks.client_config.lowerShield"))
								.setTooltip(Component.translatable("misctweaks.client_config.lowerShield.tooltip"))
								.setDefaultValue(true)
								.setBinding(ModClientUtil::setLowerShield, ModClientUtil::getLowerShield)
								.setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
								.setStorageHandler(ModClientUtil::saveConfig)
						)
				);
	}
}
