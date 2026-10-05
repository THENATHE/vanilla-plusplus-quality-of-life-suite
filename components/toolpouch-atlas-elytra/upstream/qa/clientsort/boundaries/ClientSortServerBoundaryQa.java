package clientsortqa;

import me.pajic.toolpouch.item.ModItems;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/** Candidate-only directed transaction checks; real ClientSort packet coverage is separate. */
public final class ClientSortServerBoundaryQa {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static boolean apply(ToolPouchMenu menu, int... mapping) throws Exception {
        try {
            return (boolean) Class.forName("com.thenathe.toolpouchcompat.ClientSortServerPouchSort")
                    .getMethod("apply", AbstractContainerMenu.class, int[].class).invoke(null, menu, mapping);
        } catch (InvocationTargetException error) {
            if (error.getCause() instanceof Exception cause) throw cause;
            throw error;
        }
    }

    private static List<ItemStack> snapshot(ToolPouchMenu menu) {
        return menu.slots.stream().map(slot -> slot.getItem().copy()).toList();
    }

    private static void unchanged(ToolPouchMenu menu, List<ItemStack> before, ItemStack cursor) {
        for (int i = 0; i < before.size(); i++) check(ItemStack.matches(before.get(i), menu.getSlot(i).getItem()),
                "Rejected transaction modified menu/player slot " + i);
        check(ItemStack.matches(cursor, menu.getCarried()), "Rejected transaction modified cursor");
    }

    private static void rejects(ToolPouchMenu menu, int... mapping) throws Exception {
        var before = snapshot(menu);
        var cursor = menu.getCarried().copy();
        boolean rejected = false;
        try { apply(menu, mapping); }
        catch (Exception error) {
            if (!error.getClass().getName().endsWith("PayloadHandlerException$InvalidDataException")) throw error;
            rejected = true;
        }
        check(rejected, "Invalid transaction was accepted");
        unchanged(menu, before, cursor);
    }

    private static void clear(ToolPouchMenu menu, int size) {
        for (int i = 0; i < size; i++) menu.getSlot(i).setByPlayer(ItemStack.EMPTY);
    }

    public static List<String> run(ServerPlayer player) throws Exception {
        var results = new ArrayList<String>();
        var holder = new ItemStack(ModItems.NETHERITE_TOOL_POUCH);
        int size = ToolPouchUtil.getToolPouchSize(holder);
        check(size >= 3, "Boundary fixture requires at least three storage slots");
        var menu = new ToolPouchMenu(199, player.getInventory(), holder);
        menu.setCarried(new ItemStack(Items.EGG, 3));
        var cursor = menu.getCarried().copy();
        var inventory = player.getInventory().getNonEquipmentItems().stream().map(ItemStack::copy).toList();
        menu.getSlot(0).setByPlayer(new ItemStack(Items.CLOCK));
        menu.getSlot(1).setByPlayer(new ItemStack(Items.ARROW, 7));
        menu.getSlot(2).setByPlayer(new ItemStack(Items.COMPASS));
        check(apply(menu, 0, 2, 1, 0, 2, 1), "Valid storage permutation not handled");
        check(menu.getSlot(0).getItem().is(Items.ARROW) && menu.getSlot(0).getItem().getCount() == 7
                && menu.getSlot(1).getItem().is(Items.COMPASS) && menu.getSlot(2).getItem().is(Items.CLOCK),
                "Valid permutation contents differ");
        check(ItemStack.matches(cursor, menu.getCarried()), "Valid permutation changed cursor");
        for (int i = 0; i < inventory.size(); i++) check(ItemStack.matches(inventory.get(i),
                player.getInventory().getNonEquipmentItems().get(i)), "Valid permutation changed player inventory");
        results.add("PASS valid three-way permutation preserves counts, cursor and player inventory");

        rejects(menu, 0, 1, 0, 0);
        rejects(menu, 0, 1, 1, 2);
        rejects(menu, 0, 1, 2);
        rejects(menu, 0, size, size, 0);
        rejects(menu, -1, 0, 0, -1);
        results.add("PASS duplicate source, non-permutation, odd length, mixed inventory and invalid index rejected without mutation");

        clear(menu, size);
        menu.getSlot(0).setByPlayer(new ItemStack(Items.ARROW, 7));
        menu.getSlot(1).setByPlayer(new ItemStack(Items.CLOCK));
        menu.getSlot(2).setByPlayer(new ItemStack(Items.CLOCK));
        rejects(menu, 0, 1, 1, 0);
        results.add("PASS over-quota destination rolls back earlier accepted move and preserves unrelated slot");

        clear(menu, size);
        menu.getSlot(0).setByPlayer(new ItemStack(Items.ARROW, 7));
        menu.getSlot(1).setByPlayer(new ItemStack(Items.COMPASS, 2));
        rejects(menu, 0, 1, 1, 0);
        results.add("PASS configured per-slot stack cap rejection restores complete prior state");

        clear(menu, size);
        menu.getSlot(0).setByPlayer(new ItemStack(Items.ARROW, 7));
        menu.getSlot(1).setByPlayer(new ItemStack(Items.STONE, 3));
        rejects(menu, 0, 1, 1, 0);
        results.add("PASS forbidden-item rejection restores complete prior state");

        clear(menu, size);
        menu.getSlot(0).setByPlayer(new ItemStack(Items.COMPASS));
        menu.getSlot(1).setByPlayer(new ItemStack(Items.ARROW));
        menu.getSlot(1).getItem().setCount(65);
        rejects(menu, 0, 1, 1, 0);
        results.add("PASS intrinsic stack-limit rejection restores even an originally overlarge stack without clamping");

        clear(menu, size);
        menu.getSlot(0).setByPlayer(new ItemStack(Items.FIREWORK_ROCKET, 3));
        check(menu.getSlot(0).mayPlace(new ItemStack(Items.FIREWORK_ROCKET)), "Existing stack cannot refill at quota");
        check(!menu.getSlot(1).mayPlace(new ItemStack(Items.FIREWORK_ROCKET)), "New stack bypasses quota");
        check(!menu.getSlot(0).mayPlace(new ItemStack(Items.STONE)), "Quota correction bypasses allowlist");
        results.add("PASS existing stack can refill at quota while additional stacks and forbidden items remain blocked");
        return results;
    }
}
