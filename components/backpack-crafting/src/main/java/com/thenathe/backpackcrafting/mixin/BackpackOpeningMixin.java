package com.thenathe.backpackcrafting.mixin;

import com.thenathe.backpackcrafting.BackpackCrafting;
import me.pajic.tiered_backpacks.platform.fabric.FabricLoaderUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FabricLoaderUtil.class, remap = false)
public abstract class BackpackOpeningMixin {
    @Inject(method = "openBackpackScreen", at = @At("HEAD"), cancellable = true)
    private void backpackcrafting$open(Player player, ItemStack backpack, CallbackInfo callback) {
        if (player instanceof ServerPlayer serverPlayer && BackpackCrafting.open(serverPlayer, backpack)) callback.cancel();
    }
}
