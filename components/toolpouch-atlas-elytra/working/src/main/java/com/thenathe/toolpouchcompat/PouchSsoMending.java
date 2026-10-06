package com.thenathe.toolpouchcompat;

import me.pajic.simple_smithing_overhaul.SSO;
import me.pajic.simple_smithing_overhaul.util.ModUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

/** Optional original SSO material repair for exhausted Elytra in the active pouch. */
public final class PouchSsoMending {
    private PouchSsoMending() {}
    public static boolean regularXpEnabled() {
        return !SSO.CONFIG.mendingRework.enabled.get() || SSO.CONFIG.mendingRework.enableRegularMendingBehavior.get();
    }
    public static void tick(ServerPlayer player) {
        if (!SSO.CONFIG.mendingRework.enabled.get() || !SSO.CONFIG.mendingRework.autoRepairOnBreak.get()) return;
        var pouch = PouchItems.active(player);
        if (pouch == null) return;
        var items = pouch.items();
        for (int slot = 0; slot < items.size(); slot++) {
            var before = items.get(slot);
            if (!before.is(Items.ELYTRA) || !before.isDamageableItem()
                    || !(ModUtil.isBroken(before) || before.getDamageValue() >= before.getMaxDamage() - 1)) continue;
            var updated = before.copy();
            // Original SSO enforces Mending, compatible whetstone enchantments and repair material/cost.
            if (ModUtil.tryRepairItem(updated, player, player.level())) pouch.write(player, slot, updated);
        }
    }
}
