package suite.qa;

import com.google.gson.JsonObject;
import com.thenathe.suite.SuiteItems;
import com.thenathe.suite.network.SuiteCapabilities;
import com.thenathe.suite.pickup.PickupUpgrades;
import com.thenathe.suite.wrench.DebugWrench;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PhysicalUpgradeServerQa implements ModInitializer {
    private record State(ServerPlayer player, ItemStack backpack, ItemEntity drop, boolean nativeClient, int joinedTick) {}
    private final Map<String, State> players = new HashMap<>();
    private final Map<String, Integer> stages = new HashMap<>();

    @Override public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.player;
            var name = player.getGameProfile().name();
            PickupUpgrades.setEnabled(player, true);
            player.getInventory().setItem(20, new ItemStack(SuiteItems.POUCH_MAGNET));
            player.getInventory().setItem(21, new ItemStack(SuiteItems.POUCH_COLLECTOR));
            player.getInventory().setItem(22, new ItemStack(DebugWrench.WRENCH));
            var pouch = new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
            var contents = new java.util.ArrayList<ItemStack>();
            var atlasPouch = player.getInventory().getItem(4);
            if (atlasPouch.has(DataComponents.CONTAINER)) contents.addAll(atlasPouch.get(DataComponents.CONTAINER).itemCopies().toList());
            contents.add(new ItemStack(SuiteItems.POUCH_MAGNET));
            contents.add(new ItemStack(SuiteItems.POUCH_COLLECTOR));
            pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
            player.getInventory().setItem(4, pouch);
            var backpack = new ItemStack(me.pajic.tiered_backpacks.item.ModItems.LEATHER_BACKPACK);
            player.getInventory().setItem(25, backpack);
            var drop = new ItemEntity(player.level(), player.getX() + 4, player.getY(), player.getZ(), new ItemStack(Items.DIAMOND, 17));
            drop.setNoPickUpDelay();
            player.level().addFreshEntity(drop);
            boolean nativeClient = SuiteCapabilities.isNative(handler.getPacketContext(), "thenathe_mod_suite");
            players.put(name, new State(player, backpack, drop, nativeClient, server.getTickCount()));
            stages.put(name, 0);
            player.inventoryMenu.broadcastFullState();
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Path directory = Path.of(System.getProperty("suite.qa.control"));
            for (var entry : players.entrySet()) {
                String name = entry.getKey();
                var state = entry.getValue();
                var player = state.player();
                int age = server.getTickCount() - state.joinedTick();
                int stage = stages.get(name);
                try {
                    if (stage == 0 && age >= 40) {
                        int amount = state.backpack().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).itemCopies()
                                .filter(stack -> stack.is(Items.DIAMOND)).mapToInt(ItemStack::getCount).sum();
                        if (!state.drop().isRemoved() || amount != 17) throw new AssertionError("Real connected magnet/collector did not conserve17 diamonds: " + name + " amount=" + amount);
                        stage = 1;
                    }
                    if (!state.nativeClient() && stage == 1) {
                        server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), "suite-pickup off");
                    }
                    if (stage == 1 && !PickupUpgrades.enabled(player)) stage = 2;
                    if (!state.nativeClient() && stage == 2) {
                        server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), "suite-pickup on");
                    }
                    if (stage == 2 && PickupUpgrades.enabled(player)) stage = 3;
                    stages.put(name, stage);
                    var row = new JsonObject();
                    row.addProperty("passed", stage == 3 && age >= 100);
                    row.addProperty("stage", stage);
                    row.addProperty("native_upgrades", state.nativeClient());
                    row.addProperty("collector_enabled", PickupUpgrades.enabled(player));
                    row.addProperty("real_magnet_collector_pickup", stage > 0);
                    row.addProperty("collected_diamonds", state.backpack().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).itemCopies()
                            .filter(stack -> stack.is(Items.DIAMOND)).mapToInt(ItemStack::getCount).sum());
                    row.addProperty("tick_age", age);
                    Files.writeString(directory.resolve(name + "-upgrade.json"), row.toString());
                } catch (Throwable error) {
                    error.printStackTrace();
                    try { Files.writeString(directory.resolve(name + "-upgrade-failure.txt"), error.toString()); }
                    catch (Exception ignored) {}
                }
            }
        });
    }
}
