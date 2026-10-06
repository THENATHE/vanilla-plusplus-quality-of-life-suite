package com.thenathe.stackablescompat;

import com.thenathe.suite.network.SuiteCapabilities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.loader.api.FabricLoader;
import me.pajic.sensible_stackables.handler.StackSizeOverrides;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

/** Carry effective upstream limits without changing quantities or server inventory rules. */
public final class StackablesCompat implements ModInitializer {
    public static boolean nativeClient(PacketContext context) {
        return SuiteCapabilities.isNative(context, "sensible_stackables");
    }
    public static ItemStack projectStackLimit(ItemStack original, ItemStack client, boolean nativeClient) {
        if (original.isEmpty() || client.isEmpty()) return client;
        int effective = original.getMaxStackSize();
        int advertised = nativeClient ? effective : Math.min(effective, 99);
        client = client.copy();
        client.set(DataComponents.MAX_STACK_SIZE, advertised);
        return client;
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
            // Defaults now live in a separate upstream table. Opt every overridden
            // item into transformation, including limits below 99 and native clients.
            eu.pb4.polymer.core.api.item.PolymerItemUtils.CONTEXT_ITEM_CHECK.register((stack, context) ->
                    stack.count() > 0 && stack.typeHolder().value() != Items.AIR
                            && (StackSizeOverrides.get(stack.typeHolder().value()) > 0
                            || (!nativeClient(context) && (stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 99
                            || FallbackContainerPreview.needsProjection(stack)))));
            eu.pb4.polymer.core.api.item.PolymerItemUtils.ITEM_MODIFICATION_EVENT.register((original, client, context) -> {
                // copy() of an empty stack can return the shared EMPTY singleton.
                // Never turn empty inventory slots into transformed air or mutate it.
                if (original.isEmpty() || client.isEmpty()) return client;
                // Vanilla hashes a clicked stack with the persistent component codec, whose
                // MAX_STACK_SIZE range ends at 99 even though its network codec accepts any
                // positive int. Keep the real count and server rules; only constrain the
                // fallback client's prediction metadata. Completed clicks are resynchronized.
                return projectStackLimit(original, client, nativeClient(context));
            });
        }
        static void initialize() {
            for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
                // The public API inserts defaults directly into the wire patch. Calling set()
                // on a copy would elide values equal to the patched server prototype.
                eu.pb4.polymer.core.api.item.PolymerItemUtils.syncDefaultComponent(item,
                        DataComponents.MAX_STACK_SIZE);
            }
        }
    }
}
