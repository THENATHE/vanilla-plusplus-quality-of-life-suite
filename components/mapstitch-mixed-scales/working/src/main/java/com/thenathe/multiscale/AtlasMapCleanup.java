package com.thenathe.multiscale;

import java.util.ArrayList;
import java.util.HashSet;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.AtlasItem;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.maps.MapId;

/** Convert redundant atlas item copies to blanks; never delete world map records. */
public final class AtlasMapCleanup {
    private AtlasMapCleanup() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) ->
                dispatcher.register(Commands.literal("dedupemaps").executes(context -> cleanup(context.getSource()))));
    }

    public static int cleanup(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var handle = AtlasMapExtraction.selectedAtlas(player);
        if (!player.isAlive() || handle == null) {
            source.sendFailure(Component.literal("Hold an atlas in either hand, or put one in your active Tool Pouch."));
            return 0;
        }
        var atlas = handle.atlas();
        var contents = atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        var retained = new ArrayList<ItemStackTemplate>();
        var seen = new HashSet<MapId>();
        long removed = 0;
        int selected = -1;
        for (int index = 0; index < contents.size(); index++) {
            var entry = contents.items().get(index);
            var id = entry.get(DataComponents.MAP_ID);
            if (id != null) {
                if (!seen.add(id)) { removed += entry.count(); continue; }
                removed += Math.max(0, entry.count() - 1);
                var kept = entry.create();
                kept.setCount(1);
                entry = ItemStackTemplate.fromNonEmptyStack(kept);
            }
            if (index == contents.getSelectedItemIndex()) selected = retained.size();
            retained.add(entry);
        }
        if (removed == 0) {
            source.sendSuccess(() -> Component.literal("No duplicate map IDs in this atlas. Different map IDs are preserved."), false);
            return 0;
        }
        var updated = new BundleContents(retained);
        if (selected >= 0) {
            var mutable = updated.asMutable();
            mutable.toggleSelectedItem(selected);
            updated = mutable.toImmutable();
        }
        atlas.set(DataComponents.BUNDLE_CONTENTS, updated);
        long count = retained.stream().mapToLong(ItemStackTemplate::count).sum();
        int quarter = Math.max(1, (AtlasItem.getMaxSize().intValue() * 64) / 4);
        atlas.set(ModDataComponents.ATLAS_FULLNESS, (int) Math.min(4, count == 0 ? 0 : count / quarter + 1));
        MixedScaleMaps.selectActive(atlas, player.level(), player, false);
        handle.save().run();
        long dropped = 0;
        for (long remaining = removed; remaining > 0;) {
            int batch = (int) Math.min(64, remaining);
            var blanks = new ItemStack(Items.MAP, batch);
            player.getInventory().add(blanks);
            if (!blanks.isEmpty()) {
                dropped += blanks.getCount();
                player.drop(blanks, false, Prediction.SERVER_ONLY);
            }
            remaining -= batch;
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        MapRefresh.send(player);
        final long total = removed, overflow = dropped;
        source.sendSuccess(() -> Component.literal("Removed " + total + " duplicate map copies; returned "
                + (total - overflow) + " empty maps to your inventory and dropped " + overflow
                + " at your feet. Kept one copy per map ID; distinct map records are unchanged."), false);
        return (int) Math.min(Integer.MAX_VALUE, removed);
    }
}
