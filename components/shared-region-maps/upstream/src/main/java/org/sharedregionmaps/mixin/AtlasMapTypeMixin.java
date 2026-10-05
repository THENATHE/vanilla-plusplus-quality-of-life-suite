package org.sharedregionmaps.mixin;

import org.sharedregionmaps.SharedMaps;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Optional adapter for the inspected Map Atlases 26.3 ordinary-map creation path. */
@Pseudo
@Mixin(targets = "pepjebs.mapatlases.utils.MapType", remap = false)
public abstract class AtlasMapTypeMixin {
    @Redirect(method = "createNewMapItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/MapItem;create(Lnet/minecraft/server/level/ServerLevel;IIBZZ)Lnet/minecraft/world/item/ItemStack;"),
            require = 1)
    private ItemStack sharedmaps$createOrdinaryAtlasMap(ServerLevel serverLevel, int x, int z,
            byte scale, boolean tracking, boolean unlimited,
            int destX, int destZ, byte requestedScale, Level level, Optional<Integer> height,
            ItemStack atlas) {
        // This invocation belongs only to MapType.VANILLA's nonsliced branch. An
        // antique-ink atlas may alter the returned record after creation when
        // Supplementaries is installed; bypass that integration conservatively.
        // Height slices and antique/custom records need an explicit future adapter.
        if (height.isPresent() || FabricLoader.getInstance().isModLoaded("supplementaries")) {
            return MapItem.create(serverLevel, x, z, scale, tracking, unlimited);
        }
        return SharedMaps.create(serverLevel, x, z, scale, tracking, unlimited);
    }
}
