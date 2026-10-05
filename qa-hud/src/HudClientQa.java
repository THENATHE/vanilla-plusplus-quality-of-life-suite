package suitehudqa;

import java.nio.file.*;
import java.lang.reflect.Field;
import com.google.gson.*;
import me.pajic.mapstitch.MapStitchClient;
import me.pajic.mapstitch.minimap.*;
import me.pajic.toolpouch.ToolPouchClient;
import me.pajic.toolpouch.hud.OverlayPosition;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.resources.Identifier;

public final class HudClientQa implements ClientModInitializer {
    final Path control=Path.of(System.getProperty("hud.qa.control"));
    final JsonArray observations=new JsonArray();
    int phase,ticks;boolean configured,done;
    String[] names={"atlas-left","atlas-right-mapstitch-setting","atlas-left-pouch-setting","ordinary-map-left","atlas-bottom-left","ordinary-map-bottom-left","ordinary-map-signed-offset","settings"};
    float leftX,leftY,bottomX,bottomY;
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> HudElementRegistry.addFirst(Identifier.parse("suite_hud_qa:reset"),(g,d)->HudObservation.details.clear()));
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if(done)return;
            if(Boolean.getBoolean("hud.qa.settingsOnly")) {
                try {
                    if(++ticks==30){phase=7;configure(c);}
                    if(ticks==60){
                        if(c.gui.screen()==null)throw new AssertionError("combined settings did not open");
                        Screenshot.grab(c.gameDirectory,"settings.png",c.gameRenderer.mainRenderTarget(),1,message->{});
                        Files.writeString(control.resolve("result.txt"),"PASS settings screen="+c.gui.screen().getClass().getName()+"\n");done=true;
                    }
                }catch(Throwable error){done=true;error.printStackTrace();try{Files.writeString(control.resolve("failure"),"settings-only: "+error);}catch(Exception ignored){}}
                return;
            }
            if(c.player==null||c.level==null)return;
            try {
                if(!Files.exists(control.resolve("seeded")))return;
                if(!configured){configure(c);configured=true;ticks=0;return;}
                if(++ticks<30)return;
                if(phase>=3 && phase<=6) {
                    boolean present=ToolPouchUtil.toolPouchHasItem(c.player,s->s.is(me.pajic.mapstitch.item.ModItems.ATLAS));
                    if(present!=(phase==4)){if(ticks>200)throw new AssertionError("atlas update timeout");return;}
                }
                if(phase==7) {
                    if(c.gui.screen()==null)throw new AssertionError("combined settings did not open");
                    var row=new JsonObject();row.addProperty("case",names[phase]);row.addProperty("screen_class",c.gui.screen().getClass().getName());row.addProperty("passed",true);observations.add(row);
                } else inspect(c);
                Screenshot.grab(c.gameDirectory,names[phase]+".png",c.gameRenderer.mainRenderTarget(),1,message->{});
                Files.writeString(control.resolve("observations.json"),new GsonBuilder().setPrettyPrinting().create().toJson(observations));
                if(++phase==names.length){done=true;Files.writeString(control.resolve("result.txt"),"PASS cases="+names.length+"\n");}else configured=false;
            }catch(Throwable error){done=true;error.printStackTrace();try{Files.writeString(control.resolve("failure"),"client phase="+phase+": "+error);}catch(Exception ignored){}}
        });
    }
    void configure(Minecraft c)throws Exception {
        if(phase==7) {
            var sets=com.thenathe.suite.client.SuiteSettings.collectConfigs();
            for(String id:new String[]{"simple_smithing_overhaul:config-v2","mapstitch:config","toolpouch:config","tiered_backpacks:config","misctweaks:config","simple_death_improvements:config"}) {
                String key=id.replace(':','.');if(!sets.containsKey(key))throw new AssertionError("config missing "+key+" found="+sets.keySet());
            }
            c.gui.setScreen(com.thenathe.suite.client.SuiteSettings.create(null));return;
        }
        if(phase==0) {
            MapStitchClient.CONFIG.minimap.position.accept(MinimapPosition.TOP_LEFT);
            MapStitchClient.CONFIG.minimap.xOffset.accept(0);MapStitchClient.CONFIG.minimap.yOffset.accept(0);
            MapStitchClient.CONFIG.save();MapStitchClient.CONFIG.onUpdateClient();
        } else if(phase==1) {
            MapStitchClient.CONFIG.minimap.position.accept(MinimapPosition.TOP_RIGHT);
            MapStitchClient.CONFIG.save();MapStitchClient.CONFIG.onUpdateClient();
        } else if(phase==2) {
            ToolPouchClient.CONFIG.minimapOverlaySettings.position.accept(OverlayPosition.TOP_LEFT);
            ToolPouchClient.CONFIG.save();ToolPouchClient.CONFIG.onUpdateClient();
        } else if(phase==4) {
            ToolPouchClient.CONFIG.infoOverlaySettings.position.accept(OverlayPosition.BOTTOM_LEFT);
            ToolPouchClient.CONFIG.save();ToolPouchClient.CONFIG.onUpdateClient();
        } else if(phase==6) {
            MapStitchClient.CONFIG.minimap.xOffset.accept(-2);MapStitchClient.CONFIG.minimap.yOffset.accept(-3);
            MapStitchClient.CONFIG.save();MapStitchClient.CONFIG.onUpdateClient();
        }
        MapStitchClient.CONFIG.minimap.size.accept(19);
        ToolPouchClient.CONFIG.minimapOverlaySettings.size.accept(1F);
        MapStitchClient.CONFIG.minimapInfo.coordinates.accept(false);
        MapStitchClient.CONFIG.minimapInfo.gameTime.accept(false);
        MapStitchClient.CONFIG.minimapInfo.weather.accept(false);
        MapStitchClient.CONFIG.minimapInfo.realTime.accept(false);
        ToolPouchClient.CONFIG.infoOverlaySettings.offsetX.accept(0);ToolPouchClient.CONFIG.infoOverlaySettings.offsetY.accept(0);
        if(phase>=3 && phase<=6)Files.writeString(control.resolve("server-command"),phase==4?"atlas":"map");
    }
    Object value(String name)throws Exception{Field f=Class.forName("com.thenathe.toolpouchcompat.HudLayout").getDeclaredField(name);f.setAccessible(true);return f.get(null);}
    void inspect(Minecraft c)throws Exception {
        if(HudObservation.details.isEmpty())throw new AssertionError("no original Tool Pouch detail text");
        int top=HudObservation.details.stream().mapToInt(r->r[1]).min().orElseThrow();
        int bottom=HudObservation.details.stream().mapToInt(r->r[1]+r[3]).max().orElseThrow();
        boolean shown=(boolean)value("mapShown");
        float y=(float)value("mapOriginY"),scale=(float)value("mapScaleY");
        float mapTop=y+scale*(float)value("mapLocalTop"),mapBottom=y+scale*(float)value("mapLocalBottom");
        if(!shown)throw new AssertionError("minimap did not render");
        if(!(top>=Math.ceil(mapBottom)+4||bottom<=Math.floor(mapTop)-4))throw new AssertionError("overlap: details="+top+".."+bottom+" minimap="+mapTop+".."+mapBottom);
        String expectedCorner=phase>=4?"BOTTOM_LEFT":phase==1?"TOP_RIGHT":"TOP_LEFT";
        if(!MapStitchClient.CONFIG.minimap.position.get().name().equals(expectedCorner)
            || !ToolPouchClient.CONFIG.minimapOverlaySettings.position.get().name().equals(expectedCorner)
            || !ToolPouchClient.CONFIG.infoOverlaySettings.position.get().name().equals(expectedCorner))throw new AssertionError("native position controls did not synchronize");
        float x=(float)value("mapOriginX");
        String source=(String)value("mapSource");
        if(!source.equals(phase==3||phase==5||phase==6?"map":"atlas"))throw new AssertionError("wrong minimap source "+source);
        if(phase==2){leftX=x;leftY=y;}
        if(phase==3 && (x!=leftX || y!=leftY))throw new AssertionError("ordinary map and atlas top-left anchors differ");
        if(phase==4){bottomX=x;bottomY=y;}
        if(phase==5 && (x!=bottomX || y!=bottomY))throw new AssertionError("ordinary map and atlas bottom-left anchors differ");
        if(phase==6 && (x!=bottomX-2 || y!=bottomY+3 || MapStitchClient.CONFIG.minimap.xOffset.get()!=-2 || MapStitchClient.CONFIG.minimap.yOffset.get()!=-3))throw new AssertionError("signed atlas offsets lost in ordinary minimap");
        String mapFile=Files.readString(c.gameDirectory.toPath().resolve("config/mapstitch/client_config.toml"));
        String pouchFile=Files.readString(c.gameDirectory.toPath().resolve("config/toolpouch/client_config.toml"));
        if(!mapFile.contains(expectedCorner)||!pouchFile.contains(expectedCorner))throw new AssertionError("native synchronized corner not persisted");
        var row=new JsonObject();row.addProperty("case",names[phase]);row.addProperty("map_shown",shown);row.addProperty("map_source",source);row.addProperty("map_x",x);row.addProperty("shared_corner",expectedCorner);row.addProperty("synchronized_native_files",true);row.addProperty("map_top",mapTop);row.addProperty("map_bottom",mapBottom);row.addProperty("details_top",top);row.addProperty("details_bottom",bottom);row.addProperty("detail_lines",HudObservation.details.size());row.addProperty("screen_height",c.getWindow().getGuiScaledHeight());row.addProperty("passed",true);observations.add(row);
    }
}
