package org.sharedregionmaps.mixin;

import org.sharedregionmaps.SharedMaps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.EmptyMapItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EmptyMapItem.class)
public abstract class EmptyMapMixin {
    @Redirect(method = "use", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/item/MapItem;create(Lnet/minecraft/server/level/ServerLevel;IIBZZ)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack sharedmaps$ordinaryCreation(ServerLevel level, int x, int z, byte scale, boolean tracking, boolean unlimited) {
        return SharedMaps.create(level, x, z, scale, tracking, unlimited);
    }
}
