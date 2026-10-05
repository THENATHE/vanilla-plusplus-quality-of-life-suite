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
    String[] names={"same-left","same-right","opposite-sides","larger-map-with-info","map-hidden","map-restored","atlas-removed","atlas-restored","bottom-left","settings"};
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> HudElementRegistry.addFirst(Identifier.parse("suite_hud_qa:reset"),(g,d)->HudObservation.details.clear()));
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if(done)return;
            if(Boolean.getBoolean("hud.qa.settingsOnly")) {
                try {
                    if(++ticks==30){phase=9;configure(c);}
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
                if(phase==6||phase==7) {
                    boolean present=ToolPouchUtil.toolPouchHasItem(c.player,s->s.is(me.pajic.mapstitch.item.ModItems.ATLAS));
                    if(present!=(phase==7)){if(ticks>200)throw new AssertionError("atlas update timeout");return;}
                }
                if(phase==9) {
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
        if(phase==9) {
            var sets=com.thenathe.suite.client.SuiteSettings.collectConfigs();
            for(String id:new String[]{"simple_smithing_overhaul:config","mapstitch:config","toolpouch:config","tiered_backpacks:config","misctweaks:config","simple_death_improvements:config"}) {
                String key=id.replace(':','.');if(!sets.containsKey(key))throw new AssertionError("config missing "+key+" found="+sets.keySet());
            }
            c.gui.setScreen(com.thenathe.suite.client.SuiteSettings.create(null));return;
        }
        boolean right=phase==1||phase==2;
        MapStitchClient.CONFIG.minimap.position.accept(phase==8?MinimapPosition.BOTTOM_LEFT:right?MinimapPosition.TOP_RIGHT:MinimapPosition.TOP_LEFT);
        ToolPouchClient.CONFIG.infoOverlaySettings.position.accept(phase==8?OverlayPosition.BOTTOM_LEFT:phase==1?OverlayPosition.TOP_RIGHT:OverlayPosition.TOP_LEFT);
        MapStitchClient.CONFIG.minimap.size.accept(phase==3?23:15);
        MapStitchClient.CONFIG.minimap.xOffset.accept(0);MapStitchClient.CONFIG.minimap.yOffset.accept(0);
        MapStitchClient.CONFIG.minimapInfo.coordinates.accept(phase==3);
        MapStitchClient.CONFIG.minimapInfo.gameTime.accept(phase==3);
        MapStitchClient.CONFIG.minimapInfo.weather.accept(phase==3);
        MapStitchClient.CONFIG.minimapInfo.realTime.accept(false);
        ToolPouchClient.CONFIG.infoOverlaySettings.offsetX.accept(0);ToolPouchClient.CONFIG.infoOverlaySettings.offsetY.accept(0);
        if(phase==4||phase==5)MinimapOverlay.toggle();
        if(phase==6||phase==7)Files.writeString(control.resolve("server-command"),phase==6?"remove":"restore");
    }
    Object value(String name)throws Exception{Field f=Class.forName("com.thenathe.toolpouchcompat.HudLayout").getDeclaredField(name);f.setAccessible(true);return f.get(null);}
    void inspect(Minecraft c)throws Exception {
        if(HudObservation.details.isEmpty())throw new AssertionError("no original Tool Pouch detail text");
        int top=HudObservation.details.stream().mapToInt(r->r[1]).min().orElseThrow();
        int bottom=HudObservation.details.stream().mapToInt(r->r[1]+r[3]).max().orElseThrow();
        boolean shown=(boolean)value("mapShown");
        float y=(float)value("mapOriginY"),scale=(float)value("mapScaleY");
        float mapTop=y+scale*(float)value("mapLocalTop"),mapBottom=y+scale*(float)value("mapLocalBottom");
        if(phase==4||phase==6){if(shown||top!=4)throw new AssertionError("hidden/no atlas should restore baseline: shown="+shown+" top="+top);}
        else if(phase==2){if(!shown||top!=4)throw new AssertionError("opposite sides changed original placement");}
        else {if(!shown)throw new AssertionError("minimap did not render");if(!(top>=Math.ceil(mapBottom)+4||bottom<=Math.floor(mapTop)-4))throw new AssertionError("overlap: details="+top+".."+bottom+" minimap="+mapTop+".."+mapBottom);}
        var row=new JsonObject();row.addProperty("case",names[phase]);row.addProperty("map_shown",shown);row.addProperty("map_top",mapTop);row.addProperty("map_bottom",mapBottom);row.addProperty("details_top",top);row.addProperty("details_bottom",bottom);row.addProperty("detail_lines",HudObservation.details.size());row.addProperty("screen_height",c.getWindow().getGuiScaledHeight());row.addProperty("passed",true);observations.add(row);
    }
}
