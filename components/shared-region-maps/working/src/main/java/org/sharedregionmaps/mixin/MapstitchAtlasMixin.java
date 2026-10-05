package org.sharedregionmaps.mixin;

import org.sharedregionmaps.SharedMaps;
import org.sharedregionmaps.MapstitchMaps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Optional adapter for original MapStitch 1.1.6+26.3 automatic atlas exploration. */
@Pseudo
@Mixin(targets = "me.pajic.mapstitch.item.AtlasItem", remap = false)
public abstract class MapstitchAtlasMixin {
    @Inject(method = "isValidItemForAtlas", at = @At("HEAD"), require = 1)
    private void sharedmaps$prepareInsertedMap(ItemStack map, ItemStack atlas, Level level,
            CallbackInfoReturnable<Boolean> cir) {
        if (level instanceof ServerLevel serverLevel) MapstitchMaps.refreshCenter(map, serverLevel);
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"), require = 1)
    private void sharedmaps$repairExistingAtlas(ItemStack atlas, ServerLevel level, Entity owner,
            EquipmentSlot slot, CallbackInfo ci) {
        MapstitchMaps.repairAtlas(atlas, level);
    }

    @Redirect(method = "updateActiveMap", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/MapItem;create(Lnet/minecraft/server/level/ServerLevel;IIBZZ)Lnet/minecraft/world/item/ItemStack;"),
            require = 1)
    private ItemStack sharedmaps$createMapstitchMap(ServerLevel level, int x, int z,
            byte scale, boolean tracking, boolean unlimited) {
        return SharedMaps.create(level, x, z, scale, tracking, unlimited);
    }
}
