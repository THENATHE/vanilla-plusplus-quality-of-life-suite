package com.thenathe.hotkeywheel.qa;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.thenathe.hotkeywheel.HotkeyWheelClient;
import com.thenathe.hotkeywheel.RadialGeometry;
import com.thenathe.hotkeywheel.ToolPouchToggles;
import com.thenathe.hotkeywheel.WheelConfig;
import com.thenathe.hotkeywheel.WheelScreen;
import com.thenathe.suite.SuiteItems;
import com.thenathe.toolpouchcompat.AtlasBridge;
import com.thenathe.toolpouchcompat.PouchUpgradeAccess;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.component.ModDataComponents;
import me.pajic.toolpouch.item.ModItems;
import me.pajic.toolpouch.keybind.SlotToggleKeys;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Real key presses on Tool Pouch slot-toggle binds turn each suite-driven pouch item off and on
 * (Elytra, atlas, magnet, collector), and a key bound to the removed suite Elytra control is
 * carried over to the Elytra's slot. Hotkey Wheel's pouch Elytra toggle, and a wheel slot saved with the
 * removed control, switch the same slot.
 */
public final class SlotToggleClientTest implements FabricClientGameTest {
    private final List<String> checks = new ArrayList<>();
    private final List<String> failures = new ArrayList<>();
    private final List<String> diagnostics = new ArrayList<>();

    private void check(boolean condition, String description) {
        (condition ? checks : failures).add(description);
    }

    private record Case(int slot, String name, int key, Predicate<ServerPlayer> active, Predicate<Minecraft> clientActive) {}

    @Override
    public void runTest(ClientGameTestContext context) {
        var report = new LinkedHashMap<String, Object>();
        try {
            check(context.computeOnClient(client -> KeyMapping.get("key.toolpouch.toggle_elytra") == null),
                    "Removed suite Elytra control is not registered");
            // With no such key mapping, Hotkey Wheel treats a slot saved with the old control as its pouch Elytra toggle.
            check(context.computeOnClient(client -> HotkeyWheelClient.findControl("key.toolpouch.toggle_elytra") == null),
                    "Hotkey Wheel lookup of the removed control returns no key mapping");
            try (var world = context.worldBuilder().create()) {
                world.getConnection().waitForChunksDownload();
                context.waitTicks(20);
                var contents = List.of(new ItemStack(Items.ELYTRA), new ItemStack(me.pajic.mapstitch.item.ModItems.ATLAS),
                        new ItemStack(SuiteItems.POUCH_MAGNET), new ItemStack(SuiteItems.POUCH_COLLECTOR), new ItemStack(Items.CLOCK));
                onServer(context, player -> {
                    ToolPouch.CONFIG.allowUseFromInventory.accept(true);
                    player.getInventory().clearContent();
                    var pouch = new ItemStack(ModItems.TOOL_POUCH);
                    pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
                    player.getInventory().setItem(4, pouch);
                    player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
                    player.inventoryMenu.broadcastChanges();
                    return true;
                });
                context.waitTicks(40);
                // The key bound to the removed control (seeded into options.txt) now toggles the Elytra slot.
                String legacy = context.computeOnClient(client -> {
                    var key = SlotToggleKeys.getKey(0);
                    return key == null ? "none" : key.getName();
                });
                report.put("legacy_elytra_key_slot_0", legacy);
                check(legacy.equals("key.keyboard.j"), "Legacy Elytra key carried over to the pouch Elytra slot");
                var cases = List.of(
                        new Case(0, "elytra", InputConstants.KEY_J,
                                player -> ToolPouchUtil.getElytraFromToolPouch(player, false) != null,
                                client -> ToolPouchUtil.getElytraFromToolPouch(client.player, false) != null),
                        new Case(1, "atlas", InputConstants.KEY_NUMPAD7,
                                player -> AtlasBridge.atlases(player).size() == 1,
                                client -> AtlasBridge.atlases(client.player).size() == 1),
                        new Case(2, "magnet", InputConstants.KEY_NUMPAD8,
                                player -> PouchUpgradeAccess.contains(player, SuiteItems.POUCH_MAGNET),
                                client -> PouchUpgradeAccess.contains(client.player, SuiteItems.POUCH_MAGNET)),
                        new Case(3, "collector", InputConstants.KEY_NUMPAD9,
                                player -> PouchUpgradeAccess.contains(player, SuiteItems.POUCH_COLLECTOR),
                                client -> PouchUpgradeAccess.contains(client.player, SuiteItems.POUCH_COLLECTOR)));
                for (var item : cases) {
                    if (item.slot() != 0) {
                        context.runOnClient(client -> SlotToggleKeys.bind(item.slot(), InputConstants.Type.KEYBOARD.getOrCreate(item.key())));
                    }
                    check(onServer(context, item.active()::test), item.name() + " works while its slot is enabled");
                    for (boolean disable : new boolean[] {true, false}) {
                        String state = context.computeOnClient(client -> {
                            client.gui.setScreen(null);
                            var key = InputConstants.Type.KEYBOARD.getOrCreate(item.key());
                            return "screen=" + client.gui.screen() + " slots=" + SlotToggleKeys.getSlots(key, ToolPouchUtil.getToolPouch(client.player))
                                    + " pouch=" + ToolPouchUtil.getToolPouch(client.player).getItem();
                        });
                        diagnostics.add(item.name() + " before press: " + state);
                        context.getInput().pressKey(item.key());
                        context.waitTicks(6);
                        boolean disabledOnServer = onServer(context, player -> ToolPouchUtil.isSlotDisabled(ToolPouchUtil.getToolPouch(player), item.slot()));
                        check(disabledOnServer == disable, item.name() + (disable ? " slot disabled on the server" : " slot enabled again on the server"));
                        check(onServer(context, item.active()::test) != disable, item.name() + (disable ? " stops working" : " works again"));
                        check(context.computeOnClient(item.clientActive()::test) != disable,
                                item.name() + (disable ? " stops working on the client" : " works again on the client"));
                        String message = context.computeOnClient(SlotToggleClientTest::overlayMessage);
                        check(message.endsWith(disable ? " disabled" : " enabled"), item.name() + " shows Tool Pouch's own feedback: " + message);
                    }
                }
                checkWheelElytraToggles(context);
                // Disabled state is stored on the pouch item, so leave one slot off for the restart phase of other harnesses.
                report.put("final_disabled_slots", onServer(context, player ->
                        ToolPouchUtil.getToolPouch(player).getOrDefault(ModDataComponents.DISABLED_SLOTS, List.<Integer>of()).toString()));
            }
        } catch (Throwable error) {
            failures.add("Unexpected failure: " + error);
        }
        report.put("checks", checks.size());
        report.put("check_names", checks);
        report.put("failures", failures);
        report.put("diagnostics", diagnostics);
        report.put("passed", failures.isEmpty());
        try {
            Files.writeString(Path.of(System.getProperty("wheel.qa.root"), "slot-toggle-results.json"),
                    new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n");
        } catch (Exception error) {
            throw new RuntimeException(error);
        }
    }

    // Hotkey Wheel 1.0.11 offers the pouch Elytra toggle, and wheel slots saved before 1.5.2 with the removed
    // suite control become the same toggle. Either must switch the Elytra through Tool Pouch, off and back on.
    private void checkWheelElytraToggles(ClientGameTestContext context) throws Exception {
        WheelConfig original = context.computeOnClient(client -> WheelConfig.get().copy());
        context.runOnClient(client -> {
            client.gui.setScreen(null);
            WheelConfig config = new WheelConfig();
            config.wheels.get(0).entries.clear();
            for (String control : List.of(ToolPouchToggles.SUITE_ELYTRA_MAPPING, ToolPouchToggles.ELYTRA)) {
                WheelConfig.Entry entry = new WheelConfig.Entry();
                entry.control = control;
                entry.icon = "minecraft:elytra";
                config.wheels.get(0).entries.add(entry);
            }
            check(WheelConfig.save(config), "Wheel fixture with the saved suite control and the pouch Elytra toggle saves");
            HotkeyWheelClient.WHEEL_KEYS[0].setKey(InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_NUMPAD5));
            KeyMapping.resetMapping();
        });
        String[] names = {"Saved suite Elytra control", "Wheel's Toggle pouch Elytra"};
        for (int slot = 0; slot < names.length; slot++) {
            for (boolean disable : new boolean[] {true, false}) {
                check(chooseWheelSlot(context, slot), names[slot] + " wheel opens and renders");
                check(context.computeOnClient(client -> client.player != null && client.gui.screen() == null),
                        names[slot] + " closes the wheel and play continues");
                boolean disabledOnServer = onServer(context, player -> ToolPouchUtil.isSlotDisabled(ToolPouchUtil.getToolPouch(player), 0));
                check(disabledOnServer == disable, names[slot] + (disable ? " switches the Elytra slot off" : " switches the Elytra slot back on"));
                check((onServer(context, player -> ToolPouchUtil.getElytraFromToolPouch(player, false)) == null) == disable,
                        names[slot] + (disable ? " stops pouch flight on the server" : " restores pouch flight on the server"));
                check(context.computeOnClient(client -> ToolPouchUtil.getElytraFromToolPouch(client.player, false) == null) == disable,
                        names[slot] + (disable ? " stops pouch flight on the client" : " restores pouch flight on the client"));
            }
        }
        context.runOnClient(client -> WheelConfig.save(original));
    }

