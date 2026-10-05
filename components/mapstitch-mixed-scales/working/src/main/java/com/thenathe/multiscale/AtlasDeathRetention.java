package com.thenathe.multiscale;

import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gamerules.GameRules;
import java.util.List;

/** Extracts atlases, never their containing items, before ordinary death drops. */
public final class AtlasDeathRetention {
    public static final String SAVE_KEY = "VanillaPlusPlusRetainedAtlases";
    public interface State { List<ItemStack> suite$retainedAtlases(); }
    private AtlasDeathRetention() {}

    public static void beforeDrops(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !MapStitch.CONFIG.keepAtlasOnDeath.get()
                || player.isSpectator() || serverPlayer.level().getGameRules().get(GameRules.KEEP_INVENTORY)) return;
        List<ItemStack> retained = ((State) player).suite$retainedAtlases();
        // A full inventory makes vanilla drop the cursor when closing a menu.
        // Rescue its atlas (or only the nested atlases) before that can happen.
        ItemStack cursor = serverPlayer.containerMenu.getCarried();
        serverPlayer.containerMenu.setCarried(extract(cursor, retained, 1));
        // Open pouch/backpack menus write their final contents when closed.
        if (serverPlayer.containerMenu != serverPlayer.inventoryMenu) serverPlayer.closeContainer();
        var visited = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<ItemStack, Boolean>());
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            collect(player.getInventory().getItem(i), retained, visited);
        }
        // Original optional accessory integrations expose their equipped live stack.
        if (me.pajic.toolpouch.compat.AccessoryUtil.INSTANCE != null)
            collect(me.pajic.toolpouch.compat.AccessoryUtil.INSTANCE.tryGetToolPouch(player), retained, visited);
        if (me.pajic.tiered_backpacks.compat.AccessoryUtil.INSTANCE != null)
            collect(me.pajic.tiered_backpacks.compat.AccessoryUtil.INSTANCE.getBackpack(player), retained, visited);
        player.getInventory().setChanged();
    }

    private static void collect(ItemStack stack, List<ItemStack> retained, java.util.Set<ItemStack> visited) {
        if (!stack.isEmpty() && !stack.is(ModItems.ATLAS) && !vanishes(stack) && visited.add(stack)) rewrite(stack, retained, 1);
    }

    private static boolean vanishes(ItemStack stack) {
        return EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP);
    }

    private static ItemStack extract(ItemStack stack, List<ItemStack> retained, int copies) {
        if (stack.isEmpty() || vanishes(stack)) return stack;
        if (stack.is(ModItems.ATLAS)) {
            // Atlas BUNDLE_CONTENTS are maps/paper belonging to the atlas, not an outer container.
            for (int i = 0; i < copies; i++) retained.add(stack.copy());
            return ItemStack.EMPTY;
        }
        rewrite(stack, retained, copies);
        return stack;
    }

    private static void rewrite(ItemStack stack, List<ItemStack> retained, int copies) {
        int contentCopies = Math.multiplyExact(copies, stack.getCount());
        var container = stack.get(DataComponents.CONTAINER);
        if (container != null) {
            int previous = retained.size();
            var contents = container.itemCopies().map(item -> extract(item, retained, contentCopies)).toList();
            if (retained.size() != previous) stack.set(DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.fromItems(contents));
        }
        var bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) {
            int previous = retained.size();
            var contents = bundle.itemCopies().map(item -> extract(item, retained, contentCopies)).filter(item -> !item.isEmpty()).toList();
            if (retained.size() != previous) stack.set(DataComponents.BUNDLE_CONTENTS, bundle.copyWithContents(contents.stream()));
        }
    }

    public static void transfer(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        List<ItemStack> previous = ((State) oldPlayer).suite$retainedAtlases();
        ((State) newPlayer).suite$retainedAtlases().addAll(previous);
        previous.clear();
        flush(newPlayer);
    }

    public static void flush(Player player) {
        if (!(player instanceof ServerPlayer) || !player.isAlive()) return;
        List<ItemStack> retained = ((State) player).suite$retainedAtlases();
        if (retained.isEmpty()) return;
        retained.removeIf(stack -> { player.getInventory().add(stack); return stack.isEmpty(); });
    }
}
