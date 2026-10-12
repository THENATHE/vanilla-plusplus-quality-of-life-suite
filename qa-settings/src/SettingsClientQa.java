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
    boolean hotkeyQueued;
    net.minecraft.client.gui.screens.Screen hotkeyParent;
    String opened;
    int wheelPhase;
    boolean wheelDone;
    int featurePhase;
    boolean featureDone;
    ConfigScreenManager beforeInvalidation;
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if(done) return;
            if(c.gui.overlay()!=null) return;
            if(!Boolean.getBoolean("settings.qa.standalone") && (c.player==null || c.level==null)) return;
            if(++ticks<20) return;
            ticks=0;
            try {
                if(Boolean.getBoolean("settings.qa.wheel") && !wheelDone && c.player!=null && c.level!=null) {
                    verifyWheel(c);
                    return;
                }
                if(configs==null) {
                    if(!hotkeyQueued) {
                        verifyModMenuPresentation();
                        if(!Arrays.asList(c.options.keyMappings).contains(SuiteKeybindings.OPEN_SETTINGS))throw new AssertionError("suite hotkey missing from Controls");
                        if(!SuiteKeybindings.OPEN_SETTINGS.getDefaultKey().equals(com.mojang.blaze3d.platform.InputConstants.UNKNOWN))throw new AssertionError("suite hotkey should default to unbound");
                        var probeKey=com.mojang.blaze3d.platform.InputConstants.Type.KEYBOARD.getOrCreate(com.mojang.blaze3d.platform.InputConstants.KEY_F10);
                        SuiteKeybindings.OPEN_SETTINGS.setKey(probeKey);
                        net.minecraft.client.KeyMapping.resetMapping();
                        hotkeyParent=c.gui.screen();
                        net.minecraft.client.KeyMapping.click(probeKey);
                        hotkeyQueued=true;
                        return;
                    }
                    SuiteKeybindings.OPEN_SETTINGS.setKey(com.mojang.blaze3d.platform.InputConstants.UNKNOWN);
                    net.minecraft.client.KeyMapping.resetMapping();
                    if(c.player!=null && c.level!=null && hotkeyParent==null) {
                        if(!(c.gui.screen() instanceof ConfigScreen))throw new AssertionError("suite hotkey did not open settings from gameplay");
                        row("registered rebindable Controls hotkey opens the native suite screen from gameplay");
                    } else {
                        if(c.gui.screen()!=hotkeyParent)throw new AssertionError("suite hotkey replaced an existing screen or opened outside a world");
                        row("suite hotkey safely ignores existing screens and no-world input");
                    }
                    c.gui.setScreen(SuiteSettings.create(null));
                    configs=SuiteSettings.collectConfigs();
                    ids=new ArrayList<>(configs.keySet());
                    var expected=new java.util.HashSet<>(Set.of("simple_smithing_overhaul.config-v2","mapstitch.config","mapstitch.client_config","toolpouch.config","toolpouch.client_config","tiered_backpacks.config","misctweaks.config","misctweaks.client_config","simple_death_improvements.config","vpp_mod_suite.chalk","vpp_mod_suite.features"));
                    if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("sensible_stackables")){expected.add("sensible_stackables.config");expected.add("sensible_stackables.client_config");}
                    if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("bannerpoint")){expected.add("bannerpoint.config");expected.add("bannerpoint.client_config");}
                    if(!configs.keySet().equals(expected)) throw new AssertionError("unexpected config keys="+configs.keySet());
                    for(var entry:configs.entrySet()) {
                        if(!entry.getValue().getActive().getId().toLanguageKey().equals(entry.getKey())) throw new AssertionError("identity changed "+entry.getKey());
                    }
                    var expectedLabels=Map.ofEntries(
                        Map.entry("simple_smithing_overhaul.config-v2","Simple Smithing Overhaul Settings"),
                        Map.entry("mapstitch.config","MapStitch Gameplay Settings"),
                        Map.entry("mapstitch.client_config","MapStitch Client Settings"),
                        Map.entry("toolpouch.config","Tool Pouch Gameplay Settings"),
                        Map.entry("toolpouch.client_config","Tool Pouch Client Settings"),
                        Map.entry("tiered_backpacks.config","Tiered Backpacks Settings"),
                        Map.entry("misctweaks.config","MiscTweaks Gameplay Settings"),
                        Map.entry("misctweaks.client_config","MiscTweaks Client Settings"),
                        Map.entry("simple_death_improvements.config","Simple Death Improvements Settings"),
                        Map.entry("sensible_stackables.config","Sensible Stackables Gameplay Settings"),
                        Map.entry("sensible_stackables.client_config","Sensible Stackables Client Settings"),
                        Map.entry("bannerpoint.config","Bannerpoint Gameplay Settings"),
                        Map.entry("bannerpoint.client_config","Bannerpoint Client Settings"),
                        Map.entry("vpp_mod_suite.chalk","Chalk Settings"),
                        Map.entry("vpp_mod_suite.features","Native Features"));
                    for(var entry:configs.entrySet()) {
                        String actual=entry.getValue().getActive().translation(null).getString();
                        if(!expectedLabels.get(entry.getKey()).equals(actual))throw new AssertionError("native configuration title "+entry.getKey()+"="+actual);
                        row("effective native title "+entry.getKey()+"="+actual);
                    }
                    if(configs.containsKey("vpp_mod_suite.overview"))throw new AssertionError("fake overview config remains visible");
                    row("only functional original configurations and Chalk bridge appear in native root navigation");
                    String packId=SuiteResources.SETTINGS_TITLES.toString();
                    var resourcePack=c.getResourcePackRepository().getPack(packId);
                    if(resourcePack==null || !resourcePack.isRequired() || resourcePack.getDefaultPosition()!=net.minecraft.server.packs.repository.Pack.Position.TOP
                            || !c.getResourcePackRepository().getSelectedIds().contains(packId))throw new AssertionError("settings language overlay not automatically selected and required at top");
                    row("settings language overlay automatically enabled, required, and top by default");
                    try(var input=SettingsClientQa.class.getResourceAsStream("/settings-original-language-keys.json")) {
                        if(input==null)throw new AssertionError("original-language QA reference missing");
                        int preserved=0;
                        for(var value:JsonParser.parseReader(new java.io.InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonArray()) {
                            String key=value.getAsString();
                            if(!configs.containsKey("sensible_stackables.config") && key.startsWith("sensible_stackables."))continue;
                            if(!net.minecraft.locale.Language.getInstance().has(key))throw new AssertionError("original language entry missing: "+key);
                            preserved++;
                        }
                        row("all original language entries present in live client: "+preserved);
                    }
                    for(String key:List.of("gui.toolpouch.coordinates_xyz","gui.toolpouch.facing","gui.toolpouch.day","gui.toolpouch.time","item.toolpouch.tool_pouch",
                            "mapstitch.gui.worldmap.title","simple_smithing_overhaul.config-v2.portableItemRepair",
                            "tiered_backpacks.config.leatherSize","misctweaks.config.sneakingPreventsBerryBushDamage",
                            "simple_death_improvements.config.noItemSplatterOnDeath","sensible_stackables.config.items")) {
                        if(!configs.containsKey("sensible_stackables.config") && key.startsWith("sensible_stackables."))continue;
                        if(!net.minecraft.locale.Language.getInstance().has(key))throw new AssertionError("original language entry shadowed by suite title overrides: "+key);
                        row("original translation retained "+key);
                    }
                    verifyConfigureButtons(c);
                    if(Boolean.getBoolean("settings.qa.lifecycleOnly"))phase=ids.size()*2+10;
                    return;
                }
                if(!featureDone && !Boolean.getBoolean("settings.qa.standalone") && !Boolean.getBoolean("settings.qa.visual")) {
                    verifyFeatureReadback(c);
                    return;
                }
                if(Boolean.getBoolean("settings.qa.visual")) {
                    if(phase==0) {
                        Screenshot.grab(c.gameDirectory,"suite-settings-overview.png",c.gameRenderer.mainRenderTarget(),1,m->{});
                        var expectedLabels=Map.of("vpp_mod_suite","Vanilla++ Quality of Life Suite","misctweaks.config","MiscTweaks Gameplay Settings","misctweaks.client_config","MiscTweaks Client Settings","tiered_backpacks.config","Tiered Backpacks Settings","toolpouch.config","Tool Pouch Gameplay Settings","toolpouch.client_config","Tool Pouch Client Settings");
                        for(var label:expectedLabels.entrySet()) {
                            String actual=net.minecraft.network.chat.Component.translatable(label.getKey()).getString();
                            if(!actual.equals(label.getValue()))throw new AssertionError("effective language label "+label.getKey()+"="+actual);
                            row("effective label "+label.getKey()+"="+actual);
                        }
                        var screen=(ConfigScreen)c.gui.screen();
                        CustomButtonWidget navigation=null;
                        for(var child:screen.children())if(child instanceof CustomButtonWidget button && button.getY()>=screen.height-30 && (navigation==null || button.getX()<navigation.getX()))navigation=button;
                        if(navigation==null)throw new AssertionError("missing native navigation button");
                        navigation.onPress();phase++;return;
                    }
                    Screenshot.grab(c.gameDirectory,"suite-settings-sidebar.png",c.gameRenderer.mainRenderTarget(),1,m->{});
                    Files.writeString(control.resolve("observations.json"),new GsonBuilder().setPrettyPrinting().create().toJson(observations));
                    Files.writeString(control.resolve("result.txt"),"PASS functional settings navigation, explicit module labels and rendered native sidebar\n");done=true;return;
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
                    c.gui.setScreen(manager().provideScreen$fzzy_config("vpp_mod_suite.chalk"));
                    toggle("vpp_mod_suite.chalk","emitParticles",action==2);
                    manager().provideUpdateManager$fzzy_config("vpp_mod_suite.chalk").apply(false);
                } else if(action==1 || action==3) {
                    boolean expected=action==3;
                    var holder=AutoConfig.getConfigHolder(ChalkConfig.class);
                    if(holder.getConfig().EmitParticles!=expected)throw new AssertionError("Chalk holder not updated");
                    if(!holder.load() || holder.getConfig().EmitParticles!=expected)throw new AssertionError("original Chalk file did not persist");
                    c.gui.setScreen(SuiteSettings.create(null));configs=SuiteSettings.collectConfigs();
                    ChalkSettings value=(ChalkSettings)configs.get("vpp_mod_suite.chalk").getActive();
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
                } else if(action==10) {
                    c.gui.setScreen(SuiteSettings.create(null));
                    beforeInvalidation=manager();
                    forward("reopen proposal");
                    if(forwards()!=1)throw new AssertionError("proposal not queued");
                    c.gui.setScreen(null);
                    c.gui.setScreen(SuiteSettings.create(null));
                    if(manager()!=beforeInvalidation || forwards()!=1)throw new AssertionError("reopening discarded pending proposal");
                    row("pending forwarded proposal survives suite close/reopen");
                } else if(action==11) {
                    ClientConfigRegistry.INSTANCE.receiveUpdate$fzzy_config(Map.of("toolpouch.config","canOpenWithRightClick = true"),c.player);
                } else if(action==12) {
                    if(registered().containsKey("toolpouch"))throw new AssertionError("native update did not invalidate namespace");
                    c.gui.setScreen(ClientConfigRegistry.INSTANCE.provideScreen$fzzy_config("toolpouch.config"));
                    if(c.gui.screen()==null || manager()==beforeInvalidation)throw new AssertionError("native invalidation did not rebuild settings");
                    if(registered().get("toolpouch")!=manager() || forwards()!=1)throw new AssertionError("rebuilt routing/proposal mismatch");
                    forward("post-update proposal");
                    if(forwards()!=2)throw new AssertionError("post-update forwarded proposal not routed");
                    if(ClientConfigRegistry.INSTANCE.provideUpdateManager$fzzy_config("toolpouch.config")==null)throw new AssertionError("native update manager routing missing");
                    row("native receiveUpdate invalidation rebuilds widgets, restores original routing, preserves and receives proposals");
                } else if(action==13 && Integer.getInteger("settings.qa.reconnectPort",0)>0) {
                    beforeInvalidation=manager();
                    var title=new net.minecraft.client.gui.screens.TitleScreen();
                    c.level.disconnect(net.minecraft.network.chat.Component.literal("Settings lifecycle QA reconnect"));
                    c.disconnect(title,false,true);
                    if(manager()!=null || registered().containsValue(beforeInvalidation))throw new AssertionError("disconnect retained suite session");
                    String address="127.0.0.1:"+Integer.getInteger("settings.qa.reconnectPort");
                    net.minecraft.client.gui.screens.ConnectScreen.startConnecting(title,c,
                            net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(address),
                            new net.minecraft.client.multiplayer.ServerData("Settings QA second server",address,net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);
                    row("real disconnect clears manager and original namespace aliases");
                } else if(action==14 && Integer.getInteger("settings.qa.reconnectPort",0)>0) {
                    c.gui.setScreen(ClientConfigRegistry.INSTANCE.provideScreen$fzzy_config("toolpouch.config"));
                    if(manager()==beforeInvalidation || forwards()!=0)throw new AssertionError("proposal or manager crossed servers");
                    if(registered().get("toolpouch")!=manager())throw new AssertionError("new connection alias missing");
                    forward("new-server proposal");
                    if(forwards()!=1)throw new AssertionError("new-server forwarding failed");
                    c.gui.setScreen(ClientConfigRegistry.INSTANCE.provideScreen$fzzy_config("misctweaks.config"));
                    boolean secondGuest=!Boolean.getBoolean("settings.qa.guest");
                    var button=nativeButton("misctweaks.config.preventShulkerDuplication");
                    if(button.getMessage().getString().equals("Can't Edit")!=secondGuest)throw new AssertionError("permissions leaked across servers");
                    var config=(me.pajic.misctweaks.config.ModConfig)SuiteSettings.collectConfigs().get("misctweaks.config").getActive();
                    if(config.preventShulkerDuplication.get())throw new AssertionError("old server setting crossed connection");
                    button.onPress();
                    manager().provideUpdateManager$fzzy_config("misctweaks.config").apply(false);
                    if(config.preventShulkerDuplication.get()==secondGuest)throw new AssertionError("new server permission behavior incorrect");
                    row("different-server reconnect drops old proposals, uses new permission level, and routes new proposals");
                } else if(action==15 && Integer.getInteger("settings.qa.reconnectPort",0)>0) {
                    var server=JsonParser.parseString(Files.readString(control.resolve("server2/server-config.json"))).getAsJsonObject();
                    boolean expected=Boolean.getBoolean("settings.qa.guest");
                    if(server.get("preventShulkerDuplication").getAsBoolean()!=expected)throw new AssertionError("second server native setting permission/save mismatch");
                    row("second server native checkbox permission and save verified");
                } else {
                    Files.writeString(control.resolve("observations.json"),new GsonBuilder().setPrettyPrinting().create().toJson(observations));
                    Files.writeString(control.resolve("result.txt"),"PASS native settings checks (see observations for executed cases)\n");
                    done=true;return;
                }
                phase++;
            } catch(Throwable failure) {
                failure.printStackTrace();done=true;
                try {Files.writeString(control.resolve("failure"),"phase="+phase+" opened="+opened+": "+failure);Files.writeString(control.resolve("observations.json"),new GsonBuilder().setPrettyPrinting().create().toJson(observations));}catch(Exception ignored){}
            }
        });
    }
    void verifyModMenuPresentation() {
        var mods=com.terraformersmc.modmenu.ModMenu.MODS;
        var roots=com.terraformersmc.modmenu.ModMenu.ROOT_MODS;
        var parent=mods.get("vpp_mod_suite");
        if(parent==null || roots.get(parent.getId())!=parent)throw new AssertionError("suite is not a visible Mod Menu root");
        var originals=Set.of("simple_smithing_overhaul","mapstitch","toolpouch","tiered_backpacks","misctweaks","simple_death_improvements","sensible_stackables","chalk","bannerpoint");
        for(String id:originals) {
            var mod=mods.get(id);
            if(mod==null || roots.containsKey(id) || !"vpp_mod_suite".equals(mod.getParent()) || mod.isHidden())throw new AssertionError("original feature not visible beneath suite: "+id);
        }
        row("all original feature entries remain individually visible beneath Vanilla++");
        var compatibility=Set.of("sso_backpack_toolpouch_mapstitch_shim","chalk_polymer_compat","sensible_stackables_polymer_compat","bannerpoint_polymer_compat");
        int visible=0;
        for(String id:compatibility) {
            var mod=mods.get(id);
            if(mod==null || !net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(id))throw new AssertionError("compatibility loader identity missing: "+id);
            if(!mod.isHidden()) {
                visible++;
                if(!"vpp_mod_suite".equals(mod.getParent()) || !"Compatibility + Shims".equals(mod.getTranslatedName()))throw new AssertionError("combined compatibility presentation missing: "+id);
            }
        }
        if(visible!=1)throw new AssertionError("expected one visible compatibility entry, got "+visible);
        row("one Compatibility + Shims entry with all original compatibility loader identities retained");
        for(String id:List.of("shared_region_maps","toolpouch_atlas_elytra_compat","mapstitch_mixed_scales","amethyst_curse_cleanser")) {
            var mod=mods.get(id);
            if(mod==null || mod.isHidden() || !"vpp_mod_suite".equals(mod.getParent()))throw new AssertionError("authored feature hidden or detached: "+id);
        }
        var labels=new HashSet<String>();
        for(var child:com.terraformersmc.modmenu.ModMenu.PARENT_MAP.get(parent))if(!child.isHidden())labels.add(child.getTranslatedName());
        for(String name:List.of("Drops into Shulker","Better Beacon Placement","Better Conduit Placement","Grind Enchantments","Netherite Instamine","No Enderman Grief","Swift Flight","Transfer Pet Owner","Call Your Parrot","Call Your Nautilus","Call Your Horse","Call Your Happy Ghast","Call Your Dog","Call Your Cat","Trident All Weather Channeling")) {
            if(!labels.contains(name))throw new AssertionError("native feature missing from Mod Menu: "+name+" visible="+labels);
        }
        row("all fifteen new feature entries appear individually beneath suite");
        for(var feature:com.thenathe.suite.client.SuiteFeatureEntries.FEATURES) {
            if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(feature.id()))throw new AssertionError("presentation entry invented a loader identity: "+feature.id());
            var configured=com.terraformersmc.modmenu.ModMenu.getConfigScreen(feature.id(),new net.minecraft.client.gui.screens.TitleScreen());
            String scope=feature.nativeSettings()?"vpp_mod_suite.features":"vpp_mod_suite";
            if(!(configured instanceof ConfigScreen featureScreen) || !scope.equals(featureScreen.getScope()))throw new AssertionError("feature configuration action does not reach real settings: "+feature.id());
        }
        row("all integrated feature configuration actions open real settings without inventing loader identities");
        var screen=com.terraformersmc.modmenu.ModMenu.getConfigScreen("vpp_mod_suite",new net.minecraft.client.gui.screens.TitleScreen());
        if(!(screen instanceof ConfigScreen suiteScreen) || !"vpp_mod_suite".equals(suiteScreen.getScope()))throw new AssertionError("suite Mod Menu parent does not open combined settings");
        var chalk=com.terraformersmc.modmenu.ModMenu.getConfigScreen("chalk",new net.minecraft.client.gui.screens.TitleScreen());
        if(!(chalk instanceof ConfigScreen chalkScreen) || !"vpp_mod_suite.chalk".equals(chalkScreen.getScope()))throw new AssertionError("Chalk factory no longer opens its real grouped config");
        row("suite parent and original Chalk configuration factories open their real grouped screens");
    }

    void verifyWheel(Minecraft client) throws Exception {
        var key=com.mojang.blaze3d.platform.InputConstants.Type.KEYBOARD.getOrCreate(com.mojang.blaze3d.platform.InputConstants.KEY_G);
        if(wheelPhase==0) {
            if(!client.isWindowActive()) {
                org.lwjgl.sdl.SDLVideo.SDL_RaiseWindow(client.getWindow().handle());
                row("raised real SDL window for wheel input; focused="+client.isWindowActive());
                return;
            }
            if(!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("hotkey_wheel"))throw new AssertionError("requested Hotkey Wheel missing");
            if(com.thenathe.hotkeywheel.HotkeyWheelClient.findControl(SuiteKeybindings.OPEN_SETTINGS.getName())!=SuiteKeybindings.OPEN_SETTINGS)throw new AssertionError("wheel cannot discover suite binding");
            var config=com.thenathe.hotkeywheel.WheelConfig.get().copy();
            config.wheelCount=1;
            config.wheels.get(0).toggleOpen=false;
            config.wheels.get(0).entries.get(0).control=SuiteKeybindings.OPEN_SETTINGS.getName();
            if(!com.thenathe.hotkeywheel.WheelConfig.save(config))throw new AssertionError("wheel config did not save");
            SuiteKeybindings.OPEN_SETTINGS.setKey(com.mojang.blaze3d.platform.InputConstants.UNKNOWN);
            com.thenathe.hotkeywheel.HotkeyWheelClient.WHEEL_KEYS[0].setKey(key);
            net.minecraft.client.KeyMapping.resetMapping();
            client.gui.setScreen(null);
            row("wheel activation context focused="+client.isWindowActive()+" key="+key+" mapping="+com.thenathe.hotkeywheel.HotkeyWheelClient.WHEEL_KEYS[0].saveString()+" matches="+com.thenathe.hotkeywheel.HotkeyWheelClient.WHEEL_KEYS[0].matches(key));
            dispatchWheelKey(client,com.mojang.blaze3d.platform.InputConstants.PRESS);
            if(!(client.gui.screen() instanceof com.thenathe.hotkeywheel.WheelScreen))throw new AssertionError("wheel did not open");
            wheelPhase++;
            return;
        }
        if(wheelPhase==1) {
            if(!(client.gui.screen() instanceof com.thenathe.hotkeywheel.WheelScreen screen))throw new AssertionError("wheel closed before selection; focused="+client.isWindowActive());
            Screenshot.grab(client.gameDirectory,"hotkey-wheel-suite-control.png",client.gameRenderer.mainRenderTarget(),1,m->{});
            double angle=com.thenathe.hotkeywheel.RadialGeometry.angle(0,com.thenathe.hotkeywheel.WheelConfig.get().wheels.get(0).entries.size());
            double x=screen.centerX()+Math.cos(angle)*screen.radius()*.76;
            double y=screen.centerY()+Math.sin(angle)*screen.radius()*.76;
            double windowX=x*client.getWindow().getScreenWidth()/screen.width;
            double windowY=y*client.getWindow().getScreenHeight()/screen.height;
            org.lwjgl.sdl.SDLMouse.SDL_WarpMouseInWindow(client.getWindow().handle(),(float)windowX,(float)windowY);
            client.mouseHandler.onMove(client.getWindow().handle(),windowX,windowY,0,0);
            if(screen.selection(client.mouseHandler.getScaledXPos(client.getWindow()),client.mouseHandler.getScaledYPos(client.getWindow()))!=0)throw new AssertionError("wheel cursor did not select suite control");
            SuiteKeybindings.OPEN_SETTINGS.setKey(key);
            net.minecraft.client.KeyMapping.resetMapping();
            dispatchWheelKey(client,com.mojang.blaze3d.platform.InputConstants.RELEASE);
            wheelPhase++;
            return;
        }
        if(!(client.gui.screen() instanceof ConfigScreen))throw new AssertionError("wheel selection did not open suite settings through registered control");
        SuiteKeybindings.OPEN_SETTINGS.setKey(com.mojang.blaze3d.platform.InputConstants.UNKNOWN);
        net.minecraft.client.KeyMapping.resetMapping();
        row("isolated input fixture dispatches SDL focus, native keyboard and mouse events: Hotkey Wheel selects the unbound suite control; overlapping release key opens combined settings once");
        Screenshot.grab(client.gameDirectory,"hotkey-wheel-opened-suite-settings.png",client.gameRenderer.mainRenderTarget(),1,m->{});
        client.gui.setScreen(null);
        wheelDone=true;
    }

    void dispatchWheelKey(Minecraft client, int action) throws Exception {
        Method handler=net.minecraft.client.KeyboardHandler.class.getDeclaredMethod("keyPress",long.class,int.class,net.minecraft.client.input.KeyEvent.class);
        handler.setAccessible(true);
        handler.invoke(client.keyboardHandler,client.getWindow().handle(),action,new net.minecraft.client.input.KeyEvent(com.mojang.blaze3d.platform.InputConstants.KEY_G,0,0));
    }

    void verifyFeatureReadback(Minecraft client) throws Exception {
        boolean guest=Boolean.getBoolean("settings.qa.guest");
        if(featurePhase==0 || featurePhase==2) {
            configs=SuiteSettings.collectConfigs();
            client.gui.setScreen(manager().provideScreen$fzzy_config("vpp_mod_suite.features"));
            if(guest) {
                var button=nativeButton("vpp_mod_suite.features.dropsIntoShulker");
                if(!button.getMessage().getString().equals("Can't Edit"))throw new AssertionError("new module field lacks native permission protection");
                button.onPress();
            } else toggle("vpp_mod_suite.features","dropsIntoShulker",featurePhase==2);
            manager().provideUpdateManager$fzzy_config("vpp_mod_suite.features").apply(false);
            featurePhase++;
            return;
        }
        var state=JsonParser.parseString(Files.readString(control.resolve("server-config.json"))).getAsJsonObject();
        boolean expected=guest || featurePhase==3;
        if(state.get("dropsIntoShulker").getAsBoolean()!=expected || state.get("savedDropsIntoShulker").getAsBoolean()!=expected)throw new AssertionError("native feature toggle not reflected in server runtime and saved config: "+state);
        row("new shulker module native checkbox server/runtime/file readback="+expected+" guest="+guest);
        featurePhase++;
        if(featurePhase>=4) {
            featureDone=true;
            client.gui.setScreen(SuiteSettings.create(null));
            configs=SuiteSettings.collectConfigs();
        }
    }

    void row(String text){JsonObject row=new JsonObject();row.addProperty("case",text);row.addProperty("passed",true);observations.add(row);}
    void verifyConfigureButtons(Minecraft client)throws Exception {
        var found=new HashSet<String>();
        CustomButtonWidget chalkButton=null;
        for(var entry:list((ConfigScreen)client.gui.screen()).selectableEntries()) {
            String key=entry.getScope().getScope();
            if(!configs.containsKey(key))continue;
            for(var child:entry.children())if(child instanceof CustomButtonWidget button) {
                if(!button.getMessage().getString().equals("Configure..."))throw new AssertionError("root navigation button label "+key+"="+button.getMessage().getString());
                found.add(key);
                if(key.equals("vpp_mod_suite.chalk"))chalkButton=button;
            }
        }
        if(!found.equals(configs.keySet()))throw new AssertionError("root config buttons missing: "+found);
        if(chalkButton==null || !chalkButton.active)throw new AssertionError("Chalk Configure button unavailable");
        chalkButton.onPress();
        if(!(client.gui.screen() instanceof ConfigScreen screen) || !screen.getScope().equals("vpp_mod_suite.chalk"))throw new AssertionError("Configure button changed its original navigation target");
        row("all "+found.size()+" root buttons say Configure... while native titles remain named; original Chalk button opens its correct category");
        client.gui.setScreen(SuiteSettings.create(null));
    }
    void forward(String summary){ClientConfigRegistry.INSTANCE.handleForwardedUpdate$fzzy_config("entry = false",UUID.fromString("00000000-0000-0000-0000-000000000001"),"toolpouch.config.canOpenWithRightClick",summary);}
    int forwards()throws Exception{return manager().provideUpdateManager$fzzy_config("toolpouch.config").forwardsCount();}
    @SuppressWarnings("unchecked") static Map<String,ConfigScreenManager> registered()throws Exception{Field field=ClientConfigRegistry.class.getDeclaredField("configScreenManagers");field.setAccessible(true);return(Map<String,ConfigScreenManager>)field.get(null);}
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
