import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.InputConstants;
import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLVideo;

/** Exercises original Sorted callbacks and its actual slot-render marker path. */
public final class CraftingNetworkRulesClient {
    private static final Gson gson = new GsonBuilder().create();
    private static final JsonArray actions = new JsonArray();
    private static JsonObject originalConfig;
    private static String originalSavedConfig;
    private static boolean originalFileExists;

    public static void tick(Minecraft client, Path control, int ticks) throws Exception {
        // Isolated input QA supplies the same normal SDL focus event as the wheel fixture.
        try (SDL_Event focus = SDL_Event.calloc()) {
            focus.window().type(SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED)
                    .windowID(SDLVideo.SDL_GetWindowID(client.getWindow().handle()));
            client.getWindow().handleEvent(focus);
        }
        String name = client.player.getGameProfile().name();
        Path request = control.resolve(name + "-action.json");
        if (Files.exists(request)) {
            JsonObject action = JsonParser.parseString(Files.readString(request)).getAsJsonObject();
            Files.delete(request);
            try {
                act(client, action);
                actions.add(action);
            } catch (Exception | AssertionError failure) {
                System.err.println("SORTED_RULE_QA_FAILURE " + gson.toJson(action));
                try {
                    restoreConfig();
                    noteAlt(false);
                } catch (Exception restoring) {
                    failure.addSuppressed(restoring);
                }
                throw failure;
            }
        }
        JsonObject row = new JsonObject();
        row.addProperty("ticks", ticks);
        row.addProperty("window_active", client.isWindowActive());
        row.addProperty("simulated_sdl_focus", true);
        row.addProperty("menu_id", client.player.containerMenu.containerId);
        row.addProperty("cursor", client.player.containerMenu.getCarried().getCount());
        row.add("actions", actions.deepCopy());
        if (client.gui.screen() instanceof AbstractContainerScreen<?> screen) {
            row.addProperty("size", screen.getMenu() instanceof BackpackCraftingMenu menu ? menu.getGridWidth() : 0);
            row.addProperty("capacity", storage(screen).size());
            if (screen.getMenu() instanceof BackpackCraftingMenu menu) {
                row.addProperty("grid_count", menu.getInputGridSlots().stream().mapToInt(slot -> slot.getItem().getCount()).sum());
                row.addProperty("result_count", menu.getResultSlot().getItem().getCount());
            }
        }
        Path destination = control.resolve(name + "-client.json");
        Path temporary = destination.resolveSibling(destination.getFileName() + ".tmp");
        Files.writeString(temporary, gson.toJson(row));
        Files.move(temporary, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
    }

    private static void act(Minecraft client, JsonObject action) throws Exception {
        String type = action.get("type").getAsString();
        if (type.equals("restore")) {
            restoreConfig();
            noteAlt(false);
            action.addProperty("original_config_restored", config().equals(originalConfig));
            return;
        }
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) client.gui.screen();
        if (type.equals("setup")) {
            if (originalConfig == null) {
                originalConfig = config();
                originalFileExists = Files.exists(configPath());
                originalSavedConfig = originalFileExists ? Files.readString(configPath()) : null;
            }
            restoreConfig();
            setRule("minecraft:oak_log", 2);
            setRule("minecraft:oak_planks", -1);
            setRule("minecraft:cactus", 3);
            setRule("minecraft:diamond", 0);
            setRule("minecraft:feather", 0);
            if (screen.getMenu() instanceof BackpackCraftingMenu) {
                RecipeBookComponent<?> book = (RecipeBookComponent<?>) field(screen, "recipeBookComponent");
                if (book.isVisible()) book.toggleVisibility();
            }
            screen.resize(640, 400);
            action.addProperty("seeded_log_rule", keep("minecraft:oak_log"));
            action.addProperty("seeded_output_rule", keep("minecraft:oak_planks"));
        } else if (type.equals("edit")) {
            Slot slot = target(screen, action.get("target").getAsString());
            require(slot.hasItem(), "Rule edit target is empty after fixture preparation");
            String kind = action.get("kind").getAsString();
            boolean excluded = action.get("excluded").getAsBoolean();
            String item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(slot.getItem().getItem()).toString();
            if (!excluded) setRule(item, 0);
            JsonObject before = config();
            String savedBefore = Files.readString(configPath());
            for (var child : screen.children()) {
                if (child instanceof net.minecraft.client.gui.components.AbstractWidget widget) {
                    setField(widget, "isHovered", false);
                    widget.setFocused(false);
                }
            }
            setField(screen, "hoveredSlot", slot);
            int left = number(screen, "leftPos"), top = number(screen, "topPos");
            MouseButtonEvent event = new MouseButtonEvent(left + slot.x + 8, top + slot.y + 8,
                    new MouseButtonInfo(kind.equals("right") ? InputConstants.MOUSE_BUTTON_RIGHT : InputConstants.MOUSE_BUTTON_LEFT, InputConstants.MOD_ALT));
            noteAlt(true);
            action.addProperty("window_active_before", client.isWindowActive());
            action.addProperty("event_alt_down", event.hasAltDown());
            action.addProperty("keep_indicator_alt_before", (boolean) invoke("com.sorted.client.KeepIndicator", "altHeld", new Class<?>[0]));
            action.addProperty("hovered_slot_before", ((Slot) field(screen, "hoveredSlot")).index);
            action.addProperty("keep_before", keep(item));
            if (kind.equals("scroll")) {
                action.addProperty("consumed", screen.mouseScrolled(event.x(), event.y(), 0, 1));
            } else {
                action.addProperty("consumed", screen.mouseClicked(event, false));
                screen.mouseReleased(event);
            }
            JsonObject after = config();
            action.addProperty("window_active_after", client.isWindowActive());
            action.addProperty("keep_indicator_alt_after", (boolean) invoke("com.sorted.client.KeepIndicator", "altHeld", new Class<?>[0]));
            action.addProperty("cursor_after", screen.getMenu().getCarried().getCount());
            action.addProperty("slot", slot.index);
            action.addProperty("item", item);
            action.addProperty("keep_after", keep(item));
            action.addProperty("global_config_unchanged", before.equals(after));
            action.addProperty("saved_config_unchanged", savedBefore.equals(Files.readString(configPath())));
            if (excluded) {
                require(before.equals(after), "Excluded crafting slot modified the global Sorted configuration");
                require(savedBefore.equals(Files.readString(configPath())), "Excluded crafting slot wrote changed rules to sorted.json");
            } else {
                int expected = kind.equals("left") ? -1 : 1;
                require(keep(item) == expected, "Storage/player Alt callback no longer edits its item rule");
                JsonObject expectedConfig = before.deepCopy();
                @SuppressWarnings("unchecked") List<String> list = gson.fromJson(before.get("stackBlacklist"), ArrayList.class);
                Object expectedList = invoke("com.sorted.config.KeepRules", "withRule", new Class<?>[] {List.class, String.class, int.class}, list, item, expected);
                expectedConfig.add("stackBlacklist", gson.toJsonTree(expectedList));
                require(expectedConfig.equals(after), "Storage/player Alt edit changed unrelated rules or settings");
                require(JsonParser.parseString(Files.readString(configPath())).getAsJsonObject().equals(after), "Edited item rule was not persisted");
                action.addProperty("only_target_rule_changed", true);
            }
            noteAlt(false);
        } else if (type.equals("markers")) {
            setRule("minecraft:diamond", -1);
            setRule("minecraft:feather", -1);
            noteAlt(true);
            Method extract = AbstractContainerScreen.class.getDeclaredMethod("extractSlot", GuiGraphicsExtractor.class, Slot.class, int.class, int.class);
            extract.setAccessible(true);
            JsonObject counts = new JsonObject();
            List<String> targets = new ArrayList<>(List.of("storage", "player"));
            if (screen.getMenu() instanceof BackpackCraftingMenu) targets.addAll(List.of("grid", "result"));
            for (String name : targets) {
                Slot slot = target(screen, name);
                MarkerGraphics graphics = new MarkerGraphics(client);
                extract.invoke(screen, graphics, slot, slot.x + 8, slot.y + 8);
                counts.addProperty(name, graphics.keepSprites);
                require(name.equals("grid") || name.equals("result") ? graphics.keepSprites == 0 : graphics.keepSprites > 0,
                        "Original Sorted marker renderer has wrong eligibility for " + name + ": " + graphics.keepSprites);
            }
            action.add("marker_sprite_counts", counts);
            action.addProperty("actual_extract_slot_render_path", true);
            // Retain Alt through the next real frame so the following screenshot shows markers.
        } else if (type.equals("screenshot")) {
            net.minecraft.client.Screenshot.grab(client.gameDirectory, action.get("name").getAsString() + ".png",
                    client.gameRenderer.mainRenderTarget(), 1, ignored -> { });
        } else if (type.equals("close")) {
            noteAlt(false);
            client.player.closeContainer();
        } else throw new AssertionError("Unknown rule action " + type);
    }

