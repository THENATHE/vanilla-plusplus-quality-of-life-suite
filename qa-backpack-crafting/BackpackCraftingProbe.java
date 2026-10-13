import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.thenathe.backpackcrafting.BackpackCrafting;
import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import com.thenathe.suite.pickup.PickupUpgrades;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/** Runs real server menu clicks; does not replace recipes or menu behavior. */
public final class BackpackCraftingProbe implements ModInitializer {
    private final JsonArray checks = new JsonArray();
    private ServerPlayer player;
    private ItemStack backpack;
    private ItemStack pouch;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var result = new JsonObject();
            result.addProperty("passed", false);
            result.addProperty("polymer", FabricLoader.getInstance().isModLoaded("polymer-core"));
            try {
                var profile = new GameProfile(UUID.fromString("5b5b3080-978c-4ba2-aef8-56bb17db6499"), "CraftingProbe");
                player = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault()) {
                    @Override public boolean hasDisconnected() { return false; }
                };
                player.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player,
                        CommonListenerCookie.createInitial(profile, false)) {
                    @Override public void send(Packet<?> packet) {}
                };
                player.setGameMode(GameType.SURVIVAL);
                me.pajic.toolpouch.ToolPouch.CONFIG.allowUseFromInventory.accept(true);
                for (String hammer : List.of("iron_hammer", "diamond_hammer")) {
                    check(server.getRecipeManager().byKey(net.minecraft.resources.ResourceKey.create(
                            net.minecraft.core.registries.Registries.RECIPE,
                            Identifier.fromNamespaceAndPath("vpp_backpack_crafting", hammer))).isPresent(),
                            "real resource loader registers " + hammer + " crafting recipe");
                }
                for (int size : new int[]{2, 3}) {
                    for (boolean shift : new boolean[]{false, true}) craftPlanks(size, shift);
                    closingReturnsIngredients(size);
                    recipePlacement(size);
                    collectorSharesLiveStorage(size);
                    removedUpgradeInvalidates(size);
                    savedOverflowPreserved(size);
                    lockedContainers(size);
                    fullInventoryResult(size);
                    fullInventoryRecipeClear(size);
                }
                hammerRecipes();
                cakeRemainders();
                oversizedRecipeRejected();
                if (FabricLoader.getInstance().isModLoaded("clientsort")) {
                    for (String checked : BackpackClientSortChecks.run(player)) checks.add(checked);
                }
                seed(0);
                check(BackpackCrafting.getCraftingSize(player) == 0, "no hammer leaves original backpack route available");
                result.addProperty("passed", true);
            } catch (Throwable failure) {
                failure.printStackTrace();
                result.addProperty("error", failure.toString());
            }
            result.add("checks", checks);
            try {
                Files.writeString(Path.of("backpack-crafting-result.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result));
            } catch (Exception failure) {
                throw new RuntimeException(failure);
            }
            server.halt(false);
        });
    }

    private void seed(int size) {
        player.containerMenu = player.inventoryMenu;
        player.getInventory().clearContent();
        backpack = new ItemStack(me.pajic.tiered_backpacks.item.ModItems.LEATHER_BACKPACK);
        backpack.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        pouch = new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
        ItemStack hammer = size == 0 ? ItemStack.EMPTY : new ItemStack(size == 2
                ? BackpackCrafting.IRON_HAMMER : BackpackCrafting.DIAMOND_HAMMER);
        Item collector = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("vpp_mod_suite", "pouch_collector"));
        pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(hammer, new ItemStack(collector))));
        player.getInventory().setItem(12, pouch);
        player.getInventory().setItem(13, backpack);
        PickupUpgrades.setEnabled(player, true);
        check(BackpackCrafting.getCraftingSize(player) == size, "pouch selects " + size + " crafting size");
    }

    private BackpackCraftingMenu open(int size) {
        var menu = new BackpackCraftingMenu(1, player.getInventory(), backpack, size);
        player.containerMenu = menu;
        check(menu.stillValid(player), "new " + size + " grid is valid");
        return menu;
    }

    private void craftPlanks(int size, boolean shift) {
        seed(size);
        var menu = open(size);
        menu.getSlot(1).set(new ItemStack(Items.OAK_LOG, 3));
        check(menu.getSlot(0).getItem().is(Items.OAK_PLANKS), "real recipe creates plank preview");
        menu.clicked(0, 0, shift ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP, player);
        int planks = player.getInventory().countItem(Items.OAK_PLANKS) + countStored(Items.OAK_PLANKS)
                + (menu.getCarried().is(Items.OAK_PLANKS) ? menu.getCarried().getCount() : 0);
        int logs = menu.getSlot(1).getItem().getCount();
        check(planks > 0 && planks + logs * 4 == 12, "normal/shift crafting conserves ingredients " + size + "/" + shift);
        if (!shift) check(planks == 4 && logs == 2, "ordinary click crafts exactly once");
        menu.removed(player);
    }

    private void closingReturnsIngredients(int size) {
        seed(size);
        var menu = open(size);
        menu.getSlot(1).set(new ItemStack(Items.DIAMOND, 7));
        menu.removed(player);
        check(player.getInventory().countItem(Items.DIAMOND) + countStored(Items.DIAMOND) == 7,
                "closing returns uncrafted ingredients exactly once " + size);
        check(menu.getSlot(1).getItem().isEmpty(), "closed grid emptied " + size);
    }

    private void collectorSharesLiveStorage(int size) {
        seed(size);
        var menu = open(size);
        menu.getSlot(menu.getStorageStart()).set(new ItemStack(Items.EMERALD, 5));
        check(countStored(Items.EMERALD) == 5, "storage slot write reaches backpack component immediately");
        var incoming = new ItemStack(Items.EMERALD, 3);
        check(PickupUpgrades.collectIntoBackpack(player, incoming) == 0 && incoming.getCount() == 3, "collector safely pauses while crafting menu open");
        backpack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.EMERALD, 8))));
        menu.broadcastChanges();
        check(menu.getSlot(menu.getStorageStart()).getItem().getCount() == 8, "menu sees collector insertion");
        menu.clicked(menu.getStorageStart(), 0, ContainerInput.PICKUP, player);
        check(menu.getCarried().getCount() == 8 && countStored(Items.EMERALD) == 0, "slot pickup includes collected items without duplication");
        menu.removed(player);
        check(player.getInventory().countItem(Items.EMERALD) + countStored(Items.EMERALD) == 8, "closing preserves externally updated items");
        player.containerMenu = player.inventoryMenu;
        check(PickupUpgrades.collectIntoBackpack(player, incoming) == 3 && incoming.isEmpty(), "collector resumes after crafting menu closes");
        check(player.getInventory().countItem(Items.EMERALD) + countStored(Items.EMERALD) == 11, "resumed collector conserves incoming items");
    }

    private void removedUpgradeInvalidates(int size) {
        seed(size);
        var menu = open(size);
        pouch.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        check(!menu.stillValid(player), "removed hammer invalidates open crafting menu " + size);
        menu.removed(player);
    }

    private void hammerRecipes() {
        seed(3);
        pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(
                new ItemStack(BackpackCrafting.IRON_HAMMER), new ItemStack(BackpackCrafting.DIAMOND_HAMMER))));
        check(BackpackCrafting.getCraftingSize(player) == 3, "diamond hammer wins when both upgrades are in pouch");
        var menu = open(3);
        for (int index = 1; index <= 3; index++) menu.getSlot(index).set(new ItemStack(Items.IRON_INGOT));
        menu.getSlot(5).set(new ItemStack(Items.STICK));
        menu.getSlot(8).set(new ItemStack(Items.STICK));
        check(menu.getSlot(0).getItem().is(BackpackCrafting.IRON_HAMMER), "three iron and two sticks match iron hammer recipe");
        menu.clicked(0, 0, ContainerInput.QUICK_MOVE, player);
        check(player.getInventory().countItem(BackpackCrafting.IRON_HAMMER) == 1
                && menu.getInputGridSlots().stream().allMatch(slot -> slot.getItem().isEmpty()),
                "iron hammer crafting consumes exact inputs and creates one output");
        menu.removed(player);
        seed(3);
        menu = open(3);
        for (int index : new int[]{2, 4, 6, 8}) menu.getSlot(index).set(new ItemStack(Items.DIAMOND));
        menu.getSlot(5).set(new ItemStack(BackpackCrafting.IRON_HAMMER));
        check(menu.getSlot(0).getItem().is(BackpackCrafting.DIAMOND_HAMMER), "four diamonds and iron hammer match upgrade recipe");
        menu.clicked(0, 0, ContainerInput.QUICK_MOVE, player);
        check(player.getInventory().countItem(BackpackCrafting.DIAMOND_HAMMER) == 1
                && menu.getInputGridSlots().stream().allMatch(slot -> slot.getItem().isEmpty()),
                "diamond upgrade consumes exact inputs and creates one output");
        menu.removed(player);
    }

    private void oversizedRecipeRejected() {
        seed(2);
        var menu = open(2);
        menu.getSlot(1).set(new ItemStack(Items.DIAMOND, 7));
        var level = (net.minecraft.server.level.ServerLevel) player.level();
        var recipe = level.getServer().getRecipeManager().byKey(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.RECIPE, Identifier.withDefaultNamespace("cake"))).orElseThrow();
        menu.handlePlacement(true, true, recipe, level, player.getInventory());
        check(menu.getSlot(1).getItem().is(Items.DIAMOND) && menu.getSlot(1).getItem().getCount() == 7,
                "forged three by three recipe request leaves two by two inputs unchanged");
        menu.removed(player);
    }

    private void fullInventoryRecipeClear(int size) {
        seed(size);
        var menu = open(size);
        for (int slot = 0; slot < 36; slot++) {
            if (slot == 12 || slot == 13) continue;
            var filler = new ItemStack(Items.COBBLESTONE);
            filler.setCount(filler.getMaxStackSize());
            player.getInventory().setItem(slot, filler);
        }
        for (int slot = 0; slot < menu.getStorage().getContainerSize(); slot++) {
            var filler = new ItemStack(Items.COBBLESTONE);
            filler.setCount(filler.getMaxStackSize());
            menu.getStorage().setItem(slot, filler);
        }
        menu.getSlot(1).set(new ItemStack(Items.DIAMOND, 7));
        var level = (net.minecraft.server.level.ServerLevel) player.level();
        var recipe = level.getServer().getRecipeManager().byKey(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.RECIPE, Identifier.withDefaultNamespace("stick"))).orElseThrow();
        menu.handlePlacement(true, true, recipe, level, player.getInventory());
        check(menu.getSlot(1).getItem().is(Items.DIAMOND) && menu.getSlot(1).getItem().getCount() == 7,
                "full inventories cannot eject occupied grid during recipe placement");
        player.getInventory().setItem(9, ItemStack.EMPTY);
        menu.removed(player);
    }

    private void lockedContainers(int size) {
        seed(size);
        var menu = open(size);
        int pouchSlot = menu.getInventoryStart() + 3;
        int backpackSlot = menu.getInventoryStart() + 4;
        check(!menu.getSlot(pouchSlot).mayPickup(player) && !menu.getSlot(backpackSlot).mayPickup(player),
                "active pouch and backpack cannot be lifted while crafting");
        menu.clicked(backpackSlot, 0, ContainerInput.PICKUP, player);
        menu.clicked(pouchSlot, 0, ContainerInput.QUICK_MOVE, player);
        check(menu.getCarried().isEmpty() && player.getInventory().getItem(12) == pouch
                && player.getInventory().getItem(13) == backpack, "ordinary and shift clicks preserve active containers");
        menu.removed(player);
    }

    private void fullInventoryResult(int size) {
        seed(size);
        for (int slot = 0; slot < 36; slot++) {
            if (slot != 12 && slot != 13) {
                var filler = new ItemStack(Items.COBBLESTONE);
                filler.setCount(filler.getMaxStackSize());
                player.getInventory().setItem(slot, filler);
            }
        }
        var menu = open(size);
        menu.getSlot(1).set(new ItemStack(Items.OAK_LOG));
        menu.clicked(0, 0, ContainerInput.QUICK_MOVE, player);
        check(countStored(Items.OAK_PLANKS) == 4 && menu.getSlot(1).getItem().isEmpty(),
                "full player inventory sends crafted output to backpack without losing ingredients");
        menu.removed(player);
    }

    private void savedOverflowPreserved(int size) {
        seed(size);
        var dimensions = me.pajic.tiered_backpacks.util.BackpackUtil.getBackpackDimensions(backpack);
        int capacity = dimensions.rows.get() * dimensions.columns.get();
        var saved = net.minecraft.core.NonNullList.withSize(capacity + 2, ItemStack.EMPTY);
        saved.set(capacity + 1, new ItemStack(Items.DIAMOND, 7));
        backpack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(saved));
        var menu = open(size);
        menu.getSlot(menu.getStorageStart()).set(new ItemStack(Items.EMERALD, 2));
        menu.removed(player);
        check(countStored(Items.DIAMOND) == 7 && countStored(Items.EMERALD) == 2,
                "visible storage edit preserves saved overflow after capacity reduction");
    }

    private void recipePlacement(int size) {
        for (boolean maximum : new boolean[]{false, true}) {
            seed(size);
            backpack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.OAK_PLANKS, 3))));
            player.getInventory().setItem(9, new ItemStack(Items.OAK_PLANKS, 3));
            var menu = open(size);
            var level = (net.minecraft.server.level.ServerLevel) player.level();
            var recipe = level.getServer().getRecipeManager().byKey(net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.RECIPE, Identifier.withDefaultNamespace("stick"))).orElseThrow();
            menu.handlePlacement(maximum, false, recipe, level, player.getInventory());
            check(menu.getSlot(0).getItem().is(Items.STICK), "recipe autofill creates stick result from combined sources");
            int planks = player.getInventory().countItem(Items.OAK_PLANKS) + countStored(Items.OAK_PLANKS);
            for (var slot : menu.getInputGridSlots()) if (slot.getItem().is(Items.OAK_PLANKS)) planks += slot.getItem().getCount();
            check(planks == 6, "recipe autofill conserves combined ingredients " + size + "/" + maximum);
            if (maximum) check(countStored(Items.OAK_PLANKS) == 0, "maximum recipe autofill pulls backpack ingredients");
            menu.clicked(0, 0, ContainerInput.QUICK_MOVE, player);
            int sticks = player.getInventory().countItem(Items.STICK) + countStored(Items.STICK);
            planks = player.getInventory().countItem(Items.OAK_PLANKS) + countStored(Items.OAK_PLANKS);
            for (var slot : menu.getInputGridSlots()) if (slot.getItem().is(Items.OAK_PLANKS)) planks += slot.getItem().getCount();
            check(sticks > 0 && sticks + planks * 2 == 12, "autofill followed by shift craft conserves both inventories");
            menu.removed(player);
        }
    }

    private void cakeRemainders() {
        seed(3);
        var menu = open(3);
        Item[] ingredients = {Items.MILK_BUCKET, Items.MILK_BUCKET, Items.MILK_BUCKET,
                Items.SUGAR, Items.EGG, Items.SUGAR, Items.WHEAT, Items.WHEAT, Items.WHEAT};
        for (int index = 0; index < ingredients.length; index++) menu.getSlot(index + 1).set(new ItemStack(ingredients[index]));
        check(menu.getSlot(0).getItem().is(Items.CAKE), "three by three vanilla cake recipe matches");
        menu.clicked(0, 0, ContainerInput.QUICK_MOVE, player);
        int buckets = player.getInventory().countItem(Items.BUCKET) + countStored(Items.BUCKET);
        for (int index = 1; index <= 9; index++) if (menu.getSlot(index).getItem().is(Items.BUCKET)) buckets += menu.getSlot(index).getItem().getCount();
        check(buckets == 3, "crafting returns all three bucket remainders");
        check(player.getInventory().countItem(Items.CAKE) + countStored(Items.CAKE) == 1, "exactly one cake created");
        menu.removed(player);
        check(player.getInventory().countItem(Items.BUCKET) + countStored(Items.BUCKET) == 3, "closing preserves bucket remainders");
    }

    private int countStored(Item item) {
        return backpack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).itemCopies()
                .filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    private void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
        checks.add(description);
        System.out.println("BACKPACK_CRAFTING_PASS " + description);
    }
}
