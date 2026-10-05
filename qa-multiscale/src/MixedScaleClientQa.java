package qa;

import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;
import com.google.gson.*;
import com.thenathe.multiscale.MapMetadata;
import com.thenathe.multiscale.client.MixedScalesClient;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.*;

public final class MixedScaleClientQa implements ClientModInitializer {
    public static final List<String> rendered = new ArrayList<>();
    private final Path control = Path.of(System.getProperty("maps.qa.control"));
    private final JsonArray observations = new JsonArray();
    private JsonArray records;
    private int ticks, phase;
    private boolean opened, done;
    private static Object read(String name, Object screen) throws Exception {
        var field = WorldMapScreen.class.getDeclaredField(name); field.setAccessible(true); return field.get(screen);
    }
    private static void write(String name, Object screen, Object value) throws Exception {
        var field = WorldMapScreen.class.getDeclaredField(name); field.setAccessible(true); field.set(screen, value);
    }
    private static void check(boolean okay, String message) { if (!okay) throw new AssertionError(message); }
    private void verifyMetadata(Minecraft mc) {
        for (var element : records) {
            var record = element.getAsJsonObject();
            var data = mc.level.getMapData(new MapId(record.get("id").getAsInt()));
            check(data != null && data.dimension.identifier().toString().equals(record.get("dimension").getAsString())
                    && data.scale == record.get("scale").getAsInt() && data.centerX == record.get("x").getAsInt()
                    && data.centerZ == record.get("z").getAsInt() && data.colors[0] == record.get("color").getAsByte()
                    && data.locked, "Wrong metadata/pixels for " + record);
        }
    }
    private void repairRegression(Minecraft mc) {
        var record = records.get(0).getAsJsonObject();
        var id = new MapId(record.get("id").getAsInt());
        var wrong = MapItemSavedData.createForClient((byte) 0, false, Level.END);
        wrong.colors[123] = 66;
        wrong.addClientSideDecorations(List.of(new MapDecoration(MapDecorationTypes.RED_BANNER,
                (byte) 10, (byte) 12, (byte) 0, Optional.of(net.minecraft.network.chat.Component.literal("preserved banner")))));
        mc.level.overrideMapData(id, wrong);
        MixedScalesClient.applyMetadata(new MapMetadata(id.id(), Identifier.parse("minecraft:overworld"),
                record.get("x").getAsInt(), record.get("z").getAsInt(), (byte) 0, true));
        var repaired = mc.level.getMapData(id);
        check(repaired == wrong && repaired.dimension.equals(Level.OVERWORLD) && repaired.locked && repaired.colors[123] == 66
                && repaired.getDecorations().iterator().next().name().orElseThrow().getString().equals("preserved banner"),
                "Repair lost old pixels/banner or dimension");
        repaired.colors[0] = record.get("color").getAsByte();
        long revision = MixedScalesClient.mapMetadataRevision();
        MixedScalesClient.applyMetadata(new MapMetadata(-1, Identifier.parse("minecraft:the_end"), 0, 0, (byte) 0, false));
        MixedScalesClient.applyMetadata(new MapMetadata(999999, Identifier.parse("minecraft:the_end"), 0, 0, (byte) 5, false));
        check(revision == MixedScalesClient.mapMetadataRevision(), "Invalid metadata changed map data");
        var report = new JsonObject(); report.addProperty("case", "first receipt plus stale map repair");
        report.addProperty("network_maps", records.size()); report.addProperty("preserved_pixels_and_banner", true);
        observations.add(report);
    }
    private void verifyMinimapMarkers(Minecraft mc) throws Exception {
        var entries = new ArrayList<net.minecraft.world.item.ItemStackTemplate>();
        for (var element : records) {
            var record = element.getAsJsonObject();
            if (record.get("scale").getAsInt() != 0) continue;
            var explorer = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BURIED_TREASURE_MAP);
            explorer.set(net.minecraft.core.component.DataComponents.MAP_ID, new MapId(record.get("id").getAsInt()));
            explorer.set(me.pajic.mapstitch.component.ModDataComponents.MAP_CENTER,
                    new org.joml.Vector2i(record.get("x").getAsInt(), record.get("z").getAsInt()));
            entries.add(net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(explorer));
        }
        var atlas = new net.minecraft.world.item.ItemStack(me.pajic.mapstitch.item.ModItems.ATLAS);
        atlas.set(net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS, new net.minecraft.world.item.component.BundleContents(entries));
        var overlay = me.pajic.mapstitch.minimap.MinimapOverlay.class;
        var update = overlay.getDeclaredMethod("updateMarkers", net.minecraft.world.item.ItemStack.class);
        update.setAccessible(true); update.invoke(null, atlas);
        var field = overlay.getDeclaredField("EXPLORATION_MARKERS"); field.setAccessible(true);
        var markers = (Set<?>) field.get(null);
        check(markers.size() == 1, "Minimap mixed exploration markers across dimensions: " + markers.size());
        var report = new JsonObject(); report.addProperty("case", "minimap exploration marker dimension filter");
        report.addProperty("dimension", mc.level.dimension().identifier().toString());
        report.addProperty("foreign_dimension_markers_excluded", true); observations.add(report);
    }
    private void open(Minecraft mc) throws Exception {
        var screen = new WorldMapScreen(0); mc.gui.setScreen(screen);
        write("follow", screen, false); write("zoomLevel", null, 0); opened = true; ticks = 0; rendered.clear();
        center(screen);
    }
    private void center(Object screen) throws Exception {
        String dimension = read("dimensionId", null).toString(); int scale = (int) read("scale", screen);
        for (var element : records) {
            var record = element.getAsJsonObject();
            if (record.get("dimension").getAsString().equals(dimension) && record.get("scale").getAsInt() == scale) {
                write("camX", screen, (double) record.get("x").getAsInt());
                write("camZ", screen, (double) record.get("z").getAsInt()); return;
            }
        }
        throw new AssertionError("Unknown map view");
    }
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (done || mc.player == null || mc.level == null) return;
            try {
                if (!Files.exists(control.resolve("seeded.json"))) return;
                if (records == null) records = JsonParser.parseString(Files.readString(control.resolve("seeded.json"))).getAsJsonArray();
                if (phase == 0 && !opened) {
                    if (++ticks < 30) return;
                    verifyMetadata(mc); repairRegression(mc); verifyMinimapMarkers(mc); open(mc); return;
                }
                if (phase == 15 || phase == 21) {
                    var expected = phase == 15 ? Level.NETHER : Level.OVERWORLD;
                    if (!mc.level.dimension().equals(expected)) return;
                    if (++ticks < 30) return;
                    verifyMetadata(mc); verifyMinimapMarkers(mc); open(mc); phase++; return;
                }
                if (!opened || ++ticks < 20) return;
                var screen = mc.gui.screen();
                String dimension = read("dimensionId", null).toString(); int scale = (int) read("scale", screen);
                var maps = (Map<?, ?>) read("MAPS", null);
                check(maps.size() == 5, "World-map cache mixes dimensions: " + dimension + " maps=" + maps.size());
                for (var value : maps.values()) {
                    var method = value.getClass().getDeclaredMethod("data"); method.setAccessible(true);
                    var data = (MapItemSavedData) method.invoke(value);
                    check(data.dimension.identifier().toString().equals(dimension), "Foreign-dimension tile in cache");
                }
                check(!rendered.isEmpty(), "No actual map rendered at " + dimension + " scale=" + scale);
                for (String observed : rendered) check(observed.equals(dimension + "/" + scale), "Rendered wrong map " + observed);
                var row = new JsonObject(); row.addProperty("case", "worldmap view " + phase);
                row.addProperty("dimension", dimension); row.addProperty("scale", scale);
                row.addProperty("render_calls", rendered.size()); row.addProperty("matching_dimension_cache", maps.size()); observations.add(row);
                phase++; rendered.clear(); ticks = 0;
                if (phase == 15 || phase == 21) {
                    mc.gui.setScreen(null); opened = false;
                    Files.writeString(control.resolve("travel.txt"), phase == 15 ? "nether" : "overworld"); return;
                }
                if (phase == 23) {
                    mc.gui.setScreen(null); done = true;
                    Files.writeString(control.resolve("observations.json"), new GsonBuilder().setPrettyPrinting().create().toJson(observations));
                    Files.writeString(control.resolve("result.txt"), "PASS native metadata, old pixels/banners, 21 dimension/scale render views, two real dimension travels\n"); return;
                }
                // Real native S/D buttons: not a replacement renderer or hand-built cache.
                int buttonIndex = phase < 15 && phase % 5 == 0 ? 2 : 3;
                ((Button) screen.children().get(buttonIndex)).onPress(new net.minecraft.client.input.KeyEvent(83, 0, 0));
                center(screen);
            } catch (Throwable error) {
                done = true;
                try { Files.writeString(control.resolve("failure"), error.toString()); } catch (Exception ignored) {}
                error.printStackTrace();
            }
        });
    }
}
