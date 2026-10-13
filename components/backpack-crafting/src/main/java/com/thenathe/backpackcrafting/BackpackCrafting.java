package com.thenathe.backpackcrafting;

import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import com.thenathe.suite.network.SuiteCapabilities;
import com.thenathe.toolpouchcompat.PouchUpgradeAccess;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.util.AllowedItem;
import me.pajic.tiered_backpacks.compat.AccessoryUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;

public final class BackpackCrafting implements ModInitializer {
    public static final String ID = "vpp_backpack_crafting";
    public static final Item IRON_HAMMER = hammer("iron_hammer", "Iron Crafting Hammer", "2×2");
    public static final Item DIAMOND_HAMMER = hammer("diamond_hammer", "Diamond Crafting Hammer", "3×3");
    public static final MenuType<BackpackCraftingMenu> MENU_TYPE = new ExtendedMenuType<>(
            (id, inventory, data) -> new BackpackCraftingMenu(id, inventory, data.backpack(), data.gridSize()), OpeningData.CODEC);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }

    private static Item hammer(String path, String name, String size) {
        return new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(path)))
                .stacksTo(1).component(DataComponents.ITEM_NAME,
                        Component.translatableWithFallback("item." + ID + "." + path, name))
                .component(DataComponents.LORE, new ItemLore(List.of(Component.translatableWithFallback(
                        "tooltip." + ID + "." + path, "Put in your Tool Pouch: " + size + " backpack crafting.")))));
    }

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.ITEM, id("iron_hammer"), IRON_HAMMER);
        Registry.register(BuiltInRegistries.ITEM, id("diamond_hammer"), DIAMOND_HAMMER);
        Registry.register(BuiltInRegistries.MENU, id("crafting"), MENU_TYPE);
        if (FabricLoader.getInstance().isModLoaded("polymer-core")
                && FabricLoader.getInstance().isModLoaded("polymer-resource-pack")) HammerPolymer.initialize();
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(IRON_HAMMER);
            entries.accept(DIAMOND_HAMMER);
        });
        addPouchRules();
    }

    public static int getCraftingSize(Player player) {
        if (PouchUpgradeAccess.contains(player, DIAMOND_HAMMER)) return 3;
        return PouchUpgradeAccess.contains(player, IRON_HAMMER) ? 2 : 0;
    }

    public static boolean hasBackpack(Player player, ItemStack backpack) {
        if (backpack.isEmpty()) return false;
        if (AccessoryUtil.INSTANCE != null && AccessoryUtil.INSTANCE.getBackpack(player) == backpack) return true;
        for (EquipmentSlot slot : EquipmentSlot.values()) if (player.getItemBySlot(slot) == backpack) return true;
        return player.getInventory().getNonEquipmentItems().stream().anyMatch(stack -> stack == backpack);
    }

    public static boolean open(ServerPlayer player, ItemStack backpack) {
        int size = getCraftingSize(player);
        if (size == 0 || !hasBackpack(player, backpack)
                || !SuiteCapabilities.isNative(player.connection.getPacketContext(), "vpp_mod_suite")
                || !SuiteCapabilities.isNative(player.connection.getPacketContext(), "tiered_backpacks")) return false;
        player.openMenu(new ExtendedMenuProvider<OpeningData>() {
            @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player owner) {
                return new BackpackCraftingMenu(id, inventory, backpack, size);
            }
            @Override public Component getDisplayName() { return backpack.getDisplayName(); }
            @Override public OpeningData getScreenOpeningData(ServerPlayer owner) { return new OpeningData(backpack, size); }
        });
        return true;
    }

    private static void addPouchRules() {
        var marker = FabricLoader.getInstance().getConfigDir().resolve("vanilla-plusplus-crafting-rules-v1");
        if (Files.exists(marker)) return;
        var rules = new LinkedHashSet<AllowedItem>(ToolPouch.CONFIG.allowedItems.get());
        for (String path : List.of("iron_hammer", "diamond_hammer")) {
            String itemId = id(path).toString();
            if (rules.stream().noneMatch(rule -> rule.id.get().equals(itemId))) rules.add(new AllowedItem(itemId, 1, 1));
        }
        ToolPouch.CONFIG.allowedItems.accept(rules);
        ToolPouch.CONFIG.save();
        try {
            Files.createDirectories(marker.getParent());
            Files.writeString(marker, "Crafting hammer rules added once. Later administrator changes are preserved.\n");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not record Tool Pouch crafting rules", exception);
        }
    }

    public record OpeningData(ItemStack backpack, int gridSize) {
        public static final StreamCodec<RegistryFriendlyByteBuf, OpeningData> CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, OpeningData::backpack, ByteBufCodecs.INT, OpeningData::gridSize, OpeningData::new);
    }
}
