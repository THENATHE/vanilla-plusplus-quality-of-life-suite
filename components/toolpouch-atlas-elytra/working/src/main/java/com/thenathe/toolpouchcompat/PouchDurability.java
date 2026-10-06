package com.thenathe.toolpouchcompat;

import me.pajic.toolpouch.util.ItemStackTemplateUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStackTemplate;

/** Authoritative flight wear follows ordinary enchantments and preserves the selected glider slot. */
public final class PouchDurability {
    private PouchDurability() {}
    public static void damage(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || player.hasInfiniteMaterials() || !Compat.enabled(player)) return;
        var pouch = PouchItems.active(serverPlayer);
        if (pouch == null) return;
        var items = pouch.items();
        for (int slot = 0; slot < items.size(); slot++) {
            var before = items.get(slot);
            if (before.isEmpty() || !before.has(DataComponents.GLIDER)
                    || ItemStackTemplateUtil.nextDamageWillBreak(ItemStackTemplate.fromNonEmptyStack(before))) continue;
            var updated = before.copy();
            updated.hurtAndBreak(1, serverPlayer.level(), serverPlayer,
                    item -> serverPlayer.onEquippedItemBroken(item, EquipmentSlot.CHEST));
            if (!net.minecraft.world.item.ItemStack.matches(before, updated)) pouch.write(serverPlayer, slot, updated);
            return;
        }
    }
}
