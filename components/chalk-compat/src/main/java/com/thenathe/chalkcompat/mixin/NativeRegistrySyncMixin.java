package com.thenathe.chalkcompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.thenathe.chalkcompat.NativeClients;
import com.thenathe.chalkcompat.WireRegistries;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(value = RegistrySyncManager.class, remap = false)
public abstract class NativeRegistrySyncMixin {
    @ModifyExpressionValue(method = "configureClient", at = @At(value = "INVOKE",
            target = "Lnet/fabricmc/fabric/impl/registry/sync/RegistrySyncManager;createAndPopulateRegistryMap()Ljava/util/Map;"))
    private static Map<Identifier, Object2IntMap<Identifier>> chalkcompat$clientRegistries(
            Map<Identifier, Object2IntMap<Identifier>> original,
            ServerConfigurationPacketListenerImpl listener, MinecraftServer server) {
        var context = ((PacketContextProvider) listener).getPacketContext();
        boolean nativeClient = NativeClients.nativeClient(context);
        if (!nativeClient) return original;
        var result = new LinkedHashMap<Identifier, Object2IntMap<Identifier>>();
        if (original != null) original.forEach((key, value) -> result.put(key, new Object2IntLinkedOpenHashMap<>(value)));
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            Identifier key = registry.key().identifier();
            if (!WireRegistries.handles(key)) continue;
            var entries = result.computeIfAbsent(key, ignored -> new Object2IntLinkedOpenHashMap<>());
            restore(registry, entries);
        }
        return WireRegistries.prepare(result, context);
    }

    private static <T> void restore(Registry<T> registry, Object2IntMap<Identifier> entries) {
        for (T value : registry) {
            var id = registry.getKey(value);
            if (id != null && (id.getNamespace().equals("minecraft") || NativeClients.ownEntry(id))) {
                entries.put(id, registry.getId(value));
            }
        }
    }
}
