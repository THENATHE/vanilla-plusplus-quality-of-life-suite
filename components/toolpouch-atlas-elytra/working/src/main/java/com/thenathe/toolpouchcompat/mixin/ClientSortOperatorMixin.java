package com.thenathe.toolpouchcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.thenathe.toolpouchcompat.ClientSortOperator;
import dev.terminalmc.clientsort.client.inventory.operator.SingleUseOperator;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.menu.ToolPouchSlot;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SingleUseOperator.class, remap = false)
public abstract class ClientSortOperatorMixin {
    @Shadow @Final protected AbstractContainerScreen<?> screen;

    @WrapOperation(method = "collectSlots", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/Slot;mayPlace(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean toolpouchcompat$discoverRestrictedSlots(Slot slot, ItemStack probe, Operation<Boolean> original) {
        // This probe asks whether a slot is accessible, not whether LIGHT is a valid pouch item.
        return screen.getMenu() instanceof ToolPouchMenu && slot instanceof ToolPouchSlot && probe.is(Items.LIGHT)
                || original.call(slot, probe);
    }

    @Inject(method = "getClientOperator", at = @At("HEAD"), cancellable = true)
    private static void toolpouchcompat$restrictedMoves(AbstractContainerScreen<?> screen, Slot origin,
            dev.terminalmc.clientsort.client.config.Operation operation, CallbackInfoReturnable<SingleUseOperator> cir) {
        if (ClientSortOperator.supports(screen)) cir.setReturnValue(new ClientSortOperator(screen, origin, operation));
    }
}
