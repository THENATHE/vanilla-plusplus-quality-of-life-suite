package me.pajic.toolpouch.renderer;

import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import me.pajic.toolpouch.util.ClientUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Range;

import java.util.List;

@SuppressWarnings("NullableProblems")
public final class PlayerLanternLuminance implements EntityLuminance {

	public static final PlayerLanternLuminance INSTANCE = new PlayerLanternLuminance();

	private PlayerLanternLuminance() {}

	@Override
	public Type type() {
		return PlayerLanternDynamicLighting.CONSTANT;
	}

	@Override
	public @Range(from = 0L, to = 15L) int getLuminance(ItemLightSourceManager manager, Entity entity) {
		if (entity instanceof Player player) {
			List<ItemStackTemplate> lanterns = ToolPouchUtil.getItemsFromToolPouch(player, ClientUtil.getSupportedLanterns());
			if (!lanterns.isEmpty()) return manager.getLuminance(lanterns.getFirst().create());
		}
		return 0;
	}
}
