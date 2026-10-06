package com.thenathe.stackablescompat;

import com.thenathe.combinedshim.WireRegistries;
import me.pajic.sensible_stackables.handler.StackSizeSyncPayload;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.LinkedHashMap;

/** Avoid encoding items from other absent modules in a native client's custom table. */
public final class StackSizePayloadProjection {
    private StackSizePayloadProjection() {}

    public static StackSizeSyncPayload project(StackSizeSyncPayload payload, PacketContext context) {
        if (!WireRegistries.hasMapping(context)) return payload;
        var retained = new LinkedHashMap<Item, Integer>();
        for (var entry : payload.sizes().entrySet()) {
            if (WireRegistries.isVisible(BuiltInRegistries.ITEM, entry.getKey(), context)) {
                retained.put(entry.getKey(), entry.getValue());
            }
        }
        return retained.size() == payload.sizes().size() ? payload : new StackSizeSyncPayload(retained);
    }
}
