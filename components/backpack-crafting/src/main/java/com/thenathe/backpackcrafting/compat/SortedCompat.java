package com.thenathe.backpackcrafting.compat;

import com.sorted.api.SortedStorage;
import com.thenathe.backpackcrafting.BackpackCrafting;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

/** Registers the portable menu without changing the user's Sorted configuration. */
public final class SortedCompat {
    private static final List<String> SUPPORTED_VERSIONS = List.of("0.1.25", "0.1.26");

    private SortedCompat() { }

    public static boolean isSupported() {
        return FabricLoader.getInstance().getModContainer("sorted")
                .map(mod -> SUPPORTED_VERSIONS.contains(mod.getMetadata().getVersion().getFriendlyString()))
                .orElse(false);
    }

    public static boolean supportsPortableMenus() {
        return FabricLoader.getInstance().getModContainer("sorted")
                .map(mod -> "0.1.26".equals(mod.getMetadata().getVersion().getFriendlyString()))
                .orElse(false);
    }

    public static void initialize() {
        if (isSupported()) {
            SortedStorage.register(BackpackCrafting.id("crafting").toString());
        } else {
            FabricLoader.getInstance().getModContainer("sorted").ifPresent(mod ->
                    LoggerFactory.getLogger(BackpackCrafting.ID).warn(
                            "Backpack Sorted integration supports versions {}; found {}. Keeping Sorted's existing behavior.",
                            SUPPORTED_VERSIONS, mod.getMetadata().getVersion().getFriendlyString()));
        }
    }
}
