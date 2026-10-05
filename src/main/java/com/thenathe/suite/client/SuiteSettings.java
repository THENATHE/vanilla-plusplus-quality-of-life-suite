package com.thenathe.suite.client;

import java.lang.reflect.Field;
import java.io.IOException;
import java.nio.file.Files;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.jvm.internal.Reflection;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigEntry;
import me.fzzyhmstrs.fzzy_config.impl.ConfigApiImpl;
import me.fzzyhmstrs.fzzy_config.impl.ConfigSet;
import me.fzzyhmstrs.fzzy_config.registry.ClientConfigRegistry;
import me.fzzyhmstrs.fzzy_config.registry.SyncedConfigRegistry;
import me.fzzyhmstrs.fzzy_config.screen.internal.ConfigScreen;
import me.fzzyhmstrs.fzzy_config.screen.internal.ConfigScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Groups existing native config objects without changing their identity or save/sync code. */
public final class SuiteSettings {
    public static final String SCOPE = "thenathe_mod_suite";
    private static final List<String> MODULES = List.of("simple_smithing_overhaul", "mapstitch", "toolpouch", "tiered_backpacks", "misctweaks", "simple_death_improvements", SCOPE);
    private static final Set<String> MODULE_SET = Set.copyOf(MODULES);
    private static boolean initialized, moduleProvidersRegistered;
    private static ConfigScreenManager currentManager;
    private static ChalkSettings chalk;
    private SuiteSettings() {}

    public static void initialize() {
        if (initialized) return;
        SuiteOverview overview = new SuiteOverview();
        ChalkSettings chalkConfig = new ChalkSettings();
        try {
            // Fzzy registers directory watchers before any transient GUI config is saved.
            Files.createDirectories(overview.getDir().toPath());
            Files.createDirectories(chalkConfig.getDir().toPath());
        } catch (IOException failure) {
            throw new IllegalStateException("Could not create the suite client configuration directory", failure);
        }
        ConfigApiJava.registerConfig(overview, SuiteOverview::new, RegisterType.CLIENT);
        chalk = ConfigApiJava.registerConfig(chalkConfig, ChalkSettings::new, RegisterType.CLIENT);
        initialized = true;
        ConfigApiJava.registerScreenProvider(SCOPE, (namespace, scope) -> create(Minecraft.getInstance().gui.screen()));
    }

    public static Screen create(Screen parent) {
        initialize();
        chalk.refreshFromChalk();
        try {
            Map<String, ConfigSet> configs = collectConfigs();
            ConfigScreenManager manager = new ConfigScreenManager(SCOPE, configs.keySet(), configs);
            currentManager = manager;
            // Server forwarded edits are routed by original namespace. Keep those callbacks
            // on the same manager that owns the displayed native configuration objects.
            managers().put(SCOPE, manager);
            for (String module : MODULES) {
                if (module.equals(SCOPE)) continue;
                managers().put(module, manager);
                if (!moduleProvidersRegistered) ConfigApiJava.registerScreenProvider(module, (namespace, scope) -> {
                    String target = scope.equals(namespace) ? SCOPE : scope;
                    return currentManager.provideScreen$fzzy_config(target);
                });
            }
            moduleProvidersRegistered = true;
            Screen screen = manager.provideScreen$fzzy_config(SCOPE);
            if (screen instanceof ConfigScreen configScreen) configScreen.setParent(parent);
            return screen;
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("The suite settings adapter requires the pinned Fzzy Config API; see docs/settings.md", failure);
        }
    }

    /** Visible for a bounded UI smoke test; values retain original active/default objects. */
    public static Map<String, ConfigSet> collectConfigs() throws ReflectiveOperationException {
        Map<String, ConfigSet> selected = new LinkedHashMap<>();
        Map<String, ?> entries = entries();
        for (String module : MODULES) {
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object value = entry.getValue();
                ConfigEntry<?> configEntry = (ConfigEntry<?>) value;
                Config config = configEntry.getConfig();
                if (!config.getId().getNamespace().equals(module) || !MODULE_SET.contains(module)) continue;
                if ((boolean) invoke(value, "getNoGui")) continue;
                Config base = (Config) invoke(value, "getBase");
                boolean clientOnly = !SyncedConfigRegistry.INSTANCE.hasConfig$fzzy_config(entry.getKey());
                boolean root = ConfigApiImpl.INSTANCE.isRootConfig$fzzy_config(Reflection.getOrCreateKotlinClass(config.getClass()));
                selected.put(entry.getKey(), new ConfigSet(config, base, clientOnly, root));
            }
        }
        if (selected.isEmpty()) throw new IllegalStateException("No suite settings registered");
        return selected;
    }

    private static Object invoke(Object object, String name) throws ReflectiveOperationException {
        Method method = object.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(object);
    }
    @SuppressWarnings("unchecked")
    private static Map<String, ?> entries() throws ReflectiveOperationException {
        Field field = ClientConfigRegistry.class.getDeclaredField("clientConfigs");
        field.setAccessible(true);
        return (Map<String, ?>) field.get(null);
    }
    @SuppressWarnings("unchecked")
    private static Map<String, ConfigScreenManager> managers() throws ReflectiveOperationException {
        Field field = ClientConfigRegistry.class.getDeclaredField("configScreenManagers");
        field.setAccessible(true);
        return (Map<String, ConfigScreenManager>) field.get(null);
    }
}
