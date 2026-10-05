package com.thenathe.toolpouchcompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.terminalmc.clientsort.client.config.ClassPolicy;
import dev.terminalmc.clientsort.client.util.PolicyManager;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.TreeSet;

/** Chest defaults without rewriting ClientSort's saved, per-container preferences. */
@Mixin(value = PolicyManager.class, remap = false)
public abstract class ClientSortPolicyMixin {
    @ModifyReturnValue(method = "getPolicy", at = @At("RETURN"))
    private static ClassPolicy toolpouchcompat$chestDefaults(ClassPolicy original, Class<?> type, String title) {
        if (original != null || !(type.getName().equals("me.pajic.toolpouch.menu.ToolPouchMenu")
                || type.getName().equals("me.pajic.tiered_backpacks.ui.BackpackMenu"))) return original;
        ClassPolicy chest = PolicyManager.getPolicy(ChestMenu.class, title);
        if (chest == null) return null;
        return new ClassPolicy(type.getName(), null, chest.buttonOffset(), chest.offsetFromSlot(),
                chest.sortPolicy(), chest.stackFillPolicy(), chest.matchTransferPolicy(), chest.transferPolicy(),
                chest.autoOp(), chest.autoOpOther(), new TreeSet<>(chest.ignoredSlots()));
    }
}
