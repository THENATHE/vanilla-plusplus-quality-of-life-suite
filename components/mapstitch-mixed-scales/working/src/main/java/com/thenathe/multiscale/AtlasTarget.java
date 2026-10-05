package com.thenathe.multiscale;

import com.thenathe.toolpouchcompat.AtlasBridge;
import java.util.ArrayList;
import java.util.List;
import me.pajic.mapstitch.compat.AccessoryUtil;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

/** Player-owned physical location; a map anchor rejects stale replacement books. */
public record AtlasTarget(int location, int index, int anchor) {
    public static final int INVENTORY = 0, ACCESSORY = 1, POUCH = 2;

    public record Handle(ItemStack atlas, Runnable save) {}

    public static int anchor(ItemStack atlas) {
        for (var map : atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).items()) {
            var id = map.get(DataComponents.MAP_ID);
            if (id != null) return id.id();
        }
        return -1;
    }

    public Handle resolve(Player player) {
        if (index < 0) return null;
        ItemStack atlas;
        Runnable save = () -> {};
        if (location == INVENTORY) {
            if (index >= player.getInventory().getContainerSize()) return null;
            atlas = player.getInventory().getItem(index);
        } else if (location == ACCESSORY) {
            if (AccessoryUtil.INSTANCE == null) return null;
            var atlases = AccessoryUtil.INSTANCE.getAtlases(player);
            if (index >= atlases.size()) return null;
            atlas = atlases.get(index);
        } else if (location == POUCH) {
            var atlases = AtlasBridge.atlases(player);
            if (index >= atlases.size()) return null;
            atlas = atlases.get(index);
            save = () -> AtlasBridge.save(player, atlas, index);
        } else return null;
        if (!atlas.is(ModItems.ATLAS)) return null;
        if (anchor < 0 ? anchor(atlas) != -1 : atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)
                .items().stream().noneMatch(map -> map.get(DataComponents.MAP_ID) != null && map.get(DataComponents.MAP_ID).id() == anchor)) return null;
        return new Handle(atlas, save);
    }

    public static List<AtlasTarget> all(Player player) {
        var targets = new ArrayList<AtlasTarget>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) add(targets, player.getInventory().getItem(i), INVENTORY, i);
        if (AccessoryUtil.INSTANCE != null) {
            var items = AccessoryUtil.INSTANCE.getAtlases(player);
            for (int i = 0; i < items.size(); i++) add(targets, items.get(i), ACCESSORY, i);
        }
        var pouch = AtlasBridge.atlases(player);
        for (int i = 0; i < pouch.size(); i++) add(targets, pouch.get(i), POUCH, i);
        return targets;
    }

    private static void add(List<AtlasTarget> targets, ItemStack stack, int location, int index) {
        if (stack.is(ModItems.ATLAS)) targets.add(new AtlasTarget(location, index, anchor(stack)));
    }

    /** Match the existing minimap lookup's source priority without comparing copies across locations. */
    public static AtlasTarget firstForScan(Player player, List<String> locations) {
        if (locations.contains("accessories")) {
            if (AccessoryUtil.INSTANCE != null) {
                var first = AccessoryUtil.INSTANCE.getFirstItem(ModItems.ATLAS, player);
                if (first.is(ModItems.ATLAS)) {
                    var accessories = AccessoryUtil.INSTANCE.getAtlases(player);
                    for (int i = 0; i < accessories.size(); i++)
                        if (accessories.get(i) == first) return new AtlasTarget(ACCESSORY, i, anchor(first));
                    for (int i = 0; i < accessories.size(); i++)
                        if (ItemStack.isSameItemSameComponents(accessories.get(i), first))
                            return new AtlasTarget(ACCESSORY, i, anchor(first));
                    return null;
                }
            }
            var pouch = AtlasBridge.atlases(player);
            if (!pouch.isEmpty()) return new AtlasTarget(POUCH, 0, anchor(pouch.getFirst()));
        }
        if (locations.contains("mainHand") && player.getMainHandItem().is(ModItems.ATLAS)) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++)
                if (player.getInventory().getItem(i) == player.getMainHandItem())
                    return new AtlasTarget(INVENTORY, i, anchor(player.getMainHandItem()));
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.ATLAS) && ((i == 40 && locations.contains("offhand"))
                    || (i < 9 && locations.contains("hotbar"))
                    || (i >= 9 && i < 36 && locations.contains("inventory"))))
                return new AtlasTarget(INVENTORY, i, anchor(stack));
        }
        return null;
    }

    public static AtlasTarget find(Player player, ItemStack wanted) {
        var targets = all(player);
        for (var target : targets) {
            var handle = target.resolve(player);
            if (handle != null && handle.atlas() == wanted) return target;
        }
        return null;
    }
}
