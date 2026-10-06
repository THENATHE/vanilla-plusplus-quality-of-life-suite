package com.thenathe.ssopolymer;

import com.thenathe.combinedshim.WireRegistries;
import me.pajic.simple_smithing_overhaul.repair.RepairableSyncPayload;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Repairable;

import java.util.LinkedHashMap;

/** A native SSO client can still lack other server mods referenced by its repair config. */
public final class RepairablePayloadProjection {
    private RepairablePayloadProjection() {}

    public static RepairableSyncPayload project(RepairableSyncPayload payload, PacketContext context) {
        if (!WireRegistries.hasMapping(context)) return payload;
        var retained = new LinkedHashMap<Item, Repairable>();
        boolean changed = false;
        for (var entry : payload.repairables().entrySet()) {
            if (!WireRegistries.isVisible(BuiltInRegistries.ITEM, entry.getKey(), context)) {
                changed = true;
                continue;
            }
            var repairable = entry.getValue();
            var materials = repairable.items().stream()
                    .filter(holder -> WireRegistries.isVisible(BuiltInRegistries.ITEM, holder.value(), context))
                    .toList();
            if (materials.size() != repairable.items().size()) {
                changed = true;
                if (materials.isEmpty()) continue;
                repairable = new Repairable(HolderSet.direct(materials));
            }
            retained.put(entry.getKey(), repairable);
        }
        return changed ? new RepairableSyncPayload(retained) : payload;
    }
}
