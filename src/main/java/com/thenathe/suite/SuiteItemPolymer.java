package com.thenathe.suite;

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
final class SuiteItemPolymer {
    private SuiteItemPolymer() {}

    static void initialize() {
        PolymerResourcePackUtils.addModAssets("vpp_mod_suite");
        PolymerResourcePackUtils.markAsRequired();
        PolymerItem.registerOverlay(SuiteItems.POUCH_MAGNET, new UpgradeOverlay(Items.IRON_NUGGET));
        PolymerItem.registerOverlay(SuiteItems.POUCH_COLLECTOR, new UpgradeOverlay(Items.HOPPER));
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

        @Override
        public void modifyClientTooltip(List<Component> tooltip, ItemStack stack, PacketContext context) {
            if (nativeClient(context) || !stack.is(SuiteItems.POUCH_COLLECTOR)) return;
            for (int index = 0; index < tooltip.size(); index++) {
                Component line = tooltip.get(index);
                if (line.getContents() instanceof TranslatableContents translated
                        && translated.getKey().equals("tooltip.vpp_mod_suite.toggle")) {
                    tooltip.set(index, Component.translatableWithFallback(
                            "tooltip.vpp_mod_suite.toggle_command", "Toggle with /suite-pickup.")
                            .withStyle(line.getStyle()));
                }
            }
        }

    }
}
