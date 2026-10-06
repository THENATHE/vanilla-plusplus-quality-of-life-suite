package com.thenathe.multiscale;

import java.util.ArrayList;
import java.util.HashSet;
import me.pajic.mapstitch.component.ModDataComponents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.sharedregionmaps.MapstitchMaps;

/** Repair representation and synchronization, never replace a saved map or invent exploration. */
public final class AtlasMapRepair {
    private AtlasMapRepair() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) ->
                dispatcher.register(Commands.literal("repairmaps")
                        .executes(context -> repair(context.getSource(), false))
                        .then(Commands.literal("check").executes(context -> repair(context.getSource(), true)))));
    }

    public static int repair(CommandSourceStack source, boolean check) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        var handle = AtlasMapExtraction.selectedAtlas(player);
        if (!player.isAlive() || handle == null) {
            source.sendFailure(Component.literal("Hold an atlas in either hand, or put one in your active Tool Pouch."));
            return 0;
        }
        var atlas = handle.atlas();
        var contents = atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        var entries = new ArrayList<>(contents.items());
        var seen = new HashSet<MapId>();
        int missing = 0, centers = 0, duplicates = 0, covering = 0, lockedBlank = 0, blank = 0;
        int scale = MixedScaleMaps.activeScale(atlas);
        for (int index = 0; index < contents.size(); index++) {
            var template = contents.items().get(index);
            var id = template.get(DataComponents.MAP_ID);
            if (id == null && !(template.item().value() instanceof MapItem)) continue;
            var data = id == null ? null : MapItem.getSavedData(id, player.level());
            if (data == null) { missing++; continue; }
            var copy = template.create();
            if (MapstitchMaps.refreshCenter(copy, player.level())) {
                centers++;
                if (!check) entries.set(index, ItemStackTemplate.fromNonEmptyStack(copy));
            }
            if (!seen.add(id)) { duplicates += template.count(); continue; }
            duplicates += Math.max(0, template.count() - 1);
            if (data.scale == scale && data.dimension.equals(player.level().dimension())
                    && MixedScaleMaps.covers(data, player.getBlockX(), player.getBlockZ())) {
                covering++;
                boolean empty = true;
                for (byte color : data.colors) if (color != 0) { empty = false; break; }
                if (empty) { blank++; if (data.locked) lockedBlank++; }
            }
            if (!check) resend(player, id, data);
        }
        if (!check) {
            if (centers > 0) {
                var updated = new BundleContents(entries);
                if (contents.getSelectedItemIndex() != BundleContents.NO_SELECTED_ITEM_INDEX) {
                    var mutable = updated.asMutable();
                    mutable.toggleSelectedItem(contents.getSelectedItemIndex());
                    updated = mutable.toImmutable();
                }
                atlas.set(DataComponents.BUNDLE_CONTENTS, updated);
            }
            MixedScaleMaps.selectActive(atlas, player.level(), player, false);
            handle.save().run();
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            MapRefresh.send(player);
        }
        String summary = (check ? "Atlas check: " : "Atlas refreshed: ") + seen.size() + " map records, "
                + centers + (check ? " incorrect/missing centers" : " centers repaired") + ", " + missing
                + " missing records, " + duplicates + " duplicate copies. Minimap 1:" + (1 << scale)
                + " in " + player.level().dimension().identifier() + ": " + covering + " covering maps, "
                + blank + " blank (" + lockedBlank + " locked).";
        source.sendSuccess(() -> Component.literal(summary), false);
        if (covering == 0) source.sendSuccess(() -> Component.literal("No map covers this minimap scale/location. Check generation toggles and stored empty maps/paper; repair does not create maps."), false);
        if (missing > 0 || blank > 0) source.sendSuccess(() -> Component.literal("Missing saved records and unexplored/locked blank maps are not recreated or unlocked. Existing map IDs, explored pixels and markers are preserved."), false);
        return check ? seen.size() : Math.max(1, seen.size());
    }

    /** The upstream force flag may contain only the last dirty rectangle, not all saved pixels. */
    public static void resend(ServerPlayer player, MapId id, MapItemSavedData data) {
        MapMetadata.send(id, data, player);
        if (me.pajic.mapstitch.util.CompatFlags.REMAPPED_LOADED) {
            // Preserve the existing optional integration's own color/network representation.
            me.pajic.mapstitch.compat.RemappedCompat.sendMapPackets(id, data, player);
        } else {
            var decorations = new ArrayList<MapDecoration>();
            data.getDecorations().forEach(decorations::add);
            player.connection.send(new ClientboundMapItemDataPacket(id, data.scale, data.locked,
                    decorations, new MapItemSavedData.MapPatch(0, 0, 128, 128, data.colors.clone())));
        }
    }
}
