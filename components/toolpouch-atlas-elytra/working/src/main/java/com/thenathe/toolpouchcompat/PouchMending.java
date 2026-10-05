package com.thenathe.toolpouchcompat;

import com.thenathe.toolpouchcompat.mixin.PouchMendingAccess;
import com.thenathe.toolpouchcompat.mixin.PouchMendingMenuAccess;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;

/** Repairs stored Elytra using only XP left after the regular equipment repair pass. */
public final class PouchMending {
    private static boolean eligible(ItemStack stack) {
        if (!stack.is(Items.ELYTRA) || !stack.isDamaged()) return false;
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet().stream()
                .anyMatch(entry -> entry.getKey().value().matchingSlot(EquipmentSlot.CHEST)
                        && entry.getKey().value().effects().has(EnchantmentEffectComponents.REPAIR_WITH_XP));
    }

    public static int repair(ServerPlayer player, int experience) {
        if (experience <= 0) return experience;
        ItemStack owner = PouchMendingAccess.toolpouchCompat$activePouch(player);
        if (owner.isEmpty()) return experience;

        // An open pouch owns a mutable inventory. Updating its stored snapshot would be lost on close.
        SimpleContainer live = player.containerMenu instanceof ToolPouchMenu menu && menu.getToolPouch() == owner
                ? ((PouchMendingMenuAccess) menu).toolpouchCompat$liveContents() : null;
        List<ItemStack> contents = live != null ? live.getItems()
                : owner.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).itemCopies().toList();
        var candidates = new ArrayList<Integer>();
        for (int i = 0; i < contents.size(); i++) if (eligible(contents.get(i))) candidates.add(i);

        // Each candidate is considered once: a partial repair consumes all remaining XP;
        // a full repair removes it from eligibility. Zero-effect datapack conditions must not loop.
        while (experience > 0 && !candidates.isEmpty()) {
            int slot = candidates.remove(player.getRandom().nextInt(candidates.size()));
            ItemStack before = contents.get(slot).copy();
            int capacity = EnchantmentHelper.modifyDurabilityToRepairFromXp(player.level(), before, experience);
            int repaired = Math.min(capacity, before.getDamageValue());
            if (repaired <= 0) continue;
            ItemStack updated = before.copy();
            updated.setDamageValue(before.getDamageValue() - repaired);

            if (live != null) {
                if (!ItemStack.matches(live.getItem(slot), before)) continue;
                live.setItem(slot, updated);
            } else {
                var current = new ArrayList<>(owner.getOrDefault(DataComponents.CONTAINER,
                        ItemContainerContents.EMPTY).itemCopies().toList());
                if (slot >= current.size() || !ItemStack.matches(current.get(slot), before)) continue;
                current.set(slot, updated);
                owner.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(current));
            }
            player.getInventory().setChanged();
            // Match vanilla's proportional, integer-rounded XP charge, including odd damage values.
            experience -= (int) ((long) repaired * experience / capacity);
        }
        return experience;
    }

    private PouchMending() {}
}
