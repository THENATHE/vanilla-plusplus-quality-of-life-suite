package com.thenathe.stackablescompat;

import com.thenathe.suite.network.SuiteCapabilities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.loader.api.FabricLoader;

/** Preserve upstream quantities and rules; Polymer carries the changed vanilla component defaults. */
public final class StackablesCompat implements ModInitializer {
    public static boolean nativeClient(PacketContext context) {
        return SuiteCapabilities.isNative(context, "sensible_stackables");
    }
    @Override public void onInitialize() {
        SuiteCapabilities.initialize();
        if (FabricLoader.getInstance().isModLoaded("polymer-core")) {
            PolymerDefaults.registerFallbackTransform();
            ServerLifecycleEvents.SERVER_STARTING.register(server -> PolymerDefaults.initialize());
        }
    }
    private static final class PolymerDefaults {
        static void registerFallbackTransform() {
            // Forced-default synchronization alone does not run item modification events.
            // Opt ordinary vanilla items into transformation when their limit needs a cap.
            eu.pb4.polymer.core.api.item.PolymerItemUtils.CONTEXT_ITEM_CHECK.register((stack, context) ->
                    !nativeClient(context)
                            && stack.getOrDefault(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, 1) > 99);
            eu.pb4.polymer.core.api.item.PolymerItemUtils.ITEM_MODIFICATION_EVENT.register((original, client, context) -> {
                // Vanilla hashes a clicked stack with the persistent component codec, whose
                // MAX_STACK_SIZE range ends at 99 even though its network codec accepts any
                // positive int. Keep the real count and server rules; only constrain the
                // fallback client's prediction metadata. Completed clicks are resynchronized.
                if (!nativeClient(context) && client.getMaxStackSize() > 99) {
                    client = client.copy();
                    client.set(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, 99);
                }
                return client;
            });
        }
        static void initialize() {
            for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
                // The public API inserts defaults directly into the wire patch. Calling set()
                // on a copy would elide values equal to the patched server prototype.
                eu.pb4.polymer.core.api.item.PolymerItemUtils.syncDefaultComponent(item,
                        net.minecraft.core.component.DataComponents.MAX_STACK_SIZE);
            }
        }
    }
}
