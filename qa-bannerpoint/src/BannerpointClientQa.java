package qa.bannerpoint;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

public final class BannerpointClientQa implements ClientModInitializer {
    public static final List<String> rendered = new ArrayList<>();
    private final Path control = Path.of(System.getProperty("banner.qa.control"));
    private int ticks, quietTicks;
    private boolean done;
    public static void render(Identifier sprite, int tint) {
        if (sprite.getNamespace().equals("bannerpoint") && rendered.size() < 100) rendered.add(sprite + " tint=" + Integer.toHexString(tint));
    }
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null || mc.level == null || done) return;
            try {
                var name = mc.player.getGameProfile().name(); ticks++;
                quietTicks = mc.gui.overlay() == null && mc.gui.screen() == null ? quietTicks + 1 : 0;
                var action = control.resolve(name + "-client-action.txt");
                if (Files.exists(action)) {
                    var text = Files.readString(action).trim(); Files.delete(action);
                    if (text.equals("allow")) { mc.getDownloadedPackSource().allowServerPacks(); mc.gui.setScreen(null); }
                    if (text.equals("reject")) mc.getDownloadedPackSource().rejectServerPacks();
                }
                if (ticks % 10 != 0) return;
                var row = new JsonObject(); row.addProperty("ticks", ticks);
                row.addProperty("overlay_visible", mc.gui.overlay() != null);
                row.addProperty("quiet_hud_ticks", quietTicks);
                var waypoints = new JsonArray();
                mc.getConnection().getWaypointManager().forEachWaypoint(mc.player, waypoint -> {
                    var observed = new JsonObject(); observed.addProperty("id", waypoint.id().toString());
                    observed.addProperty("style", waypoint.icon().style.identifier().toString());
                    observed.addProperty("color", waypoint.icon().color.orElse(0)); waypoints.add(observed);
                });
                row.add("waypoints", waypoints);
                var resources = new JsonArray();
                for (int i = 0; i < 4; i++) {
                    var id = Identifier.parse("bannerpoint:hud/locator_bar_dot/banner_" + i);
                    var sprite = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.GUI).getSprite(id);
                    var observed = new JsonObject(); observed.addProperty("requested", id.toString());
                    observed.addProperty("resolved", sprite.contents().name().toString());
                    observed.addProperty("width", sprite.contents().width()); observed.addProperty("height", sprite.contents().height()); resources.add(observed);
                }
                row.add("sprites", resources); row.add("rendered", new Gson().toJsonTree(rendered));
                row.addProperty("pack_style_resource", mc.getResourceManager().getResource(Identifier.parse("bannerpoint:waypoint_style/banner.json")).isPresent());
                Files.writeString(control.resolve(name + "-client.json"), new GsonBuilder().setPrettyPrinting().create().toJson(row));
                var screenshot = control.resolve(name + "-screenshot.txt");
                if (Files.exists(screenshot) && quietTicks >= 20) {
                    var file = Files.readString(screenshot).trim(); Files.delete(screenshot);
                    net.minecraft.client.Screenshot.grab(mc.gameDirectory, file, mc.gameRenderer.mainRenderTarget(), 1, message -> {});
                }
            } catch (Throwable error) {
                done = true;
                try { Files.writeString(control.resolve("failure"), error.toString()); } catch (Exception ignored) {}
                error.printStackTrace();
            }
        });
    }
}
