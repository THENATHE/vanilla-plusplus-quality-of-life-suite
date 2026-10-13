package com.thenathe.backpackcrafting.mixin.client;

import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(AbstractContainerScreen.class)
abstract class CraftingScreenDimensionsMixin {
    @ModifyArgs(method = "<init>(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;<init>(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;II)V"))
    private static void backpackCrafting$dimensions(Args args) {
        if (args.get(0) instanceof BackpackCraftingMenu menu) {
            args.set(3, menu.getWidth());
            args.set(4, menu.getHeight());
        }
    }
}
