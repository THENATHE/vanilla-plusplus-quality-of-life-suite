package com.thenathe.multiscale;

import java.util.ArrayList;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.ModItems;
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
import net.minecraft.world.item.component.CustomData;

/** Copies item references to saved maps; never allocates, merges or resets world map records. */
public final class AtlasCartographyCopy {
    private AtlasCartographyCopy() {}

    public static boolean matches(ItemStack atlas, ItemStack book) {
        return atlas.is(ModItems.ATLAS) && book.is(Items.BOOK) && !book.isEmpty();
    }

    public static ItemStack copy(ItemStack source) {
        if (!source.is(ModItems.ATLAS)) return ItemStack.EMPTY;
        var result = source.copyWithCount(1);
        var original = source.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        var maps = new ArrayList<ItemStackTemplate>();
        int selected = -1;
        long count = 0;
        for (int index = 0; index < original.size(); index++) {
            var entry = original.items().get(index);
            if (entry.get(DataComponents.MAP_ID) == null) continue;
            if (index == original.getSelectedItemIndex()) selected = maps.size();
            maps.add(entry); count += entry.count();
        }
        var contents = new BundleContents(maps);
        if (selected >= 0) {
            var mutable = contents.asMutable(); mutable.toggleSelectedItem(selected); contents = mutable.toImmutable();
        }
        result.set(DataComponents.BUNDLE_CONTENTS, contents);
        int quarter = Math.max(1, (AtlasItem.getMaxSize().intValue() * 64) / 4);
        result.set(ModDataComponents.ATLAS_FULLNESS, (int) Math.min(4, count == 0 ? 0 : count / quarter + 1));
        CustomData.update(DataComponents.CUSTOM_DATA, result, tag -> tag.remove(AtlasOptions.IDENTITY));
        AtlasOptions.ensureIdentity(result);
        return result;
    }

    /** Keep a stable preview until the input changes; each taken output gets its own identity. */
    public static ItemStack preview(ItemStack source, ItemStack previous) {
        var result = copy(source);
        if (previous.is(ModItems.ATLAS) && !AtlasOptions.identity(previous).isEmpty()) {
            CustomData.update(DataComponents.CUSTOM_DATA, result,
                    tag -> tag.putString(AtlasOptions.IDENTITY, AtlasOptions.identity(previous)));
            if (ItemStack.isSameItemSameComponents(result, previous)) return previous;
            CustomData.update(DataComponents.CUSTOM_DATA, result, tag -> tag.remove(AtlasOptions.IDENTITY));
            AtlasOptions.ensureIdentity(result);
        }
        return result;
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) ->
                dispatcher.register(Commands.literal("atlas").then(Commands.literal("makecopy")
                        .executes(context -> makeCopy(context.getSource())))));
    }

    public static int makeCopy(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var handle = AtlasMapExtraction.selectedAtlas(player);
        if (!player.isAlive() || handle == null) {
            source.sendFailure(Component.literal("Hold an atlas in either hand, or put one in your active Tool Pouch."));
            return 0;
        }
        int bookSlot = -1;
        for (int index = 0; index < player.getInventory().getContainerSize(); index++)
            if (player.getInventory().getItem(index).is(Items.BOOK)) { bookSlot = index; break; }
        if (bookSlot < 0) {
            source.sendFailure(Component.literal("You need one ordinary book in your inventory to copy an atlas."));
            return 0;
        }
        var output = copy(handle.atlas());
        if (output.isEmpty()) return 0;
        player.getInventory().removeItem(bookSlot, 1);
        // A fresh atlas identity cannot merge with another stack. Inventory.add discards
        // uninserted items in creative mode, so place this one item explicitly or drop it.
        int outputSlot = player.getInventory().getFreeSlot();
        boolean overflow = outputSlot < 0;
        if (overflow) player.drop(output, false, Prediction.SERVER_ONLY);
        else {
            output.setPopTime(5);
            player.getInventory().setItem(outputSlot, output);
        }
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastFullState();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastFullState();
        source.sendSuccess(() -> Component.literal("Copied every stored filled map into a new atlas; used one book. "
                + (overflow ? "The copy dropped at your feet." : "The copy went into your inventory.")
                + " Stored empty maps and paper were not copied."), false);
        return 1;
    }
}
