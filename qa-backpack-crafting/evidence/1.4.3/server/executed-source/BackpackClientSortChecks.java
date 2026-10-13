import com.thenathe.backpackcrafting.BackpackCrafting;
import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import dev.terminalmc.clientsort.exception.PayloadHandlerException.InvalidDataException;
import dev.terminalmc.clientsort.network.handler.SortHandler;
import dev.terminalmc.clientsort.network.handler.validate.SchemaValidator;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

/** Real ClientSort server schema and permutation execution, without claiming client button coverage. */
public final class BackpackClientSortChecks {
    public static List<String> run(ServerPlayer player) throws Exception {
        var checks = new ArrayList<String>();
        var sort = SortHandler.class.getDeclaredMethod("sort", net.minecraft.server.MinecraftServer.class,
                AbstractContainerMenu.class, int[].class);
        sort.setAccessible(true);
        for (int size : new int[]{2, 3}) {
            player.getInventory().clearContent();
            ItemStack backpack = new ItemStack(me.pajic.tiered_backpacks.item.ModItems.LEATHER_BACKPACK);
            ItemStack pouch = new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
            pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(
                    new ItemStack(size == 2 ? BackpackCrafting.IRON_TOOLBOX : BackpackCrafting.NETHERITE_TOOLBOX))));
            player.getInventory().setItem(12, pouch);
            player.getInventory().setItem(13, backpack);
            var menu = new BackpackCraftingMenu(201 + size, player.getInventory(), backpack, size);
            player.containerMenu = menu;
            menu.getSlot(1).setByPlayer(new ItemStack(Items.OAK_LOG, 3));
            ItemStack output = menu.getSlot(0).getItem().copy();
            int first = menu.getStorageStart();
            menu.getSlot(first).setByPlayer(new ItemStack(Items.STONE, 7));
            menu.getSlot(first + 1).setByPlayer(new ItemStack(Items.DIRT, 11));
            int[] swap = {first, first + 1, first + 1, first};
            SchemaValidator.validateSlotArray(player, menu, new int[]{first, first + 1});
            SchemaValidator.validateSlotMapping(player, menu, swap);
            sort.invoke(null, player.level().getServer(), menu, swap);
            check(menu.getSlot(first).getItem().is(Items.DIRT) && menu.getSlot(first).getItem().getCount() == 11
                    && menu.getSlot(first + 1).getItem().is(Items.STONE)
                    && menu.getSlot(first + 1).getItem().getCount() == 7, "ClientSort storage permutation differs");
            check(ItemStack.matches(menu.getSlot(0).getItem(), output)
                    && menu.getSlot(1).getItem().is(Items.OAK_LOG) && menu.getSlot(1).getItem().getCount() == 3,
                    "Sorting changed crafting input or result");
            check(player.getInventory().getItem(13) == backpack && player.getInventory().getItem(12) == pouch,
                    "Sorting replaced a protected holder");
            var saved = backpack.get(DataComponents.CONTAINER).itemCopies().toList();
            check(saved.get(0).is(Items.DIRT) && saved.get(0).getCount() == 11
                    && saved.get(1).is(Items.STONE) && saved.get(1).getCount() == 7,
                    "Sorted storage did not persist");
            checks.add("PASS ClientSort " + size + "x" + size + " storage permutation preserves counts, grid, result, holder identity and persistence");
            for (int forbidden = 0; forbidden < first; forbidden++) {
                int other = forbidden == 0 ? first : 1 + forbidden % (first - 1);
                rejects(player, menu, forbidden, other);
            }
            for (var slot : menu.slots) {
                if (slot.container == player.getInventory() && (slot.getContainerSlot() == 12 || slot.getContainerSlot() == 13)) {
                    rejects(player, menu, slot.index, menu.getInventoryStart());
                }
            }
            checks.add("PASS ClientSort " + size + "x" + size + " rejects every input/result and both locked holder slots in array and mapping schemas without mutation");
            int inventory = menu.getInventoryStart();
            menu.getSlot(inventory).setByPlayer(new ItemStack(Items.COBBLESTONE, 5));
            menu.getSlot(inventory + 1).setByPlayer(new ItemStack(Items.GRAVEL, 9));
            int[] playerSwap = {inventory, inventory + 1, inventory + 1, inventory};
            SchemaValidator.validateSlotMapping(player, menu, playerSwap);
            sort.invoke(null, player.level().getServer(), menu, playerSwap);
            check(menu.getSlot(inventory).getItem().is(Items.GRAVEL)
                    && menu.getSlot(inventory + 1).getItem().is(Items.COBBLESTONE), "Player inventory sorting blocked");
            checks.add("PASS ClientSort " + size + "x" + size + " ordinary player storage remains sortable");
        }
        return checks;
    }

    private static void rejects(ServerPlayer player, BackpackCraftingMenu menu, int forbidden, int other) throws Exception {
        var before = menu.slots.stream().map(slot -> slot.getItem().copy()).toList();
        boolean arrayRejected = false;
        boolean mappingRejected = false;
        try { SchemaValidator.validateSlotArray(player, menu, new int[]{forbidden}); }
        catch (InvalidDataException expected) { arrayRejected = true; }
        try { SchemaValidator.validateSlotMapping(player, menu, new int[]{forbidden, other, other, forbidden}); }
        catch (InvalidDataException expected) { mappingRejected = true; }
        check(arrayRejected && mappingRejected, "Protected slot accepted by ClientSort schema: " + forbidden);
        for (int i = 0; i < before.size(); i++) {
            check(ItemStack.matches(before.get(i), menu.getSlot(i).getItem()), "Rejected schema modified slot " + i);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
