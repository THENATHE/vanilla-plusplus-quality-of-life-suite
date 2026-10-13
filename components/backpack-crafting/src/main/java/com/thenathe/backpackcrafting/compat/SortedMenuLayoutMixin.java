package com.thenathe.backpackcrafting.compat;

import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Reserves an 18px Sorted control row between the storage and player slot backgrounds. */
@Mixin(value = BackpackCraftingMenu.class, remap = false)
public abstract class SortedMenuLayoutMixin {
    @Shadow @Final private Player player;

    @Inject(method = "getInventoryY", at = @At("RETURN"), cancellable = true)
    private void backpackCrafting$controlRow(CallbackInfoReturnable<Integer> cir) {
        if (player != null && player.level().isClientSide()) cir.setReturnValue(cir.getReturnValueI() + 4);
    }
}
