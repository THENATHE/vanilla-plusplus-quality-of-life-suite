import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import com.mojang.blaze3d.platform.InputConstants;

/** Real client command parser, widget callbacks and held-cursor clicks in disposable menus. */
public final class CraftingNetworkLayoutClient {
    private static final JsonArray actions = new JsonArray();
    private static Integer originalNetheriteRows;
    private static Integer originalNetheriteColumns;

    public static void tick(Minecraft client, Path control, int ticks) throws Exception {
        String name = client.player.getGameProfile().name();
        Path requestFile = control.resolve(name + "-action.json");
        if (Files.exists(requestFile)) {
            JsonObject action = JsonParser.parseString(Files.readString(requestFile)).getAsJsonObject();
            Files.delete(requestFile);
            act(client, action);
            actions.add(action);
        }
        JsonObject row = new JsonObject();
        row.addProperty("ticks", ticks);
        row.addProperty("menu_id", client.player.containerMenu.containerId);
        row.addProperty("screen", client.gui.screen() == null ? "none" : client.gui.screen().getClass().getName());
        row.addProperty("cursor", client.player.containerMenu.getCarried().getCount());
        row.add("actions", actions.deepCopy());
        row.addProperty("sort_running", !((Boolean) invoke("com.sorted.sort.SortQueue", "isEmpty", new Class<?>[0])));
        if (client.gui.screen() instanceof AbstractContainerScreen<?> screen) {
            row.addProperty("size", screen.getMenu() instanceof BackpackCraftingMenu crafting ? crafting.getGridWidth() : 0);
            row.addProperty("capacity", storageSlots(screen.getMenu()).size());
            row.addProperty("narrow", screen.getMenu() instanceof BackpackCraftingMenu && (boolean) field(screen, "widthTooNarrow"));
            RecipeBookComponent<?> book = book(screen);
            row.addProperty("book_visible", book != null && book.isVisible());
        }
        Path destination = control.resolve(name + "-client.json");
        Path temporary = destination.resolveSibling(destination.getFileName() + ".tmp");
        Files.writeString(temporary, new GsonBuilder().create().toJson(row));
        Files.move(temporary, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
    }

    private static void act(Minecraft client, JsonObject action) throws Exception {
        String type = action.get("type").getAsString();
        if (type.equals("backpack-size")) {
            var dimensions = me.pajic.tiered_backpacks.TieredBackpacks.CONFIG.netheriteSize.get();
            if (originalNetheriteRows == null) {
                originalNetheriteRows = dimensions.rows.get();
                originalNetheriteColumns = dimensions.columns.get();
            }
            boolean restore = action.has("restore") && action.get("restore").getAsBoolean();
            dimensions.rows.accept(restore ? originalNetheriteRows : action.get("rows").getAsInt());
            dimensions.columns.accept(restore ? originalNetheriteColumns : action.get("columns").getAsInt());
            action.addProperty("accepted_rows", dimensions.rows.get());
            action.addProperty("accepted_columns", dimensions.columns.get());
            action.addProperty("restored", restore);
            action.addProperty("original_size_restored", restore
                    && dimensions.rows.get().equals(originalNetheriteRows)
                    && dimensions.columns.get().equals(originalNetheriteColumns));
            return;
        }
        if (type.equals("register")) {
            AbstractContainerMenu menu = client.player.containerMenu;
            String id = (String) invoke("com.sorted.client.StorageMenus", "typeId", new Class<?>[] {AbstractContainerMenu.class}, menu);
            String last = (String) invoke("com.sorted.client.StorageMenus", "lastOpenedId", new Class<?>[0]);
            require(id.equals(last), "Sorted last-opened identifier differs from the actual backpack menu");
            client.player.closeContainer();
            Object dispatcher = invoke("net.fabricmc.fabric.api.client.command.v2.ClientCommands", "getActiveDispatcher", new Class<?>[0]);
            Object source = client.getConnection().getSuggestionsProvider();
            Method execute = dispatcher.getClass().getMethod("execute", String.class, Object.class);
            require((int) execute.invoke(dispatcher, "sorted storage", source) == 1, "Registered /sorted storage command failed");
            require((int) execute.invoke(dispatcher, "sorted storage add", source) == 1, "Registered /sorted storage add command failed");
            require((boolean) invoke("com.sorted.client.StorageMenus", "isStorageId", new Class<?>[] {String.class}, id), "Actual menu ID was not registered after /sorted storage add");
            action.addProperty("registered_menu_id", id);
            return;
        }
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) client.gui.screen();
        RecipeBookComponent<?> book = book(screen);
        if (type.equals("resize")) {
            screen.resize(action.get("width").getAsInt(), action.get("height").getAsInt());
        } else if (type.equals("book-state")) {
            require(book != null, "Crafting menu has no native recipe book");
            if (book.isVisible() != action.get("open").getAsBoolean()) book.toggleVisibility();
            // The native opener also recomputes the container position after toggling.
            screen.resize(screen.width, screen.height);
        } else if (type.equals("button-reference")) {
            inventoryButtonReference(screen, action);
        } else if (type.equals("validate")) {
            validateStorage(screen, action);
            validateButtons(screen, action);
        } else if (type.equals("storage-recovery")) {
            CraftingNetworkStorageRecovery.probe(screen.getMenu(), action);
        } else if (type.equals("sort") || type.equals("stack") || type.equals("storage-operation")) {
            String desired = type.equals("storage-operation") ? action.get("icon").getAsString()
                    : type.equals("sort") ? "sorted:widget/sort_container" : "sorted:widget/quick_stack";
            if (type.equals("storage-operation")) {
                try (org.lwjgl.sdl.SDL_Event focus = org.lwjgl.sdl.SDL_Event.calloc()) {
                    focus.window().type(org.lwjgl.sdl.SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED)
                            .windowID(org.lwjgl.sdl.SDLVideo.SDL_GetWindowID(client.getWindow().handle()));
                    client.getWindow().handleEvent(focus);
                }
                require(screen.getMenu().getCarried().isEmpty(), "Sorted operation fixture starts with a held cursor stack");
                action.add("client_crafting_before", craftingSnapshot(screen));
                action.addProperty("window_active", client.isWindowActive());
                action.addProperty("simulated_sdl_focus", true);
                action.addProperty("cursor_before", screen.getMenu().getCarried().getCount());
            }
            AbstractWidget button = widgets(screen).stream().filter(widget -> {
                try { return field(widget, "icon").toString().equals(desired); }
                catch (Exception failure) { throw new RuntimeException(failure); }
            }).findFirst().orElseThrow(() -> new AssertionError("Sorted button missing: " + desired));
            clearHover(screen);
            setField(button, "isHovered", true);
            boolean shift = type.equals("storage-operation") && action.has("shift") && action.get("shift").getAsBoolean();
            MouseButtonEvent event = shift
                    ? new MouseButtonEvent(button.getX() + 8, button.getY() + 8,
                            new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, InputConstants.MOD_SHIFT))
                    : click(button.getX() + 8, button.getY() + 8);
            require(screen.mouseClicked(event, false), "Real screen ignored Sorted " + type + " click");
            screen.mouseReleased(event);
            action.addProperty("clicked_icon", desired);
            if (type.equals("storage-operation")) {
                action.add("client_crafting_after", craftingSnapshot(screen));
                action.addProperty("cursor_after", screen.getMenu().getCarried().getCount());
                action.addProperty("event_button", event.button());
                action.addProperty("shift", shift);
                action.addProperty("event_has_shift", event.hasShiftDown());
            }
        } else if (type.equals("point")) {
            String pointId = action.get("point").getAsString();
            double[] point = point(screen, pointId);
            clearHover(screen);
            describeBookPoint(book, point, action);
            if (!pointId.equals("book-tab") && !pointId.equals("pins")) verifyNeutralBookPoint(book, point, action);
            if (pointId.equals("book-tab")) {
                // Vanilla's release/outside guard reads the selected tab's render hover state.
                // Supply the hover that an actual frame at these coordinates would produce.
                for (Object object : (List<?>) field(book, "tabButtons")) {
                    AbstractWidget tab = (AbstractWidget) object;
                    if (tab.visible && point[0] >= tab.getX() && point[0] < tab.getRight()
                            && point[1] >= tab.getY() && point[1] < tab.getBottom()) {
                        setField(tab, "isHovered", true);
                        action.addProperty("native_tab_hovered", true);
                    }
                }
            }
            if (pointId.equals("pins")) {
                setField(pinnedTab(book), "isHovered", true);
                action.addProperty("pinned_tab_hovered", true);
            }
            MouseButtonEvent event = click(point[0], point[1]);
            action.addProperty("click_consumed", screen.mouseClicked(event, false));
            screen.mouseReleased(event);
            action.addProperty("x", point[0]);
            action.addProperty("y", point[1]);
            if (pointId.equals("pins")) {
                Class<?> api = Class.forName("marsh.town.brb.interfaces.PinnedRecipeBook");
                action.addProperty("pinned_tab_selected", (boolean) api.getMethod("brb$isPinnedTabSelected").invoke(book));
            }
        } else if (type.equals("close")) {
            client.player.closeContainer();
        } else if (type.equals("screenshot")) {
            net.minecraft.client.Screenshot.grab(client.gameDirectory, action.get("name").getAsString() + ".png",
                    client.gameRenderer.mainRenderTarget(), 1, ignored -> { });
        } else throw new AssertionError("Unknown layout action " + type);
    }

    private static void inventoryButtonReference(AbstractContainerScreen<?> screen, JsonObject action) throws Exception {
        Slot firstInventory = screen.getMenu().slots.stream().filter(slot -> slot.container instanceof net.minecraft.world.entity.player.Inventory
                && slot.getContainerSlot() == 9).findFirst().orElseThrow(() -> new AssertionError("Reference menu has no player inventory row"));
        int originX = number(screen, "leftPos") + firstInventory.x;
        int panelBottom = number(screen, "topPos") + number(screen, "imageHeight");
        JsonObject offsets = new JsonObject();
        for (String icon : List.of("sorted:widget/search", "sorted:widget/sort_inventory", "sorted:widget/quick_stack",
                "sorted:widget/quick_fill", "sorted:widget/quick_empty")) {
            AbstractWidget widget = widgets(screen).stream().filter(button -> {
                try { return field(button, "icon").toString().equals(icon); }
                catch (Exception failure) { throw new RuntimeException(failure); }
            }).findFirst().orElseThrow(() -> new AssertionError("Reference inventory button missing: " + icon));
            require(widget.visible && widget.active, "Reference inventory button is unavailable: " + icon);
            JsonObject position = new JsonObject();
            position.addProperty("x", widget.getX() - originX);
            position.addProperty("y", widget.getY() - panelBottom);
            offsets.add(icon, position);
        }
        action.add("inventory_button_offsets", offsets);
        int storageRight = number(screen, "leftPos") + (screen.getMenu() instanceof BackpackCraftingMenu menu
                ? menu.getStorageWidth() : number(screen, "imageWidth"));
        int panelTop = number(screen, "topPos");
        JsonObject containerOffsets = new JsonObject();
        for (String icon : List.of("sorted:widget/sort_container", "sorted:widget/quick_dump")) {
            AbstractWidget widget = widgets(screen).stream().filter(button -> {
                try { return field(button, "icon").toString().equals(icon); }
                catch (Exception failure) { throw new RuntimeException(failure); }
            }).findFirst().orElseThrow(() -> new AssertionError("Reference container button missing: " + icon));
            require(widget.visible && widget.active, "Reference container button is unavailable: " + icon);
            JsonObject position = new JsonObject();
            position.addProperty("x", widget.getX() - storageRight);
            position.addProperty("y", widget.getY() - panelTop);
            containerOffsets.add(icon, position);
        }
        action.add("container_button_offsets", containerOffsets);
        action.addProperty("storage_right", storageRight);
        action.addProperty("panel_top", panelTop);
        action.addProperty("inventory_origin_x", originX);
        action.addProperty("panel_bottom", panelBottom);
        action.addProperty("menu_class", screen.getMenu().getClass().getName());
        action.addProperty("screen_class", screen.getClass().getName());
    }

    private static JsonObject craftingSnapshot(AbstractContainerScreen<?> screen) {
        JsonObject snapshot = new JsonObject();
        if (screen.getMenu() instanceof BackpackCraftingMenu menu) {
            JsonArray grid = new JsonArray();
            for (Slot slot : menu.getInputGridSlots()) grid.add(describeStack(slot.getItem()));
            snapshot.add("grid", grid);
            snapshot.addProperty("result", describeStack(menu.getResultSlot().getItem()));
        }
        return snapshot;
    }

    private static String describeStack(net.minecraft.world.item.ItemStack stack) {
        return stack.isEmpty() ? "empty" : stack + " components=" + stack.getComponents();
    }

    private static void validateStorage(AbstractContainerScreen<?> screen, JsonObject action) throws Exception {
        AbstractContainerMenu menu = screen.getMenu();
        List<Slot> actual = storageSlots(menu);
        for (Slot slot : menu.slots) {
            boolean storage = (boolean) invoke("com.sorted.client.StorageMenus", "isStorageSlot",
                    new Class<?>[] {AbstractContainerMenu.class, int.class}, menu, slot.index);
            require(storage == actual.contains(slot), "Sorted classified non-storage slot " + slot.index + " as backpack storage");
        }
        Class<?> viewType = Class.forName("com.sorted.client.MenuStorageView");
        var constructor = viewType.getDeclaredConstructor(AbstractContainerMenu.class);
        constructor.setAccessible(true);
        Container filtered = (Container) constructor.newInstance(menu);
        require(filtered.getContainerSize() == actual.size(), "Sorted filtering/cache view includes crafting inputs or result");
        for (int index = 0; index < actual.size(); index++) {
            require(net.minecraft.world.item.ItemStack.matches(filtered.getItem(index), actual.get(index).getItem()),
                    "Sorted filtering/cache view lost an actual storage stack");
        }
        action.addProperty("storage_slots_verified", actual.size());
        action.addProperty("filtered_grid_and_result_excluded", true);
    }

    private static void validateButtons(AbstractContainerScreen<?> screen, JsonObject action) throws Exception {
        int left = number(screen, "leftPos"), top = number(screen, "topPos");
        int storageWidth = screen.getMenu() instanceof BackpackCraftingMenu crafting ? crafting.getStorageWidth() : number(screen, "imageWidth");
        int height = number(screen, "imageHeight");
        int inventoryLeft = 0;
        int inventoryRight = storageWidth;
        List<AbstractWidget> controls = widgets(screen);
        require(controls.size() == 7, "Expected seven normal Sorted storage/inventory buttons, found " + controls.size());
        for (AbstractWidget widget : controls) {
            String icon = field(widget, "icon").toString();
            int x = switch (icon) {
                case "sorted:widget/sort_container" -> storageWidth - 18;
                case "sorted:widget/quick_dump" -> storageWidth - 36;
                case "sorted:widget/sort_inventory" -> inventoryRight - 18;
                case "sorted:widget/quick_stack" -> inventoryRight - 36;
                case "sorted:widget/quick_fill" -> inventoryRight - 54;
                case "sorted:widget/quick_empty" -> inventoryRight - 72;
                case "sorted:widget/search" -> inventoryLeft;
                default -> throw new AssertionError("Unexpected Sorted icon " + icon);
            };
            boolean container = icon.equals("sorted:widget/sort_container") || icon.equals("sorted:widget/quick_dump");
            int y = screen.getMenu() instanceof BackpackCraftingMenu crafting
                    ? (container ? -20 : height + 1) : (container ? -20 : height + 1);
            require(widget.visible && widget.getX() == left + x && widget.getY() == top + y,
                    "Sorted button follows the wrong panel: " + icon + " at " + widget.getX() + "," + widget.getY());
            require(widget.getX() >= 0 && widget.getY() >= 0 && widget.getRight() <= screen.width && widget.getBottom() <= screen.height,
                    "Sorted button lies outside the viewport: " + icon);
            for (Slot slot : screen.getMenu().slots) {
                require(!overlaps(widget, left + slot.x, top + slot.y, 16, 16),
                        "Sorted control overlaps an interactive item slot " + slot.index + ": " + icon);
            }
            if (screen.getMenu() instanceof BackpackCraftingMenu) {
                int labelX = left + number(screen, "inventoryLabelX");
                int labelY = top + number(screen, "inventoryLabelY");
                int labelWidth = Minecraft.getInstance().font.width(net.minecraft.network.chat.Component.translatable("container.inventory"));
                require(!overlaps(widget, labelX, labelY, labelWidth, 9),
                        "Sorted control obscures the player inventory label: " + icon);
            }
        }
        action.addProperty("sorted_buttons_verified", controls.size());
        action.addProperty("storage_panel_width", storageWidth);
        action.addProperty("book_visible", book(screen) != null && book(screen).isVisible());
        AbstractWidget pins = pinnedTab(book(screen));
        action.addProperty("pinned_tab_visible", pins != null && pins.visible);
        action.addProperty("narrow", screen.getMenu() instanceof BackpackCraftingMenu && (boolean) field(screen, "widthTooNarrow"));
    }

    private static boolean overlaps(AbstractWidget widget, int x, int y, int width, int height) {
        return widget.getX() < x + width && widget.getRight() > x
                && widget.getY() < y + height && widget.getBottom() > y;
    }

    private static void describeBookPoint(RecipeBookComponent<?> book, double[] point, JsonObject action) throws Exception {
        boolean inside = false;
        if (book != null && book.isVisible()) {
            Method originX = RecipeBookComponent.class.getDeclaredMethod("getXOrigin");
            Method originY = RecipeBookComponent.class.getDeclaredMethod("getYOrigin");
            originX.setAccessible(true);
            originY.setAccessible(true);
            int x = (int) originX.invoke(book), y = (int) originY.invoke(book);
            JsonObject bounds = new JsonObject();
            bounds.addProperty("x", x);
            bounds.addProperty("y", y);
            bounds.addProperty("width", 147);
            bounds.addProperty("height", 166);
            action.add("book_bounds", bounds);
            inside = point[0] >= x && point[0] < x + 147 && point[1] >= y && point[1] < y + 166;
            for (Object object : (List<?>) field(book, "tabButtons")) {
                AbstractWidget tab = (AbstractWidget) object;
                if (tab.visible && point[0] >= tab.getX() && point[0] < tab.getRight()
                        && point[1] >= tab.getY() && point[1] < tab.getBottom()) inside = true;
            }
        }
        AbstractWidget pins = pinnedTab(book);
        if (pins != null && pins.visible && point[0] >= pins.getX() && point[0] < pins.getRight()
                && point[1] >= pins.getY() && point[1] < pins.getBottom()) inside = true;
        action.addProperty("book_intersects_point", inside);
    }

    private static void verifyNeutralBookPoint(RecipeBookComponent<?> book, double[] point, JsonObject action) throws Exception {
        if (book == null || !book.isVisible()) return;
        List<AbstractWidget> controls = new ArrayList<>();
        Object page = field(book, "recipeBookPage");
        for (Object button : (List<?>) field(page, "buttons")) controls.add((AbstractWidget) button);
        for (String name : List.of("forwardButton", "backButton")) {
            Object control = field(page, name);
            if (control != null) controls.add((AbstractWidget) control);
        }
        for (String name : List.of("filterButton", "searchBox")) {
            Object control = field(book, name);
            if (control != null) controls.add((AbstractWidget) control);
        }
        for (AbstractWidget control : controls) {
            require(!control.isActive() || point[0] < control.getX() || point[0] >= control.getRight()
                            || point[1] < control.getY() || point[1] >= control.getBottom(),
                    "Cursor-protection point intersects an active recipe/filter/search control at " + point[0] + "," + point[1]);
        }
        action.addProperty("neutral_book_point_verified", true);
    }

    private static double[] point(AbstractContainerScreen<?> screen, String id) throws Exception {
        int left = number(screen, "leftPos"), top = number(screen, "topPos");
        if (id.equals("storage")) return new double[] {left + 4, top + 8};
        if (id.equals("outside")) return new double[] {left - 12, top - 6};
        RecipeBookComponent<?> book = book(screen);
        if (id.equals("book")) {
            Method originX = RecipeBookComponent.class.getDeclaredMethod("getXOrigin");
            Method originY = RecipeBookComponent.class.getDeclaredMethod("getYOrigin");
            originX.setAccessible(true); originY.setAccessible(true);
            return new double[] {(int) originX.invoke(book) + 3, (int) originY.invoke(book) + 45};
        }
        if (id.equals("pins")) {
            AbstractWidget pins = pinnedTab(book);
            require(pins != null && pins.visible, "Visible crafting book has no BRBR pinned tab");
            return new double[] {pins.getX() + 10, pins.getY() + 10};
        }
        if (id.equals("book-tab")) {
            for (Object object : (List<?>) field(book, "tabButtons")) {
                AbstractWidget tab = (AbstractWidget) object;
                if (tab.visible) return new double[] {tab.getX() + 10, tab.getY() + 10};
            }
            throw new AssertionError("Visible book has no native category tab");
        }
        BackpackCraftingMenu menu = (BackpackCraftingMenu) screen.getMenu();
        int panelLeft = left + menu.getStorageWidth() + 8, panelTop = top + menu.getCraftingPanelY();
        if (id.equals("gap")) {
            double x = panelLeft - 4, y = panelTop + 8;
            if (book != null && book.isVisible()) {
                Method originX = RecipeBookComponent.class.getDeclaredMethod("getXOrigin");
                Method originY = RecipeBookComponent.class.getDeclaredMethod("getYOrigin");
                originX.setAccessible(true);
                originY.setAccessible(true);
                int bookX = (int) originX.invoke(book), bookY = (int) originY.invoke(book);
                if (x >= bookX && x < bookX + 147 && y >= bookY && y < bookY + 166) y = bookY + 3;
            }
            return new double[] {x, y};
        }
        return switch (id) {
            case "crafting" -> new double[] {panelLeft + menu.getCraftingPanelWidth() - 4, panelTop + 8};
            case "above" -> new double[] {panelLeft + menu.getCraftingPanelWidth() - 8, panelTop - 4};
            case "below" -> new double[] {panelLeft + menu.getCraftingPanelWidth() - 8, panelTop + menu.getCraftingPanelHeight() + 4};
            default -> throw new AssertionError("Unknown cursor point " + id);
        };
    }

    private static AbstractWidget pinnedTab(RecipeBookComponent<?> book) throws Exception {
        if (book == null || !net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("brb")) return null;
        Class<?> api = Class.forName("marsh.town.brb.interfaces.PinnedRecipeBook");
        return api.isInstance(book) ? (AbstractWidget) api.getMethod("brb$getPinnedTab").invoke(book) : null;
    }

    private static List<Slot> storageSlots(AbstractContainerMenu menu) {
        return menu.slots.stream().filter(slot -> menu instanceof BackpackCraftingMenu crafting
                ? crafting.isStorageSlot(slot) : !(slot.container instanceof net.minecraft.world.entity.player.Inventory)).toList();
    }

    private static List<AbstractWidget> widgets(AbstractContainerScreen<?> screen) {
        List<AbstractWidget> result = new ArrayList<>();
        for (var child : screen.children()) {
            if (child.getClass().getName().equals("com.sorted.client.gui.SortButtonWidget")) result.add((AbstractWidget) child);
        }
        return result;
    }

    private static RecipeBookComponent<?> book(AbstractContainerScreen<?> screen) throws Exception {
        return screen.getMenu() instanceof BackpackCraftingMenu ? (RecipeBookComponent<?>) field(screen, "recipeBookComponent") : null;
    }

    private static void clearHover(AbstractContainerScreen<?> screen) throws Exception {
        for (var child : screen.children()) if (child instanceof AbstractWidget widget) {
            setField(widget, "isHovered", false); widget.setFocused(false);
        }
        RecipeBookComponent<?> book = book(screen);
        if (book != null) for (Object tab : (List<?>) field(book, "tabButtons")) {
            setField(tab, "isHovered", false); ((AbstractWidget) tab).setFocused(false);
        }
        AbstractWidget pins = pinnedTab(book);
        if (pins != null) {
            setField(pins, "isHovered", false);
            pins.setFocused(false);
        }
    }

    private static MouseButtonEvent click(double x, double y) {
        return new MouseButtonEvent(x, y, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0));
    }

    private static Object invoke(String type, String method, Class<?>[] parameters, Object... args) throws Exception {
        return Class.forName(type).getMethod(method, parameters).invoke(null, args);
    }

    private static Object field(Object object, String name) throws Exception {
        for (Class<?> type = object.getClass(); type != null; type = type.getSuperclass()) {
            try { var field = type.getDeclaredField(name); field.setAccessible(true); return field.get(object); }
            catch (NoSuchFieldException ignored) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static void setField(Object object, String name, Object value) throws Exception {
        for (Class<?> type = object.getClass(); type != null; type = type.getSuperclass()) {
            try { var field = type.getDeclaredField(name); field.setAccessible(true); field.set(object, value); return; }
            catch (NoSuchFieldException ignored) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static int number(Object object, String name) throws Exception { return ((Number) field(object, name)).intValue(); }
    private static void require(boolean passed, String message) { if (!passed) throw new AssertionError(message); }
}
