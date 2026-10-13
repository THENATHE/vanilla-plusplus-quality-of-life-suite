package com.thenathe.backpackcrafting;

import com.thenathe.suite.network.SuiteCapabilities;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Polymer stays optional; only the guarded initialization loads this class. */
final class HammerPolymer {
    private HammerPolymer() {}

    static void initialize() {
        PolymerResourcePackUtils.addModAssets(BackpackCrafting.ID);
        PolymerResourcePackUtils.markAsRequired();
        eu.pb4.polymer.rsm.api.RegistrySyncUtils.setServerEntry(BuiltInRegistries.MENU, BackpackCrafting.MENU_TYPE);
        PolymerItem.registerOverlay(BackpackCrafting.IRON_HAMMER, new UpgradeOverlay(Items.IRON_AXE));
        PolymerItem.registerOverlay(BackpackCrafting.DIAMOND_HAMMER, new UpgradeOverlay(Items.DIAMOND_AXE));
    }

    private record UpgradeOverlay(Item fallback) implements PolymerItem {
        private boolean nativeClient(PacketContext context) {
            return SuiteCapabilities.isNative(context, "vpp_mod_suite");
        }

        @Override
        public boolean canSyncRawToClient(PacketContext context) {
            return nativeClient(context);
        }

        @Override
        public Item getPolymerItem(ItemStack stack, PacketContext context) {
            return nativeClient(context) ? stack.getItem() : fallback;
        }

        @Override
        public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
            return BuiltInRegistries.ITEM.getKey(stack.getItem());
        }

        @Override
        public ItemStack getPolymerItemStack(ItemStack stack, TooltipFlag tooltip, PacketContext context,
                HolderLookup.Provider lookup) {
            return nativeClient(context) ? stack : PolymerItem.super.getPolymerItemStack(stack, tooltip, context, lookup);
        }

    }
}
