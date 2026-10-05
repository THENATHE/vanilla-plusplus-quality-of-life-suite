package suitesettingsqa;

import com.google.gson.*;
import com.thenathe.suite.client.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;
import me.fzzyhmstrs.fzzy_config.impl.ConfigSet;
import me.fzzyhmstrs.fzzy_config.registry.ClientConfigRegistry;
import me.fzzyhmstrs.fzzy_config.screen.internal.*;
import me.fzzyhmstrs.fzzy_config.screen.widget.DynamicListWidget;
import me.fzzyhmstrs.fzzy_config.screen.widget.custom.CustomButtonWidget;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.shedaniel.autoconfig.AutoConfig;
import de.dafuqs.chalk.config.ChalkConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.events.GuiEventListener;

public final class SettingsClientQa implements ClientModInitializer {
    final Path control = Path.of(System.getProperty("settings.qa.control"));
    final JsonArray observations = new JsonArray();
    Map<String, ConfigSet> configs;
    List<String> ids;
    int ticks, phase;
    boolean done;
    String opened;
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if(done) return;
            if(c.gui.overlay()!=null) return;
            if(!Boolean.getBoolean("settings.qa.standalone") && (c.player==null || c.level==null)) return;
            if(++ticks<20) return;
            ticks=0;
            try {
                if(configs==null) {
                    c.gui.setScreen(SuiteSettings.create(null));
                    configs=SuiteSettings.collectConfigs();
                    ids=new ArrayList<>(configs.keySet());
                    var expected=Set.of("simple_smithing_overhaul.config-v2","mapstitch.config","mapstitch.client_config","toolpouch.config","toolpouch.client_config","tiered_backpacks.config","misctweaks.config","misctweaks.client_config","simple_death_improvements.config","thenathe_mod_suite.overview","thenathe_mod_suite.chalk");
                    if(!configs.keySet().equals(expected)) throw new AssertionError("unexpected config keys="+configs.keySet());
                    for(var entry:configs.entrySet()) {
                        if(!entry.getValue().getActive().getId().toLanguageKey().equals(entry.getKey())) throw new AssertionError("identity changed "+entry.getKey());
                    }
                    return;
                }
                if(Boolean.getBoolean("settings.qa.visual")) {
                    if(phase==0) {
                        Screenshot.grab(c.gameDirectory,"suite-settings-overview.png",c.gameRenderer.mainRenderTarget(),1,m->{});
                        var expectedLabels=Map.of("thenathe_mod_suite.overview","Vanilla++ Quality of Life Suite","misctweaks.config","MiscTweaks Gameplay Settings","misctweaks.client_config","MiscTweaks Client Settings","tiered_backpacks.config","Mod Configuration","toolpouch.config","Tool Pouch Gameplay Settings","toolpouch.client_config","Tool Pouch Client Settings");
                        for(var label:expectedLabels.entrySet()) {
                            String actual=net.minecraft.network.chat.Component.translatable(label.getKey()).getString();
                            if(!actual.equals(label.getValue()))throw new AssertionError("effective language label "+label.getKey()+"="+actual);
                            row("effective label "+label.getKey()+"="+actual);
                        }
                        String tieredDescription=configs.get("tiered_backpacks.config").getActive().description(null).getString();
                        if(!tieredDescription.contains("Tiered Backpacks"))throw new AssertionError("Tiered module context missing");
                        row("Tiered native config context="+tieredDescription);
                        var screen=(ConfigScreen)c.gui.screen();
                        CustomButtonWidget navigation=null;
                        for(var child:screen.children())if(child instanceof CustomButtonWidget button && button.getY()>=screen.height-30 && (navigation==null || button.getX()<navigation.getX()))navigation=button;
                        if(navigation==null)throw new AssertionError("missing native navigation button");
                        navigation.onPress();phase++;return;
                    }
                    Screenshot.grab(c.gameDirectory,"suite-settings-sidebar.png",c.gameRenderer.mainRenderTarget(),1,m->{});
                    Files.writeString(control.resolve("observations.json"),new GsonBuilder().setPrettyPrinting().create().toJson(observations));
                    Files.writeString(control.resolve("result.txt"),"PASS branded overview, explicit module labels and rendered native sidebar\n");done=true;return;
                }
                if(phase < ids.size()*2) {
                    String key=ids.get(phase/2);
                    if(phase%2==0) {
                        if(phase==0) Screenshot.grab(c.gameDirectory,"suite-settings-overview.png",c.gameRenderer.mainRenderTarget(),1,m->{});
                        c.gui.setScreen(manager().provideScreen$fzzy_config(key));
                        opened=key;
                    } else {
                        ConfigScreen screen=(ConfigScreen)c.gui.screen();
                        DynamicListWidget list=list(screen);
                        if(list.children().isEmpty())throw new AssertionError("empty native config screen "+opened);
                        JsonObject row=new JsonObject();row.addProperty("config_id",opened);row.addProperty("rows",list.children().size());row.addProperty("client_only",configs.get(opened).getClientOnly());row.addProperty("screen_class",screen.getClass().getName());row.addProperty("passed",true);observations.add(row);
                        Screenshot.grab(c.gameDirectory,opened+".png",c.gameRenderer.mainRenderTarget(),1,m->{});
                    }
                    phase++;return;
                }
                int action=phase-ids.size()*2;
                if(action==0 || action==2) {
                    c.gui.setScreen(manager().provideScreen$fzzy_config("thenathe_mod_suite.chalk"));
                    toggle("thenathe_mod_suite.chalk","emitParticles",action==2);
                    manager().provideUpdateManager$fzzy_config("thenathe_mod_suite.chalk").apply(false);
                } else if(action==1 || action==3) {
                    boolean expected=action==3;
                    var holder=AutoConfig.getConfigHolder(ChalkConfig.class);
                    if(holder.getConfig().EmitParticles!=expected)throw new AssertionError("Chalk holder not updated");
                    if(!holder.load() || holder.getConfig().EmitParticles!=expected)throw new AssertionError("original Chalk file did not persist");
                    c.gui.setScreen(SuiteSettings.create(null));configs=SuiteSettings.collectConfigs();
                    ChalkSettings value=(ChalkSettings)configs.get("thenathe_mod_suite.chalk").getActive();
                    if(value.emitParticles.get()!=expected)throw new AssertionError("Chalk bridge reopen mismatch");
                    if(de.dafuqs.chalk.common.Chalk.CONFIG!=holder.getConfig() || de.dafuqs.chalk.common.Chalk.CONFIG.EmitParticles!=expected)throw new AssertionError("Chalk particle runtime is not the canonical saved object");
                    row("Chalk original AutoConfig saved/reloaded/reopened="+expected);
                } else if(action==4 || action==6) {
                    c.gui.setScreen(manager().provideScreen$fzzy_config("toolpouch.client_config"));
                    toggle("toolpouch.client_config","showUIHints",action==6);
                    manager().provideUpdateManager$fzzy_config("toolpouch.client_config").apply(false);
                } else if(action==5 || action==7) {
                    boolean expected=action==7;
                    Path file=c.gameDirectory.toPath().resolve("config/toolpouch/client_config.toml");
                    String text=Files.readString(file);
                    if(!text.contains("showUIHints = "+expected))throw new AssertionError("original Tool Pouch client file not saved "+text);
                    var loaded=me.fzzyhmstrs.fzzy_config.api.ConfigApiJava.readOrCreateAndValidate(me.pajic.toolpouch.config.ModClientConfig::new);
                    if(loaded.showUIHints.get()!=expected)throw new AssertionError("native Fzzy re-read did not preserve Tool Pouch value");
                    row("Tool Pouch native checkbox original file="+expected);
                } else if(!Boolean.getBoolean("settings.qa.standalone") && action==8) {
                    c.gui.setScreen(manager().provideScreen$fzzy_config("misctweaks.config"));
                    Method out=ConfigScreenManager.class.getDeclaredMethod("outOfGame");out.setAccessible(true);
                    Method outUpdate=ConfigSingleUpdateManager.class.getDeclaredMethod("outOfGame");outUpdate.setAccessible(true);
                    var update=manager().provideUpdateManager$fzzy_config("misctweaks.config");
                    var row=new JsonObject();row.addProperty("case","remote-server-library-context");row.addProperty("client_level_present",c.level!=null);row.addProperty("client_connection_present",c.getConnection()!=null);row.addProperty("is_local_server",c.isLocalServer());row.addProperty("screen_out_of_game",(boolean)out.invoke(manager()));row.addProperty("update_out_of_game",(boolean)outUpdate.invoke(update));row.addProperty("permission_level",me.fzzyhmstrs.fzzy_config.impl.ConfigApiImplClient.INSTANCE.getPlayerPermissionLevel$fzzy_config());observations.add(row);
                    var config=configs.get("misctweaks.config").getActive();
                    var value=(ValidatedBoolean)config.getClass().getField("preventShulkerDuplication").get(config);
                    row.addProperty("native_widget_enabled",nativeEnabled("misctweaks.config.preventShulkerDuplication"));
                    if(Boolean.getBoolean("settings.qa.guest")) {
                        var button=nativeButton("misctweaks.config.preventShulkerDuplication");
                        row.addProperty("native_protected_button",button.getMessage().getString());
                        if(!button.getMessage().getString().equals("Can't Edit"))throw new AssertionError("protected field did not use the native permission widget");
                        boolean before=value.get();button.onPress();update.apply(false);
                        if(value.get()!=before)throw new AssertionError("protected button changed the configuration");
                        row.addProperty("protected_button_changed_value",false);
                    } else {
                        toggle("misctweaks.config","preventShulkerDuplication",true);
                        update.apply(false);
                    }
                } else if(!Boolean.getBoolean("settings.qa.standalone") && action==9) {
                    String server=Files.readString(control.resolve("server-config.json"));
                    JsonObject state=JsonParser.parseString(server).getAsJsonObject();
                    var row=new JsonObject();row.addProperty("case","remote-server-original-config-routing");row.addProperty("server_preventShulkerDuplication",state.get("preventShulkerDuplication").getAsBoolean());row.addProperty("passed",state.get("preventShulkerDuplication").getAsBoolean()!=Boolean.getBoolean("settings.qa.guest"));observations.add(row);
                    Files.writeString(control.resolve("observations.json"),new GsonBuilder().setPrettyPrinting().create().toJson(observations));
                    if(state.get("preventShulkerDuplication").getAsBoolean()==Boolean.getBoolean("settings.qa.guest"))throw new AssertionError("native server config permission/routing mismatch");
                } else {
                    Files.writeString(control.resolve("observations.json"),new GsonBuilder().setPrettyPrinting().create().toJson(observations));
                    Files.writeString(control.resolve("result.txt"),"PASS all11 native config screens, Chalk false/true persistence, Tool Pouch original client file persistence\n");
                    done=true;return;
                }
                phase++;
            } catch(Throwable failure) {
                failure.printStackTrace();done=true;
                try {Files.writeString(control.resolve("failure"),"phase="+phase+" opened="+opened+": "+failure);Files.writeString(control.resolve("observations.json"),new GsonBuilder().setPrettyPrinting().create().toJson(observations));}catch(Exception ignored){}
            }
        });
    }
    void row(String text){JsonObject row=new JsonObject();row.addProperty("case",text);row.addProperty("passed",true);observations.add(row);}
    static ConfigScreenManager manager()throws Exception {Field field=SuiteSettings.class.getDeclaredField("currentManager");field.setAccessible(true);return(ConfigScreenManager)field.get(null);}
    static DynamicListWidget list(ConfigScreen screen)throws Exception {Field field=ConfigScreen.class.getDeclaredField("configList");field.setAccessible(true);return(DynamicListWidget)field.get(screen);}
    CustomButtonWidget nativeButton(String key)throws Exception {
        for(var entry:list((ConfigScreen)Minecraft.getInstance().gui.screen()).selectableEntries())if(entry.getScope().getScope().equals(key))for(var widget:entry.children())if(widget instanceof CustomButtonWidget button)return button;
        throw new AssertionError("missing native field widget "+key);
    }
    boolean nativeEnabled(String key)throws Exception {
        for(var entry:list((ConfigScreen)Minecraft.getInstance().gui.screen()).selectableEntries())if(entry.getScope().getScope().equals(key))for(var widget:entry.children())if(widget instanceof net.minecraft.client.gui.components.AbstractWidget button)return button.active;
        return false;
    }
    void toggle(String config,String field,boolean target)throws Exception {
        ValidatedBoolean value=(ValidatedBoolean)configs.get(config).getActive().getClass().getField(field).get(configs.get(config).getActive());
        if(value.get()==target)return;
        for(var entry:list((ConfigScreen)Minecraft.getInstance().gui.screen()).selectableEntries()) {
            if(!entry.getScope().getScope().equals(config+"."+field))continue;
            for(GuiEventListener widget:entry.children())if(widget instanceof CustomButtonWidget button && button.active){button.onPress();if(value.get()!=target)throw new AssertionError("native checkbox did not toggle "+field);return;}
        }
        throw new AssertionError("no native checkbox widget for "+config+"."+field);
    }
}
