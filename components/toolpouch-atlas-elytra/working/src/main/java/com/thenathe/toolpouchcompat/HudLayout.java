package com.thenathe.toolpouchcompat;

import me.pajic.mapstitch.MapStitchClient;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.toolpouch.ToolPouchClient;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import me.pajic.toolpouch.hud.InfoOverlays;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Current-frame minimap bounds; no guessed information-row count or persisted HUD state. */
public final class HudLayout {
    private static GuiGraphicsExtractor mapGraphics;
    private static boolean pouchAtlas;
    private static boolean mapShown;
    private static boolean mapRight;
    private static float mapOriginY;
    private static float mapScaleY;
    private static float mapLocalBottom;
    private static float mapLocalTop;
    private static int detailCount;
    private static boolean detailsBottom;
    private static Integer detailShift;

    private HudLayout() {}

    public static void register() {
        // All client entrypoints have run by CLIENT_STARTED, regardless of mod
        // initialization order. Keep the original identifier as an empty layer.
        HudElementRegistry.replaceElement(Identifier.fromNamespaceAndPath("toolpouch", "info_overlay"),
                original -> (graphics, delta) -> {});
        HudElementRegistry.attachElementAfter(Identifier.fromNamespaceAndPath("mapstitch", "minimap"),
                Identifier.fromNamespaceAndPath("toolpouch_atlas_elytra_compat", "details_after_atlas"),
                (graphics, delta) -> InfoOverlays.render(graphics));
    }

    public static void beginMap(GuiGraphicsExtractor graphics) {
        mapGraphics = graphics;
        mapShown = false;
        mapLocalBottom = 128;
        mapLocalTop = 0;
        mapScaleY = 1;
        var player = Minecraft.getInstance().player;
        pouchAtlas = player != null && ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(ModItems.ATLAS));
    }

    public static void translated(float y) {
        mapShown = true;
        mapOriginY = y;
        mapRight = MapStitchClient.CONFIG.minimap.position.get().name().endsWith("RIGHT");
    }

    public static void scaled(float y) { mapScaleY = y; }

    public static void drawnTo(float y) { mapLocalBottom = Math.max(mapLocalBottom, y); }

    public static void background(float top, float bottom) {
        mapLocalTop = Math.min(mapLocalTop, top);
        drawnTo(bottom);
    }

    public static void beginDetails(int count) {
        detailCount = count;
        detailsBottom = ToolPouchClient.CONFIG.infoOverlaySettings.position.get().name().startsWith("BOTTOM");
        detailShift = null;
    }

    public static int shift(GuiGraphicsExtractor graphics, int originalTextY) {
        boolean detailsRight = ToolPouchClient.CONFIG.infoOverlaySettings.position.get().name().endsWith("RIGHT");
        if (!mapShown || !pouchAtlas || graphics != mapGraphics || detailsRight != mapRight || detailCount == 0) return 0;
        if (detailShift == null) {
            // Original bottom-corner rendering emits the lowest row first. Keep its
            // ordering while finding the top of the whole details block.
            int top = originalTextY - (detailsBottom ? 12 * (detailCount - 1) : 0);
            int requiredTop = (int) Math.ceil(mapOriginY + mapScaleY * mapLocalBottom) + 4;
            int targetTop = Math.max(top, requiredTop);
            var mc = Minecraft.getInstance();
            int textHeight = 12 * (detailCount - 1) + mc.font.lineHeight;
            if (targetTop + textHeight > mc.getWindow().getGuiScaledHeight() - 2) {
                // Bottom minimaps leave no space below; use the free space above
                // without moving either configured corner or changing offsets.
                targetTop = (int) Math.floor(mapOriginY + mapScaleY * mapLocalTop) - 4 - textHeight;
            }
            detailShift = targetTop - top;
        }
        return detailShift;
    }
}
