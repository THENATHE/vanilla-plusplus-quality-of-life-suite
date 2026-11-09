package com.thenathe.toolpouchcompat;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.util.GameplayUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Turns the pouch Elytra on or off through Tool Pouch's own disabled slots, for players and
 * commands that cannot use Tool Pouch's client-side slot-toggle binds.
 */
public final class PouchElytraToggle {
    private PouchElytraToggle() {}

    /** The pouch Tool Pouch itself would toggle: the open pouch menu's, otherwise the active pouch. */
    public static ItemStack activePouch(ServerPlayer player) {
        return player.containerMenu instanceof ToolPouchMenu menu ? menu.getToolPouch() : ToolPouchUtil.getToolPouch(player);
    }

    public static IntList gliderSlots(ItemStack pouch) {
        var slots = new IntArrayList();
        if (!GameplayUtil.isValidContainerHolder(pouch)) return slots;
        var items = ToolPouchUtil.readItems(pouch);
        for (int slot = 0; slot < items.size(); slot++) {
            if (items.get(slot).has(DataComponents.GLIDER)) slots.add(slot);
        }
        return slots;
    }

    /** True when at least one stored glider is enabled, which is all Tool Pouch needs to fly. */
    public static boolean enabled(ServerPlayer player) {
        var pouch = activePouch(player);
        for (int slot : gliderSlots(pouch)) if (!ToolPouchUtil.isSlotDisabled(pouch, slot)) return true;
        return false;
    }

    /** Sets every stored glider slot to the requested state; returns how many slots changed. */
    public static int set(ServerPlayer player, boolean enabled) {
        var pouch = activePouch(player);
        int changed = setSlots(pouch, enabled);
        var slots = gliderSlots(pouch);
        Component message;
        if (slots.isEmpty()) {
            message = Component.literal("No Elytra in the active Tool Pouch");
        } else {
            Component name = ToolPouchUtil.getItemInToolPouch(pouch, slots.getInt(0)).getHoverName();
            // Tool Pouch's own feedback; the fallback covers clients without its translations.
            message = enabled ? Component.translatableWithFallback("text.toolpouch.slot_enabled", "%s enabled", name)
                    : Component.translatableWithFallback("text.toolpouch.slot_disabled", "%s disabled", name);
        }
        player.sendOverlayMessage(message);
        return changed;
    }

    static int setSlots(ItemStack pouch, boolean enabled) {
        int changed = 0;
        for (int slot : gliderSlots(pouch)) {
            if (ToolPouchUtil.isSlotDisabled(pouch, slot) == enabled) {
                ToolPouchUtil.toggleSlot(pouch, slot);
                changed++;
            }
        }
        return changed;
    }
}
