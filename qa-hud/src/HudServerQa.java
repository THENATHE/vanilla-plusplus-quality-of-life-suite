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
                    var map = MapItem.create(p.level(), p.getBlockX(), p.getBlockZ(), (byte)0, true, false);
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
                        command = next;seed(p, next.equals("restore"));
                        Files.writeString(control.resolve("server-ack"), next);
                    }
                }
            } catch (Throwable error) {
                error.printStackTrace();
                try { Files.writeString(control.resolve("failure"), "server: " + error); } catch (Exception ignored) {}
            }
        });
    }
    void seed(net.minecraft.server.level.ServerPlayer p, boolean withAtlas) {
        var items = withAtlas ? List.of(atlas.copy(), new ItemStack(Items.COMPASS), new ItemStack(Items.CLOCK))
                : List.of(new ItemStack(Items.COMPASS), new ItemStack(Items.CLOCK));
        var pouch = new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
        pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        p.getInventory().setItem(12, pouch);p.inventoryMenu.broadcastFullState();
    }
}
