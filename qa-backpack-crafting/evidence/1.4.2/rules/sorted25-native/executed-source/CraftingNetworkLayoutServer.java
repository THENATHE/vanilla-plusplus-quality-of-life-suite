import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thenathe.backpackcrafting.BackpackCrafting;
import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.pajic.tiered_backpacks.ui.BackpackMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;

/** Disposable authoritative inventory fixtures for the actual connected screen checks. */
public final class CraftingNetworkLayoutServer {
    private static final Map<String, Integer> prepared = new HashMap<>();

    public static void tick(MinecraftServer server, Path control) throws Exception {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String name = player.getGameProfile().name();
            Path open = control.resolve(name + "-open.json");
            if (Files.exists(open)) {
                JsonObject request = JsonParser.parseString(Files.readString(open)).getAsJsonObject();
                Files.delete(open);
                open(player, request.get("size").getAsInt(), request.get("tier").getAsString());
            }
            Path requestFile = control.resolve(name + "-layout-server-request.json");
            if (Files.exists(requestFile)) {
                JsonObject request = JsonParser.parseString(Files.readString(requestFile)).getAsJsonObject();
                Files.delete(requestFile);
                if (request.get("type").getAsString().equals("cursor")) {
                    clearDrops(player);
                    player.containerMenu.setCarried(new ItemStack(Items.EMERALD, 4));
                    player.containerMenu.broadcastFullState();
                } else if (request.get("type").getAsString().equals("reset")) {
                    seed(player);
                }
                prepared.put(name, request.get("id").getAsInt());
            }
            JsonObject row = snapshot(player);
            row.addProperty("prepared_id", prepared.getOrDefault(name, -1));
            write(control.resolve(name + "-server.json"), row);
        }
    }

    private static void open(ServerPlayer player, int size, String tier) {
        player.closeContainer();
        clearDrops(player);
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        me.pajic.toolpouch.ToolPouch.CONFIG.allowUseFromInventory.accept(true);
        if (size != 0) {
            ItemStack pouch = new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
            pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(size == 2
                    ? BackpackCrafting.IRON_TOOLBOX : BackpackCrafting.NETHERITE_TOOLBOX))));
            player.getInventory().setItem(12, pouch);
        }
        ItemStack bag = new ItemStack(tier.equals("netherite") ? me.pajic.tiered_backpacks.item.ModItems.NETHERITE_BACKPACK
                : me.pajic.tiered_backpacks.item.ModItems.LEATHER_BACKPACK);
        bag.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        player.getInventory().setItem(13, bag);
        player.awardRecipes(player.level().getServer().getRecipeManager().getRecipes());
        me.pajic.tiered_backpacks.platform.MultiLoaderUtil.INSTANCE.openBackpackScreen(player, bag);
        seed(player);
    }

    private static Container storage(AbstractContainerMenu menu) {
        if (menu instanceof BackpackCraftingMenu crafting) return crafting.getStorage();
        return menu.slots.stream().filter(slot -> !(slot.container instanceof net.minecraft.world.entity.player.Inventory))
                .findFirst().orElseThrow().container;
    }

    private static void seed(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        Container bag = storage(menu);
        bag.clearContent();
        bag.setItem(0, new ItemStack(Items.DIAMOND, 3));
        bag.setItem(1, new ItemStack(Items.COBBLESTONE, 9));
        bag.setItem(3, new ItemStack(Items.DIAMOND, 5));
        bag.setItem(4, new ItemStack(Items.EMERALD, 2));
        bag.setItem(5, new ItemStack(Items.OAK_LOG));
        bag.setItem(6, new ItemStack(Items.OAK_PLANKS, 6));
        for (int index = 0; index < 36; index++) {
            if (index != 12 && index != 13) player.getInventory().setItem(index, ItemStack.EMPTY);
        }
        player.getInventory().setItem(9, new ItemStack(Items.DIAMOND, 7));
        player.getInventory().setItem(10, new ItemStack(Items.COBBLESTONE, 11));
        player.getInventory().setItem(11, new ItemStack(Items.OAK_LOG, 7));
        player.getInventory().setItem(14, new ItemStack(Items.FEATHER, 4));
        player.getInventory().setItem(15, new ItemStack(Items.OAK_PLANKS, 10));
        if (menu instanceof BackpackCraftingMenu crafting) {
            for (Slot slot : crafting.getInputGridSlots()) slot.set(ItemStack.EMPTY);
            ItemStack sentinel = new ItemStack(Items.OAK_LOG, 2);
            sentinel.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Crafting grid sentinel"));
            crafting.getInputGridSlots().getFirst().set(sentinel);
            crafting.slotsChanged(crafting.getInputGridSlots().getFirst().container);
        }
        menu.setCarried(ItemStack.EMPTY);
        clearDrops(player);
        menu.broadcastFullState();
    }

    private static JsonObject snapshot(ServerPlayer player) {
        JsonObject row = new JsonObject();
        AbstractContainerMenu menu = player.containerMenu;
        row.addProperty("menu", menu.getClass().getName());
        row.addProperty("menu_id", menu.containerId);
        row.addProperty("cursor", menu.getCarried().getCount());
        row.add("held_bag", counts(player.getInventory().getItem(13)
                .getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).itemCopies().toList()));
        if (menu instanceof BackpackCraftingMenu || menu instanceof BackpackMenu) {
            Container bag = storage(menu);
            row.addProperty("size", menu instanceof BackpackCraftingMenu crafting ? crafting.getGridWidth() : 0);
            row.addProperty("capacity", bag.getContainerSize());
            row.add("storage", counts(menu.slots.stream().filter(slot -> slot.container == bag).map(Slot::getItem).toList()));
            row.add("inventory", counts(player.getInventory().getNonEquipmentItems()));
            row.addProperty("diamond_stacks", menu.slots.stream().filter(slot -> slot.container == bag && slot.getItem().is(Items.DIAMOND)).count());
            if (menu instanceof BackpackCraftingMenu crafting) {
                row.addProperty("grid", crafting.getInputGridSlots().stream().map(slot -> describe(slot.getItem())).toList().toString());
                row.addProperty("result", describe(crafting.getResultSlot().getItem()));
            }
            ItemStack holder = menu instanceof BackpackCraftingMenu crafting ? crafting.getBackpack() : ((BackpackMenu) menu).getBackpack();
            row.add("holder", counts(holder.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).itemCopies().toList()));
        }
        row.addProperty("dropped_emeralds", player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(32)).stream()
                .map(ItemEntity::getItem).filter(stack -> stack.is(Items.EMERALD)).mapToInt(ItemStack::getCount).sum());
        return row;
    }

    private static JsonObject counts(List<ItemStack> items) {
        JsonObject result = new JsonObject();
        for (ItemStack item : items) {
            if (item.isEmpty()) continue;
            String key = BuiltInRegistries.ITEM.getKey(item.getItem()).toString();
            result.addProperty(key, item.getCount() + (result.has(key) ? result.get(key).getAsInt() : 0));
        }
        return result;
    }

    private static String describe(ItemStack stack) {
        return stack.isEmpty() ? "empty" : stack + " components=" + stack.getComponents();
    }

    private static void clearDrops(ServerPlayer player) {
        player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(32)).forEach(ItemEntity::discard);
    }

    private static void write(Path destination, JsonObject data) throws Exception {
        Path temporary = destination.resolveSibling(destination.getFileName() + ".tmp");
        Files.writeString(temporary, new GsonBuilder().create().toJson(data));
        Files.move(temporary, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
    }
}
