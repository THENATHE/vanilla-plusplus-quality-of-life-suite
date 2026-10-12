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
                        var received=client.player.getInventory().getItem(pair.equals("stone")?5:6);
                        int count=marker.get(pair+"_count").getAsInt(), max=marker.get(pair+"_max").getAsInt();
                        if(!Boolean.getBoolean("suite.qa.native-stackables"))max=Math.min(99,max);
                        if(received.getCount()!=count||received.getMaxStackSize()!=max) {
                            if(ticks<200)return;
                            throw new IllegalStateException(pair+" packet count="+received.getCount()+" max="+received.getMaxStackSize()+" expected "+count+"/"+max);
                        }
                    }
                }
                if (marker.has("nested_potion_count")) {
                    var pouch = client.player.getInventory().getItem(8);
                    var containers = pouch.getOrDefault(DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.EMPTY).itemCopies().toList();
                    if (containers.size() != 1 || !containers.getFirst().is(net.minecraft.world.item.Items.SHULKER_BOX)) throw new IllegalStateException("Nested shulker missing from received pouch");
                    var potions = containers.getFirst().getOrDefault(DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.EMPTY).itemCopies().toList();
                    if (potions.size() != 1 || !potions.getFirst().is(net.minecraft.world.item.Items.POTION) || potions.getFirst().getCount() != 3 || potions.getFirst().getMaxStackSize() != 3) throw new IllegalStateException("Nested potion stack changed during packet reception: " + potions);
                    if (Boolean.getBoolean("suite.qa.native-stackables")) {
                        var plain = net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.POTION, 3));
                        if (plain.getMaxStackSize() != 3 || plain.create().isEmpty()) throw new IllegalStateException("Native client template override missing");
                    }
                }
                if(Boolean.getBoolean("suite.qa.stackables-actions")&&marker.has("stone_count")) {
                    int expected=marker.get("stone_count").getAsInt();
                    var menu=client.player.inventoryMenu;
                    if(stackStage==0) {
                        client.gameMode.handleContainerInput(menu.containerId,41,0,net.minecraft.world.inventory.ContainerInput.PICKUP,client.player);
                        stackStage=1;stackTicks=ticks;return;
                    }
                    if(stackStage==1) {
                        if(ticks-stackTicks<20)return;
                        if(menu.getCarried().getCount()!=expected||!client.player.getInventory().getItem(5).isEmpty())throw new IllegalStateException("pickup lost stack: carried="+menu.getCarried().getCount());
                        client.gameMode.handleContainerInput(menu.containerId,43,0,net.minecraft.world.inventory.ContainerInput.PICKUP,client.player);
                        stackStage=2;stackTicks=ticks;return;
                    }
                    if(stackStage==2) {
                        if(ticks-stackTicks<20)return;
                        if(client.player.getInventory().getItem(7).getCount()!=expected||!menu.getCarried().isEmpty())throw new IllegalStateException("place lost stack: slot="+client.player.getInventory().getItem(7).getCount()+" carried="+menu.getCarried().getCount());
                        stackStage=3;
                    }
                }
                if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("mapstitch_mixed_scales")&&nativeChalk&&!MixedUi.progress(client,ticks))return;
                if(nativeChalk && MixedUi.mixedStage!=11)throw new IllegalStateException("Merged atlas addon was not exercised");
                done = true;
                Files.writeString(directory.resolve("client-joined.txt"), "PASS in-world client ticks\n");
                JsonObject result = new JsonObject();if(marker.has("stone_count")) {result.addProperty("stone_count",client.player.getInventory().getItem(stackStage==3?7:5).getCount());result.addProperty("inventory_packet_moves",stackStage==3);result.addProperty("stone_max",client.player.getInventory().getItem(stackStage==3?7:5).getMaxStackSize());result.addProperty("potion_count",client.player.getInventory().getItem(6).getCount());result.addProperty("potion_max",client.player.getInventory().getItem(6).getMaxStackSize());}
                result.addProperty("nested_potion_packet_roundtrip",marker.has("nested_potion_count"));
                result.addProperty("native_template_override",Boolean.getBoolean("suite.qa.native-stackables"));
                result.addProperty("mixed_scale_ui_roundtrip",nativeChalk&&MixedUi.mixedStage==11);result.addProperty("join_count", joins);result.addProperty("passed", true);
                result.addProperty("native_chalk",nativeChalk);result.addProperty("actual_block",actualBlock);result.addProperty("actual_item",actualItem);
                result.addProperty("damage",stack.getDamageValue());result.addProperty("custom_name",stack.get(DataComponents.CUSTOM_NAME).getString());result.addProperty("virtual_mark_displays",displays);
                result.addProperty("native_sso", nativeSso);result.addProperty("actual_anvil", actualAnvil);
                result.addProperty("actual_anvil_item", actualAnvilItem);result.addProperty("anvil_facing", "east");
                Files.writeString(directory.resolve("client-joined.json"), result.toString());
            } catch (Exception error) { throw new RuntimeException(error); }
        });
    }
    private static final class MixedUi {
        static int mixedStage,mixedTicks;
        static void press(net.minecraft.client.Minecraft client,String label) {
            var screen=client.gui.screen();
            for(var child:screen.children())if(child instanceof net.minecraft.client.gui.components.Button button&&button.getMessage().getString().equals(label)) {
                button.onPress(new net.minecraft.client.input.KeyEvent(com.mojang.blaze3d.platform.InputConstants.KEY_S,0,0));return;
            }
            throw new IllegalStateException("Missing atlas control: "+label);
        }
        static boolean progress(net.minecraft.client.Minecraft client,int ticks) {
            if(mixedStage==11)return true;
            var atlas=client.player.getInventory().getItem(2);
            if(mixedStage==0) {
                com.thenathe.multiscale.client.MixedScalesClient.rememberUse(atlas);
                var screen=new me.pajic.mapstitch.worldmap.WorldMapScreen(0);
                client.gui.setScreen(screen);
                // Observe actual widgets: original sidebar geometry must stay native.
                for (int i = 0; i < 8; i++) {
                    var button = (net.minecraft.client.gui.components.Button) screen.children().get(i);
                    if (button.getX() != screen.width - 20 || button.getY() != screen.height / 2 - 72 + 18 * i
                            || button.getWidth() != 16 || button.getHeight() != 16)
                        throw new IllegalStateException("Original atlas sidebar spacing changed at " + i);
                }
                int rowX = screen.width - 152;
                for (int i = 8; i < 14; i++) {
                    var button = (net.minecraft.client.gui.components.Button) screen.children().get(i);
                    int wantedX = i == 8 ? rowX : rowX + 40 + 22 * (i - 9);
                    if (button.getX() != wantedX || button.getY() != 16 || button.getHeight() != 16
                            || button.getX() < 0 || button.getX() + button.getWidth() > screen.width)
                        throw new IllegalStateException("Top atlas controls are misaligned at " + i);
                }
                var tooltip = me.pajic.mapstitch.item.AtlasItem.getTooltip(atlas);
                if (!tooltip.get(0).getString().equals("Stores maps")
                        || !tooltip.get(1).getString().startsWith("Generating:")
                        || !tooltip.get(2).getString().startsWith("Minimap scale"))
                    throw new IllegalStateException("Atlas tooltip ordering changed: " + tooltip);
                var key=new net.minecraft.client.input.KeyEvent(com.mojang.blaze3d.platform.InputConstants.KEY_S,0,0);
                ((net.minecraft.client.gui.components.Button)screen.children().get(3)).onPress(key);
                if(atlas.get(me.pajic.mapstitch.component.ModDataComponents.ATLAS_SCALE)!=0)throw new IllegalStateException("World-map S changed minimap");
                press(client,"M1");mixedStage=1;mixedTicks=ticks;return false;
            }
            if(ticks-mixedTicks<15)return false;
            if(mixedStage==10) {
                if(!(client.gui.screen() instanceof me.fzzyhmstrs.fzzy_config.screen.internal.ConfigScreen))throw new IllegalStateException("Controls hotkey did not open suite settings");
                com.thenathe.suite.client.SuiteKeybindings.OPEN_SETTINGS.setKey(com.mojang.blaze3d.platform.InputConstants.UNKNOWN);
                net.minecraft.client.KeyMapping.resetMapping();
                mixedStage=11;client.gui.setScreen(null);return true;
            }
            if(mixedStage==9) {
                var pouch=com.thenathe.toolpouchcompat.AtlasBridge.atlases(client.player);
                boolean correct=pouch.size()==1&&pouch.getFirst().getOrDefault(me.pajic.mapstitch.component.ModDataComponents.ATLAS_SCALE,-1)==1;
                if(!correct) {
                    if(ticks-mixedTicks<100)return false;
                    throw new IllegalStateException("Minimap control selected wrong duplicate atlas: pouch="+pouch);
                }
                if(atlas.get(me.pajic.mapstitch.component.ModDataComponents.ATLAS_SCALE)!=0||client.player.getInventory().getItem(3).get(me.pajic.mapstitch.component.ModDataComponents.ATLAS_SCALE)!=2)throw new IllegalStateException("Pouch scale changed an inventory book");
                client.gui.setScreen(null);
                var probeKey=com.mojang.blaze3d.platform.InputConstants.Type.KEYBOARD.getOrCreate(com.mojang.blaze3d.platform.InputConstants.KEY_F10);
                com.thenathe.suite.client.SuiteKeybindings.OPEN_SETTINGS.setKey(probeKey);
                net.minecraft.client.KeyMapping.resetMapping();net.minecraft.client.KeyMapping.click(probeKey);
                mixedStage=10;mixedTicks=ticks;return false;
            }
            var stored=com.thenathe.toolpouchcompat.AtlasBridge.atlases(client.player);
            if(stored.size()!=1||stored.getFirst().get(me.pajic.mapstitch.component.ModDataComponents.ATLAS_SCALE)!=0)throw new IllegalStateException("Explicit inventory selection changed pouch");
            int wanted=mixedStage<=5?mixedStage%5:0;
            int mask=com.thenathe.multiscale.AtlasOptions.generationMask(atlas);
            int wantedMask=mixedStage==6?3:mixedStage==7?0:1;
            if(atlas.getOrDefault(me.pajic.mapstitch.component.ModDataComponents.ATLAS_SCALE,-1)!=wanted||mask!=wantedMask) {
                if(ticks-mixedTicks<100)return false;
                throw new IllegalStateException("Independent atlas options not synchronized: stage="+mixedStage+" atlas="+atlas);
            }
            if(client.player.getInventory().getItem(3).get(me.pajic.mapstitch.component.ModDataComponents.ATLAS_SCALE)!=2)throw new IllegalStateException("Selection changed second atlas");
            if(mixedStage<5)press(client,"M"+(1<<wanted));
            else if(mixedStage==5)press(client,"2");
            else if(mixedStage==6){
                net.minecraft.client.Screenshot.grab(client.gameDirectory,"atlas-independent-controls.png",client.gameRenderer.mainRenderTarget(),1,m->{});
                press(client,"2");press(client,"1");
            }
            else if(mixedStage==7)press(client,"1");
            else {
                client.gui.setScreen(null);
                var inventoryCopy=atlas.copy();var pouchCopy=stored.getFirst().copy();
                net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,inventoryCopy,tag->tag.remove(com.thenathe.multiscale.AtlasOptions.IDENTITY));
                net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,pouchCopy,tag->tag.remove(com.thenathe.multiscale.AtlasOptions.IDENTITY));
                if(!net.minecraft.world.item.ItemStack.isSameItemSameComponents(inventoryCopy,pouchCopy))throw new IllegalStateException("Duplicate-source fixture is not component-identical");
                client.gui.setScreen(new me.pajic.mapstitch.worldmap.WorldMapScreen(-1));press(client,"M1");
            }
            mixedStage++;mixedTicks=ticks;return false;
        }
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
