package com.thenathe.toolpouchcompat;

import me.pajic.mapstitch.MapStitchClient;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.toolpouch.ToolPouchClient;
import me.pajic.toolpouch.util.ToolPouchUtil;
import me.pajic.toolpouch.util.CompatFlags;
import me.pajic.toolpouch.hud.RaisedCompat;
import me.pajic.mapstitch.minimap.MinimapPosition;
import me.pajic.toolpouch.hud.OverlayPosition;
import me.fzzyhmstrs.fzzy_config.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import me.pajic.toolpouch.hud.InfoOverlays;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Current-frame minimap bounds; no guessed information-row count or persisted HUD state. */
public final class HudLayout {
    private static GuiGraphicsExtractor mapGraphics;
    private static boolean pouchAtlas;
    private static String mapSource;
    private static float mapOriginX;
    private static boolean mapShown;
    private static boolean mapRight;
    private static float mapOriginY;
    private static float mapScaleY;
    private static float mapLocalBottom;
    private static float mapLocalTop;
    private static int detailCount;
    private static boolean detailsBottom;
    private static Integer detailShift;
    private static boolean preferencesInitialized;
    private static boolean savingPreferences;
    private static String sharedCorner;
    private static int sharedX, sharedY;

    private HudLayout() {}

    public static void register() {
        synchronizePreferences(null);
        // All client entrypoints have run by CLIENT_STARTED, regardless of mod
        // initialization order. Keep the original identifier as an empty layer.
        HudElementRegistry.replaceElement(Identifier.fromNamespaceAndPath("toolpouch", "info_overlay"),
                original -> (graphics, delta) -> {});
        HudElementRegistry.replaceElement(Identifier.fromNamespaceAndPath("toolpouch", "minimap_overlay"),
                original -> (graphics, delta) -> {});
        HudElementRegistry.replaceElement(Identifier.fromNamespaceAndPath("mapstitch", "minimap"),
                original -> (graphics, delta) -> {
                    beginMap(graphics);
                    me.pajic.mapstitch.minimap.MinimapOverlay.render(graphics);
                    if (!mapShown) me.pajic.toolpouch.hud.MinimapOverlay.render(graphics);
                    else me.pajic.toolpouch.hud.MinimapOverlay.minimapActive = false;
                });
        HudElementRegistry.attachElementAfter(Identifier.fromNamespaceAndPath("mapstitch", "minimap"),
                Identifier.fromNamespaceAndPath("toolpouch_atlas_elytra_compat", "details_after_atlas"),
                (graphics, delta) -> InfoOverlays.render(graphics));
    }

