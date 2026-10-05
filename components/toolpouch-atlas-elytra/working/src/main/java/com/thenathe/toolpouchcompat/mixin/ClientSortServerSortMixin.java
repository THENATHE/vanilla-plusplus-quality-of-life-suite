package com.thenathe.toolpouchcompat.mixin;

import com.thenathe.toolpouchcompat.ClientSortServerPouchSort;
import dev.terminalmc.clientsort.exception.PayloadHandlerException.InvalidDataException;
import dev.terminalmc.clientsort.network.handler.SortHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SortHandler.class, remap = false)
public abstract class ClientSortServerSortMixin {
    @Inject(method = "sort", at = @At("HEAD"), cancellable = true)
    private static void toolpouchCompat$sortWithoutTransientDuplicates(MinecraftServer server,
            AbstractContainerMenu menu, int[] mapping, CallbackInfo ci) throws InvalidDataException {
        if (ClientSortServerPouchSort.apply(menu, mapping)) ci.cancel();
    }
}
