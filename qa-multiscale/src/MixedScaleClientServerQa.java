package qa;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.thenathe.multiscale.AtlasOptions;
import com.thenathe.suite.network.SuiteCapabilities;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.util.ModUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.*;
import org.joml.Vector2i;

/** Deliberately first-send maps from all dimensions while the player is in the overworld. */
public final class MixedScaleClientServerQa implements ModInitializer {
    private boolean seeded;
    private ItemStack originalAtlas;
    private final Path control = Path.of(System.getProperty("maps.qa.control"));
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            try {
                if (server.getPlayerList().getPlayers().isEmpty()) return;
                var player = server.getPlayerList().getPlayers().getFirst();
                if (!SuiteCapabilities.isNative(player.connection.getPacketContext(), "mapstitch")) return;
                if (!seeded) {
                    seeded = true;
                    player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
                    var records = new JsonArray();
                    var contents = new ArrayList<ItemStackTemplate>();
                    int dimensionIndex = 0;
                    for (var dimension : List.of(Level.OVERWORLD, Level.NETHER, Level.END)) {
                        var level = server.getLevel(dimension);
                        for (byte scale = 0; scale < 5; scale++) {
                            var map = MapItem.create(level, dimensionIndex * 4096, 0, scale, true, false);
                            var id = map.get(DataComponents.MAP_ID);
                            var data = MapItem.getSavedData(map, level).locked();
                            level.setMapData(id, data);
                            Arrays.fill(data.colors, (byte) ((dimensionIndex + 1) * 4 + scale));
                            data.addClientSideDecorations(List.of(new MapDecoration(MapDecorationTypes.RED_BANNER,
                                    (byte) 0, (byte) 0, (byte) 0, Optional.of(net.minecraft.network.chat.Component.literal("dimension " + dimensionIndex)))));
                            map.set(ModDataComponents.MAP_CENTER, new Vector2i(data.centerX, data.centerZ));
                            contents.add(ItemStackTemplate.fromNonEmptyStack(map));
                            player.getInventory().setItem(2, map);
                            data.tickCarriedBy(player, map, null);
                            // Alternate ordinary getUpdatePacket and forced atlas initial load.
                            if (scale % 2 == 0) player.connection.send(data.getUpdatePacket(id, player));
                            else ModUtil.sendVanillaMapPacket(id, data, player, true);
                            var record = new JsonObject();
                            record.addProperty("id", id.id()); record.addProperty("dimension", dimension.identifier().toString());
                            record.addProperty("scale", scale); record.addProperty("x", data.centerX); record.addProperty("z", data.centerZ);
                            record.addProperty("color", data.colors[0]); records.add(record);
                        }
                        dimensionIndex++;
                    }
                    var atlas = new ItemStack(me.pajic.mapstitch.item.ModItems.ATLAS);
                    atlas.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(contents));
                    atlas.set(ModDataComponents.ATLAS_SCALE, 0);
                    AtlasOptions.setGenerationMask(atlas, 0);
                    originalAtlas = atlas.copy();
                    player.getInventory().setItem(0, atlas);
                    player.getInventory().setItem(1, new ItemStack(Items.COMPASS));
                    player.getInventory().setItem(2, ItemStack.EMPTY);
                    player.inventoryMenu.broadcastFullState();
                    Files.writeString(control.resolve("seeded.json"), new GsonBuilder().setPrettyPrinting().create().toJson(records));
                }
                if (Files.exists(control.resolve("restore-atlas"))) {
                    Files.delete(control.resolve("restore-atlas"));
                    player.getInventory().setItem(0, originalAtlas.copy());
                    player.inventoryMenu.broadcastFullState();
                }
                var travel = control.resolve("travel.txt");
                if (Files.exists(travel)) {
                    String target = Files.readString(travel).trim(); Files.delete(travel);
                    var dimension = target.equals("nether") ? Level.NETHER : Level.OVERWORLD;
                    player.teleportTo(server.getLevel(dimension), 0, 90, 0, Set.of(), 0, 0, true);
                }
            } catch (Throwable error) {
                try { Files.writeString(control.resolve("failure"), error.toString()); } catch (Exception ignored) {}
                throw new RuntimeException(error);
            }
        });
    }
}
