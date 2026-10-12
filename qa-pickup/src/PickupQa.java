package qa.pickup;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.thenathe.suite.pickup.PickupUpgrades;
import me.pajic.tiered_backpacks.TieredBackpacks;
import me.pajic.tiered_backpacks.item.ModItems;
import me.pajic.tiered_backpacks.ui.BackpackMenu;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Real dropped-item touch/scan mechanics; physical client connections have separate evidence. */
public final class PickupQa implements ModInitializer {
    private final JsonArray cases = new JsonArray();
    private int checks;
    private MinecraftServer server;
    private Item magnet;
    private Item collector;

    private void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private void record(String name) {
        cases.add(name);
    }

    private void enableEntityTracking() throws Exception {
        var field = net.minecraft.server.level.ServerLevel.class.getDeclaredField("entityManager");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        var manager = (net.minecraft.world.level.entity.PersistentEntitySectionManager<net.minecraft.world.entity.Entity>) field.get(server.overworld());
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            server.overworld().getChunk(x, z);
            manager.updateChunkStatus(new net.minecraft.world.level.ChunkPos(x, z), net.minecraft.world.level.entity.Visibility.TRACKED);
        }
    }

    private ServerPlayer player() {
        var player = new ServerPlayer(server, server.overworld(), new GameProfile(UUID.randomUUID(), "PickupQa"), ClientInformation.createDefault());
        player.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player,
                CommonListenerCookie.createInitial(player.getGameProfile(), false)) {
            @Override public void send(Packet<?> packet) {}
        };
        player.setPos(0, 100, 0);
        player.level().getChunkAt(player.blockPosition());
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }

    private ItemStack holder(Item item, ItemStack... contents) {
        var holder = new ItemStack(item);
        holder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(contents)));
        return holder;
    }

    private ItemStack seed(ServerPlayer player, boolean useMagnet, boolean useCollector, boolean backpack) {
        player.containerMenu = player.inventoryMenu;
        player.getInventory().clearContent();
        player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        player.setGameMode(GameType.SURVIVAL);
        PickupUpgrades.setEnabled(player, true);
        var upgrades = new ArrayList<ItemStack>();
        if (useMagnet) upgrades.add(new ItemStack(magnet));
        if (useCollector) upgrades.add(new ItemStack(collector));
        var pouch = new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
        pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(upgrades));
        player.getInventory().setItem(12, pouch);
        var bag = holder(ModItems.LEATHER_BACKPACK);
        if (backpack) player.getInventory().setItem(13, bag);
        return bag;
    }

    private ItemEntity drop(ServerPlayer player, ItemStack stack, double x, double z) {
        var entity = new ItemEntity(player.level(), player.getX() + x, player.getY(), player.getZ() + z, stack);
        entity.setNoPickUpDelay();
        check(player.level().addFreshEntity(entity), "Item entity spawned in actual world");
        return entity;
    }

    private int stored(ItemStack bag, Item item) {
        return bag.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).itemCopies()
                .filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    private int inventory(ServerPlayer player, Item item) {
        return player.getInventory().getNonEquipmentItems().stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    private void pouchRules(ServerPlayer player) {
        var pouch = holder(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
        var menu = new ToolPouchMenu(1, player.getInventory(), pouch);
        for (Item upgrade : List.of(magnet, collector)) {
            check(menu.getSlot(0).mayPlace(new ItemStack(upgrade)), "Real pouch slot accepts registered upgrade");
            check(menu.getSlot(0).getMaxStackSize(new ItemStack(upgrade)) == 1, "Upgrade storage stack limit remains one");
            menu.getSlot(0).set(new ItemStack(upgrade));
            check(!menu.getSlot(1).mayPlace(new ItemStack(upgrade)), "Duplicate upgrade slot is rejected by count rule");
            menu.getSlot(0).set(ItemStack.EMPTY);
        }
        menu.removed(player);
        check(ToolPouch.CONFIG.allowedItems.get().stream().filter(rule -> rule.id.get().equals(PickupUpgrades.MAGNET_ID.toString())).count() == 1,
                "Migration appends magnet rule once across restart");
        check(ToolPouch.CONFIG.allowedItems.get().stream().filter(rule -> rule.id.get().equals(PickupUpgrades.COLLECTOR_ID.toString())).count() == 1,
                "Migration appends collector rule once across restart");
        record("real-pouch-slot-allowlist-and-one-upgrade-rule");
    }

    private void recipes() {
        var inputs = List.of(
                CraftingInput.of(3, 3, List.of(new ItemStack(Items.IRON_INGOT), ItemStack.EMPTY, new ItemStack(Items.IRON_INGOT),
                        new ItemStack(Items.IRON_INGOT), new ItemStack(Items.NETHER_STAR), new ItemStack(Items.IRON_INGOT),
                        new ItemStack(Items.REDSTONE), ItemStack.EMPTY, new ItemStack(Items.REDSTONE))),
                CraftingInput.of(3, 3, List.of(new ItemStack(Items.LEATHER), ItemStack.EMPTY, new ItemStack(Items.LEATHER),
                        new ItemStack(Items.HOPPER), new ItemStack(Items.NETHER_STAR), new ItemStack(Items.HOPPER),
                        new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER))));
        for (int index = 0; index < inputs.size(); index++) {
            var input = inputs.get(index);
            var recipe = server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, server.overworld()).orElseThrow();
            var output = recipe.value().assemble(input);
            check(output.is(index == 0 ? magnet : collector) && output.getCount() == 1, "Real upgrade crafting result");
            var changed = new ArrayList<>(input.items());
            changed.set(4, new ItemStack(Items.QUARTZ));
            check(server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, CraftingInput.of(3, 3, changed), server.overworld()).isEmpty(), "Nether star cannot be substituted");
        }
        record("both-real-nether-star-crafting-recipes");
    }

    private void mechanics(ServerPlayer player) throws Exception {
        if (FabricLoader.getInstance().isModLoaded("polymer-core")) {
            seed(player, true, true, true);
            check(!com.thenathe.toolpouchcompat.ToolpouchCompat.nativeClient(player.connection.getPacketContext()),
                    "Polymer fixture intentionally has unsupported client capabilities");
            check(!me.pajic.toolpouch.util.ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(magnet)),
                    "Existing generic pouch gameplay lookup remains guarded for unsupported client");
            check(PickupUpgrades.hasMagnet(player) && PickupUpgrades.hasCollector(player),
                    "New upgrades activate for unsupported client through scoped selector");
            check(!me.pajic.toolpouch.util.ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(magnet)),
                    "Scoped lookup leaves existing passive feature guard intact afterward");
            record("unsupported-client-upgrades-preserve-existing-passive-guard");
        }
        var bag = seed(player, true, false, true);
        var near = drop(player, new ItemStack(Items.DIAMOND, 7), 5.9, 0);
        var outside = drop(player, new ItemStack(Items.EMERALD, 3), 6.1, 0);
        var diagonal = drop(player, new ItemStack(Items.GOLD_INGOT, 4), 5, 5);
        PickupUpgrades.pickupNearby(player);
        check(near.isRemoved() && inventory(player, Items.DIAMOND) == 7 && stored(bag, Items.DIAMOND) == 0, "Magnet alone uses ordinary inventory: removed=" + near.isRemoved() + " inventory=" + inventory(player, Items.DIAMOND) + " hasMagnet=" + PickupUpgrades.hasMagnet(player) + " nearby=" + player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(6)).size());
        check(!outside.isRemoved() && !diagonal.isRemoved(), "Six-block spherical radius excludes outside and diagonal corner");
        outside.discard(); diagonal.discard();
        record("magnet-only-inventory-and-spherical-radius");

        bag = seed(player, false, true, true);
        var normal = drop(player, new ItemStack(Items.DIAMOND, 11), 0, 0);
        normal.playerTouch(player);
        check(normal.isRemoved() && stored(bag, Items.DIAMOND) == 11 && inventory(player, Items.DIAMOND) == 0, "Collector alone routes real pickup");
        var distant = drop(player, new ItemStack(Items.EMERALD, 2), 5, 0);
        PickupUpgrades.pickupNearby(player);
        check(!distant.isRemoved(), "Collector alone does not extend range");
        distant.discard();
        record("collector-only-normal-range");

        bag = seed(player, true, true, true);
        var combined = drop(player, new ItemStack(Items.DIAMOND, 9), 5, 0);
        PickupUpgrades.pickupNearby(player);
        check(combined.isRemoved() && stored(bag, Items.DIAMOND) == 9 && inventory(player, Items.DIAMOND) == 0, "Combined magnet/collector routes distant drop");
        var delayed = drop(player, new ItemStack(Items.EMERALD), 3, 0);
        delayed.setPickUpDelay(20);
        var targeted = drop(player, new ItemStack(Items.GOLD_INGOT), 3, 0);
        targeted.setTarget(UUID.randomUUID());
        PickupUpgrades.pickupNearby(player);
        check(!delayed.isRemoved() && !targeted.isRemoved() && stored(bag, Items.EMERALD) == 0 && stored(bag, Items.GOLD_INGOT) == 0, "Delay and ownership protection survive both upgrades");
        delayed.discard(); targeted.discard();
        record("combined-and-pickup-delay-target-protection");

        PickupUpgrades.setEnabled(player, false);
        var disabled = drop(player, new ItemStack(Items.EMERALD, 6), 4, 0);
        PickupUpgrades.pickupNearby(player);
        check(disabled.isRemoved() && inventory(player, Items.EMERALD) == 6 && stored(bag, Items.EMERALD) == 0, "Disabled collector keeps magnet working");
        server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), "suite-pickup on");
        check(PickupUpgrades.enabled(player), "Registered on command works without operator permission");
        server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), "suite-pickup");
        check(!PickupUpgrades.enabled(player), "Registered toggle command works");
        record("collector-toggle-command-magnet-independence");

        seed(player, true, true, false);
        var missingBag = drop(player, new ItemStack(Items.DIAMOND, 8), 4, 0);
        PickupUpgrades.pickupNearby(player);
        check(missingBag.isRemoved() && inventory(player, Items.DIAMOND) == 8, "No backpack falls back to inventory");
        player.getInventory().setItem(12, ItemStack.EMPTY);
        var missingPouch = drop(player, new ItemStack(Items.EMERALD), 4, 0);
        PickupUpgrades.pickupNearby(player);
        check(!missingPouch.isRemoved(), "Removed pouch disables magnet");
        missingPouch.discard();
        player.getInventory().setItem(1, new ItemStack(magnet));
        player.getInventory().setItem(2, new ItemStack(collector));
        check(!PickupUpgrades.hasMagnet(player) && !PickupUpgrades.hasCollector(player), "Loose upgrades do not activate");
        record("no-backpack-no-pouch-and-loose-upgrades");

        bag = seed(player, true, true, true);
        var pouch = player.getInventory().getItem(12);
        var attached = new ItemStack(Items.DIAMOND_LEGGINGS);
        var attachInput = CraftingInput.of(2, 1, List.of(attached, pouch));
        var attachRecipe = new me.pajic.toolpouch.recipe.AttachToolPouchRecipe();
        check(attachRecipe.matches(attachInput, player.level()), "Native pouch attachment recipe works");
        player.getInventory().setItem(12, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.LEGS, attachRecipe.assemble(attachInput));
        check(PickupUpgrades.hasMagnet(player) && PickupUpgrades.hasCollector(player), "Attached pouch activates both upgrades");
        var chestInput = CraftingInput.of(2, 1, List.of(new ItemStack(Items.DIAMOND_CHESTPLATE), bag));
        var chestRecipe = new me.pajic.tiered_backpacks.recipe.AttachBackpackRecipe();
        check(chestRecipe.matches(chestInput, player.level()), "Native backpack attachment recipe works");
        var chest = chestRecipe.assemble(chestInput);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        var attachedDrop = drop(player, new ItemStack(Items.DIAMOND, 5), 4, 0);
        PickupUpgrades.pickupNearby(player);
        check(attachedDrop.isRemoved() && stored(chest, Items.DIAMOND) == 5 && stored(bag, Items.DIAMOND) == 0, "Attached backpack takes precedence over inventory backpack");
        record("attached-pouch-and-backpack-priority");

        bag = seed(player, true, true, true);
        pouch = player.getInventory().getItem(12);
        var pouchMenu = new ToolPouchMenu(2, player.getInventory(), pouch);
        player.containerMenu = pouchMenu;
        var openPouch = drop(player, new ItemStack(Items.EMERALD), 4, 0);
        PickupUpgrades.pickupNearby(player);
        check(!openPouch.isRemoved() && !PickupUpgrades.hasCollector(player), "Open pouch snapshot suspends upgrades");
        openPouch.discard();
        pouchMenu.removed(player); player.containerMenu = player.inventoryMenu;
        var bagMenu = new BackpackMenu(3, player.getInventory(), bag);
        player.containerMenu = bagMenu;
        var openBag = drop(player, new ItemStack(Items.DIAMOND, 3), 0, 0);
        openBag.playerTouch(player);
        check(openBag.isRemoved() && inventory(player, Items.DIAMOND) == 3 && stored(bag, Items.DIAMOND) == 0, "Open backpack uses ordinary pickup without stale saveback");
        bagMenu.removed(player); player.containerMenu = player.inventoryMenu;
        check(stored(bag, Items.DIAMOND) == 0, "Close preserves unchanged backpack contents");
        record("open-pouch-and-backpack-snapshot-safety");

        bag = seed(player, false, true, true);
        TieredBackpacks.CONFIG.canStoreShulkers.accept(false);
        var shulker = new ItemStack(Items.SHULKER_BOX);
        check(PickupUpgrades.collectIntoBackpack(player, shulker) == 0 && shulker.getCount() == 1, "Configured shulker restriction applies");
        var nested = new ItemStack(ModItems.COPPER_BACKPACK);
        check(PickupUpgrades.collectIntoBackpack(player, nested) == 0 && nested.getCount() == 1, "Backpack nesting restriction applies");
        TieredBackpacks.CONFIG.canStoreShulkers.accept(true);
        check(PickupUpgrades.collectIntoBackpack(player, shulker) == 1 && shulker.isEmpty(), "Allowed shulker can be collected");
        TieredBackpacks.CONFIG.canStoreShulkers.accept(false);
        record("backpack-nesting-and-configured-shulker-rules");

        bag = seed(player, false, true, true);
        var slots = new ArrayList<ItemStack>();
        for (int index = 0; index < 27; index++) slots.add(new ItemStack(Items.DIAMOND, new ItemStack(Items.DIAMOND).getMaxStackSize()));
        slots.set(0, new ItemStack(Items.DIAMOND, new ItemStack(Items.DIAMOND).getMaxStackSize() - 3));
        bag.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(slots));
        var partial = drop(player, new ItemStack(Items.DIAMOND, 10), 0, 0);
        partial.playerTouch(player);
        check(partial.isRemoved() && inventory(player, Items.DIAMOND) == 7 && stored(bag, Items.DIAMOND) == 27 * new ItemStack(Items.DIAMOND).getMaxStackSize(), "Partial backpack insert puts exact remainder in inventory");
        var full = drop(player, new ItemStack(Items.EMERALD, 10), 0, 0);
        for (int index = 0; index < 36; index++) if (index != 12 && index != 13) player.getInventory().setItem(index, new ItemStack(Items.STONE, new ItemStack(Items.STONE).getMaxStackSize()));
        full.playerTouch(player);
        check(!full.isRemoved() && full.getItem().getCount() == 10 && inventory(player, Items.EMERALD) == 0, "Full bag and inventory retain world drop without loss");
        full.discard();
        record("partial-insert-and-full-inventory-conservation");

        bag = seed(player, false, true, true);
        var oversized = new ArrayList<ItemStack>();
        for (int index = 0; index < 28; index++) oversized.add(index == 27 ? new ItemStack(Items.EMERALD, 7) : ItemStack.EMPTY);
        bag.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(oversized));
        var reduced = new ItemStack(Items.DIAMOND, 5);
        var before = bag.get(DataComponents.CONTAINER);
        check(PickupUpgrades.collectIntoBackpack(player, reduced) == 0 && reduced.getCount() == 5 && before.equals(bag.get(DataComponents.CONTAINER)) && stored(bag, Items.EMERALD) == 7, "Reduced capacity preserves saved overflow unchanged");
        record("saved-overflow-after-capacity-reduction");

        bag = seed(player, true, true, true);
        player.setGameMode(GameType.SPECTATOR);
        var spectator = drop(player, new ItemStack(Items.DIAMOND), 4, 0);
        PickupUpgrades.pickupNearby(player);
        check(!spectator.isRemoved(), "Spectator cannot collect via magnet");
        spectator.discard(); player.setGameMode(GameType.SURVIVAL);
        record("spectator-guard");
    }

    private void largeStacks(ServerPlayer player) throws Exception {
        var config = me.pajic.sensible_stackables.SensibleStackables.CONFIG;
        boolean originalUncap = config.uncapStackSize.get();
        int originalCommon = config.commonStackSize.get();
        var originalItems = new java.util.HashMap<>(config.items.get());
        try {
            config.uncapStackSize.validateAndSet(false);
            config.commonStackSize.validateAndSet(64);
            config.items.validateAndSet(java.util.Map.of());
            me.pajic.sensible_stackables.SensibleStackables.onUpdateConfig(server);
            var bag = seed(player, false, true, true);
            var large = new ItemStack(Items.DIAMOND, 150);
            large.set(DataComponents.MAX_STACK_SIZE, 200);
            int limit = new net.minecraft.world.SimpleContainer(27).getMaxStackSize(large);
            check(limit == 99, "Bounded container fixture uses original99 limit");
            check(PickupUpgrades.collectIntoBackpack(player, large) == 150 && large.isEmpty(), "Huge incoming count splits without losing remainder");
            var placed = bag.get(DataComponents.CONTAINER).itemCopies().filter(stack -> !stack.isEmpty()).toList();
            check(placed.size() == 2 && placed.get(0).getCount() == 99 && placed.get(1).getCount() == 51,
                    "Huge drop occupies exact99 and51 slots");

            bag = seed(player, false, true, true);
            var saved = new ItemStack(Items.DIAMOND, 150);
            saved.set(DataComponents.MAX_STACK_SIZE, 200);
            bag.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(saved)));
            var unrelated = new ItemStack(Items.EMERALD, 4);
            check(PickupUpgrades.collectIntoBackpack(player, unrelated) == 4 && unrelated.isEmpty(), "Unrelated pickup succeeds beside saved oversize slot");
            check(stored(bag, Items.DIAMOND) == 150 && stored(bag, Items.EMERALD) == 4,
                    "Saved oversize stack is never clamped by new insertion");

            bag = seed(player, false, true, true);
            var contents = new ArrayList<ItemStack>();
            for (int index = 0; index < 27; index++) contents.add(index == 0 ? ItemStack.EMPTY : new ItemStack(Items.EMERALD, 64));
            bag.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
            var remainder = new ItemStack(Items.DIAMOND, 150);
            remainder.set(DataComponents.MAX_STACK_SIZE, 200);
            check(PickupUpgrades.collectIntoBackpack(player, remainder) == 99 && remainder.getCount() == 51
                    && stored(bag, Items.DIAMOND) == 99, "One available slot conserves99inserted plus51remaining");
            record("huge-incoming-and-oversize-saved-stack-conservation");

            config.uncapStackSize.validateAndSet(true);
            config.items.validateAndSet(java.util.Map.of("minecraft:diamond", 200));
            me.pajic.sensible_stackables.SensibleStackables.onUpdateConfig(server);
            bag = seed(player, false, true, true);
            bag.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND, 150))));
            var snapshot = bag.get(DataComponents.CONTAINER);
            config.uncapStackSize.validateAndSet(false);
            config.items.validateAndSet(java.util.Map.of("minecraft:diamond", 32));
            me.pajic.sensible_stackables.SensibleStackables.onUpdateConfig(server);
            var incoming = new ItemStack(Items.EMERALD, 6);
            check(PickupUpgrades.collectIntoBackpack(player, incoming) == 0 && incoming.getCount() == 6
                    && snapshot.equals(bag.get(DataComponents.CONTAINER)), "Reduced effective limit cannot overwrite unmaterializable saved templates");
            config.uncapStackSize.validateAndSet(true);
            config.items.validateAndSet(java.util.Map.of("minecraft:diamond", 200));
            me.pajic.sensible_stackables.SensibleStackables.onUpdateConfig(server);
            check(stored(bag, Items.DIAMOND) == 150, "Restoring limit materializes all150original saved items");
            record("hot-reduced-stack-limit-fails-closed-with-original-content");
        } finally {
            config.uncapStackSize.validateAndSet(originalUncap);
            config.commonStackSize.validateAndSet(originalCommon);
            config.items.validateAndSet(originalItems);
            me.pajic.sensible_stackables.SensibleStackables.onUpdateConfig(server);
        }
    }

    private void persistence(ServerPlayer player) throws Exception {
        var path = Path.of("pickup-player.nbt");
        if (Files.exists(path)) {
            var saved = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            player.load(TagValueInput.create(ProblemReporter.DISCARDING, server.registryAccess(), saved));
            check(!PickupUpgrades.enabled(player), "Collector preference survives actual file and server restart");
            check(PickupUpgrades.hasMagnet(player) && PickupUpgrades.hasCollector(player), "Pouch upgrade IDs and contents survive restart");
            check(stored(PickupUpgrades.activeBackpack(player), Items.DIAMOND) == 17, "Backpack collected contents survive restart");
            record("server-restart-player-preference-and-stored-items");
        }
        var bag = seed(player, true, true, true);
        var incoming = new ItemStack(Items.DIAMOND, 17);
        check(PickupUpgrades.collectIntoBackpack(player, incoming) == 17 && incoming.isEmpty(), "Persistence seed uses real insert");
        PickupUpgrades.setEnabled(player, false);
        var restored = player();
        restored.restoreFrom(player, false);
        ServerPlayerEvents.COPY_FROM.invoker().copyFromPlayer(player, restored, false);
        check(!PickupUpgrades.enabled(restored), "Disabled preference copied across real respawn/event");
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, server.registryAccess());
        player.saveWithoutId(output);
        NbtIo.writeCompressed(output.buildResult(), path);
        var reloaded = player();
        reloaded.load(TagValueInput.create(ProblemReporter.DISCARDING, server.registryAccess(), output.buildResult()));
        check(!PickupUpgrades.enabled(reloaded) && stored(PickupUpgrades.activeBackpack(reloaded), Items.DIAMOND) == 17, "Player codec preserves preference and backpack content");
        PickupUpgrades.setEnabled(player, true);
        ServerPlayerEvents.COPY_FROM.invoker().copyFromPlayer(player, restored, true);
        check(PickupUpgrades.enabled(restored), "Enabled preference clears prior disabled respawn state");
        record("player-save-load-and-respawn-toggle");
    }

    private static final class PolymerChecks {
        private static void run(PickupQa qa, ServerPlayer player) throws Exception {
            var context = player.connection.getPacketContext();
            for (Item item : List.of(qa.magnet, qa.collector)) {
                var original = new ItemStack(item);
                original.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Pickup Upgrade QA"));
                var before = original.copy();
                var translated = eu.pb4.polymer.core.api.item.PolymerItemUtils.getPolymerItemStack(original, context, qa.server.registryAccess());
                qa.check(translated.is(item == qa.magnet ? Items.IRON_NUGGET : Items.HOPPER), "Unsupported client gets safe upgrade base");
                qa.check(BuiltInRegistries.ITEM.getKey(item).equals(translated.get(DataComponents.ITEM_MODEL)), "Upgrade fixed model available without pack detection");
                qa.check(eu.pb4.polymer.core.api.item.PolymerItemUtils.getRealItemStack(translated, qa.server.registryAccess()).is(item), "Upgrade Polymer round trip retains item identity");
                qa.check(ItemStack.matches(original, before), "Upgrade projection preserves server item/name");
            }
            try (var zip = new java.util.zip.ZipFile("qa-wrench-resource-pack.zip")) {
                for (String name : List.of("pouch_magnet", "pouch_collector")) {
                    String texture = "assets/vpp_mod_suite/textures/item/" + name + ".png";
                    qa.check(zip.getEntry(texture) != null, "Generated pack contains upgrade sprite " + name);
                    try (var input = zip.getInputStream(zip.getEntry(texture))) {
                        var image = javax.imageio.ImageIO.read(input);
                        qa.check(image.getWidth() == 16 && image.getHeight() == 16, "Generated upgrade sprite exactly16x16");
                        int minX = 16, minY = 16, maxX = -1, maxY = -1;
                        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
                            int alpha = image.getRGB(x, y) >>> 24;
                            qa.check(alpha == 0 || alpha == 255, "Sprite pixels are locked opaque/transparent");
                            if (alpha != 0) {
                                minX = Math.min(minX, x);
                                minY = Math.min(minY, y);
                                maxX = Math.max(maxX, x);
                                maxY = Math.max(maxY, y);
                            }
                        }
                        if (name.equals("pouch_collector")) {
                            qa.check(maxX - minX + 1 >= 12 && maxY - minY + 1 >= 12,
                                    "Collector artwork fills at least12x12 of its16x16 item sprite");
                        }
                    }
                    qa.check(zip.getEntry("assets/vpp_mod_suite/items/" + name + ".json") != null, "Pack contains upgrade item definition");
                    qa.check(zip.getEntry("assets/vpp_mod_suite/models/item/" + name + ".json") != null, "Pack contains upgrade model");
                }
            }
            qa.record("fallback-upgrade-models-roundtrip-and-fixed16-pack");
        }
    }

    @Override public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(started -> {
            server = started;
            var result = new JsonObject();
            result.addProperty("passed", false);
            try {
                magnet = BuiltInRegistries.ITEM.getValue(PickupUpgrades.MAGNET_ID);
                collector = BuiltInRegistries.ITEM.getValue(PickupUpgrades.COLLECTOR_ID);
                check(magnet != Items.AIR && collector != Items.AIR, "Both upgrade registry IDs exist");
                ToolPouch.CONFIG.allowUseFromInventory.accept(true);
                ToolPouch.CONFIG.canAttachToLeggings.accept(true);
                TieredBackpacks.CONFIG.canAttachToChestplate.accept(true);
                enableEntityTracking();
                var player = player();
                pouchRules(player);
                recipes();
                mechanics(player);
                largeStacks(player);
                persistence(player);
                int wrenchChecks = qa.DebugWrenchChecks.run(server);
                result.addProperty("wrench_checks", wrenchChecks);
                if (FabricLoader.getInstance().isModLoaded("polymer-core")) PolymerChecks.run(this, player);
                result.addProperty("passed", true);
            } catch (Throwable error) {
                error.printStackTrace();
                result.addProperty("error", error.toString());
            }
            result.addProperty("checks", checks);
            result.addProperty("polymer", FabricLoader.getInstance().isModLoaded("polymer-core"));
            result.add("cases", cases);
            try {
                Files.writeString(Path.of("pickup-result.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n");
            } catch (Exception error) {
                throw new RuntimeException(error);
            }
            server.halt(false);
        });
    }
}
