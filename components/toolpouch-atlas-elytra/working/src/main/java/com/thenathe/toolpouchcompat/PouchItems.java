package com.thenathe.toolpouchcompat;

import com.thenathe.toolpouchcompat.mixin.PouchMendingAccess;
import com.thenathe.toolpouchcompat.mixin.PouchMendingMenuAccess;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import java.util.ArrayList;
import java.util.List;

/** Uses the original active-pouch policy and its live menu inventory when open. */
public record PouchItems(ItemStack owner, SimpleContainer live) {
    public static PouchItems active(ServerPlayer player) {
        var owner = PouchMendingAccess.toolpouchCompat$activePouch(player);
        if (owner.isEmpty()) return null;
        var live = player.containerMenu instanceof ToolPouchMenu menu && menu.getToolPouch() == owner
                ? ((PouchMendingMenuAccess) menu).toolpouchCompat$liveContents() : null;
        return new PouchItems(owner, live);
    }
    public List<ItemStack> items() {
        return live != null ? live.getItems()
                : owner.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).itemCopies().toList();
    }
    public void write(ServerPlayer player, int slot, ItemStack updated) {
        if (live != null) live.setItem(slot, updated);
        else {
            var contents = new ArrayList<>(items());
            contents.set(slot, updated);
            owner.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
        }
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
    }
}
