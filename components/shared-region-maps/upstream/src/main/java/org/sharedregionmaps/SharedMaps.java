package org.sharedregionmaps;

import org.sharedregionmaps.mixin.MapDataAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;

/** Explicit opt-in API for creation of ordinary maps. Unknown plugin allocators stay independent. */
public final class SharedMaps {
    private static final boolean INTERDIMENSIONAL_MARKERS =
            FabricLoader.getInstance().isModLoaded("interdimensional-map-markers");
    private SharedMaps() {}

    public static ItemStack create(ServerLevel level, int x, int z, byte scale, boolean tracking, boolean unlimited) {
        if (!tracking || unlimited || scale < 0 || scale > 4)
            return MapItem.create(level, x, z, scale, tracking, unlimited);
        ItemStack stack = new ItemStack(Items.FILLED_MAP);
        stack.set(DataComponents.MAP_ID, RegionIndex.get(level).getOrCreate(level, Region.at(level.dimension(), x, z, scale)));
        MapstitchMaps.refreshCenter(stack, level);
        return stack;
    }

    public static boolean ordinary(MapItemSavedData data) {
        if (data == null || data.getClass() != MapItemSavedData.class || data.locked || data.scale < 0 || data.scale > 4)
            return false;
        MapDataAccessor flags = (MapDataAccessor) data;
        if (!flags.sharedmaps$trackingPosition() || flags.sharedmaps$unlimitedTracking()) return false;
        for (var decoration : data.getDecorations()) {
            // This mod uses vanilla red/blue markers for temporary player positions.
            // Compare registered types, not merely matching custom texture names.
            if (INTERDIMENSIONAL_MARKERS && (decoration.type().equals(MapDecorationTypes.RED_MARKER)
                    || decoration.type().equals(MapDecorationTypes.BLUE_MARKER))) continue;
            var icon = decoration.type().value().assetId();
            if (!icon.getNamespace().equals("minecraft")) return false;
            String path = icon.getPath();
            if (!path.endsWith("_banner") && !java.util.Set.of("player", "player_off_map", "player_off_limits", "frame").contains(path))
                return false;
        }
        return Region.of(data).equals(Region.at(data.dimension, data.centerX, data.centerZ, data.scale));
    }

    public static boolean scale(ItemStack stack, ServerLevel level) {
        MapItemSavedData source = MapItem.getSavedData(stack, level);
        if (!stack.is(Items.FILLED_MAP) || !ordinary(source) || source.scale >= 4
                || stack.has(DataComponents.CUSTOM_DATA)
                || !stack.getOrDefault(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY).decorations().isEmpty()) return false;
        // Provenance is required: visually ordinary custom artwork cannot be identified from pixels.
        // Legacy and unknown plugin maps therefore remain independent, including their scaled outputs.
        RegionIndex index = RegionIndex.get(level);
        if (!index.contains(stack.get(DataComponents.MAP_ID), source)) return false;
        stack.set(DataComponents.MAP_ID, index.getOrCreate(level,
                Region.at(source.dimension, source.centerX, source.centerZ, source.scale + 1)));
        return true;
    }
}