    public static void beginMap(GuiGraphicsExtractor graphics) {
        // Applied settings are also observed between sessions and after file reloads.
        if (Minecraft.getInstance().gui.screen() == null) synchronizePreferences(null);
        mapGraphics = graphics;
        mapShown = false;
        mapSource = "atlas";
        mapLocalBottom = 128;
        mapLocalTop = 0;
        mapScaleY = 1;
        var player = Minecraft.getInstance().player;
        pouchAtlas = player != null && ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(ModItems.ATLAS));
    }

    public static void beginPouchMap(GuiGraphicsExtractor graphics) {
        beginMap(graphics);
        mapSource = "map";
    }

    /** Save only after a native config apply; GUI previews must not become persisted edits. */
    public static void configApplied(Config config) {
        if (config.getId().equals(Identifier.fromNamespaceAndPath("mapstitch", "client_config"))
                || config.getId().equals(Identifier.fromNamespaceAndPath("toolpouch", "client_config"))) {
            synchronizePreferences(config.getId().getNamespace());
        }
    }

    public static void synchronizePreferences(String appliedNamespace) {
        if (savingPreferences || MapStitchClient.CONFIG == null || ToolPouchClient.CONFIG == null) return;
        var atlas = MapStitchClient.CONFIG.minimap;
        var pouch = ToolPouchClient.CONFIG.minimapOverlaySettings;
        var info = ToolPouchClient.CONFIG.infoOverlaySettings;
        String corner;
        int x, y;
        if (!preferencesInitialized) {
            // Adopt the existing pouch details corner, matching its familiar HUD layout.
            corner = info.position.get().name();
            x = atlas.xOffset.get() < 0 ? atlas.xOffset.get() : pouch.offsetX.get();
            y = atlas.yOffset.get() < 0 ? atlas.yOffset.get() : pouch.offsetY.get();
        } else {
            boolean mapChanged = !atlas.position.get().name().equals(sharedCorner);
            boolean pouchChanged = !pouch.position.get().name().equals(sharedCorner);
            boolean infoChanged = !info.position.get().name().equals(sharedCorner);
            // The namespace whose native Apply ran wins simultaneous conflicting edits.
            corner = "mapstitch".equals(appliedNamespace) && mapChanged ? atlas.position.get().name()
                    : infoChanged ? info.position.get().name()
                    : pouchChanged ? pouch.position.get().name()
                    : mapChanged ? atlas.position.get().name() : sharedCorner;
            x = atlas.xOffset.get() != sharedX ? atlas.xOffset.get()
                    : pouch.offsetX.get() != Math.max(0, sharedX) ? pouch.offsetX.get() : sharedX;
            y = atlas.yOffset.get() != sharedY ? atlas.yOffset.get()
                    : pouch.offsetY.get() != Math.max(0, sharedY) ? pouch.offsetY.get() : sharedY;
        }
        boolean mapDirty = !atlas.position.get().name().equals(corner) || atlas.xOffset.get() != x || atlas.yOffset.get() != y;
        boolean pouchDirty = !pouch.position.get().name().equals(corner) || !info.position.get().name().equals(corner)
                || pouch.offsetX.get() != Math.max(0, x) || pouch.offsetY.get() != Math.max(0, y);
        sharedCorner = corner;
        sharedX = x; sharedY = y;
        preferencesInitialized = true;
        savingPreferences = true;
        try {
            atlas.position.accept(MinimapPosition.valueOf(corner));
            pouch.position.accept(OverlayPosition.valueOf(corner));
            info.position.accept(OverlayPosition.valueOf(corner));
            atlas.xOffset.accept(x); atlas.yOffset.accept(y);
            // Pouch validation is nonnegative. Preserve signed atlas values at render time.
            pouch.offsetX.accept(Math.max(0, x)); pouch.offsetY.accept(Math.max(0, y));
            if (mapDirty) MapStitchClient.CONFIG.save();
            if (pouchDirty) ToolPouchClient.CONFIG.save();
        } finally { savingPreferences = false; }
    }

    public static float atlasOriginX(float x) {
        return x + (CompatFlags.RAISED_LOADED ? RaisedCompat.getOtherComponentOffsets().leftInt() : 0);
    }
    public static float atlasOriginY(float y) {
        return y + (CompatFlags.RAISED_LOADED ? RaisedCompat.getOtherComponentOffsets().rightInt() : 0);
    }
    public static float pouchOriginX(float x) {
        int delta = sharedX - ToolPouchClient.CONFIG.minimapOverlaySettings.offsetX.get();
        return x + (sharedCorner.endsWith("RIGHT") ? -delta : delta);
    }
    public static float pouchOriginY(float y) {
        int delta = sharedY - ToolPouchClient.CONFIG.minimapOverlaySettings.offsetY.get();
        return y + (sharedCorner.startsWith("BOTTOM") ? -delta : delta);
    }

    public static void translated(float y) {
        mapShown = true;
        mapOriginY = y;
        mapRight = MapStitchClient.CONFIG.minimap.position.get().name().endsWith("RIGHT");
    }
    public static void translated(float x, float y) { mapOriginX = x; translated(y); }

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
        if (!mapShown || graphics != mapGraphics || detailsRight != mapRight || detailCount == 0) return 0;
        if (detailShift == null) {
            // Original bottom-corner rendering emits the lowest row first. Keep its
            // ordering while finding the top of the whole details block.
            int top = originalTextY - (detailsBottom ? 12 * (detailCount - 1) : 0);
            int padding = ToolPouchClient.CONFIG.infoOverlaySettings.offsetY.get();
            int requiredTop = (int) Math.ceil(mapOriginY + mapScaleY * mapLocalBottom) + 4 + padding;
            int targetTop = Math.max(top, requiredTop);
            var mc = Minecraft.getInstance();
            int textHeight = 12 * (detailCount - 1) + mc.font.lineHeight;
            if (targetTop + textHeight > mc.getWindow().getGuiScaledHeight() - 2) {
                // Bottom minimaps leave no space below; use the free space above
                // without moving either configured corner or changing offsets.
                targetTop = (int) Math.floor(mapOriginY + mapScaleY * mapLocalTop) - 4 - padding - textHeight;
            }
            detailShift = targetTop - top;
        }
        return detailShift;
    }
}
