package qa.featurenetwork;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.nio.file.Files;
import java.nio.file.Path;

/** Sends ordinary container clicks without registering suite capabilities. */
public final class FeatureNetworkClientQa implements ClientModInitializer {
    private final JsonArray clicks = new JsonArray();
    private int ticks;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null || ++ticks % 5 != 0) return;
            try {
                Path control = Path.of(System.getProperty("features.qa.control"));
                String name = client.player.getGameProfile().name();
                Path requestPath = control.resolve(name + "-click.json");
                var result = new JsonObject();
                result.addProperty("connected", client.getConnection() != null);
                result.addProperty("ticks", ticks);
                result.add("clicks", clicks.deepCopy());
                if (client.player.containerMenu instanceof GrindstoneMenu menu) {
                    result.addProperty("top_count", menu.getSlot(0).getItem().getCount());
                    result.addProperty("bottom_count", menu.getSlot(1).getItem().getCount());
                    result.addProperty("output_count", menu.getSlot(2).getItem().getCount());
                    result.addProperty("carried_count", menu.getCarried().getCount());
                    result.addProperty("book_slot_accepts_locally", menu.getSlot(1).mayPlace(new ItemStack(Items.BOOK)));
                    result.addProperty("scrap_slot_accepts_locally", menu.getSlot(1).mayPlace(new ItemStack(Items.NETHERITE_SCRAP)));
                    if (Files.exists(requestPath)) {
                        var request = JsonParser.parseString(Files.readString(requestPath)).getAsJsonObject();
                        Files.delete(requestPath);
                        client.gameMode.handleContainerInput(menu.containerId, request.get("slot").getAsInt(), 0,
                                request.get("quick").getAsBoolean() ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP,
                                client.player);
                        clicks.add(request.deepCopy());
                    }
                }
                Files.writeString(control.resolve(name + "-client.json"),
                        new GsonBuilder().setPrettyPrinting().create().toJson(result));
            } catch (Exception error) {
                throw new RuntimeException(error);
            }
        });
    }
}
