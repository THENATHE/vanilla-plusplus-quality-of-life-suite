package com.thenathe.backpackcrafting.compat;

import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Extends Sorted 0.1.26's portable-container protections to the crafting backpack. */
@Pseudo
@Mixin(targets = "com.sorted.client.StorageMenus", remap = false)
public abstract class SortedPortableMixin {
    @Inject(method = "isPortable", at = @At("HEAD"), cancellable = true)
    private static void backpackCrafting$portableMenu(AbstractContainerMenu menu, CallbackInfoReturnable<Boolean> cir) {
        if (menu instanceof BackpackCraftingMenu) cir.setReturnValue(true);
    }
}
