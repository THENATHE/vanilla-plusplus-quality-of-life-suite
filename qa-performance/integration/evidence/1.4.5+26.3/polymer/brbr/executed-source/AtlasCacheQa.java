package com.thenathe.hotkeywheel.qa;

import com.thenathe.toolpouchcompat.AtlasBridge;
import com.sun.management.ThreadMXBean;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.util.ModClientUtil;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Runs on a real client player in the disposable Fabric game-test world. */
public final class AtlasCacheQa {
    private int checks;
    private static volatile Object benchmarkSink;

    private void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    private static ItemStack atlas(String name, int scale, int mapCount) {
        var atlas = new ItemStack(ModItems.ATLAS);
        atlas.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        atlas.set(ModDataComponents.ATLAS_SCALE, scale);
        atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, 10000 + scale);
        var maps = new ArrayList<ItemStackTemplate>();
        for (int index = 0; index < mapCount; index++) {
            var map = new ItemStack(Items.FILLED_MAP);
            map.set(DataComponents.MAP_ID, new MapId(10000 + index));
            maps.add(ItemStackTemplate.fromNonEmptyStack(map));
        }
        atlas.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(maps));
        return atlas;
    }

    private static ItemStack pouch(List<ItemStack> items) {
        var pouch = new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
        pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        return pouch;
    }

    public static Map<String, Object> run(Player player) throws Exception {
        return new AtlasCacheQa().test(player);
    }

    private Map<String, Object> test(Player player) throws Exception {
        check(player.level().isClientSide(), "Must exercise the client bridge branch");
        var inventory = player.getInventory().getNonEquipmentItems();
        var savedInventory = new ArrayList<>(inventory);
        var savedLegs = player.getItemBySlot(EquipmentSlot.LEGS);
        var savedMenu = player.containerMenu;
        boolean savedInventoryUse = ToolPouch.CONFIG.allowUseFromInventory.get();
        var savedMinimapScan = new ArrayList<>(MapStitch.CONFIG.itemRequirements.minimapAtlasScan);
        var report = new LinkedHashMap<String, Object>();
        try {
            for (int index = 0; index < inventory.size(); index++) inventory.set(index, ItemStack.EMPTY);
            player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
            player.containerMenu = player.inventoryMenu;
            ToolPouch.CONFIG.allowUseFromInventory.accept(true);
            MapStitch.CONFIG.itemRequirements.minimapAtlasScan.accept(List.of("inventory", "accessories"));

            check(AtlasBridge.atlases(player).isEmpty(), "Missing pouch must return no atlases");
            var first = atlas("First atlas", 0, 12);
            var second = atlas("Second atlas", 4, 9);
            var source = pouch(List.of(new ItemStack(Items.STONE), first, ItemStack.EMPTY, second));
            inventory.set(12, source);
            var found = AtlasBridge.atlases(player);
            check(found.size() == 2, "Negative cache must notice a newly added pouch");
            check(ItemStack.matches(found.get(0), first) && ItemStack.matches(found.get(1), second),
                    "Atlas filtering preserves order, count, selected map, scale and nested maps");
            check(ModClientUtil.getFirstItem(Minecraft.getInstance(), ModItems.ATLAS)
                    .getHoverName().getString().equals("First atlas"), "Minimap selects first pouch atlas");
            try {
                found.add(first);
                throw new AssertionError("Returned list must remain unmodifiable");
            } catch (UnsupportedOperationException expected) { checks++; }

            found.get(0).setCount(3);
            found.get(0).set(ModDataComponents.ATLAS_SCALE, 2);
            found.get(0).set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            found.get(1).set(DataComponents.CUSTOM_NAME, Component.literal("Caller mutation"));
            var fresh = AtlasBridge.atlases(player);
            check(ItemStack.matches(fresh.get(0), first) && ItemStack.matches(fresh.get(1), second),
                    "Returned stack mutations cannot poison cache or source pouch");
            check(fresh.get(0) != found.get(0), "Each lookup owns detached mutable stacks");
            check(ToolPouchUtil.getItemsFromToolPouch(player, stack -> stack.is(ModItems.ATLAS))
                    .getFirst().create().getCount() == 1, "Source template remains unchanged");

            var updated = atlas("Updated atlas", 3, 17);
            source.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(updated)));
            check(ItemStack.matches(AtlasBridge.atlases(player).getFirst(), updated),
                    "Replacing CONTAINER on the same ItemStack invalidates immediately");
            source.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            check(AtlasBridge.atlases(player).isEmpty(), "Removing all atlases invalidates immediately");
            source.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(second, first)));
            check(AtlasBridge.atlases(player).getFirst().get(ModDataComponents.ATLAS_SCALE) == 4,
                    "Reordered mixed-scale atlases update selection immediately");

            var replacement = pouch(List.of(updated));
            inventory.set(12, replacement);
            check(ItemStack.matches(AtlasBridge.atlases(player).getFirst(), updated), "Replacing source stack invalidates");
            inventory.set(0, source);
            check(ItemStack.matches(AtlasBridge.atlases(player).getFirst(), second), "Earlier inventory pouch takes priority");
            inventory.set(0, ItemStack.EMPTY);
            ToolPouch.CONFIG.allowUseFromInventory.accept(false);
            check(AtlasBridge.atlases(player).isEmpty(), "Disabling inventory pouch use invalidates selection");
            ToolPouch.CONFIG.allowUseFromInventory.accept(true);
            check(ItemStack.matches(AtlasBridge.atlases(player).getFirst(), updated), "Re-enabling pouch use restores current source");
            MapStitch.CONFIG.itemRequirements.minimapAtlasScan.accept(List.of("inventory"));
            check(ModClientUtil.getFirstItem(Minecraft.getInstance(), ModItems.ATLAS).isEmpty(),
                    "Disabling accessory scan removes pouch from minimap");
            MapStitch.CONFIG.itemRequirements.minimapAtlasScan.accept(List.of("inventory", "accessories"));
            check(ItemStack.matches(ModClientUtil.getFirstItem(Minecraft.getInstance(), ModItems.ATLAS), updated),
                    "Re-enabling accessory scan uses current pouch");

            player.containerMenu = new ToolPouchMenu(177, player.getInventory(), replacement);
            check(AtlasBridge.atlases(player).isEmpty(), "Open pouch menu excludes stored snapshot");
            replacement.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(first)));
            player.containerMenu = player.inventoryMenu;
            check(ItemStack.matches(AtlasBridge.atlases(player).getFirst(), first), "Closing menu observes current contents");

            boolean cachePresent;
            try { Class.forName("com.thenathe.toolpouchcompat.AtlasStackCache"); cachePresent = true; }
            catch (ClassNotFoundException baseline) { cachePresent = false; }
            report.put("candidate_cache_present", cachePresent);
            if (cachePresent) checkCacheLifecycle(player.level(), replacement);
            checkLiveStackSizeChanges(player);
            report.put("live_stack_size_changes", true);

            inventory.set(12, replacement);

            var largeAtlas = atlas("Allocation benchmark", 2, 1024);
            replacement.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(largeAtlas)));
            for (int index = 0; index < 100; index++) {
                benchmarkSink = AtlasBridge.atlases(player);
                benchmarkSink = baseline(player);
            }
            var bean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
            check(bean.isThreadAllocatedMemorySupported(), "Thread allocation accounting required");
            bean.setThreadAllocatedMemoryEnabled(true);
            int iterations = 400;
            long oldBytes = allocated(bean, iterations, () -> baseline(player));
            long newBytes = allocated(bean, iterations, () -> AtlasBridge.atlases(player));
            report.put("atlas_maps", 1024);
            report.put("lookup_iterations", iterations);
            report.put("baseline_lookup_bytes", oldBytes);
            report.put("candidate_lookup_bytes", newBytes);
            report.put("lookup_allocation_reduction_percent", 100.0 * (oldBytes - newBytes) / oldBytes);
            if (cachePresent) check(newBytes < oldBytes / 2,
                    "Cached lookup must cut measured allocation by at least half: baseline=" + oldBytes
                            + ", candidate=" + newBytes + ", iterations=" + iterations);
            report.put("checks", checks);
            report.put("passed", true);
            return report;
        } finally {
            for (int index = 0; index < inventory.size(); index++) inventory.set(index, savedInventory.get(index));
            player.setItemSlot(EquipmentSlot.LEGS, savedLegs);
            player.containerMenu = savedMenu;
            ToolPouch.CONFIG.allowUseFromInventory.accept(savedInventoryUse);
            MapStitch.CONFIG.itemRequirements.minimapAtlasScan.accept(savedMinimapScan);
            benchmarkSink = null;
        }
    }

    private void checkCacheLifecycle(Level level, ItemStack source) throws Exception {
        var cacheClass = Class.forName("com.thenathe.toolpouchcompat.AtlasStackCache");
        Object cache = cacheClass.getConstructor().newInstance();
        Method copies = cacheClass.getMethod("copies", Level.class, ItemStack.class);
        var snapshots = cacheClass.getDeclaredField("atlases");
        snapshots.setAccessible(true);
        copies.invoke(cache, level, source);
        Object initial = snapshots.get(cache);
        copies.invoke(cache, level, source);
        check(snapshots.get(cache) == initial, "Stable component reuses materialized snapshots");
        copies.invoke(cache, level, source.copy());
        check(snapshots.get(cache) != initial, "Source identity invalidates even with identical component identity");
        initial = snapshots.get(cache);
        copies.invoke(cache, null, source);
        check(snapshots.get(cache) != initial, "World key transition invalidates materialized snapshots");
        initial = snapshots.get(cache);
        copies.invoke(cache, level, source);
        check(snapshots.get(cache) != initial, "Returning to prior world key rebuilds snapshots");
        Object otherPlayerCache = cacheClass.getConstructor().newInstance();
        copies.invoke(otherPlayerCache, level, source);
        check(snapshots.get(otherPlayerCache) != snapshots.get(cache), "Cache instances do not share mutable snapshots");
    }

    private static List<ItemStack> baseline(Player player) {
        return ToolPouchUtil.getItemsFromToolPouch(player, stack -> stack.is(ModItems.ATLAS)).stream()
                .map(ItemStackTemplate::create).toList();
    }

    private void checkLiveStackSizeChanges(Player player) throws Exception {
        var overrides = Class.forName("me.pajic.sensible_stackables.handler.StackSizeOverrides");
        Method snapshot = overrides.getMethod("snapshot");
        Method set = overrides.getMethod("set", Map.class);
        var saved = new HashMap<>((Map<?, ?>) snapshot.invoke(null));
        try {
            set.invoke(null, Map.of(ModItems.ATLAS, 2, Items.FILLED_MAP, 2));
            var stackedAtlas = atlas("Live stack limit", 0, 1);
            stackedAtlas.setCount(2);
            var source = pouch(List.of(stackedAtlas));
            player.getInventory().setItem(12, source);
            var unchangedContents = source.get(DataComponents.CONTAINER);
            check(AtlasBridge.atlases(player).getFirst().getCount() == 2, "Atlas count two is valid at live maximum two");
            set.invoke(null, Map.of(ModItems.ATLAS, 1, Items.FILLED_MAP, 2));
            check(AtlasBridge.atlases(player).getFirst().isEmpty(), "Lowered live atlas limit revalidates cached materialization");
            set.invoke(null, Map.of(ModItems.ATLAS, 2, Items.FILLED_MAP, 2));
            check(AtlasBridge.atlases(player).getFirst().getCount() == 2, "Raised live atlas limit recovers rejected materialization");
            check(source.get(DataComponents.CONTAINER) == unchangedContents, "Live atlas limits changed without replacing container");

            var nested = atlas("Live nested map limit", 4, 1);
            var map = new ItemStack(Items.FILLED_MAP, 2);
            map.set(DataComponents.MAP_ID, new MapId(20000));
            nested.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(ItemStackTemplate.fromNonEmptyStack(map))));
            source.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(nested)));
            unchangedContents = source.get(DataComponents.CONTAINER);
            check(!AtlasBridge.atlases(player).getFirst().isEmpty(), "Nested map count two is valid at live maximum two");
            set.invoke(null, Map.of(ModItems.ATLAS, 2, Items.FILLED_MAP, 1));
            check(AtlasBridge.atlases(player).getFirst().isEmpty(), "Lowered live map limit revalidates nested templates");
            set.invoke(null, Map.of(ModItems.ATLAS, 2, Items.FILLED_MAP, 2));
            check(!AtlasBridge.atlases(player).getFirst().isEmpty(), "Raised live map limit recovers rejected nested template");
            check(source.get(DataComponents.CONTAINER) == unchangedContents, "Live map limits changed without replacing container");
        } finally {
            set.invoke(null, saved);
        }
    }

    private static long allocated(ThreadMXBean bean, int iterations, Supplier<List<ItemStack>> operation) {
        long thread = Thread.currentThread().threadId();
        long before = bean.getThreadAllocatedBytes(thread);
        for (int index = 0; index < iterations; index++) benchmarkSink = operation.get();
        return bean.getThreadAllocatedBytes(thread) - before;
    }
}
