package com.thenathe.suite.client;

import java.lang.reflect.Field;
import java.io.IOException;
import java.nio.file.Files;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/** Groups existing native config objects without changing their identity or save/sync code. */
public final class SuiteSettings {
    public static final String SCOPE = "thenathe_mod_suite";
    private static final List<String> MODULES = List.of("simple_smithing_overhaul", "mapstitch", "toolpouch", "tiered_backpacks", "misctweaks", "simple_death_improvements", "sensible_stackables", SCOPE);
    private static final Set<String> MODULE_SET = Set.copyOf(MODULES);
    private static boolean initialized;
    private static ConfigScreenManager currentManager;
    private static ChalkSettings chalk;
    private SuiteSettings() {}

    public static void initialize() {
        if (initialized) return;
        ChalkSettings chalkConfig = new ChalkSettings();
        try {
            // Fzzy registers directory watchers before any transient GUI config is saved.
            Files.createDirectories(chalkConfig.getDir().toPath());
        } catch (IOException failure) {
            throw new IllegalStateException("Could not create the suite client configuration directory", failure);
        }
        chalk = ConfigApiJava.registerConfig(chalkConfig, ChalkSettings::new, RegisterType.CLIENT);
        initialized = true;
        ConfigApiJava.registerScreenProvider(SCOPE, (namespace, scope) -> create(Minecraft.getInstance().gui.screen()));
        for (String module : MODULES) {
            if (!module.equals(SCOPE)) ConfigApiJava.registerScreenProvider(module, (namespace, scope) ->
                    provide(Minecraft.getInstance().gui.screen(), scope.equals(namespace) ? SCOPE : scope));
        }
        ClientPlayConnectionEvents.INIT.register((handler, client) -> resetSession());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetSession());
    }

    public static Screen create(Screen parent) {
        return provide(parent, SCOPE);
    }

    /** Open Chalk's real setting directly while retaining the shared sidebar. */
    public static Screen createChalk(Screen parent) {
        return provide(parent, SCOPE + ".chalk");
    }

    private static Screen provide(Screen parent, String scope) {
        initialize();
        chalk.refreshFromChalk();
        try {
            Map<String, ConfigScreenManager> registered = managers();
            // Fzzy removes a namespace's manager when a server sync/update invalidates
            // its widgets. Rebuild all grouped widgets together, retaining proposals.
            boolean invalid = currentManager == null || MODULES.stream().anyMatch(module -> registered.get(module) != currentManager);
            if (invalid) {
                var pending = pendingUpdates(currentManager);
                Map<String, ConfigSet> configs = collectConfigs();
                ConfigScreenManager manager = new ConfigScreenManager(SCOPE, configs.keySet(), configs);
                for (var update : pending) manager.receiveForwardedUpdate$fzzy_config(
                        update.getUpdate(), update.getPlayer(), update.getScope(), update.getSummary());
                currentManager = manager;
                for (String module : MODULES) registered.put(module, manager);
            }
            Screen screen = currentManager.provideScreen$fzzy_config(scope);
            if (screen instanceof ConfigScreen configScreen) configScreen.setParent(parent);
            return screen;
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("The suite settings adapter requires the pinned Fzzy Config API; see docs/settings.md", failure);
        }
    }

    /** A proposal belongs to one play connection, never the next server/world. */
    private static void resetSession() {
        try {
            ConfigScreenManager previous = currentManager;
            currentManager = null;
            if (previous != null) managers().entrySet().removeIf(entry -> entry.getValue() == previous);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Could not reset the suite settings session", failure);
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<ConfigScreenManager.ForwardedUpdate> pendingUpdates(ConfigScreenManager manager) throws ReflectiveOperationException {
        Set<ConfigScreenManager.ForwardedUpdate> updates = new LinkedHashSet<>();
        if (manager == null) return updates;
        Field field = ConfigScreenManager.class.getDeclaredField("screenCaches");
        field.setAccessible(true);
        for (Object cache : ((Map<?, ?>) field.get(manager)).values()) {
            updates.addAll((List<ConfigScreenManager.ForwardedUpdate>) invoke(cache, "getForwardedUpdates"));
        }
        return updates;
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
