package com.thenathe.multiscale;

import java.util.ArrayList;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2i;
import org.sharedregionmaps.SharedMaps;

/** Scale selection never converts, merges or deletes existing map records. */
public final class MixedScaleMaps {
    private MixedScaleMaps() {}

    public static int activeScale(ItemStack atlas) {
        int scale = atlas.getOrDefault(ModDataComponents.ATLAS_SCALE, 0);
        return scale >= 0 && scale <= 4 ? scale : 0;
    }

    public static boolean covers(MapItemSavedData data, int x, int z) {
        long radius = 64L << data.scale;
        return x >= (long) data.centerX - radius && x < (long) data.centerX + radius
                && z >= (long) data.centerZ - radius && z < (long) data.centerZ + radius;
    }

    /** Called before upstream ticking and whenever its legacy selection path runs. */
    public static void selectActive(ItemStack atlas, ServerLevel level, Entity owner, boolean generate) {
        int scale = activeScale(atlas);
        if (atlas.getOrDefault(ModDataComponents.ATLAS_SCALE, -1) != scale)
            atlas.set(ModDataComponents.ATLAS_SCALE, scale);
        BundleContents contents = atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        int blankIndex = -1;
        for (int i = 0; i < contents.size(); i++) {
            ItemStackTemplate map = contents.items().get(i);
            var id = map.get(DataComponents.MAP_ID);
            if (id != null) {
                var data = MapItem.getSavedData(id, level);
                if (data != null && data.scale == scale && data.dimension.equals(level.dimension())
                        && !ModUtil.isExplorationMap(map.create(), level)
                        && covers(data, owner.getBlockX(), owner.getBlockZ())) {
                    atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, id.id());
                    return;
                }
            } else if (blankIndex == -1 && (map.is(Items.PAPER) || map.is(Items.MAP))) {
                blankIndex = i;
            }
        }
        atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
        if (!generate || blankIndex < 0) return;

        // Sharing is still dimension + aligned center + scale. Another layer covering
        // this position is not a reason to discard a newly requested scale.
        ItemStack created = SharedMaps.create(level, owner.getBlockX(), owner.getBlockZ(), (byte) scale, true, false);
        var data = MapItem.getSavedData(created, level);
        var id = created.get(DataComponents.MAP_ID);
        if (data == null || id == null) return;
        created.set(ModDataComponents.MAP_CENTER, new Vector2i(data.centerX, data.centerZ));

        // Replace exactly one blank without re-inserting the whole atlas through
        // ordinary bundle capacity/merge rules. Preserve every other stack and ID.
        var entries = new ArrayList<>(contents.items());
        ItemStack remaining = entries.get(blankIndex).create();
        remaining.shrink(1);
        int selected = contents.getSelectedItemIndex();
        if (remaining.isEmpty()) entries.set(blankIndex, ItemStackTemplate.fromNonEmptyStack(created));
        else {
            entries.set(blankIndex, ItemStackTemplate.fromNonEmptyStack(remaining));
            entries.add(blankIndex + 1, ItemStackTemplate.fromNonEmptyStack(created));
            if (selected > blankIndex) selected++;
        }
        BundleContents updated = new BundleContents(entries);
        if (selected >= 0 && selected < entries.size()) {
            var mutable = updated.asMutable();
            mutable.toggleSelectedItem(selected);
            updated = mutable.toImmutable();
        }
        atlas.set(DataComponents.BUNDLE_CONTENTS, updated);
        int count = entries.stream().mapToInt(ItemStackTemplate::count).sum();
        int quarter = Math.max(1, (AtlasItem.getMaxSize().intValue() * 64) / 4);
        atlas.set(ModDataComponents.ATLAS_FULLNESS, Math.min(4, count == 0 ? 0 : count / quarter + 1));
        atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, id.id());
        if (!data.locked) ((MapItem) created.getItem()).update(level, owner, data);
        if (owner instanceof ServerPlayer player) {
            player.awardStat(Stats.ITEM_USED.get(atlas.getItem()));
            player.playSound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT);
            player.containerMenu.slotsChanged(player.getInventory());
        }
    }
}
