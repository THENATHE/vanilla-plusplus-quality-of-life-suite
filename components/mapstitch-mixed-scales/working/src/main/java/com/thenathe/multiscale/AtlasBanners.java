package com.thenathe.multiscale;

import java.util.LinkedHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/** One coherent banner edit across existing maps in the enabled generation layers. */
public final class AtlasBanners {
    private AtlasBanners() {}

    public static boolean toggle(ItemStack atlas, ServerLevel level, BlockPos pos) {
        MapBanner banner = MapBanner.fromWorld(level, pos);
        if (banner == null) return false;
        int mask = AtlasOptions.generationMask(atlas);
        var maps = new LinkedHashSet<MapItemSavedData>();
        var targets = new LinkedHashSet<MapItemSavedData>();
        for (var entry : atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).items()) {
            var id = entry.get(DataComponents.MAP_ID);
            if (id == null) continue;
            var data = MapItem.getSavedData(id, level);
            if (data == null || !data.dimension.equals(level.dimension())) continue;
            maps.add(data);
            if (data.scale >= 0 && data.scale <= 4 && (mask & (1 << data.scale)) != 0
                    && MixedScaleMaps.covers(data, pos.getX(), pos.getZ())) targets.add(data);
        }
        if (targets.isEmpty()) return false;

        // Matching includes the live banner's name and color: renamed banners update
        // their labels on every selected map rather than removing an obsolete label.
        boolean remove = targets.stream().allMatch(data -> data.getBanners().contains(banner));
        for (var data : targets) {
            // Vanilla rejects the outermost map pixels even though the map covers
            // the block. Check before editing any layer, so an edge/full map never
            // leaves only part of the requested group changed. Locked maps still
            // accept decorations in vanilla and retain that behavior here.
            double unit = 1 << data.scale;
            double x = (pos.getX() + 0.5D - data.centerX) / unit;
            double z = (pos.getZ() + 0.5D - data.centerZ) / unit;
            if (x < -63 || x > 63 || z < -63 || z > 63) return false;
            if (!remove && !data.getBanners().contains(banner)
                    && data.isTrackedCountOverLimit(MapItemSavedData.TRACKED_DECORATION_LIMIT)) return false;
        }
        for (var data : targets) {
            if (remove || !data.getBanners().contains(banner)) data.toggleBanner(level, pos);
        }
        boolean retained = maps.stream().anyMatch(data -> data.getBanners().stream().anyMatch(marker -> marker.pos().equals(pos)));
        AtlasBannerEvents.AFTER_EDIT.invoker().afterEdit(atlas, level, pos, retained);
        return true;
    }
}
