package suitehudqa;

import java.nio.file.*;
import java.lang.reflect.Field;
import java.util.regex.Pattern;
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

/** Rendering acceptance using real native Apply callbacks and saved config files. */
public final class HudClientQa implements ClientModInitializer {
    final Path control=Path.of(System.getProperty("hud.qa.control"));
    final JsonArray observations=new JsonArray();
    final String[] names={"startup-atlas-right-info-left","ordinary-map-right-info-left","info-apply-bottom-right",
        "ordinary-map-same-top-left","atlas-same-top-left","map-apply-right-info-left",
        "atlas-top-left-info-bottom-left","ordinary-map-same-bottom-left","ordinary-map-signed-offset",
        "ordinary-map-separated-same-corner","settings"};
    final boolean[] atlasScene={true,false,false,false,true,true,true,false,false,false};
    final String[] mapCorners={"TOP_RIGHT","TOP_RIGHT","TOP_RIGHT","TOP_LEFT","TOP_LEFT","TOP_RIGHT","TOP_LEFT","BOTTOM_LEFT","BOTTOM_LEFT","TOP_LEFT"};
    final String[] infoCorners={"TOP_LEFT","TOP_LEFT","BOTTOM_RIGHT","TOP_LEFT","TOP_LEFT","TOP_LEFT","BOTTOM_LEFT","BOTTOM_LEFT","BOTTOM_LEFT","TOP_LEFT"};
    final int[] mapXs={5,5,5,0,0,5,0,0,-2,0},mapYs={7,7,7,0,0,7,0,0,-3,0};
    final int[] infoXs={11,11,17,17,17,17,11,11,11,11},infoYs={13,13,19,19,19,19,13,13,13,210};
    int phase,ticks;boolean configured,done;
    float previousX,previousY;

    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> HudElementRegistry.addFirst(Identifier.parse("suite_hud_qa:reset"),(g,d)->HudObservation.details.clear()));
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if(done)return;
            if(Boolean.getBoolean("hud.qa.settingsOnly")) {
                try {
                    if(++ticks==30){phase=10;configure(c);}
                    if(ticks==60){
                        if(c.gui.screen()==null)throw new AssertionError("combined settings did not open");
                        Screenshot.grab(c.gameDirectory,"settings.png",c.gameRenderer.mainRenderTarget(),1,message->{});
                        Files.writeString(control.resolve("result.txt"),"PASS settings screen="+c.gui.screen().getClass().getName()+"\n");done=true;
                    }
                }catch(Throwable error){fail(error,"settings-only");}
                return;
            }
            if(c.player==null||c.level==null)return;
            try {
                if(!Files.exists(control.resolve("seeded")))return;
                if(!configured){configure(c);configured=true;ticks=0;return;}
                if(++ticks<30)return;
                if(phase<10) {
                    boolean present=ToolPouchUtil.toolPouchHasItem(c.player,s->s.is(me.pajic.mapstitch.item.ModItems.ATLAS));
                    if(present!=atlasScene[phase]){if(ticks>200)throw new AssertionError("map source update timeout");return;}
                    inspect(c);
                } else {
                    if(c.gui.screen()==null)throw new AssertionError("combined settings did not open");
                    var row=new JsonObject();row.addProperty("case",names[phase]);row.addProperty("screen_class",c.gui.screen().getClass().getName());row.addProperty("passed",true);observations.add(row);
                }
                Screenshot.grab(c.gameDirectory,names[phase]+".png",c.gameRenderer.mainRenderTarget(),1,message->{});
                Files.writeString(control.resolve("observations.json"),new GsonBuilder().setPrettyPrinting().create().toJson(observations));
                if(++phase==names.length){done=true;Files.writeString(control.resolve("result.txt"),"PASS cases="+names.length+"\n");}else configured=false;
            }catch(Throwable error){fail(error,"client phase="+phase);}
        });
    }
    void fail(Throwable error,String stage){done=true;error.printStackTrace();try{Files.writeString(control.resolve("failure"),stage+": "+error);}catch(Exception ignored){}}
    String mapState(){var m=MapStitchClient.CONFIG.minimap;return m.position.get()+":"+m.xOffset.get()+":"+m.yOffset.get();}
    String infoState(){var i=ToolPouchClient.CONFIG.infoOverlaySettings;return i.position.get()+":"+i.offsetX.get()+":"+i.offsetY.get();}
    void applyInfo(OverlayPosition position,int x,int y){
        String before=mapState();var info=ToolPouchClient.CONFIG.infoOverlaySettings;
        info.position.accept(position);info.offsetX.accept(x);info.offsetY.accept(y);
        ToolPouchClient.CONFIG.save();ToolPouchClient.CONFIG.onUpdateClient();
        if(!before.equals(mapState()))throw new AssertionError("info Apply moved map: "+before+" -> "+mapState());
    }
    void applyAtlas(MinimapPosition position,int x,int y){
        String before=infoState();var map=MapStitchClient.CONFIG.minimap;
        map.position.accept(position);map.xOffset.accept(x);map.yOffset.accept(y);
        MapStitchClient.CONFIG.save();MapStitchClient.CONFIG.onUpdateClient();
        if(!before.equals(infoState()))throw new AssertionError("map Apply moved info: "+before+" -> "+infoState());
    }
    void applyPouchMap(OverlayPosition position,int x,int y){
        String before=infoState();var map=ToolPouchClient.CONFIG.minimapOverlaySettings;
        map.position.accept(position);map.offsetX.accept(x);map.offsetY.accept(y);
        ToolPouchClient.CONFIG.save();ToolPouchClient.CONFIG.onUpdateClient();
        if(!before.equals(infoState()))throw new AssertionError("pouch map Apply moved info: "+before+" -> "+infoState());
    }
    void configure(Minecraft c)throws Exception {
        if(phase==10) {
            var sets=com.thenathe.suite.client.SuiteSettings.collectConfigs();
            for(String id:new String[]{"simple_smithing_overhaul:config-v2","mapstitch:config","toolpouch:config","tiered_backpacks:config","misctweaks:config","simple_death_improvements:config"}) {
                String key=id.replace(':','.');if(!sets.containsKey(key))throw new AssertionError("config missing "+key+" found="+sets.keySet());
            }
            c.gui.setScreen(com.thenathe.suite.client.SuiteSettings.create(null));return;
        }
        switch(phase) {
            // Phase zero changes nothing: first startup begins with conflicting native map files.
            case 2 -> applyInfo(OverlayPosition.BOTTOM_RIGHT,17,19);
            case 3 -> {applyInfo(OverlayPosition.TOP_LEFT,17,19);applyPouchMap(OverlayPosition.TOP_LEFT,0,0);}
            case 5 -> applyAtlas(MinimapPosition.TOP_RIGHT,5,7);
            case 6 -> {applyInfo(OverlayPosition.BOTTOM_LEFT,11,13);applyAtlas(MinimapPosition.TOP_LEFT,0,0);}
            case 7 -> applyAtlas(MinimapPosition.BOTTOM_LEFT,0,0);
            case 8 -> applyAtlas(MinimapPosition.BOTTOM_LEFT,-2,-3);
            case 9 -> {applyInfo(OverlayPosition.TOP_LEFT,11,210);applyAtlas(MinimapPosition.TOP_LEFT,0,0);}
        }
        Files.writeString(control.resolve("server-command"),atlasScene[phase]?"atlas":"map");
    }
    Object value(String name)throws Exception{Field f=Class.forName("com.thenathe.toolpouchcompat.HudLayout").getDeclaredField(name);f.setAccessible(true);return f.get(null);}
    void field(String text,String section,String key,String expected){
        var block=Pattern.compile("(?ms)^\\["+Pattern.quote(section)+"\\]\\s*\\n(.*?)(?=^\\[|\\z)").matcher(text);
        if(!block.find())throw new AssertionError("missing saved section "+section);
        var setting=Pattern.compile("(?m)^"+Pattern.quote(key)+"\\s*=\\s*(.*?)\\s*$").matcher(block.group(1));
        if(!setting.find()||!setting.group(1).equals(expected))throw new AssertionError("saved "+section+"."+key+" expected "+expected);
    }
    void inspect(Minecraft c)throws Exception {
        if(HudObservation.details.isEmpty())throw new AssertionError("no original Tool Pouch detail text");
        int left=HudObservation.details.stream().mapToInt(r->r[0]).min().orElseThrow();
        int right=HudObservation.details.stream().mapToInt(r->r[0]+r[2]).max().orElseThrow();
        int top=HudObservation.details.stream().mapToInt(r->r[1]).min().orElseThrow();
        int bottom=HudObservation.details.stream().mapToInt(r->r[1]+r[3]).max().orElseThrow();
        boolean shown=(boolean)value("mapShown");
        float x=(float)value("mapOriginX"),y=(float)value("mapOriginY"),scale=(float)value("mapScaleY");
        float mapTop=y+scale*(float)value("mapLocalTop"),mapBottom=y+scale*(float)value("mapLocalBottom");
        if(!shown)throw new AssertionError("minimap did not render");
        String mapCorner=mapCorners[phase],infoCorner=infoCorners[phase];
        var map=MapStitchClient.CONFIG.minimap;var pouch=ToolPouchClient.CONFIG.minimapOverlaySettings;var info=ToolPouchClient.CONFIG.infoOverlaySettings;
        if(!map.position.get().name().equals(mapCorner)||!pouch.position.get().name().equals(mapCorner)||!info.position.get().name().equals(infoCorner))throw new AssertionError("independent corner mismatch: map="+mapState()+" pouch="+pouch.position.get()+" info="+infoState());
        if(map.xOffset.get()!=mapXs[phase]||map.yOffset.get()!=mapYs[phase]||info.offsetX.get()!=infoXs[phase]||info.offsetY.get()!=infoYs[phase])throw new AssertionError("independent offsets changed: map="+mapState()+" info="+infoState());
        boolean rightMap=mapCorner.endsWith("RIGHT"),rightInfo=infoCorner.endsWith("RIGHT");
        if(rightMap?x<=c.getWindow().getGuiScaledWidth()/2:x>=c.getWindow().getGuiScaledWidth()/2)throw new AssertionError("minimap rendered on wrong half");
        if(rightMap!=rightInfo) {
            if(rightMap ? right>=c.getWindow().getGuiScaledWidth()/2 : left<=c.getWindow().getGuiScaledWidth()/2)throw new AssertionError("details on wrong half");
        } else if(!(top>=Math.ceil(mapBottom)+4||bottom<=Math.floor(mapTop)-4))throw new AssertionError("overlap: details="+top+".."+bottom+" map="+mapTop+".."+mapBottom);
        // Separated scenes retain exact upstream corner/offset placement.
        boolean shifted=phase==3||phase==4||phase==7||phase==8;
        if(!shifted) {
            int expectedTop=infoCorner.startsWith("BOTTOM")?c.getWindow().getGuiScaledHeight()-12-info.offsetY.get()-12*(HudObservation.details.size()-1):4+info.offsetY.get();
            if(top!=expectedTop)throw new AssertionError("nonoverlapping details moved: "+top+" expected "+expectedTop);
        }
        int expectedEdge=rightInfo?c.getWindow().getGuiScaledWidth()-4-info.offsetX.get():4+info.offsetX.get();
        if((rightInfo?right:left)!=expectedEdge)throw new AssertionError("info x offset or alignment changed");
        String source=(String)value("mapSource");
        if(!source.equals(atlasScene[phase]?"atlas":"map"))throw new AssertionError("wrong minimap source "+source);
        if((phase==1||phase==4)&&(x!=previousX||y!=previousY))throw new AssertionError("ordinary map and atlas corner anchors differ");
        if(phase==8&&(x!=previousX-2||y!=previousY+3||map.xOffset.get()!=-2||map.yOffset.get()!=-3))throw new AssertionError("signed atlas offsets lost in ordinary minimap");
        String mapFile=Files.readString(c.gameDirectory.toPath().resolve("config/mapstitch/client_config.toml"));
        String pouchFile=Files.readString(c.gameDirectory.toPath().resolve("config/toolpouch/client_config.toml"));
        field(mapFile,"minimap","position","\""+mapCorner+"\"");field(mapFile,"minimap","xOffset",""+map.xOffset.get());field(mapFile,"minimap","yOffset",""+map.yOffset.get());
        field(pouchFile,"minimapOverlaySettings","position","\""+mapCorner+"\"");field(pouchFile,"minimapOverlaySettings","offsetX",""+Math.max(0,map.xOffset.get()));field(pouchFile,"minimapOverlaySettings","offsetY",""+Math.max(0,map.yOffset.get()));
        field(pouchFile,"infoOverlaySettings","position","\""+infoCorner+"\"");field(pouchFile,"infoOverlaySettings","offsetX",""+info.offsetX.get());field(pouchFile,"infoOverlaySettings","offsetY",""+info.offsetY.get());
        var row=new JsonObject();row.addProperty("case",names[phase]);row.addProperty("map_shown",shown);row.addProperty("map_source",source);row.addProperty("map_x",x);row.addProperty("map_y",y);row.addProperty("map_corner",mapCorner);row.addProperty("info_corner",infoCorner);row.addProperty("info_offset_x",info.offsetX.get());row.addProperty("info_offset_y",info.offsetY.get());row.addProperty("native_files_persisted",true);row.addProperty("map_top",mapTop);row.addProperty("map_bottom",mapBottom);row.addProperty("details_left",left);row.addProperty("details_right",right);row.addProperty("details_top",top);row.addProperty("details_bottom",bottom);row.addProperty("details_repositioned_for_overlap",shifted);row.addProperty("detail_lines",HudObservation.details.size());row.addProperty("screen_height",c.getWindow().getGuiScaledHeight());row.addProperty("passed",true);observations.add(row);
        previousX=x;previousY=y;
    }
}
