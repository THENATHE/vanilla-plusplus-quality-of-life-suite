package com.thenathe.multiscale.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Repair client metadata in place, preserving pixels, decorations and optional integration fields. */
@Mixin(MapItemSavedData.class)
public interface MapMetadataAccess {
    @Mutable @Accessor("dimension") void mixedScales$dimension(ResourceKey<Level> dimension);
    @Mutable @Accessor("centerX") void mixedScales$centerX(int centerX);
    @Mutable @Accessor("centerZ") void mixedScales$centerZ(int centerZ);
    @Mutable @Accessor("scale") void mixedScales$scale(byte scale);
    @Mutable @Accessor("locked") void mixedScales$locked(boolean locked);
}
