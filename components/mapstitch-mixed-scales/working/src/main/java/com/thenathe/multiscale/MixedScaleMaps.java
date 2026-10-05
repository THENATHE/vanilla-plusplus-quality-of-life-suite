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
        AtlasOptions.ensureIdentity(atlas);
        int scale = activeScale(atlas);
        if (atlas.getOrDefault(ModDataComponents.ATLAS_SCALE, -1) != scale)
            atlas.set(ModDataComponents.ATLAS_SCALE, scale);
        if (generate) {
            int mask = AtlasOptions.generationMask(atlas);
            // Freeze old books' single-scale generation before their minimap is changed.
            AtlasOptions.setGenerationMask(atlas, mask);
            boolean createdAny = false;
            for (int layer = 0; layer < 5; layer++) {
                if ((mask & (1 << layer)) == 0) continue;
                var existing = coveringMap(atlas, level, owner, layer);
                if (existing == null) createdAny |= generateMap(atlas, level, owner, layer);
                existing = coveringMap(atlas, level, owner, layer);
                // Other enabled layers continue filling while the minimap shows its own layer.
                if (existing != null && layer != scale) updateLayer(atlas, existing, level, owner);
            }
            if (createdAny && owner instanceof ServerPlayer player) {
                // Player.playSound on the server excludes the player themselves. Use a
                // normal vanilla sound packet so the explorer hears one creation chime.
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                        net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT),
                        net.minecraft.sounds.SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(),
                        1.0F, 1.0F, level.getRandom().nextLong()));
            }
        }
        var active = coveringMap(atlas, level, owner, scale);
        atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, active == null ? -1 : active.get(DataComponents.MAP_ID).id());
    }

    private static ItemStack coveringMap(ItemStack atlas, ServerLevel level, Entity owner, int scale) {
        for (var map : atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).items()) {
            var id = map.get(DataComponents.MAP_ID);
            if (id == null) continue;
            var data = MapItem.getSavedData(id, level);
            if (data != null && data.scale == scale && data.dimension.equals(level.dimension())
                    && !ModUtil.isExplorationMap(map.create(), level)
                    && covers(data, owner.getBlockX(), owner.getBlockZ())) return map.create();
        }
        return null;
    }

    private static void updateLayer(ItemStack atlas, ItemStack map, ServerLevel level, Entity owner) {
        var data = MapItem.getSavedData(map, level);
        if (data == null) return;
        if (!data.locked) ((MapItem) map.getItem()).update(level, owner, data);
        if (owner instanceof ServerPlayer player) {
            data.tickCarriedBy(player, atlas, null);
            var id = map.get(DataComponents.MAP_ID);
            if (me.pajic.mapstitch.util.CompatFlags.REMAPPED_LOADED)
                me.pajic.mapstitch.compat.RemappedCompat.sendMapPackets(id, data, player);
            else ModUtil.sendVanillaMapPacket(id, data, player, false);
        }
    }

    private static boolean generateMap(ItemStack atlas, ServerLevel level, Entity owner, int scale) {
        BundleContents contents = atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        int blankIndex = -1;
        for (int i = 0; i < contents.size(); i++) {
            var entry = contents.items().get(i);
            if (entry.is(Items.PAPER) || entry.is(Items.MAP)) { blankIndex = i; break; }
        }
        if (blankIndex < 0) return false;

        // Sharing is still dimension + aligned center + scale. Another layer covering
        // this position is not a reason to discard a newly requested scale.
        ItemStack created = SharedMaps.create(level, owner.getBlockX(), owner.getBlockZ(), (byte) scale, true, false);
        var data = MapItem.getSavedData(created, level);
        var id = created.get(DataComponents.MAP_ID);
        if (data == null || id == null) return false;
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
        if (!data.locked) ((MapItem) created.getItem()).update(level, owner, data);
        if (owner instanceof ServerPlayer player) {
            player.awardStat(Stats.ITEM_USED.get(atlas.getItem()));
            player.containerMenu.slotsChanged(player.getInventory());
        }
        return true;
    }
}
