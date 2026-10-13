import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thenathe.backpackcrafting.BackpackCrafting;
import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CraftingNetworkServer implements ModInitializer {
    @Override public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            try {
                Path control = Path.of(System.getProperty("crafting.qa.control"));
                for (var player : server.getPlayerList().getPlayers()) {
                    String name = player.getGameProfile().name();
                    Path request = control.resolve(name + "-open.json");
                    if (Files.exists(request)) {
                        var command = JsonParser.parseString(Files.readString(request)).getAsJsonObject();
                        Files.delete(request);
                        player.closeContainer();
                        player.getInventory().clearContent();
                        me.pajic.toolpouch.ToolPouch.CONFIG.allowUseFromInventory.accept(true);
                        var hammer = command.get("size").getAsInt() == 2 ? BackpackCrafting.IRON_HAMMER : BackpackCrafting.DIAMOND_HAMMER;
                        var pouch = new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
                        pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(hammer))));
                        player.getInventory().setItem(12, pouch);
                        var backpack = new ItemStack(command.has("tier") && command.get("tier").getAsString().equals("netherite")
                                ? me.pajic.tiered_backpacks.item.ModItems.NETHERITE_BACKPACK
                                : me.pajic.tiered_backpacks.item.ModItems.LEATHER_BACKPACK);
                        boolean fromBackpack = command.has("backpack") && command.get("backpack").getAsBoolean();
                        backpack.set(DataComponents.CONTAINER, fromBackpack ? ItemContainerContents.fromItems(List.of(new ItemStack(Items.OAK_PLANKS, 12))) : ItemContainerContents.EMPTY);
                        player.getInventory().setItem(13, backpack);
                        if (!fromBackpack) player.getInventory().setItem(9, new ItemStack(Items.OAK_PLANKS, 12));
                        player.awardRecipes(server.getRecipeManager().getRecipes());
                        me.pajic.tiered_backpacks.platform.MultiLoaderUtil.INSTANCE.openBackpackScreen(player, backpack);
                    }
                    var row = new JsonObject();
                    row.addProperty("menu", player.containerMenu.getClass().getName());
                    row.addProperty("crafted_tables", player.getInventory().countItem(Items.CRAFTING_TABLE));
                    row.addProperty("planks", player.getInventory().countItem(Items.OAK_PLANKS));
                    if (player.containerMenu instanceof BackpackCraftingMenu menu) {
                        row.addProperty("size", menu.getGridWidth());
                        row.addProperty("menu_id", menu.containerId);
                        row.addProperty("valid", menu.stillValid(player));
                        row.addProperty("knows_recipe", player.getRecipeBook().contains(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, net.minecraft.resources.Identifier.withDefaultNamespace("crafting_table"))));
                        row.addProperty("capacity", menu.getStorage().getContainerSize());
                        row.addProperty("result_count", menu.getSlot(0).getItem().getCount());
                        row.addProperty("grid_planks", menu.getInputGridSlots().stream().filter(slot -> slot.getItem().is(Items.OAK_PLANKS)).mapToInt(slot -> slot.getItem().getCount()).sum());
                        row.addProperty("stored_planks", menu.getBackpack().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).itemCopies().filter(stack -> stack.is(Items.OAK_PLANKS)).mapToInt(ItemStack::getCount).sum());
                    }
                    Files.writeString(control.resolve(name + "-server.json"), new GsonBuilder().create().toJson(row));
                }
            } catch (Throwable failure) {
                failure.printStackTrace();
                try { Files.writeString(Path.of(System.getProperty("crafting.qa.control"), "failure"), failure.toString()); }
                catch (Exception ignored) {}
            }
        });
    }
}
