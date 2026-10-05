package org.sharedregionmaps.mixin;

import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MapItemSavedData.class)
public interface MapDataAccessor {
    @Accessor("trackingPosition") boolean sharedmaps$trackingPosition();
    @Accessor("unlimitedTracking") boolean sharedmaps$unlimitedTracking();
}
