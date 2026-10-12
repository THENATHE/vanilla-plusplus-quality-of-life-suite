package com.thenathe.suite.pickup;

import me.pajic.tiered_backpacks.TieredBackpacks;
import me.pajic.tiered_backpacks.ui.BackpackMenu;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.util.AllowedItem;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashSet;

/** Server-authoritative upgrades use the existing pouch selector and backpack storage. */
public final class PickupUpgrades {
    public static final Identifier MAGNET_ID = Identifier.fromNamespaceAndPath("thenathe_mod_suite", "pouch_magnet");
    public static final Identifier COLLECTOR_ID = Identifier.fromNamespaceAndPath("thenathe_mod_suite", "pouch_collector");
    public static final double MAGNET_RADIUS = 6.0;
    public static final String DISABLED_TAG = "thenathe_mod_suite:collector_disabled";

    private static Item magnet;
    private static Item collector;

    private PickupUpgrades() {}

    public static void initialize(Item magnetItem, Item collectorItem) {
        magnet = magnetItem;
        collector = collectorItem;
        migratePouchRules();
        PayloadTypeRegistry.serverboundPlay().register(PickupTogglePayload.TYPE, PickupTogglePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PickupTogglePayload.TYPE,
                (payload, context) -> toggle(context.player()));
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (enabled(oldPlayer)) newPlayer.removeTag(DISABLED_TAG);
            else newPlayer.addTag(DISABLED_TAG);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> dispatcher.register(
                Commands.literal("suite-pickup")
                        .executes(context -> {
                            toggle(context.getSource().getPlayerOrException());
                            return 1;
                        })
                        .then(Commands.literal("on").executes(context -> {
                            setEnabled(context.getSource().getPlayerOrException(), true);
                            return 1;
                        }))
                        .then(Commands.literal("off").executes(context -> {
                            setEnabled(context.getSource().getPlayerOrException(), false);
                            return 1;
                        }))));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 5 != 0) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                pickupNearby(player);
            }
        });
    }

    public static void pickupNearby(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || !hasMagnet(player)) return;
        var nearby = player.level().getEntitiesOfClass(ItemEntity.class,
                player.getBoundingBox().inflate(MAGNET_RADIUS),
                entity -> entity.isAlive() && entity.distanceToSqr(player) <= MAGNET_RADIUS * MAGNET_RADIUS);
        for (ItemEntity entity : nearby) entity.playerTouch(player);
    }

    public static boolean enabled(Player player) {
        return !player.entityTags().contains(DISABLED_TAG);
    }

    public static void toggle(ServerPlayer player) {
        setEnabled(player, !enabled(player));
    }

    public static void setEnabled(ServerPlayer player, boolean value) {
        if (value) player.removeTag(DISABLED_TAG);
        else player.addTag(DISABLED_TAG);
        player.sendOverlayMessage(Component.translatable(value
                ? "message.thenathe_mod_suite.collector_enabled"
                : "message.thenathe_mod_suite.collector_disabled"));
    }

    public static boolean hasMagnet(Player player) {
        return magnet != null && !(player.containerMenu instanceof ToolPouchMenu)
                && com.thenathe.toolpouchcompat.PouchUpgradeAccess.contains(player, magnet);
    }

    public static boolean hasCollector(Player player) {
        return collector != null && !(player.containerMenu instanceof ToolPouchMenu)
                && com.thenathe.toolpouchcompat.PouchUpgradeAccess.contains(player, collector);
    }

    /** Mutates the dropped stack only after the backpack component is committed. */
    public static int collectIntoBackpack(ServerPlayer player, ItemStack incoming) {
        if (incoming.isEmpty() || !player.isAlive() || player.isSpectator() || !enabled(player)
                || !hasCollector(player) || player.containerMenu instanceof BackpackMenu) return 0;
        if (BackpackUtil.isValidContainerHolder(incoming)
                || (!TieredBackpacks.CONFIG.canStoreShulkers.get() && incoming.is(ItemTags.SHULKER_BOXES))) return 0;
        ItemStack backpack = activeBackpack(player);
        if (backpack.isEmpty()) return 0;

        var dimensions = BackpackUtil.getBackpackDimensions(backpack);
        int capacity = dimensions.rows.get() * dimensions.columns.get();
        ItemContainerContents contents = backpack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        // A reduced configured size must never truncate existing saved slots.
        if (capacity <= 0 || contents.size() > capacity) return 0;
        NonNullList<ItemStack> slots = NonNullList.withSize(capacity, ItemStack.EMPTY);
        contents.copyInto(slots);
        // Templates can fail strict validation after an administrator reduces item limits.
        // Keep that original component intact instead of committing a copy with missing items.
        int savedItems = 0;
        for (var template : contents.nonEmptyItems()) savedItems++;
        if (savedItems != slots.stream().filter(stack -> !stack.isEmpty()).count()) return 0;
        SimpleContainer container = new SimpleContainer(capacity);
        int remaining = incoming.getCount();
        for (ItemStack stored : slots) {
            if (stored.isEmpty() || !ItemStack.isSameItemSameComponents(stored, incoming)) continue;
            int available = Math.max(0, container.getMaxStackSize(stored) - stored.getCount());
            int moved = Math.min(remaining, available);
            stored.grow(moved);
            remaining -= moved;
            if (remaining == 0) break;
        }
        for (int index = 0; index < capacity && remaining > 0; index++) {
            if (!slots.get(index).isEmpty()) continue;
            int moved = Math.min(remaining, container.getMaxStackSize(incoming));
            if (moved <= 0) break;
            slots.set(index, incoming.copyWithCount(moved));
            remaining -= moved;
        }
        int inserted = incoming.getCount() - remaining;
        if (inserted > 0) {
            backpack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(slots));
            incoming.shrink(inserted);
        }
        return inserted;
    }

    public static ItemStack activeBackpack(Player player) {
        var accessories = me.pajic.tiered_backpacks.compat.AccessoryUtil.INSTANCE;
        if (accessories != null) {
            ItemStack equipped = accessories.getBackpack(player);
            if (BackpackUtil.isValidContainerHolder(equipped)) return equipped;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (BackpackUtil.isValidContainerHolder(chest)) return chest;
        return player.getInventory().getNonEquipmentItems().stream()
                .filter(BackpackUtil::isValidContainerHolder).findFirst().orElse(ItemStack.EMPTY);
    }

    private static void migratePouchRules() {
        var marker = FabricLoader.getInstance().getConfigDir().resolve("vanilla-plusplus-pickup-rules-v1");
        if (Files.exists(marker)) return;
        var rules = new LinkedHashSet<AllowedItem>(ToolPouch.CONFIG.allowedItems.get());
        for (Identifier id : new Identifier[]{MAGNET_ID, COLLECTOR_ID}) {
            if (rules.stream().noneMatch(rule -> rule.id.get().equals(id.toString()))) {
                rules.add(new AllowedItem(id.toString(), 1, 1));
            }
        }
        ToolPouch.CONFIG.allowedItems.accept(rules);
        ToolPouch.CONFIG.save();
        try {
            Files.createDirectories(marker.getParent());
            Files.writeString(marker, "Pickup upgrade rules added once. Later administrator removals and limits are preserved.\n");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not record Tool Pouch pickup upgrade migration", exception);
        }
    }
}
