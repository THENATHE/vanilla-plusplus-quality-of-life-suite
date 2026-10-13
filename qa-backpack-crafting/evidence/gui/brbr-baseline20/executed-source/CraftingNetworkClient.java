import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Drives actual screen mouse callbacks and normal container packets. */
public final class CraftingNetworkClient implements ClientModInitializer {
    private int ticks;
    private final JsonArray actions = new JsonArray();
    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || ++ticks % 5 != 0) return;
            try {
                Path control = Path.of(System.getProperty("crafting.qa.control"));
                String name = client.player.getGameProfile().name();
                var row = new JsonObject();
                row.addProperty("ticks", ticks);
                row.addProperty("screen", client.gui.screen() == null ? "none" : client.gui.screen().getClass().getName());
                row.add("actions", actions.deepCopy());
                if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("brb")) {
                    Object manager = Class.forName("marsh.town.brb.BetterRecipeBook").getField("instantCraftingManager").get(null);
                    Object pending = field(manager, "pending");
                    row.addProperty("brbr_pending", pending != null);
                    if (pending != null) {
                        var pendingState = new JsonObject();
                        for (String flag : List.of("receivedResult", "awaitingNextBatch", "awaitingPlacement", "lastState")) {
                            pendingState.addProperty(flag, String.valueOf(field(pending, flag)));
                        }
                        row.add("brbr_state", pendingState);
                    }
                }
                if (client.player.containerMenu instanceof net.minecraft.world.inventory.AbstractCraftingMenu menu && client.gui.screen() instanceof net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen) {
                    int capacity = menu instanceof BackpackCraftingMenu backpack ? backpack.getStorage().getContainerSize() : 0;
                    int menuWidth = menu instanceof BackpackCraftingMenu backpack ? backpack.getWidth() : 176;
                    int menuHeight = menu instanceof BackpackCraftingMenu backpack ? backpack.getHeight() : 166;
                    row.addProperty("size", menu.getGridWidth());
                    row.addProperty("menu_id", menu.containerId);
                    row.addProperty("capacity", capacity);
                    int left = ((Number) field(client.gui.screen(), "leftPos")).intValue();
                    int top = ((Number) field(client.gui.screen(), "topPos")).intValue();
                    row.addProperty("screen_width", client.gui.screen().width);
                    row.addProperty("screen_height", client.gui.screen().height);
                    row.addProperty("menu_left", left);
                    row.addProperty("menu_top", top);
                    row.addProperty("menu_width", menuWidth);
                    row.addProperty("menu_height", menuHeight);
                    if (left < 0 || top < 0 || left + menuWidth > client.gui.screen().width
                            || top + menuHeight > client.gui.screen().height) {
                        throw new AssertionError("crafting menu exceeds viewport");
                    }
                    row.addProperty("output", menu.getSlot(0).getItem().getCount());
                    var book = (RecipeBookComponent<?>) field(client.gui.screen(), "recipeBookComponent");
                    row.addProperty("book_visible", book.isVisible());
                    row.addProperty("ghost_slots", ((java.util.Map<?, ?>) field(field(book, "ghostSlots"), "ingredients")).size());
                    if (book.isVisible()) {
                        var origin = RecipeBookComponent.class.getDeclaredMethod("getXOrigin");
                        origin.setAccessible(true);
                        int bookLeft = (Integer) origin.invoke(book);
                        row.addProperty("book_left", bookLeft);
                        @SuppressWarnings("unchecked") var tabs = (List<net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton>) field(book, "tabButtons");
                        for (var tab : tabs) {
                            if (tab.visible && (tab.getX() < 0 || tab.getX() + tab.getWidth() > bookLeft + 8)) {
                                throw new AssertionError("recipe tab overlaps recipe content or leaves viewport");
                            }
                        }
                        if (bookLeft < 0 || bookLeft + RecipeBookComponent.IMAGE_WIDTH > left + 2) {
                            throw new AssertionError("recipe book overlaps backpack at wide viewport");
                        }
                    }
                    Path request = control.resolve(name + "-action.json");
                    if (Files.exists(request)) {
                        var action = JsonParser.parseString(Files.readString(request)).getAsJsonObject();
                        Files.delete(request);
                        String type = action.get("type").getAsString();
                        if (type.equals("book")) {
                            if (!book.isVisible()) {
                                var screen = client.gui.screen();
                                var opener = screen.children().stream()
                                        .filter(child -> child instanceof net.minecraft.client.gui.components.ImageButton)
                                        .map(child -> (net.minecraft.client.gui.components.ImageButton) child)
                                        .findFirst().orElseThrow(() -> new AssertionError("recipe book button missing"));
                                System.out.println("CRAFTING_BOOK_BUTTON x=" + opener.getX() + " y=" + opener.getY());
                                if (!opener.mouseClicked(new MouseButtonEvent(opener.getX() + 8, opener.getY() + 8, new MouseButtonInfo(1, 0)), false)
                                        || !book.isVisible()) {
                                    throw new AssertionError("recipe book opener callback did not open book");
                                }
                            }
                            ((EditBox) field(book, "searchBox")).setValue("crafting table");
                            var updateSearch = RecipeBookComponent.class.getDeclaredMethod("checkSearchStringUpdate");
                            updateSearch.setAccessible(true);
                            updateSearch.invoke(book);
                            book.tick();
                        } else if (type.equals("recipe")) {
                            Object page = field(book, "recipeBookPage");
                            @SuppressWarnings("unchecked") var buttons = (List<RecipeButton>) field(page, "buttons");
                            System.out.println("CRAFTING_RECIPE_BUTTONS " + buttons.stream().filter(candidate -> candidate.visible).map(candidate -> candidate.getDisplayStack().toString()).toList());
                            var button = buttons.stream().filter(candidate -> candidate.visible && candidate.getDisplayStack().is(Items.CRAFTING_TABLE)).findFirst().orElseThrow(() -> new AssertionError("crafting table recipe button missing"));
                            net.minecraft.client.Screenshot.grab(client.gameDirectory, "backpack-crafting-" + menu.getGridWidth() + "-" + capacity + ".png", client.gameRenderer.mainRenderTarget(), 1, message -> {});
                            int mouseButton = action.has("right") && action.get("right").getAsBoolean() ? 3 : 1;
                            boolean clicked = client.gui.screen().mouseClicked(new MouseButtonEvent(button.getX() + 8, button.getY() + 8, new MouseButtonInfo(mouseButton, action.has("maximum") && action.get("maximum").getAsBoolean() ? 1 : 0)), false);
                            if (!clicked) throw new AssertionError("recipe click unhandled");
                            action.addProperty("recipe_id", button.getCurrentRecipe().toString());
                            action.addProperty("recipe_craftable", button.getCollection().isCraftable(button.getCurrentRecipe()));
                            action.addProperty("last_placed", String.valueOf(field(book, "lastPlacedRecipe")));
                            System.out.println("CRAFTING_RECIPE_CLICK " + action);
                        } else if (type.equals("cake")) {
                            ((EditBox) field(book, "searchBox")).setValue("cake");
                            var updateSearch = RecipeBookComponent.class.getDeclaredMethod("checkSearchStringUpdate");
                            updateSearch.setAccessible(true);
                            updateSearch.invoke(book);
                            @SuppressWarnings("unchecked") var buttons = (List<RecipeButton>) field(field(book, "recipeBookPage"), "buttons");
                            var cake = buttons.stream().filter(candidate -> candidate.visible && candidate.getDisplayStack().is(Items.CAKE)).findFirst();
                            if (menu.getGridWidth() == 2) {
                                if (cake.isPresent()) throw new AssertionError("three by three cake visible for two by two grid");
                                action.addProperty("larger_recipe_filtered", true);
                            } else {
                                var button = cake.orElseThrow(() -> new AssertionError("uncraftable cake absent from recipe book"));
                                if (button.getCollection().isCraftable(button.getCurrentRecipe())) throw new AssertionError("cake incorrectly marked craftable");
                                client.gui.screen().mouseClicked(new MouseButtonEvent(button.getX() + 8, button.getY() + 8, new MouseButtonInfo(1, 0)), false);
                                action.addProperty("uncraftable_recipe_clicked", true);
                            }
                        } else if (type.equals("snapshot")) {
                            net.minecraft.client.Screenshot.grab(client.gameDirectory, "backpack-crafting-ghost-" + menu.getGridWidth() + "-" + capacity + ".png", client.gameRenderer.mainRenderTarget(), 1, message -> {});
                        } else if (type.equals("take")) {
                            net.minecraft.client.Screenshot.grab(client.gameDirectory, "backpack-crafting-result-" + menu.getGridWidth() + "-" + capacity + ".png", client.gameRenderer.mainRenderTarget(), 1, message -> {});
                            client.gameMode.handleContainerInput(menu.containerId, 0, 0, ContainerInput.QUICK_MOVE, client.player);
                        } else if (type.equals("close")) {
                            client.player.closeContainer();
                        }
                        actions.add(action);
                    }
                }
                Files.writeString(control.resolve(name + "-client.json"), new GsonBuilder().create().toJson(row));
            } catch (Throwable failure) {
                failure.printStackTrace();
                try { Files.writeString(Path.of(System.getProperty("crafting.qa.control"), "failure"), failure.toString()); }
                catch (Exception ignored) {}
            }
        });
    }
    private static Object field(Object target, String name) throws Exception {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                var field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) {}
        }
        throw new NoSuchFieldException(name);
    }
}
