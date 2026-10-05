package com.thenathe.toolpouchcompat;

import dev.terminalmc.clientsort.exception.PayloadHandlerException.InvalidDataException;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.menu.ToolPouchSlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/** A synchronous permutation avoids temporary duplicate stacks tripping pouch quotas. */
public final class ClientSortServerPouchSort {
    public static boolean apply(AbstractContainerMenu menu, int[] mapping) throws InvalidDataException {
        if (!(menu instanceof ToolPouchMenu) || mapping.length == 0) return false;
        if (mapping[0] < 0 || mapping[0] >= menu.slots.size()) throw invalid("Invalid first slot");
        Slot first = menu.getSlot(mapping[0]);
        if (!(first instanceof ToolPouchSlot) || first.container instanceof Inventory) return false;
        Container container = first.container;
        if (!(container instanceof SimpleContainer storage)) throw invalid("Unexpected pouch container");
        if (mapping.length % 2 != 0) throw invalid("Incomplete slot permutation");

        Map<Integer, ItemStack> before = new LinkedHashMap<>();
        var destinations = new HashSet<Integer>();
        var physicalSlots = new HashSet<Integer>();
        for (int i = 0; i < mapping.length; i += 2) {
            int source = mapping[i], destination = mapping[i + 1];
            if (source < 0 || source >= menu.slots.size() || destination < 0 || destination >= menu.slots.size()) {
                throw invalid("Slot outside menu");
            }
            Slot src = menu.getSlot(source), dst = menu.getSlot(destination);
            if (!(src instanceof ToolPouchSlot) || !(dst instanceof ToolPouchSlot)
                    || src.container != container || dst.container != container) throw invalid("Mixed containers");
            if (before.putIfAbsent(source, src.getItem().copy()) != null
                    || !destinations.add(destination) || !physicalSlots.add(src.getContainerSlot())) {
                throw invalid("Repeated slot");
            }
        }
        if (!before.keySet().equals(destinations)) throw invalid("Mapping is not a permutation");

        boolean committed = false;
        try {
            for (int source : before.keySet()) menu.getSlot(source).setByPlayer(ItemStack.EMPTY);
            for (int i = 0; i < mapping.length; i += 2) {
                ItemStack stack = before.get(mapping[i]).copy();
                Slot destination = menu.getSlot(mapping[i + 1]);
                if (!stack.isEmpty() && (!destination.mayPlace(stack)
                        || stack.getCount() > Math.min(stack.getMaxStackSize(), destination.getMaxStackSize(stack)))) {
                    throw invalid("Destination rejects item or stack capacity");
                }
                destination.setByPlayer(stack);
                if (!ItemStack.matches(destination.getItem(), before.get(mapping[i]))) throw invalid("Destination altered stack");
            }
            committed = true;
        } finally {
            if (!committed) {
                // Restore the exact previous state, even if a preexisting stack exceeded normal limits.
                // SimpleContainer.setItem would clamp that stack while rolling back a rejected request.
                for (var original : before.entrySet()) {
                    storage.getItems().set(menu.getSlot(original.getKey()).getContainerSlot(), original.getValue());
                }
                storage.setChanged();
            }
        }
        return true;
    }

    private static InvalidDataException invalid(String detail) {
        return new InvalidDataException("Tool Pouch sort rejected: " + detail);
    }

    private ClientSortServerPouchSort() {}
}
