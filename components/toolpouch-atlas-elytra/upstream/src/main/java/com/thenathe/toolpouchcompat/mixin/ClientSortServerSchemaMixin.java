package com.thenathe.toolpouchcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.terminalmc.clientsort.network.handler.validate.SchemaValidator;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.menu.ToolPouchSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SchemaValidator.class, remap = false)
public abstract class ClientSortServerSchemaMixin {
    @WrapOperation(method = {"validateSlotArray", "validateSlotMapping"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/Slot;mayPlace(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean toolpouchCompat$recognizeStorageProbe(Slot slot, ItemStack probe,
                                                                 Operation<Boolean> original,
                                                                 @Local(argsOnly = true) AbstractContainerMenu menu) {
        // This call is only ClientSort's accessibility probe. Its real-item checks still run later.
        if (menu instanceof ToolPouchMenu && slot instanceof ToolPouchSlot
                && !(slot.container instanceof Inventory) && probe.is(Items.LIGHT)) return true;
        return original.call(slot, probe);
    }
}
