package me.pajic.toolpouch.renderer;

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.util.ClientUtil;
import org.jspecify.annotations.NonNull;

@Entrypoint("lambdynlights:initializer")
public class PlayerLanternDynamicLighting implements DynamicLightsInitializer {

	public static final EntityLuminance.Type CONSTANT = EntityLuminance.Type.registerSimple(
			ToolPouch.id("player_lantern"),
			PlayerLanternLuminance.INSTANCE
	);

	@Override
	public void onInitializeDynamicLights(@NonNull DynamicLightsContext context) {
		context.entityLightSourceManager().onRegisterEvent().register(registerContext ->
				registerContext.register(ClientUtil.PLAYER, PlayerLanternLuminance.INSTANCE)
		);
	}
}
