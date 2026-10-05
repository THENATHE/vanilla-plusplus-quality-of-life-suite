package com.thenathe.chalkcompat.mixin;

import com.thenathe.chalkcompat.NativeClients;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Consumer;

/** Other shims may defer registry creation too, so schedule confirmation from the real sync task. */
@Mixin(value = RegistrySyncManager.SyncConfigurationTask.class, remap = false)
public abstract class NativeStateSyncSchedulingMixin {
    @Shadow public abstract ServerConfigurationPacketListenerImpl handler();
    @Inject(method = "start", at = @At("TAIL"))
    private void chalkcompat$afterRegistrySync(Consumer<Packet<?>> sender, CallbackInfo ci) {
        NativeClients.afterRegistrySyncStarted(handler());
    }
}
