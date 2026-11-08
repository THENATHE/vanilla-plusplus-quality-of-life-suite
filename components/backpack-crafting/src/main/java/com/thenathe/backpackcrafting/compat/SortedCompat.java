package com.thenathe.backpackcrafting.compat;

import com.sorted.api.SortedStorage;
import com.thenathe.backpackcrafting.BackpackCrafting;
import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.slf4j.LoggerFactory;

/** Registers the portable menu without changing the user's Sorted configuration. */
public final class SortedCompat {
    // Exact Loader version strings: each listed release was checked against every mixin target and API call.
    private static final List<String> SUPPORTED_VERSIONS = List.of("0.1.25", "0.1.26", "0.1.41+26.3");
    private static final List<String> PORTABLE_MENU_VERSIONS = List.of("0.1.26", "0.1.41+26.3");

    private SortedCompat() { }

    /**
     * Both toolbox layouts and every backpack size share one menu ID, while Sorted keeps one
     * button placement per ID. Publish this instance's placement before Sorted adds its buttons.
     */
    public static void placeButtons(BackpackCraftingMenu menu) {
        if (!isSupported()) return;
        SortedStorage.placeButtons(menuId(), menu.getStorageWidth(), -20, 0, menu.getStorageWidth(), menu.getHeight() + 1);
    }

    // Resolved on use: the mixin plugin loads this class before the game registries are bootstrapped.
    private static String menuId() {
        return BackpackCrafting.id("crafting").toString();
    }

    private static boolean isOpenStorageSlot(int index) {
        var player = Minecraft.getInstance().player;
        return player != null && player.containerMenu instanceof BackpackCraftingMenu menu
                && index >= 0 && index < menu.slots.size() && menu.isStorageSlot(menu.getSlot(index));
    }

    public static boolean isSupported() {
        return FabricLoader.getInstance().getModContainer("sorted")
                .map(mod -> SUPPORTED_VERSIONS.contains(mod.getMetadata().getVersion().getFriendlyString()))
                .orElse(false);
    }

    public static boolean supportsPortableMenus() {
        return FabricLoader.getInstance().getModContainer("sorted")
                .map(mod -> PORTABLE_MENU_VERSIONS.contains(mod.getMetadata().getVersion().getFriendlyString()))
                .orElse(false);
    }

    public static void initialize() {
        if (isSupported()) {
            // Sorted's public API asks only for slot indices; it always queries the open menu.
            SortedStorage.register(menuId(), SortedCompat::isOpenStorageSlot);
        } else {
            FabricLoader.getInstance().getModContainer("sorted").ifPresent(mod ->
                    LoggerFactory.getLogger(BackpackCrafting.ID).warn(
                            "Backpack Sorted integration supports versions {}; found {}. Keeping Sorted's existing behavior.",
                            SUPPORTED_VERSIONS, mod.getMetadata().getVersion().getFriendlyString()));
        }
    }
}
