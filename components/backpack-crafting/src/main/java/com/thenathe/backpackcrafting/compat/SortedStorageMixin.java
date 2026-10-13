package com.thenathe.backpackcrafting.compat;

import com.sorted.client.StorageMenus;
import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Resolves storage and button placement from each actual backpack menu instance. */
@Pseudo
@Mixin(targets = "com.sorted.client.StorageMenus", remap = false)
public abstract class SortedStorageMixin {
    @Inject(method = "isStorageSlot", at = @At("HEAD"), cancellable = true)
    private static void backpackCrafting$storageSlots(AbstractContainerMenu menu, int index,
                                                      CallbackInfoReturnable<Boolean> cir) {
        if (menu instanceof BackpackCraftingMenu backpack) {
            cir.setReturnValue(index >= 0 && index < menu.slots.size() && backpack.isStorageSlot(menu.getSlot(index)));
        }
    }

    @Inject(method = "placement", at = @At("HEAD"), cancellable = true)
    private static void backpackCrafting$buttonPlacement(AbstractContainerMenu menu,
                                                          CallbackInfoReturnable<StorageMenus.Placement> cir) {
        if (menu instanceof BackpackCraftingMenu backpack) {
            int center = backpack.getStorageWidth() / 2;
            cir.setReturnValue(new StorageMenus.Placement(backpack.getStorageWidth(), 0,
                    center - 88, center + 88, backpack.getInventoryY() - 19));
        }
    }
}
