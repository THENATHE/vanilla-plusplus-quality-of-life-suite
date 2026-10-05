package org.sharedregionmaps.mixin;

import org.sharedregionmaps.SharedMaps;
import org.sharedregionmaps.MapstitchMaps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapItem.class)
public abstract class MapItemMixin {
    @Inject(method = "scaleMap", at = @At("HEAD"), cancellable = true)
    private static void sharedmaps$scale(ItemStack stack, ServerLevel level, CallbackInfo ci) {
        if (SharedMaps.scale(stack, level)) {
            MapstitchMaps.refreshCenter(stack, level);
            ci.cancel();
        }
    }

    @Inject(method = "scaleMap", at = @At("RETURN"))
    private static void sharedmaps$refreshIndependentCenter(ItemStack stack, ServerLevel level, CallbackInfo ci) {
        // The normal allocator also carries the old component forward. Repair
        // independent maps without enrolling or merging their saved records.
        MapstitchMaps.refreshCenter(stack, level);
    }
}
