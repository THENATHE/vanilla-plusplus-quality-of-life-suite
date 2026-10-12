package com.thenathe.suite.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class SuiteModMenu implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() { return SuiteSettings::create; }

    @Override public java.util.Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        java.util.Map<String, ConfigScreenFactory<?>> factories = new java.util.LinkedHashMap<>();
        for (SuiteFeatureEntries.Feature feature : SuiteFeatureEntries.FEATURES) {
            factories.put(feature.id(), feature.nativeSettings() ? SuiteSettings::createFeatures : SuiteSettings::create);
        }
        factories.put("sso_backpack_toolpouch_mapstitch_shim", SuiteSettings::create);
        return factories;
    }
}
