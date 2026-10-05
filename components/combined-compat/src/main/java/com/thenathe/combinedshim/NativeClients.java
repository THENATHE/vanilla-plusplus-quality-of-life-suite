package com.thenathe.combinedshim;

import com.thenathe.suite.network.SuiteCapabilities;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

/** Suite-owned decisions, shared by all compatibility modules; no UUID overrides. */
public final class NativeClients {
    private NativeClients() {}
    public static boolean isNative(PacketContext context, String modId) { return SuiteCapabilities.isNative(context, modId); }
    public static boolean isNativeEntry(PacketContext context, Identifier id) { return SuiteCapabilities.isNativeEntry(context, id); }
    public static boolean hasRegistryReceiver(ServerConfigurationPacketListenerImpl listener) { return SuiteCapabilities.hasRegistryReceiver(listener); }
    public static void classify(ServerConfigurationPacketListenerImpl listener) { /* Already selected by the coordinator before registry sync. */ }
    public static void beforeRegistrySync(ServerConfigurationPacketListenerImpl listener, Runnable configure) { SuiteCapabilities.beforeRegistrySync(listener, configure); }
}
