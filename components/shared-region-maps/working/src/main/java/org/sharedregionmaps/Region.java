package org.sharedregionmaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/** The containing world's SavedData storage supplies the world identity. */
public record Region(ResourceKey<Level> dimension, int centerX, int centerZ, int scale) {
    public static final Codec<Region> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(Region::dimension),
            Codec.INT.fieldOf("center_x").forGetter(Region::centerX),
            Codec.INT.fieldOf("center_z").forGetter(Region::centerZ),
            Codec.intRange(0, 4).fieldOf("scale").forGetter(Region::scale)
    ).apply(i, Region::new));

    public static Region at(ResourceKey<Level> dimension, int x, int z, int scale) {
        if (scale < 0 || scale > 4) throw new IllegalArgumentException("Map scale must be 0..4");
        int width = 128 << scale;
        // Vanilla's fixed -64 origin is NOT minus half the map width. Floor, not truncation.
        int cx = (int) (Math.floorDiv((long) x + 64, width) * width + width / 2 - 64);
        int cz = (int) (Math.floorDiv((long) z + 64, width) * width + width / 2 - 64);
        return new Region(dimension, cx, cz, scale);
    }

    public static Region of(MapItemSavedData data) {
        return new Region(data.dimension, data.centerX, data.centerZ, data.scale);
    }
}
