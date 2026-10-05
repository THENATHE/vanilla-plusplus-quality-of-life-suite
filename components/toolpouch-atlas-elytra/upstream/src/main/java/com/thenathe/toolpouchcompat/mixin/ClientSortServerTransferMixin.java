package com.thenathe.toolpouchcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.terminalmc.clientsort.network.handler.TransferHandler;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.menu.ToolPouchSlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = TransferHandler.class, remap = false)
public abstract class ClientSortServerTransferMixin {
    @WrapMethod(method = "transfer")
    private static void toolpouchCompat$continueCappedTransfers(MinecraftServer server,
            AbstractContainerMenu menu, int[] sources, int[] destinations, boolean reversed,
            Operation<Void> original) {
        boolean restrictedDestination = false;
        if (menu instanceof ToolPouchMenu) {
            for (int id : destinations) {
                var slot = menu.getSlot(id);
                if (slot instanceof ToolPouchSlot && !(slot.container instanceof Inventory)) {
                    restrictedDestination = true;
                    break;
                }
            }
        }
        if (!restrictedDestination) {
            original.call(server, menu, sources, destinations, reversed);
            return;
        }

        // Upstream stops after one empty destination, even when its per-slot cap leaves a remainder.
        // Drain each source in the configured order through the original checked transfer routine.
        for (int i = reversed ? sources.length - 1 : 0;
             reversed ? i >= 0 : i < sources.length; i += reversed ? -1 : 1) {
            int source = sources[i];
            int remaining = menu.getSlot(source).getItem().getCount();
            while (remaining > 0) {
                original.call(server, menu, new int[]{source}, destinations, reversed);
                int after = menu.getSlot(source).getItem().getCount();
                if (after >= remaining) break; // Full storage, quota or rejected item: no further progress.
                remaining = after;
            }
        }
    }
}
