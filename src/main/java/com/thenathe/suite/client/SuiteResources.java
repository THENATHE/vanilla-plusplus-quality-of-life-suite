package com.thenathe.suite.client;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Required language overlay loads after the individual bundled mod resource packs. */
public final class SuiteResources {
    public static final Identifier SETTINGS_TITLES = Identifier.fromNamespaceAndPath(SuiteSettings.SCOPE, "settings_titles");
    private SuiteResources() {}

    public static void initialize() {
        var container = FabricLoader.getInstance().getModContainer(SuiteSettings.SCOPE).orElseThrow();
        boolean registered = ResourceLoader.registerBuiltinPack(SETTINGS_TITLES, container,
                Component.literal("Vanilla++ Quality of Life Suite Settings"), PackActivationType.ALWAYS_ENABLED);
        if (!registered) throw new IllegalStateException("The suite settings language overlay is missing from this installation");
    }
}
