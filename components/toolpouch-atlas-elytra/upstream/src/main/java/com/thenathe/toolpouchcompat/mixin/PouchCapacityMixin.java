package com.thenathe.toolpouchcompat.mixin;

import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.component.ModDataComponents;
import me.pajic.toolpouch.util.GameplayUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Attached pouches retain their tier even though the holder is an armor item. */
@Mixin(value = ToolPouchUtil.class, remap = false)
public abstract class PouchCapacityMixin {
    private static boolean toolpouchcompat$attachedNetherite(ItemStack stack) {
        return GameplayUtil.isLeggingsWithPouchAttached(stack)
                && stack.getOrDefault(ModDataComponents.IS_NETHERITE_POUCH, false);
    }

    @Inject(method = "getToolPouchRows", at = @At("HEAD"), cancellable = true)
    private static void toolpouchcompat$attachedRows(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (toolpouchcompat$attachedNetherite(stack)) cir.setReturnValue(ToolPouch.CONFIG.netheriteToolPouchRows.get());
    }

    @Inject(method = "getToolPouchColumns", at = @At("HEAD"), cancellable = true)
    private static void toolpouchcompat$attachedColumns(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (toolpouchcompat$attachedNetherite(stack)) cir.setReturnValue(ToolPouch.CONFIG.netheriteToolPouchColumns.get());
    }
}
