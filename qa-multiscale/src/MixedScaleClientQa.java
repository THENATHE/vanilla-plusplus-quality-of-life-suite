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
    public static final List<String> minimapRendered = new ArrayList<>();
    public static final List<Integer> ejected = new ArrayList<>();
    public static final Set<String> worldMessages = new HashSet<>();
    private final Path control = Path.of(System.getProperty("maps.qa.control"));
    private final JsonArray observations = new JsonArray();
    private JsonArray records;
    private int ticks, phase;
    private boolean opened, done;
    private int minimapCase;
    private boolean minimapSetup, minimapComplete;
    private net.minecraft.world.item.ItemStack minimapAtlas;
    private boolean repairRequested, repairComplete;
    private long preRepairRevision;
    private int repairTicks;
    private int fullscreenCase, fullscreenTicks;
    private boolean fullscreenReady, fullscreenEjectRequested, fullscreenComplete;
    private int sourceCase, sourceTicks;
    private boolean sourceReady, sourcesComplete;
    private net.minecraft.world.item.ItemStack sourceAtlas;
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
    private boolean minimapRegression(Minecraft mc) throws Exception {
        if (minimapComplete) return true;
        if (!minimapSetup) {
            minimapSetup = true; ticks = 0; minimapRendered.clear();
            var record = records.get(minimapCase == 3 ? 10 : 0).getAsJsonObject();
            var map = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FILLED_MAP);
            map.set(net.minecraft.core.component.DataComponents.MAP_ID, new MapId(record.get("id").getAsInt()));
            if (minimapCase != 1) map.set(me.pajic.mapstitch.component.ModDataComponents.MAP_CENTER,
                    minimapCase == 2 ? new org.joml.Vector2i(765432, -765432)
                            : new org.joml.Vector2i(record.get("x").getAsInt(), record.get("z").getAsInt()));
            minimapAtlas = new net.minecraft.world.item.ItemStack(me.pajic.mapstitch.item.ModItems.ATLAS);
            minimapAtlas.set(net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS,
                    new net.minecraft.world.item.component.BundleContents(List.of(net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(map))));
            minimapAtlas.set(me.pajic.mapstitch.component.ModDataComponents.ATLAS_ACTIVE_MAP_ID, record.get("id").getAsInt());
            minimapAtlas.set(me.pajic.mapstitch.component.ModDataComponents.ATLAS_SCALE, 0);
        }
        // Test the real upstream HUD against a legacy/corrupt item component without
        // changing saved map data or replacing any renderer implementation.
        mc.player.getInventory().setItem(0, minimapAtlas);
        if (++ticks < 20) return false;
        var record = records.get(minimapCase == 3 ? 10 : 0).getAsJsonObject();
        if (minimapCase == 3) check(minimapRendered.isEmpty(), "Foreign-dimension active map rendered in minimap");
        else {
            check(!minimapRendered.isEmpty(), "Minimap blank for case " + minimapCase + " (0 normal, 1 missing center, 2 stale center)");
            var field = me.pajic.mapstitch.minimap.MinimapOverlay.class.getDeclaredField("CACHED_CENTERS");
            field.setAccessible(true);
            var center = (org.joml.Vector2i) ((Map<?, ?>) field.get(null)).get(new MapId(record.get("id").getAsInt()));
            check(center != null && center.x == record.get("x").getAsInt() && center.y == record.get("z").getAsInt(),
                    "Minimap used stale item center: " + center);
        }
        var row = new JsonObject(); row.addProperty("case", List.of("minimap normal center", "minimap missing item center",
                "minimap stale item center", "minimap foreign active dimension").get(minimapCase));
        row.addProperty("render_calls", minimapRendered.size()); observations.add(row);
        Files.writeString(control.resolve("observations.json"), new GsonBuilder().setPrettyPrinting().create().toJson(observations));
        net.minecraft.client.Screenshot.grab(mc.gameDirectory, "minimap-case-" + minimapCase + ".png",
                mc.gameRenderer.mainRenderTarget(), 1, message -> {});
        if (++minimapCase == 4) { minimapComplete = true; Files.writeString(control.resolve("restore-atlas"), "restore"); }
        minimapSetup = false; ticks = 0;
        return false;
    }
    private boolean repairNetworkRegression(Minecraft mc) throws Exception {
        if (repairComplete) return true;
        if (!repairRequested) {
            verifyMetadata(mc);
            repairRequested = true; repairTicks = 0;
            preRepairRevision = MixedScalesClient.mapMetadataRevision();
            minimapRendered.clear();
            for (var element : records) {
                var id = new MapId(element.getAsJsonObject().get("id").getAsInt());
                var data = mc.level.getMapData(id);
                Arrays.fill(data.colors, (byte) 0);
                mc.getMapTextureManager().update(id, data);
            }
            mc.getConnection().sendCommand(Boolean.getBoolean("maps.qa.baseline114") ? "repairmaps" : "atlas fix");
            return false;
        }
        if (++repairTicks < 20) return false;
        if (MixedScalesClient.mapMetadataRevision() <= preRepairRevision) {
            check(repairTicks < 120, "Repair command did not send native cache refresh"); return false;
        }
        for (var element : records) {
            var record = element.getAsJsonObject();
            var data = mc.level.getMapData(new MapId(record.get("id").getAsInt()));
            check(data != null, "Repair deleted client map " + record);
            byte expected = record.get("color").getAsByte();
            for (byte pixel : data.colors) check(pixel == expected, "Repair did not restore full client pixel buffer for " + record);
        }
        check(!minimapRendered.isEmpty(), "Minimap failed to resume drawing after full packet repair");
        var row = new JsonObject(); row.addProperty("case", "real atlas fix network pixel recovery");
        row.addProperty("maps_restored", records.size()); row.addProperty("pixels_per_map", 128 * 128);
        row.addProperty("native_cache_refresh_received", true); row.addProperty("minimap_render_calls", minimapRendered.size());
        observations.add(row);
        net.minecraft.client.Screenshot.grab(mc.gameDirectory, "minimap-repaired-pixels.png",
                mc.gameRenderer.mainRenderTarget(), 1, message -> {});
        repairComplete = true; return true;
    }
    private boolean fullscreenRegression(Minecraft mc) throws Exception {
        if (fullscreenComplete) return true;
        boolean smoke = Boolean.getBoolean("maps.qa.atlasSmoke");
        int caseNumber = smoke && fullscreenCase == 1 ? 11 : fullscreenCase;
        int offset = caseNumber % 6;
        int recordIndex = offset < 5 ? offset : (caseNumber < 6 ? 14 : 8);
        var record = records.get(recordIndex).getAsJsonObject();
        int id = record.get("id").getAsInt();
        if (!fullscreenReady) {
            if (++fullscreenTicks < 30) return false;
            fullscreenReady = true; fullscreenTicks = 0; rendered.clear(); ejected.clear();
            var atlas = mc.player.getInventory().getItem(0).copy();
            var contents = atlas.getOrDefault(net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS,
                    net.minecraft.world.item.component.BundleContents.EMPTY);
            check(contents.size() == 15, "Expected all 15 real maps before full-screen case");
            var entries = new ArrayList<net.minecraft.world.item.ItemStackTemplate>();
            for (var template : contents.items()) {
                var map = template.create();
                if (caseNumber < 6) map.remove(me.pajic.mapstitch.component.ModDataComponents.MAP_CENTER);
                else map.set(me.pajic.mapstitch.component.ModDataComponents.MAP_CENTER, new org.joml.Vector2i(765432, -765432));
                entries.add(net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(map));
            }
            atlas.set(net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS,
                    new net.minecraft.world.item.component.BundleContents(entries));
            mc.player.getInventory().setItem(0, atlas);
            var screen = new WorldMapScreen(record.get("scale").getAsInt()); mc.gui.setScreen(screen);
            write("dimensionId", null, Identifier.parse(record.get("dimension").getAsString()));
            write("follow", screen, false); write("zoomLevel", null, 0);
            write("camX", screen, (double)record.get("x").getAsInt());
            write("camZ", screen, (double)record.get("z").getAsInt());
            WorldMapScreen.clearMaps(); return false;
        }
        if (!fullscreenEjectRequested) {
            if (++fullscreenTicks < 20) return false;
            var screen = (WorldMapScreen)mc.gui.screen();
            var maps = (Map<?, ?>)read("MAPS", null);
            var expectedGrid = screen.worldToGrid(record.get("x").getAsInt(), record.get("z").getAsInt(), record.get("scale").getAsInt());
            var indexed = maps.get(expectedGrid);
            check(indexed != null, "Full-screen map missing at authoritative grid for " + record + ", item center " + (caseNumber < 6 ? "missing" : "stale"));
            var method = indexed.getClass().getDeclaredMethod("id"); method.setAccessible(true);
            check(((MapId)method.invoke(indexed)).id() == id, "Full-screen indexed wrong map ID");
            check(maps.size() == 5, "Full-screen cache mixed dimensions: " + maps.size());
            check(!rendered.isEmpty(), "Full-screen authoritative tile was not rendered");
            for (String observed : rendered) check(observed.equals(record.get("dimension").getAsString() + "/" + record.get("scale").getAsInt()), "Wrong full-screen tile " + observed);
            int mouseX = mc.getWindow().getGuiScaledWidth() / 2, mouseY = mc.getWindow().getGuiScaledHeight() / 2;
            write("mouseX", screen, mouseX); write("mouseY", screen, mouseY);
            check(screen.screenToGrid(mouseX, mouseY, record.get("scale").getAsInt()).equals(expectedGrid), "Negative-grid cursor disagrees with tile grid");
            Files.writeString(control.resolve("eject-request.json"), record.toString());
            net.minecraft.client.Screenshot.grab(mc.gameDirectory, "worldmap-center-case-" + caseNumber + ".png", mc.gameRenderer.mainRenderTarget(), 1, message -> {});
            check(screen.keyPressed(new net.minecraft.client.input.KeyEvent(com.mojang.blaze3d.platform.InputConstants.KEY_Q, 0,
                    com.mojang.blaze3d.platform.InputConstants.MOD_CONTROL)), "Original Ctrl+Q handler rejected key");
            check(ejected.equals(List.of(id)), "Actual Ctrl+Q sent wrong map ID(s): " + ejected + " expected " + id);
            fullscreenEjectRequested = true; fullscreenTicks = 0; return false;
        }
        var response = control.resolve("eject-observed.json");
        if (!Files.exists(response)) { check(++fullscreenTicks < 120, "Server did not eject requested map " + id); return false; }
        var acknowledgement = JsonParser.parseString(Files.readString(response)).getAsJsonObject(); Files.delete(response);
        check(acknowledgement.get("id").getAsInt() == id && acknowledgement.get("dropped_count").getAsInt() == 1
                && acknowledgement.get("removed_from_atlas").getAsBoolean() && acknowledgement.get("saved_map_retained").getAsBoolean(), "Wrong real Ctrl+Q server result " + acknowledgement);
        var row = new JsonObject(); row.addProperty("case", "worldmap " + (caseNumber < 6 ? "missing" : "stale") + " item center Ctrl+Q " + caseNumber);
        row.addProperty("id", id); row.addProperty("dimension", record.get("dimension").getAsString()); row.addProperty("scale", record.get("scale").getAsInt());
        row.addProperty("center_x", record.get("x").getAsInt()); row.addProperty("center_z", record.get("z").getAsInt()); row.addProperty("render_calls", rendered.size());
        row.addProperty("actual_ctrl_q_payload_id", ejected.getFirst()); row.add("server_drop", acknowledgement); observations.add(row);
        Files.writeString(control.resolve("observations.json"), new GsonBuilder().setPrettyPrinting().create().toJson(observations));
        mc.gui.setScreen(null); Files.writeString(control.resolve("restore-atlas"), "restore");
        fullscreenReady = false; fullscreenEjectRequested = false; fullscreenTicks = 0;
        if (++fullscreenCase == (smoke ? 2 : 12)) { fullscreenComplete = true; ticks = 0; }
        return false;
    }
    private boolean sourceRegression(Minecraft mc) throws Exception {
        if (sourcesComplete) return true;
        if (!sourceReady) {
            if (++sourceTicks < 30) return false;
            if (sourceAtlas == null) sourceAtlas = mc.player.getInventory().getItem(0).copy();
            sourceReady = true; sourceTicks = 0; rendered.clear(); worldMessages.clear();
            var empty = new net.minecraft.world.item.ItemStack(me.pajic.mapstitch.item.ModItems.ATLAS);
            mc.player.getInventory().setItem(0, sourceCase == 1 || sourceCase == 3 ? empty.copy() : sourceAtlas.copy());
            mc.player.getInventory().setItem(3, empty.copy());
            mc.player.getInventory().setItem(12, net.minecraft.world.item.ItemStack.EMPTY);
            if (sourceCase >= 2) {
                var pouchAtlas = sourceCase == 2 ? empty.copy() : sourceAtlas.copy();
                if (sourceCase >= 3) {
                    pouchAtlas.set(me.pajic.mapstitch.component.ModDataComponents.ATLAS_SCALE, 4);
                    pouchAtlas.set(me.pajic.mapstitch.component.ModDataComponents.ATLAS_ACTIVE_MAP_ID, records.get(4).getAsJsonObject().get("id").getAsInt());
                }
                var pouch = new net.minecraft.world.item.ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
                pouch.set(net.minecraft.core.component.DataComponents.CONTAINER,
                        net.minecraft.world.item.component.ItemContainerContents.fromItems(List.of(pouchAtlas)));
                mc.player.getInventory().setItem(12, pouch);
                check(com.thenathe.toolpouchcompat.AtlasBridge.atlases(mc.player).size() == 1, "Active pouch atlas not resolved");
            }
            // Reuse the same screen for filled -> empty-only, exercising frame reset.
            if (!(mc.gui.screen() instanceof WorldMapScreen)) mc.gui.setScreen(new WorldMapScreen(0));
            var screen = mc.gui.screen(); var record = records.get(0).getAsJsonObject();
            write("dimensionId", null, Identifier.parse("minecraft:overworld")); write("scale", screen, 0);
            write("follow", screen, false); write("zoomLevel", null, 0);
            write("camX", screen, (double)record.get("x").getAsInt()); write("camZ", screen, (double)record.get("z").getAsInt());
            WorldMapScreen.clearMaps(); return false;
        }
        if (++sourceTicks < 20) return false;
        var maps = (Map<?, ?>)read("MAPS", null);
        if (sourceCase == 1) {
            check(rendered.isEmpty() && maps.isEmpty() && worldMessages.contains("mapstitch.gui.worldmap.no_map_sources"),
                    "Empty-only scene retained previous source/frame state: maps=" + maps.size() + " messages=" + worldMessages);
        } else {
            check(!rendered.isEmpty() && maps.size() == 5 && !worldMessages.contains("mapstitch.gui.worldmap.no_map_sources"),
                    "Later empty atlas hid real map sources in scene " + sourceCase + " messages=" + worldMessages);
        }
        if (sourceCase >= 3) {
            var selected = me.pajic.mapstitch.util.ModClientUtil.getFirstItem(mc, me.pajic.mapstitch.item.ModItems.ATLAS);
            check(selected.getOrDefault(me.pajic.mapstitch.component.ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1)
                    == records.get(4).getAsJsonObject().get("id").getAsInt(), "Inventory atlas replaced active pouch minimap priority");
        }
        var row = new JsonObject(); row.addProperty("case", List.of("filled then empty inventory atlas", "empty-only after filled frame",
                "filled inventory then empty pouch atlas", "filled pouch with empty inventory atlases", "pouch minimap priority with filled inventory atlas").get(sourceCase));
        row.addProperty("render_calls", rendered.size()); row.addProperty("cache_maps", maps.size()); row.addProperty("no_sources_message", worldMessages.contains("mapstitch.gui.worldmap.no_map_sources"));
        row.addProperty("pouch_priority_preserved", sourceCase >= 3); observations.add(row);
        net.minecraft.client.Screenshot.grab(mc.gameDirectory, "worldmap-source-case-" + sourceCase + ".png", mc.gameRenderer.mainRenderTarget(), 1, message -> {});
        sourceReady = false; sourceTicks = 0;
        if (++sourceCase == 5) {
            sourcesComplete = true; mc.gui.setScreen(null);
            mc.player.getInventory().setItem(3, net.minecraft.world.item.ItemStack.EMPTY); mc.player.getInventory().setItem(12, net.minecraft.world.item.ItemStack.EMPTY);
            Files.writeString(control.resolve("restore-atlas"), "restore"); ticks = 0;
        }
        return false;
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
                    if (!minimapComplete) {
                        if (minimapCase == 0 && !minimapSetup && ++ticks < 30) return;
                        verifyMetadata(mc);
                        if (!minimapRegression(mc)) return;
                    }
                    if (++ticks < 30) return;
                    if (!repairNetworkRegression(mc)) return;
                    if (!fullscreenRegression(mc)) return;
                    if (!sourceRegression(mc)) return;
                    verifyMetadata(mc); repairRegression(mc); verifyMinimapMarkers(mc);
                    if (Boolean.getBoolean("maps.qa.atlasSmoke")) {
                        mc.gui.setScreen(null); done = true;
                        Files.writeString(control.resolve("observations.json"), new GsonBuilder().setPrettyPrinting().create().toJson(observations));
                        Files.writeString(control.resolve("result.txt"), "PASS final atlas smoke: four minimap regressions, /atlas fix restores full pixels for 15 maps, two negative-grid full-screen Ctrl+Q payload/drop cases across dimensions, five inventory/pouch source cases, metadata pixels/banners preserved. Broader 21 views and two travels retained as candidate evidence, not rerun.\n");
                        return;
                    }
                    open(mc); return;
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
                    Files.writeString(control.resolve("result.txt"), "PASS four minimap regressions, /atlas fix restores all pixels of 15 maps, 12 full-screen missing/stale-center Ctrl+Q payload and real drop cases including negative grids, five multiple-atlas/pouch source and priority cases, native metadata, old pixels/banners, 21 dimension/scale render views, two real dimension travels\n"); return;
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
