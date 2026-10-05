package me.pajic.toolpouch.compat;

import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import me.pajic.toolpouch.ToolPouchClient;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import sereneseasons.api.SSItems;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

public class SeasonsCompat {

	public static ObjectIntImmutablePair<Component> getSeasonStringData(Level level) {
		Season.SubSeason subSeason = SeasonHelper.getSeasonState(level).getSubSeason();
		return ObjectIntImmutablePair.of(
				switch (subSeason) {
					case EARLY_SPRING -> Component.translatable("gui.toolpouch.early_spring");
					case MID_SPRING -> Component.translatable("gui.toolpouch.mid_spring");
					case LATE_SPRING -> Component.translatable("gui.toolpouch.late_spring");
					case EARLY_SUMMER -> Component.translatable("gui.toolpouch.early_summer");
					case MID_SUMMER -> Component.translatable("gui.toolpouch.mid_summer");
					case LATE_SUMMER -> Component.translatable("gui.toolpouch.late_summer");
					case EARLY_AUTUMN -> Component.translatable("gui.toolpouch.early_autumn");
					case MID_AUTUMN -> Component.translatable("gui.toolpouch.mid_autumn");
					case LATE_AUTUMN -> Component.translatable("gui.toolpouch.late_autumn");
					case EARLY_WINTER -> Component.translatable("gui.toolpouch.early_winter");
					case MID_WINTER -> Component.translatable("gui.toolpouch.mid_winter");
					case LATE_WINTER -> Component.translatable("gui.toolpouch.late_winter");
				},
				switch (subSeason.getSeason()) {
					case Season.SPRING -> ToolPouchClient.CONFIG.infoOverlaySettings.overlayColors.spring.get().argb();
					case Season.SUMMER -> ToolPouchClient.CONFIG.infoOverlaySettings.overlayColors.summer.get().argb();
					case Season.AUTUMN -> ToolPouchClient.CONFIG.infoOverlaySettings.overlayColors.autumn.get().argb();
					case Season.WINTER -> ToolPouchClient.CONFIG.infoOverlaySettings.overlayColors.winter.get().argb();
				}
		);
	}

	public static boolean toolPouchHasCalendar(Player player) {
		return ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(SSItems.CALENDAR));
	}
}
