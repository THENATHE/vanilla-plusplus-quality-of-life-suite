package com.thenathe.suite.client;

import me.fzzyhmstrs.fzzy_config.annotations.RootConfig;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigAction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Non-setting overview entries explain modules whose controls live elsewhere. */
@RootConfig
public final class SuiteOverview extends Config {
    public ConfigAction sharedRegionMaps = information("Shared Region Maps", "Shares vanilla and MapStitch maps automatically. No separate settings. Map Atlas and Improved Maps integrations are excluded.");
    public ConfigAction amethystCurseRemoval = information("Amethyst Curse Removal", "Use an amethyst shard with a cursed item in a grindstone. No separate settings.");
    public ConfigAction colorfulChalk = information("Colorful Chalk", "All Chalk colors, dye recoloring and glow ink crafting are available. Chalk particle settings are in the Chalk section.");
    public ConfigAction elytraIntegration = information("Elytra and MapStitch Integration", "Uses the original Tool Pouch settings, key bindings and addon commands. Key bindings remain under Options > Controls.");
    public ConfigAction compatibility = information("Client and Server Compatibility", "The suite negotiates installed modules automatically. Polymer on the server enables the existing vanilla gameplay layer.");
    public SuiteOverview() { super(Identifier.fromNamespaceAndPath(SuiteSettings.SCOPE, "overview")); }
    private static ConfigAction information(String title, String description) {
        return new ConfigAction.Builder().title(Component.literal(title)).desc(Component.literal(description)).active(() -> false).build(() -> {});
    }
}
