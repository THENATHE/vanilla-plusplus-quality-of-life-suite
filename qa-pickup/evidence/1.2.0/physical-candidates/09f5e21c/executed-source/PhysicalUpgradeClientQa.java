package suite.qa;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;

public final class PhysicalUpgradeClientQa implements ClientModInitializer {
    private int stage;
    private int ticks;
    private int screenTicks;
    private boolean done;

    private void clickCollector() throws Exception {
        var mapping = (KeyMapping) Class.forName("com.thenathe.suite.pickup.PickupUpgradeClient").getField("TOGGLE_COLLECTOR").get(null);
        var key = InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_F8);
        mapping.setKey(key);
        KeyMapping.resetMapping();
        KeyMapping.click(key);
    }

    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (done || client.player == null || client.level == null) return;
            Path directory = Path.of(System.getProperty("suite.qa.control"));
            String name = client.player.getGameProfile().name();
            try {
                if (!Files.exists(directory.resolve("client-joined.txt"))) return;
                if (++ticks > 1200) throw new AssertionError("Physical upgrade check timed out at stage " + stage);
                Path serverDirectory = Files.exists(directory.resolve(name + "-upgrade.json")) ? directory : directory.getParent().resolve("server-polymer");
                if (!Files.exists(serverDirectory.resolve(name + "-upgrade.json"))) serverDirectory = directory.getParent().resolve("server-native");
                Path status = serverDirectory.resolve(name + "-upgrade.json");
                if (!Files.exists(status)) return;
                var row = JsonParser.parseString(Files.readString(status)).getAsJsonObject();
                boolean nativeClient = row.get("native_upgrades").getAsBoolean();
                String[] names = {"pouch_magnet", "pouch_collector", "debug_wrench"};
                String[] nativeIds = {"thenathe_mod_suite:pouch_magnet", "thenathe_mod_suite:pouch_collector", "debugwrench:debug_wrench"};
                String[] fallbackIds = {"minecraft:iron_nugget", "minecraft:hopper", "minecraft:iron_nugget"};
                for (int index = 0; index < 3; index++) {
                    var stack = client.player.getInventory().getItem(20 + index);
                    String actual = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                    if (!actual.equals(nativeClient ? nativeIds[index] : fallbackIds[index])) throw new AssertionError("Received new item registry differs " + names[index] + ": " + actual);
                    if (!stack.get(DataComponents.ITEM_MODEL).equals(Identifier.parse(nativeIds[index]))) throw new AssertionError("Received new item fixed model differs " + names[index]);
                }
                if (stage == 0) {
                    if (row.get("stage").getAsInt() < 1) return;
                    if (nativeClient) {
                        client.gui.setScreen(null);
                        clickCollector();
                    }
                    stage = 1;
                    return;
                }
                if (stage == 1) {
                    if (nativeClient && row.get("stage").getAsInt() < 2) return;
                    if (nativeClient) clickCollector();
                    stage = 2;
                    return;
                }
                if (stage == 2) {
                    if (!row.get("passed").getAsBoolean()) return;
                    client.gui.setScreen(new InventoryScreen(client.player));
                    stage = 3;
                    screenTicks = 0;
                    return;
                }
                if (++screenTicks < 30) return;
                Screenshot.grab(client.gameDirectory, "pickup-upgrades-inventory.png", client.gameRenderer.mainRenderTarget(), 1, message -> {});
                var report = new JsonObject();
                report.addProperty("passed", true);
                report.addProperty("native_upgrades", nativeClient);
                report.addProperty("new_item_packet_ids_and_fixed_models", true);
                report.addProperty("real_collected_diamonds", row.get("collected_diamonds").getAsInt());
                report.addProperty("hotkey_off_on_roundtrip", nativeClient);
                report.addProperty("fallback_command_off_on", !nativeClient);
                report.addProperty("inventory_screenshot", "screenshots/pickup-upgrades-inventory.png");
                Files.writeString(directory.resolve("upgrade-client.json"), report.toString());
                done = true;
            } catch (Throwable error) {
                done = true;
                error.printStackTrace();
                try { Files.writeString(directory.resolve("upgrade-client-failure.txt"), error.toString()); }
                catch (Exception ignored) {}
            }
        });
    }
}
