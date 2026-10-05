package me.pajic.toolpouch.hud;

import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import me.pajic.toolpouch.ToolPouchClient;
import me.pajic.toolpouch.util.CompatFlags;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.List;

public class MinimapOverlay {

	private static final Minecraft MC = Minecraft.getInstance();
	private static final MapRenderState STATE = new MapRenderState();
	private static final Identifier BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("container/cartography_table/map");
	public static boolean minimapOn = true;
	public static boolean minimapActive = false;

	@SuppressWarnings("DataFlowIssue")
	public static void render(GuiGraphicsExtractor guiGraphics) {
		if (minimapOn && MC.player != null && MC.level != null && !MC.gui.hud.isHidden() && !MC.gui.hud.getDebugOverlay().showDebugScreen()) {
			List<ItemStackTemplate> maps = ToolPouchUtil.getItemsFromToolPouch(MC.player, stack -> stack.get(DataComponents.MAP_ID) != null);
			if (!maps.isEmpty()) {
				ItemStackTemplate map = maps.getFirst();
				MapId mapId = map.get(DataComponents.MAP_ID);
				MapItemSavedData mapData = MapItem.getSavedData(map.get(DataComponents.MAP_ID), MC.level);
				if (mapData != null) {
					int width = MC.getWindow().getGuiScaledWidth();
					int height = MC.getWindow().getGuiScaledHeight();
					int offsetX = ToolPouchClient.CONFIG.minimapOverlaySettings.offsetX.get();
					int offsetY = ToolPouchClient.CONFIG.minimapOverlaySettings.offsetY.get();
					float scale = ToolPouchClient.CONFIG.minimapOverlaySettings.size.get();
					int offset = Math.round(scale * switch (ToolPouchClient.CONFIG.minimapOverlaySettings.minimapBackgroundStyle.get()) {
						case TEXTURE -> 12;
						case CLEAR -> 6;
						case NONE -> 4;
					});
					int raisedOffsetX = 0;
					int raisedOffsetY = 0;
					if (CompatFlags.RAISED_LOADED) {
						IntIntImmutablePair offsets = RaisedCompat.getOtherComponentOffsets();
						raisedOffsetX = offsets.leftInt();
						raisedOffsetY = offsets.rightInt();
					}

					IntIntImmutablePair position;
					int scaleOffset = Math.round(128 * scale);
					switch (ToolPouchClient.CONFIG.minimapOverlaySettings.position.get()) {
						case TOP_RIGHT -> position = new IntIntImmutablePair(
								width - offset - scaleOffset - offsetX + raisedOffsetX,
								offset + offsetY + raisedOffsetY + (!MC.player.getActiveEffects().isEmpty() && ToolPouchClient.CONFIG.minimapOverlaySettings.preventEffectOverlap.get() ? 51 : 0)
						);
						case BOTTOM_LEFT -> position = new IntIntImmutablePair(
								offset + offsetX + raisedOffsetX,
								height - offset - scaleOffset - offsetY + raisedOffsetY
						);
						case BOTTOM_RIGHT -> position = new IntIntImmutablePair(
								width - offset - scaleOffset - offsetX + raisedOffsetX,
								height - offset - scaleOffset - offsetY + raisedOffsetY
						);
						default -> position = new IntIntImmutablePair(
								offset + offsetX + raisedOffsetX,
								offset + offsetY + raisedOffsetY
						);
					}
					int x = position.leftInt();
					int y = position.rightInt();

					guiGraphics.pose().pushMatrix();
					guiGraphics.pose().translate(x, y);
					guiGraphics.pose().scale(scale, scale);
					switch (ToolPouchClient.CONFIG.minimapOverlaySettings.minimapBackgroundStyle.get()) {
						case TEXTURE -> guiGraphics.blitSprite(
								RenderPipelines.GUI_TEXTURED,
								BACKGROUND_TEXTURE,
								-8, -8, 144, 144
						);
						case CLEAR -> guiGraphics.fill(-4, -4, 132, 132, ARGB.color(
								ARGB.as8BitChannel(ToolPouchClient.CONFIG.minimapOverlaySettings.minimapBackgroundOpacity.get()),
								0, 0, 0
						));
					}
					MC.getMapRenderer().extractRenderState(mapId, mapData, STATE);
					STATE.decorations.forEach(decor -> decor.renderOnFrame = true);
					guiGraphics.map(STATE);
					guiGraphics.pose().popMatrix();
					minimapActive = true;
					return;
				}
			}
		}
		minimapActive = false;
	}
}
