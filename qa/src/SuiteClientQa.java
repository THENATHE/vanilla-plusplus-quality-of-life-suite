package suite.qa;

import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import java.nio.file.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Display;

public final class SuiteClientQa implements ClientModInitializer {
    private int ticks, joins, stackStage, stackTicks;
    private boolean done, mutated;
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> { joins++; ticks = 0; done = false; });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Path directory = Path.of(System.getProperty("suite.qa.control"));
            try {
                Path reconnect = directory.resolve("reconnect.txt");
                if (Files.exists(reconnect) && client.gui.screen() instanceof DisconnectedScreen) {
                    Files.delete(reconnect);
                    String address = System.getProperty("suite.qa.server.address");
                    ConnectScreen.startConnecting(client.gui.screen(), client, ServerAddress.parseString(address),
                            new ServerData("Suite QA", address, ServerData.Type.OTHER), false, null);
                    return;
                }
                if (!mutated && !System.getProperty("suite.qa.reply-mismatch", "").isEmpty()) { Mutation.install(); mutated = true; }
                if (done || client.player == null || client.level == null || ++ticks < 60) return;
                Path markerFile = directory.resolve("marker-expected.json");
                if (!Files.exists(markerFile)) return;
                var marker = com.google.gson.JsonParser.parseString(Files.readString(markerFile)).getAsJsonObject();
                var position = new BlockPos(marker.get("x").getAsInt(), marker.get("y").getAsInt(), marker.get("z").getAsInt());
                boolean nativeChalk = Boolean.getBoolean("suite.qa.native-chalk");
                String expectedBlock = nativeChalk ? "chalk:red_glow_chalk_mark" : "minecraft:air";
                String expectedItem = nativeChalk ? "chalk:red_glow_chalk" : "minecraft:paper";
                var state = client.level.getBlockState(position);
                var stack = client.player.getInventory().getItem(0);
                String actualBlock = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                String actualItem = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                int displays=0;
                for (var entity : client.level.entitiesForRendering()) if (entity instanceof Display.ItemDisplay && entity.distanceToSqr(position.getX()+0.5,position.getY()+0.5,position.getZ()+0.5)<4) displays++;
                if (!actualBlock.equals(expectedBlock) || !actualItem.equals(expectedItem)
                        || stack.getDamageValue()!=13 || !stack.getOrDefault(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.empty()).getString().equals("Suite QA Chalk")
                        || (nativeChalk ? displays!=0 : displays!=1)) {
                    if (ticks < 200) return;
                    throw new IllegalStateException("Marker/slot packet mismatch: block="+actualBlock+" item="+actualItem+" damage="+stack.getDamageValue()+" displays="+displays);
                }
                if (nativeChalk) {
                    String facing=state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING).getName();
                    var orientation=state.getBlock().getStateDefinition().getProperty("orientation");
                    if (!facing.equals("up") || !state.getValue((net.minecraft.world.level.block.state.properties.Property<Integer>)orientation).equals(3)) throw new IllegalStateException("Native mark state properties differ");
                }
                boolean nativeSso = Boolean.getBoolean("suite.qa.native-sso");
                var anvilPosition = new BlockPos(marker.get("anvil_x").getAsInt(), marker.get("anvil_y").getAsInt(), marker.get("anvil_z").getAsInt());
                var anvilState = client.level.getBlockState(anvilPosition);
                var anvilStack = client.player.getInventory().getItem(1);
                String actualAnvil = BuiltInRegistries.BLOCK.getKey(anvilState.getBlock()).toString();
                String actualAnvilItem = BuiltInRegistries.ITEM.getKey(anvilStack.getItem()).toString();
                String expectedAnvil = nativeSso ? "simple_smithing_overhaul:broken_anvil" : "minecraft:damaged_anvil";
                if (!actualAnvil.equals(expectedAnvil) || !actualAnvilItem.equals(expectedAnvil)
                        || !anvilState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING).getName().equals("east")
                        || !anvilStack.getOrDefault(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.empty()).getString().equals("Suite QA Broken Anvil")) {
                    if (ticks < 200) return;
                    throw new IllegalStateException("Broken anvil packet mismatch: block=" + actualAnvil + " item=" + actualAnvilItem);
                }
                if(marker.has("stone_count")&&stackStage==0) {
                    for(var pair:new String[]{"stone","potion"}) {
                        var received=client.player.getInventory().getItem(pair.equals("stone")?2:3);
                        int count=marker.get(pair+"_count").getAsInt(), max=marker.get(pair+"_max").getAsInt();
                        if(!nativeChalk)max=Math.min(99,max);
                        if(received.getCount()!=count||received.getMaxStackSize()!=max) {
                            if(ticks<200)return;
                            throw new IllegalStateException(pair+" packet count="+received.getCount()+" max="+received.getMaxStackSize()+" expected "+count+"/"+max);
                        }
                    }
                }
                if(Boolean.getBoolean("suite.qa.stackables-actions")&&marker.has("stone_count")) {
                    int expected=marker.get("stone_count").getAsInt();
                    var menu=client.player.inventoryMenu;
                    if(stackStage==0) {
                        client.gameMode.handleContainerInput(menu.containerId,38,0,net.minecraft.world.inventory.ContainerInput.PICKUP,client.player);
                        stackStage=1;stackTicks=ticks;return;
                    }
                    if(stackStage==1) {
                        if(ticks-stackTicks<20)return;
                        if(menu.getCarried().getCount()!=expected||!client.player.getInventory().getItem(2).isEmpty())throw new IllegalStateException("pickup lost stack: carried="+menu.getCarried().getCount());
                        client.gameMode.handleContainerInput(menu.containerId,40,0,net.minecraft.world.inventory.ContainerInput.PICKUP,client.player);
                        stackStage=2;stackTicks=ticks;return;
                    }
                    if(stackStage==2) {
                        if(ticks-stackTicks<20)return;
                        if(client.player.getInventory().getItem(4).getCount()!=expected||!menu.getCarried().isEmpty())throw new IllegalStateException("place lost stack: slot="+client.player.getInventory().getItem(4).getCount()+" carried="+menu.getCarried().getCount());
                        stackStage=3;
                    }
                }
                done = true;
                Files.writeString(directory.resolve("client-joined.txt"), "PASS in-world client ticks\n");
                JsonObject result = new JsonObject();if(marker.has("stone_count")) {result.addProperty("stone_count",client.player.getInventory().getItem(stackStage==3?4:2).getCount());result.addProperty("inventory_packet_moves",stackStage==3);result.addProperty("stone_max",client.player.getInventory().getItem(stackStage==3?4:2).getMaxStackSize());result.addProperty("potion_count",client.player.getInventory().getItem(3).getCount());result.addProperty("potion_max",client.player.getInventory().getItem(3).getMaxStackSize());}
                result.addProperty("join_count", joins);result.addProperty("passed", true);
                result.addProperty("native_chalk",nativeChalk);result.addProperty("actual_block",actualBlock);result.addProperty("actual_item",actualItem);
                result.addProperty("damage",stack.getDamageValue());result.addProperty("custom_name",stack.get(DataComponents.CUSTOM_NAME).getString());result.addProperty("virtual_mark_displays",displays);
                result.addProperty("native_sso", nativeSso);result.addProperty("actual_anvil", actualAnvil);
                result.addProperty("actual_anvil_item", actualAnvilItem);result.addProperty("anvil_facing", "east");
                Files.writeString(directory.resolve("client-joined.json"), result.toString());
            } catch (Exception error) { throw new RuntimeException(error); }
        });
    }
    /** Only loaded for a deliberate mismatch fixture running the full suite. */
    private static final class Mutation {
        static void install() {
            var type = com.thenathe.suite.network.SuiteCapabilities.Offer.TYPE;
            net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking.unregisterGlobalReceiver(type);
            net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking.registerGlobalReceiver(type, (offer, context) -> {
                var fingerprints = new java.util.ArrayList<>(com.thenathe.suite.network.SuiteCapabilities.fingerprints());
                fingerprints.set(com.thenathe.suite.network.SuiteCapabilities.MODULES.indexOf(System.getProperty("suite.qa.reply-mismatch")), "0".repeat(64));
                context.responseSender().sendPacket(new com.thenathe.suite.network.SuiteCapabilities.Reply(offer.nonce(), java.util.List.copyOf(fingerprints)));
            });
        }
    }
}
