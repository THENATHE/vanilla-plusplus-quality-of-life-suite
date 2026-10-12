package com.thenathe.suite;

import com.thenathe.suite.pickup.PickupUpgrades;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public final class SuiteItems {
    public static final Item POUCH_MAGNET = create("pouch_magnet", "Pouch Magnet");
    public static final Item POUCH_COLLECTOR = create("pouch_collector", "Pouch Collector");

    private SuiteItems() {}

    private static Item create(String path, String name) {
        var id = Identifier.fromNamespaceAndPath("thenathe_mod_suite", path);
        List<Component> help = path.equals("pouch_magnet")
                ? List.of(Component.translatableWithFallback("tooltip.thenathe_mod_suite.magnet", "Put in your Tool Pouch: 6-block pickup range."))
                : List.of(Component.translatableWithFallback("tooltip.thenathe_mod_suite.collector", "Put in your Tool Pouch: collect into your backpack."),
                        Component.translatableWithFallback("tooltip.thenathe_mod_suite.toggle", "Toggle with your pickup hotkey or /suite-pickup."));
        return new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))
                .stacksTo(1).component(DataComponents.ITEM_NAME,
                        Component.translatableWithFallback("item.thenathe_mod_suite." + path, name))
                .component(DataComponents.LORE, new ItemLore(help)));
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.ITEM, Identifier.parse("thenathe_mod_suite:pouch_magnet"), POUCH_MAGNET);
        Registry.register(BuiltInRegistries.ITEM, Identifier.parse("thenathe_mod_suite:pouch_collector"), POUCH_COLLECTOR);
        if (FabricLoader.getInstance().isModLoaded("polymer-core")
                && FabricLoader.getInstance().isModLoaded("polymer-resource-pack")) {
            SuiteItemPolymer.initialize();
        }
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(POUCH_MAGNET);
            entries.accept(POUCH_COLLECTOR);
            entries.accept(com.thenathe.suite.wrench.DebugWrench.WRENCH);
        });
        PickupUpgrades.initialize(POUCH_MAGNET, POUCH_COLLECTOR);
    }
}
