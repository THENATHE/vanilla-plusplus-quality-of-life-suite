package com.thenathe.toolpouchcompat.mixin;
import com.thenathe.toolpouchcompat.AtlasBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.*;
@Mixin(value=WorldMapScreen.class,remap=false)
public class AtlasWorldMapMixin {
 @WrapOperation(method="renderMaps",at=@At(value="INVOKE",target="Ljava/util/List;iterator()Ljava/util/Iterator;",ordinal=0))
 private Iterator<ItemStack> addPouch(List<ItemStack> items, Operation<Iterator<ItemStack>> original) {
  var player=Minecraft.getInstance().player;
  if (player!=null && MapStitch.CONFIG.itemRequirements.worldMapAtlasScan.contains("accessories")) items.addAll(AtlasBridge.atlases(player));
  return original.call(items);
 }
}
