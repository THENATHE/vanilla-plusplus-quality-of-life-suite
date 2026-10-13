package com.thenathe.backpackcrafting.compat;

import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Extends BRBR's existing click planner, including its capacity and remainder checks. */
@Pseudo
@Mixin(targets = "marsh.town.brb.workstation.WorkstationTransfer", remap = false)
public abstract class BrbrRecipePlacementMixin {
    @Inject(method = "isInventory", at = @At("RETURN"), cancellable = true)
    private static void backpackCrafting$includeBackpackStorage(Slot slot, CallbackInfoReturnable<Boolean> cir) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof BackpackCraftingMenu menu) {
            cir.setReturnValue((cir.getReturnValueZ() || menu.isStorageSlot(slot)) && slot.mayPickup(player));
        }
    }
}
