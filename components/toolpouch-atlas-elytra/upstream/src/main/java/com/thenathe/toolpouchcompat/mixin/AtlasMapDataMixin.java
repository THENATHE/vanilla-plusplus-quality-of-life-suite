package com.thenathe.toolpouchcompat.mixin;
import com.thenathe.toolpouchcompat.AtlasBridge;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(MapItemSavedData.class)
public class AtlasMapDataMixin {
 @ModifyExpressionValue(method="tickCarriedBy",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Inventory;contains(Ljava/util/function/Predicate;)Z"))
 private boolean contains(boolean original, @Local(argsOnly=true) Player player) { return original || AtlasBridge.hasItem(player, ModItems.ATLAS); }
}
