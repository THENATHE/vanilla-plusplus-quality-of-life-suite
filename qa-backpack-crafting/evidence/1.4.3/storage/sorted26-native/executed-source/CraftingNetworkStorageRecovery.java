import com.google.gson.JsonObject;
import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Probes Sorted's real recovery selector without changing the server-seeded menu. */
public final class CraftingNetworkStorageRecovery {
    private CraftingNetworkStorageRecovery() { }

    public static void probe(AbstractContainerMenu menu, JsonObject action) throws Exception {
        require(menu instanceof BackpackCraftingMenu, "Recovery probe requires the actual backpack crafting menu");
        BackpackCraftingMenu backpack = (BackpackCraftingMenu) menu;
        String scenario = action.get("scenario").getAsString();
        boolean baseline = action.has("baseline") && action.get("baseline").getAsBoolean();
        int origin = action.get("origin").getAsInt();
        int expected = action.get("expected_target").getAsInt();
        ItemStack carriedBefore = menu.getCarried().copy();
        ItemStack cursor = carriedBefore.copy();
        require(!cursor.isEmpty(), "Authoritative recovery fixture has no carried stack");
        require(origin >= -1 && origin < menu.slots.size(), "Recovery fixture origin is outside the actual menu");

        List<ItemStack> before = new ArrayList<>();
        for (Slot slot : menu.slots) before.add(slot.getItem().copy());
        validateFixture(backpack, cursor, origin, expected, scenario);

        Method selector = Class.forName("com.sorted.sort.SortQueue").getDeclaredMethod(
                "returnTarget", AbstractContainerMenu.class, ItemStack.class, int.class);
        selector.setAccessible(true);
        int target = (int) selector.invoke(null, menu, cursor, origin);
        if (baseline && expected == -1) {
            require(target > 0 && isCraftingSlot(backpack, target), "Published baseline did not expose the crafting-grid recovery path");
        } else {
            require(target == expected, "Sorted recovery chose " + target + " instead of " + expected + " for " + scenario);
            require(target < 0 || !isCraftingSlot(backpack, target), "Sorted recovery chose a crafting input or output");
        }
        require(sameStack(carriedBefore, menu.getCarried()), "Recovery selector changed the actual carried stack");
        require(sameStack(carriedBefore, cursor), "Recovery selector changed its held-stack argument");
        for (int index = 0; index < before.size(); index++) {
            require(sameStack(before.get(index), menu.getSlot(index).getItem()), "Recovery selector changed slot " + index);
        }

        action.addProperty("actual_return_target", target);
        action.addProperty("selected_target", target);
        action.addProperty("target_is_crafting", target >= 0 && isCraftingSlot(backpack, target));
        action.addProperty("cursor_unchanged", true);
        action.addProperty("all_slots_unchanged", true);
        action.addProperty("actual_private_selector_invoked", true);
        action.addProperty("cursor_item", cursor.getItem().toString());
        action.addProperty("cursor_count", cursor.getCount());
        action.addProperty("grid_width", backpack.getGridWidth());
    }

    private static void validateFixture(BackpackCraftingMenu menu, ItemStack cursor,
                                        int origin, int expected, String scenario) {
        if (scenario.equals("origin-grid")) {
            require(isCraftingSlot(menu, origin) && origin != 0, "Fixture origin must be an actual crafting input");
            Slot slot = menu.getSlot(origin);
            require(slot.getItem().isEmpty() && slot.mayPlace(cursor), "Crafting origin must otherwise accept the held stack");
        } else if (scenario.equals("matching-grid") || scenario.equals("empty-grid")) {
            require(expected == -1, "Blocked storage/player recovery must expect no target");
            boolean eligibleGrid = false;
            for (Slot slot : menu.slots) {
                boolean eligible = acceptsRecovery(slot, cursor);
                if (isCraftingSlot(menu, slot.index)) {
                    if (slot.index != 0 && eligible && (scenario.equals("empty-grid")
                            ? slot.getItem().isEmpty() : !slot.getItem().isEmpty())) eligibleGrid = true;
                } else {
                    require(!eligible, "Blocked fixture has an eligible storage/player slot " + slot.index);
                }
            }
            require(eligibleGrid, "Fixture does not expose the intended original crafting-grid recovery path");
        } else if (scenario.equals("storage-partial") || scenario.equals("player-partial")
                || scenario.equals("storage-empty") || scenario.equals("player-empty")) {
            require(expected >= 0 && expected < menu.slots.size(), "Partial recovery fixture target is invalid");
            Slot slot = menu.getSlot(expected);
            boolean player = slot.container instanceof Inventory;
            require(scenario.startsWith("player-") ? player : menu.isStorageSlot(slot), "Recovery target is in the wrong inventory");
            require(scenario.endsWith("empty") ? slot.getItem().isEmpty() : !slot.getItem().isEmpty(),
                    "Recovery target does not have the intended empty/partial state");
            require(acceptsRecovery(slot, cursor), "Recovery fixture target cannot accept the held stack");
        } else {
            throw new AssertionError("Unknown recovery scenario " + scenario);
        }
    }

    private static boolean acceptsRecovery(Slot slot, ItemStack cursor) {
        ItemStack stack = slot.getItem();
        return slot.mayPlace(cursor) && (stack.isEmpty() || ItemStack.isSameItemSameComponents(stack, cursor)
                && stack.getCount() < stack.getMaxStackSize());
    }

    private static boolean isCraftingSlot(BackpackCraftingMenu menu, int index) {
        return index >= 0 && index < menu.getStorageStart();
    }

    private static boolean sameStack(ItemStack first, ItemStack second) {
        return first.getCount() == second.getCount() && ItemStack.isSameItemSameComponents(first, second);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