    private static final class MarkerGraphics extends GuiGraphicsExtractor {
        int keepSprites;
        MarkerGraphics(Minecraft client) { super(client, new GuiRenderState(), 0, 0); }
        @Override public void blitSprite(RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, int tint) {
            if (sprite.toString().equals("sorted:widget/keep")) keepSprites++;
            super.blitSprite(pipeline, sprite, x, y, width, height, tint);
        }
    }

    private static Slot target(AbstractContainerScreen<?> screen, String name) {
        return switch (name) {
            case "grid" -> ((BackpackCraftingMenu) screen.getMenu()).getInputGridSlots().getFirst();
            case "result" -> ((BackpackCraftingMenu) screen.getMenu()).getResultSlot();
            case "storage" -> storage(screen).stream().filter(slot -> slot.getItem().is(Items.DIAMOND)).findFirst().orElseThrow();
            case "player" -> screen.getMenu().slots.stream().filter(slot -> slot.container instanceof Inventory && slot.getItem().is(Items.FEATHER)).findFirst().orElseThrow();
            default -> throw new AssertionError("Unknown rule target " + name);
        };
    }

    private static List<Slot> storage(AbstractContainerScreen<?> screen) {
        return screen.getMenu().slots.stream().filter(slot -> screen.getMenu() instanceof BackpackCraftingMenu menu
                ? menu.isStorageSlot(slot) : !(slot.container instanceof Inventory)).toList();
    }

