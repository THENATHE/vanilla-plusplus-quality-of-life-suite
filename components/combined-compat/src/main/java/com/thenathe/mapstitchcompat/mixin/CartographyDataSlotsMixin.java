package com.thenathe.mapstitchcompat.mixin;

import com.thenathe.combinedshim.NativeClients;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MapStitch adds atlas mode/status data slots to the cartography table. A vanilla
 * cartography menu has none, so a client without MapStitch disconnects on the first value.
 */
@Mixin(targets = "net.minecraft.server.level.ServerPlayer$1")
public abstract class CartographyDataSlotsMixin {
    @Shadow @Final ServerPlayer this$0;

    @Inject(method = "broadcastDataValue", at = @At("HEAD"), cancellable = true)
    private void mapstitchcompat$skipAtlasData(AbstractContainerMenu menu, int index, int value, CallbackInfo ci) {
        if (menu instanceof CartographyTableMenu
                && !NativeClients.isNative(this$0.connection.getPacketContext(), "mapstitch")) ci.cancel();
    }
}