    private boolean chooseWheelSlot(ClientGameTestContext context, int slot) {
        context.getInput().holdKey(InputConstants.KEY_NUMPAD5);
        context.waitTicks(3);
        double[] position = context.computeOnClient(client -> {
            if (!(client.gui.screen() instanceof WheelScreen wheel)) return null;
            int count = WheelConfig.get().wheels.get(wheel.wheelIndex()).entries.size();
            double angle = RadialGeometry.angle(slot, count);
            return new double[] {
                    (wheel.centerX() + Math.cos(angle) * wheel.radius() * 0.76) * client.getWindow().getScreenWidth() / wheel.width,
                    (wheel.centerY() + Math.sin(angle) * wheel.radius() * 0.76) * client.getWindow().getScreenHeight() / wheel.height};
        });
        if (position != null) context.getInput().setCursorPos(position[0], position[1]);
        context.waitTicks(2);
        context.getInput().releaseKey(InputConstants.KEY_NUMPAD5);
        context.waitTicks(6);
        return position != null;
    }

    private static String overlayMessage(Minecraft client) {
        try {
            Field field = client.gui.hud.getClass().getDeclaredField("overlayMessageString");
            field.setAccessible(true);
            Object message = field.get(client.gui.hud);
            return message instanceof Component component ? component.getString() : "";
        } catch (ReflectiveOperationException error) {
            return "unreadable: " + error;
        }
    }

    private static <T> T onServer(ClientGameTestContext context, Function<ServerPlayer, T> action) throws Exception {
        UUID uuid = context.computeOnClient(client -> client.player.getUUID());
        var server = context.computeOnClient(Minecraft::getSingleplayerServer);
        var result = server.submit(() -> action.apply(server.getPlayerList().getPlayer(uuid)));
        context.waitFor(client -> result.isDone(), 200);
        return result.get();
    }
}
