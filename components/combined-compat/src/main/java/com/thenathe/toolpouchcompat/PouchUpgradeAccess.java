package com.thenathe.toolpouchcompat;

import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/** Pickup upgrades may read the active pouch on clients without its native interface. */
public final class PouchUpgradeAccess {
    private static final ScopedValue<Boolean> READING_UPGRADES = ScopedValue.newInstance();

    private PouchUpgradeAccess() {}

    public static boolean readingUpgrades() {
        return READING_UPGRADES.orElse(false);
    }

    public static boolean contains(Player player, Item upgrade) {
        return ScopedValue.where(READING_UPGRADES, true)
                .call(() -> ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(upgrade)));
    }
}
