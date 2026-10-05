package com.thenathe.multiscale;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.ModItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.BundleContents;

/** Extract only matching contents from the player's held or active pouch atlas. */
public final class AtlasMapExtraction {
    private AtlasMapExtraction() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            var root = Commands.literal("extractmap")
                    .executes(context -> usage(context.getSource()))
                    .then(Commands.literal("empty").executes(context -> extract(context.getSource(), null, null, true)));
            var scales = Commands.literal("scale");
            for (int layer = 0; layer < 5; layer++) {
                root.then(scale(layer));
                scales.then(scale(layer));
            }
            root.then(scales);
            root.then(dimension());
            root.then(Commands.literal("dimension").then(dimension()));
            dispatcher.register(root);
        });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> scale(int layer) {
        return Commands.literal("1:" + (1 << layer))
                .executes(context -> extract(context.getSource(), layer, null, false))
                .then(Commands.argument("dimension", IdentifierArgument.id())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(context.getSource().levels().stream().map(key -> key.identifier()), builder))
                        .executes(context -> extract(context.getSource(), layer, IdentifierArgument.getId(context, "dimension"), false)));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, Identifier> dimension() {
        var argument = Commands.argument("dimension", IdentifierArgument.id())
                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(context.getSource().levels().stream().map(key -> key.identifier()), builder))
                .executes(context -> extract(context.getSource(), null, IdentifierArgument.getId(context, "dimension"), false));
        for (int layer = 0; layer < 5; layer++) {
            final int selected = layer;
            argument.then(Commands.literal("1:" + (1 << layer))
                    .executes(context -> extract(context.getSource(), selected, IdentifierArgument.getId(context, "dimension"), false)));
        }
        return argument;
    }

    private static int usage(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Use /extractmap <1:1|1:2|1:4|1:8|1:16> [dimension], /extractmap <dimension>, or /extractmap empty. Explicit scale/dimension prefixes also work. Hold an atlas or use the atlas in your active Tool Pouch."), false);
        return 0;
    }

    private static AtlasTarget.Handle selectedAtlas(ServerPlayer player) {
        for (var held : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (!held.is(ModItems.ATLAS)) continue;
            var target = AtlasTarget.find(player, held);
            if (target != null) return target.resolve(player);
        }
        for (var target : AtlasTarget.all(player)) {
            if (target.location() == AtlasTarget.POUCH) return target.resolve(player);
        }
        return null;
    }

    public static int extract(CommandSourceStack source, Integer scale, Identifier dimension, boolean empty) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        var handle = selectedAtlas(player);
        if (!player.isAlive() || handle == null) {
            source.sendFailure(Component.literal("Hold an atlas in either hand, or put one in your active Tool Pouch."));
            return 0;
        }
        var atlas = handle.atlas();
        var contents = atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        var retained = new ArrayList<ItemStackTemplate>();
        var extracted = new ArrayList<ItemStack>();
        int selected = -1;
        long total = 0;
        for (int index = 0; index < contents.size(); index++) {
            var entry = contents.items().get(index);
            boolean match = empty && (entry.is(Items.MAP) || entry.is(Items.PAPER));
            if (!empty && entry.is(Items.FILLED_MAP)) {
                var id = entry.get(DataComponents.MAP_ID);
                var data = id == null ? null : MapItem.getSavedData(id, player.level());
                match = data != null && (scale == null || data.scale == scale)
                        && (dimension == null || data.dimension.identifier().equals(dimension));
            }
            if (match) {
                var stack = entry.create();
                total += stack.getCount();
                extracted.add(stack);
            } else {
                if (index == contents.getSelectedItemIndex()) selected = retained.size();
                retained.add(entry);
            }
        }
        if (extracted.isEmpty()) {
            source.sendFailure(Component.literal("No matching " + (empty ? "empty maps or paper" : "filled maps") + " in that atlas."));
            return 0;
        }
        var updated = new BundleContents(retained);
        if (selected >= 0) {
            var mutable = updated.asMutable();
            mutable.toggleSelectedItem(selected);
            updated = mutable.toImmutable();
        }
        atlas.set(DataComponents.BUNDLE_CONTENTS, updated);
        long remaining = retained.stream().mapToLong(ItemStackTemplate::count).sum();
        int quarter = Math.max(1, (AtlasItem.getMaxSize().intValue() * 64) / 4);
        atlas.set(ModDataComponents.ATLAS_FULLNESS, (int) Math.min(4, remaining == 0 ? 0 : remaining / quarter + 1));
        atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
        MixedScaleMaps.selectActive(atlas, player.level(), player, false);
        handle.save().run();
        long dropped = 0;
        for (var stack : extracted) {
            player.getInventory().add(stack);
            if (!stack.isEmpty()) {
                dropped += stack.getCount();
                player.drop(stack, false, Prediction.SERVER_ONLY);
            }
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        final long count = total;
        final long overflow = dropped;
        source.sendSuccess(() -> Component.literal("Extracted " + count + (empty ? " empty maps/paper" : " maps") + "; "
                + (count - overflow) + " added to your inventory, " + overflow + " dropped at your feet."), false);
        return (int) Math.min(Integer.MAX_VALUE, total);
    }
}
