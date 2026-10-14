package suitehudqa;

import java.nio.file.*;
import java.util.List;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.recipe.AtlasRecipe;
import me.pajic.toolpouch.ToolPouch;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.GameType;

public final class HudServerQa implements ModInitializer {
    final Path control = Path.of(System.getProperty("hud.qa.control"));
    ItemStack atlas;
    ItemStack map;
    String command = "";
    public void onInitialize() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            me.pajic.mapstitch.MapStitch.CONFIG.minimapInfo.allowCoordinates.accept(true);
            me.pajic.mapstitch.MapStitch.CONFIG.minimapInfo.allowGameTime.accept(true);
            me.pajic.mapstitch.MapStitch.CONFIG.minimapInfo.allowWeather.accept(true);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getPlayerList().getPlayers().isEmpty()) return;
            var p = server.getPlayerList().getPlayers().getFirst();
            try {
                if (atlas == null) {
                    p.closeContainer();p.getInventory().clearContent();p.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
                    p.setGameMode(GameType.SURVIVAL);p.setPermanentlyInvulnerable(true);
                    ToolPouch.CONFIG.allowUseFromInventory.accept(true);
                    ToolPouch.CONFIG.infoOverlaySettings.obfuscateClockIfNotOverworld.accept(false);
                    ToolPouch.CONFIG.infoOverlaySettings.obfuscateCompassIfNotOverworld.accept(false);
                    map = MapItem.create(p.level(), p.getBlockX(), p.getBlockZ(), (byte)0, true, false);
                    var data = MapItem.getSavedData(map, p.level());
                    map.set(ModDataComponents.MAP_CENTER, new org.joml.Vector2i(data.centerX, data.centerZ));
                    var recipe = new AtlasRecipe();
                    var input = CraftingInput.of(2,1,List.of(new ItemStack(Items.BOOK),map));
                    if(!recipe.matches(input,p.level()))throw new AssertionError("atlas fixture recipe did not match");
                    atlas = recipe.assemble(input);
                    if(atlas.isEmpty())throw new AssertionError("empty atlas fixture");
                    atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, map.get(DataComponents.MAP_ID).id());
                    seed(p, true);
                    Files.writeString(control.resolve("seeded"), "ready\n");
                }
                Path file = control.resolve("server-command");
                if (Files.exists(file)) {
                    String next = Files.readString(file).trim();
                    if (!next.equals(command)) {
                        command = next;
                        if(next.endsWith("-edge"))addEdgeBanner(p);
                        seed(p, next.startsWith("atlas"));
                        Files.writeString(control.resolve("server-ack"), next);
                    }
                }
            } catch (Throwable error) {
                error.printStackTrace();
                try { Files.writeString(control.resolve("failure"), "server: " + error); } catch (Exception ignored) {}
            }
        });
    }
    void addEdgeBanner(net.minecraft.server.level.ServerPlayer player) {
        var data=MapItem.getSavedData(map,player.level());
        if(data.getBanners().isEmpty()) {
            var position=new net.minecraft.core.BlockPos(data.centerX-63,player.getBlockY(),data.centerZ-50);
            player.level().setBlockAndUpdate(position.below(),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            player.level().setBlockAndUpdate(position,net.minecraft.world.level.block.Blocks.BANNER.white().defaultBlockState());
            var banner=(net.minecraft.world.level.block.entity.BannerBlockEntity)player.level().getBlockEntity(position);
            var stack=new ItemStack(Items.BANNER.white());
            stack.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Named marker at the map edge"));
            banner.applyComponentsFromItemStack(stack);
            if(!data.toggleBanner(player.level(),position))throw new AssertionError("edge banner not added to map");
            var packet=data.getUpdatePacket(map.get(DataComponents.MAP_ID),player);
            if(packet!=null)player.connection.send(packet);
            try {Files.writeString(control.resolve("edge-banner.json"),"{\"banner_world_x\":"+position.getX()+",\"banner_world_z\":"+position.getZ()+",\"map_center_x\":"+data.centerX+",\"map_center_z\":"+data.centerZ+"}");}
            catch(Exception error){throw new RuntimeException(error);}
        }
    }
    void seed(net.minecraft.server.level.ServerPlayer p, boolean withAtlas) {
        var items = withAtlas ? List.of(atlas.copy(), new ItemStack(Items.COMPASS), new ItemStack(Items.CLOCK))
                : List.of(map.copy(), new ItemStack(Items.COMPASS), new ItemStack(Items.CLOCK));
        var pouch = new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
        pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        p.getInventory().setItem(12, pouch);p.inventoryMenu.broadcastFullState();
    }
}
