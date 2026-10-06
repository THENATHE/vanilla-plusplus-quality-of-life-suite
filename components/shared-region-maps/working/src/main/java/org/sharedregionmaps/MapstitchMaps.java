package org.sharedregionmaps;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import org.sharedregionmaps.mixin.BundleContentsAccessor;
import org.joml.Vector2i;

import java.util.ArrayList;

/** Optional metadata access without a compile-time or runtime MapStitch dependency. */
public final class MapstitchMaps {
    private static final Identifier MAP_CENTER = Identifier.fromNamespaceAndPath("mapstitch", "map_center");

    private MapstitchMaps() {}

    @SuppressWarnings("unchecked")
    public static boolean refreshCenter(ItemStack stack, ServerLevel level) {
        if (stack.get(DataComponents.MAP_ID) == null) return false;
        DataComponentType<?> registered = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(MAP_CENTER);
        if (registered == null) return false;
        // A newly created map can enter an atlas before MapStitch's loose-item
        // synchronization has initialized this component. Its ID is not enough
        // for either of the original client's map views to draw it.
        Object previous = stack.get(registered);
        if (previous != null && !(previous instanceof Vector2i)) return false;
        var data = MapItem.getSavedData(stack, level);
        if (data == null) return false;
        if (previous instanceof Vector2i center && center.x == data.centerX && center.y == data.centerZ)
            return false;
        stack.set((DataComponentType<Vector2i>) registered, new Vector2i(data.centerX, data.centerZ));
        return true;
    }

    public static void repairAtlas(ItemStack atlas, ServerLevel level) {
        var centerType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(MAP_CENTER);
        var contents = atlas.get(DataComponents.BUNDLE_CONTENTS);
        if (centerType == null || contents == null || contents.isEmpty()) return;
        ArrayList<ItemStackTemplate> repaired = null;
        for (int i = 0; i < contents.size(); i++) {
            var template = contents.items().get(i);
            // Persisted maps may already have a center from a previous ID or scale.
            // Validate present values too; a wrong center survives reconnect and
            // can place a real map in the wrong atlas grid cell without Polymer.
            if (template.get(DataComponents.MAP_ID) == null) continue;
            var map = template.create();
            if (refreshCenter(map, level)) {
                if (repaired == null) repaired = new ArrayList<>(contents.items());
                repaired.set(i, ItemStackTemplate.fromNonEmptyStack(map));
            }
        }
        if (repaired != null) {
            // Never reinsert through normal bundle limits, merge duplicates or
            // reorder contents. Even an over-capacity legacy atlas is retained.
            atlas.set(DataComponents.BUNDLE_CONTENTS,
                    BundleContentsAccessor.sharedmaps$create(repaired, contents.getSelectedItemIndex()));
        }
    }
}