    private static Path configPath() { return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("sorted.json"); }
    private static JsonObject config() throws Exception { return gson.toJsonTree(invoke("com.sorted.config.ConfigManager", "get", new Class<?>[0])).getAsJsonObject(); }
    private static int keep(String item) throws Exception { return (int) invoke("com.sorted.config.KeepRules", "keep", new Class<?>[] {String.class}, item); }
    private static void setRule(String item, int value) throws Exception { invoke("com.sorted.config.KeepRules", "set", new Class<?>[] {String.class, int.class}, item, value); }
    private static void noteAlt(boolean alt) throws Exception {
        invoke("com.sorted.client.Modifiers", "note", new Class<?>[] {InputWithModifiers.class}, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, alt ? InputConstants.MOD_ALT : 0));
    }
    private static void restoreConfig() throws Exception {
        if (originalConfig == null) return;
        Class<?> type = Class.forName("com.sorted.config.ModConfig");
        var instance = Class.forName("com.sorted.config.ConfigManager").getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, gson.fromJson(originalConfig, type));
        invoke("com.sorted.config.KeepRules", "invalidate", new Class<?>[0]);
        if (originalFileExists) Files.writeString(configPath(), originalSavedConfig);
        else Files.deleteIfExists(configPath());
    }
    private static Object invoke(String type, String method, Class<?>[] parameters, Object... values) throws Exception {
        return Class.forName(type).getMethod(method, parameters).invoke(null, values);
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
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
