package com.thenathe.multiscale;

import java.util.ArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Per-book generation choices; preserve existing custom data and vanilla persistence. */
public final class AtlasOptions {
    public static final String IDENTITY = "mapstitch_mixed_scales:book_id";
    private static final String GENERATION = "mapstitch_mixed_scales:generation_mask";
    private AtlasOptions() {}

    public static String identity(ItemStack atlas) {
        return atlas.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getStringOr(IDENTITY, "");
    }
    public static void ensureIdentity(ItemStack atlas) {
        if (identity(atlas).isEmpty()) CustomData.update(DataComponents.CUSTOM_DATA, atlas,
                tag -> tag.putString(IDENTITY, java.util.UUID.randomUUID().toString()));
    }
    public static int generationMask(ItemStack atlas) {
        int fallback = 1 << MixedScaleMaps.activeScale(atlas);
        int mask = atlas.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr(GENERATION, fallback);
        return mask >= 0 && mask <= 31 ? mask : fallback;
    }
    public static void setGenerationMask(ItemStack atlas, int mask) {
        if (mask < 0 || mask > 31) throw new IllegalArgumentException("Invalid atlas generation mask");
        CustomData.update(DataComponents.CUSTOM_DATA, atlas, tag -> tag.putInt(GENERATION, mask));
    }
    public static String generationLabel(int mask) {
        var scales = new ArrayList<String>();
        for (int scale = 0; scale < 5; scale++) if ((mask & (1 << scale)) != 0) scales.add("1:" + (1 << scale));
        return scales.isEmpty() ? "" : String.join(", ", scales);
    }
}
