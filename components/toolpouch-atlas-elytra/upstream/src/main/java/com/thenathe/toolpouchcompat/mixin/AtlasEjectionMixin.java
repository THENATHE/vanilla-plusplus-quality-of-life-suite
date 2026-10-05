package com.thenathe.toolpouchcompat.mixin;
import com.thenathe.toolpouchcompat.AtlasBridge;
import me.pajic.mapstitch.compat.AccessoryUtil;
import me.pajic.mapstitch.extension.BundleContentsMutableExtension;
import me.pajic.mapstitch.item.*;
import me.pajic.mapstitch.networking.ServerNetworkEvents;
import me.pajic.mapstitch.networking.payload.*;
import me.pajic.mapstitch.platform.MultiLoaderUtil;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.*;
@Mixin(value=ServerNetworkEvents.class,remap=false)
public class AtlasEjectionMixin {
 @Inject(method="ejectMap",at=@At("HEAD"),cancellable=true)
 private static void eject(C2SEjectMap payload, Player player, CallbackInfo ci) {
  List<ItemStack> items=new ArrayList<>(player.getInventory().getNonEquipmentItems());
  if (AccessoryUtil.INSTANCE != null) items.addAll(AccessoryUtil.INSTANCE.getAtlases(player));
  List<ItemStack> pouch=AtlasBridge.atlases(player); items.addAll(pouch);
  for (ItemStack atlas : items) {
   if (!atlas.is(ModItems.ATLAS)) continue;
   BundleContents contents=atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
   for (int i=0;i<contents.size();i++) {
    if (!payload.mapId().equals(contents.items().get(i).get(DataComponents.MAP_ID))) continue;
    var mutable=ModUtil.toMutable(contents);
    player.drop(((BundleContentsMutableExtension)mutable).mapstitch$removeOneItemAtIndex(i),true,Prediction.SERVER_ONLY);
    player.playSound(SoundEvents.BUNDLE_REMOVE_ONE);
    ((AtlasItem)atlas.getItem()).updateAtlas(mutable,atlas,player);
    int index=pouch.indexOf(atlas); if(index!=-1) AtlasBridge.save(player,atlas,index);
    MultiLoaderUtil.INSTANCE.s2c((ServerPlayer)player,new S2CSyncWorldMap());
    ci.cancel(); return;
   }
  }
  ci.cancel();
 }
}
