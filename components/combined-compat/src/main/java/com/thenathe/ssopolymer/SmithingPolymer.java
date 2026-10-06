package com.thenathe.ssopolymer;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import me.pajic.simple_smithing_overhaul.blocks.ModBlocks;
import me.pajic.simple_smithing_overhaul.items.ModItems;
import me.pajic.simple_smithing_overhaul.recipe.ModRecipeSerializers;
import me.pajic.simple_smithing_overhaul.repair.RepairableOverrides;
import me.pajic.simple_smithing_overhaul.util.ModDataComponents;
import me.pajic.simple_smithing_overhaul.util.ModUtil;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;

/** Overlays preserve upstream registry objects, recipes, components and saved worlds. */
public final class SmithingPolymer {
    private SmithingPolymer() {}

    public static void initialize() {
        TextFallbacks.initialize();
        PolymerResourcePackUtils.addModAssets("simple_smithing_overhaul");
        PolymerItem.registerOverlay(ModItems.WHETSTONE, new ItemOverlay(Items.FLINT));
        PolymerItem.registerOverlay(ModItems.ENCHANTMENT_UPGRADE_SMITHING_TEMPLATE,
                new ItemOverlay(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
        PolymerItem.registerOverlay(ModItems.PINNACLE_ENCHANTMENT_SMITHING_TEMPLATE,
                new ItemOverlay(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
        PolymerItem.registerOverlay(ModItems.BROKEN_ANVIL, new ItemOverlay(Items.DAMAGED_ANVIL));
        PolymerItem.registerOverlay(ModItems.INFO_ENCHANTMENT_UPGRADE, new ItemOverlay(Items.PAPER));
        PolymerItem.registerOverlay(ModItems.INFO_PINNACLE_ENCHANTMENT, new ItemOverlay(Items.PAPER));
        PolymerBlock.registerOverlay(ModBlocks.BROKEN_ANVIL, new PolymerBlock() {
            @Override public boolean canSyncRawToClient(PacketContext context) {
                return com.thenathe.combinedshim.NativeClients.isNative(context, "simple_smithing_overhaul");
            }
            @Override public net.minecraft.world.level.block.state.BlockState getPolymerBlockState(
                    net.minecraft.world.level.block.state.BlockState state, PacketContext context) {
                return canSyncRawToClient(context) ? state
                        : Blocks.DAMAGED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING));
            }
            @Override public boolean handleMiningOnServer(ItemStack stack,
                    net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos,
                    net.minecraft.server.level.ServerPlayer player) {
                return !canSyncRawToClient(player.connection.getPacketContext());
            }
        });
        PolymerComponent.registerDataComponent(ModDataComponents.REPAIR_COUNT,
                ModDataComponents.PINNACLE_COUNT, ModDataComponents.BROKEN);
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.RECIPE_SERIALIZER, ModRecipeSerializers.PORTABLE_ITEM_REPAIR);
        // Broken vanilla gear also needs a server-computed display name and attributes.
        PolymerItemUtils.CONTEXT_ITEM_CHECK.register((stack, context) ->
                stack.count() > 0 && stack.typeHolder().value() != Items.AIR
                && !com.thenathe.combinedshim.NativeClients.isNative(context, "simple_smithing_overhaul")
                && (stack.get(ModDataComponents.BROKEN) != null || stack.get(ModDataComponents.PINNACLE_COUNT) != null
                || RepairableOverrides.get(stack.typeHolder().value()) != null));
        PolymerItemUtils.ITEM_MODIFICATION_EVENT.register((original, client, context) -> {
            if (original.isEmpty() || client.isEmpty()) return client;
            if (com.thenathe.combinedshim.NativeClients.isNative(context, "simple_smithing_overhaul")) return client;
            client = client.copy();
            // The new upstream registry changes effective getters rather than item
            // prototypes. Explicitly carry that value to clients without its table.
            var repairable = repairableForClient(original.get(DataComponents.REPAIRABLE), context);
            if (repairable == null) client.remove(DataComponents.REPAIRABLE);
            else client.set(DataComponents.REPAIRABLE, repairable);
            if (ModUtil.isBroken(original)) {
                client.set(DataComponents.CUSTOM_NAME, TextFallbacks.withFallback(original.getHoverName()));
                client.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            }
            var custom = client.get(DataComponents.CUSTOM_NAME);
            if (custom != null) client.set(DataComponents.CUSTOM_NAME, TextFallbacks.withFallback(custom));
            var name = client.get(DataComponents.ITEM_NAME);
            if (name != null) client.set(DataComponents.ITEM_NAME, TextFallbacks.withFallback(name));
            var lore = client.get(DataComponents.LORE);
            if (lore != null) client.set(DataComponents.LORE,
                    new ItemLore(lore.lines().stream().map(TextFallbacks::withFallback).toList()));
            return client;
        });
        MenuGuidance.initialize();
    }

    private static Repairable repairableForClient(Repairable repairable, PacketContext context) {
        if (repairable == null) return null;
        var materials = repairable.items().stream().filter(holder -> {
            if (com.thenathe.combinedshim.WireRegistries.hasMapping(context)) {
                return com.thenathe.combinedshim.WireRegistries.isVisible(BuiltInRegistries.ITEM, holder.value(), context);
            }
            return !RegistrySyncUtils.isServerEntry(BuiltInRegistries.ITEM, holder.value())
                    || PolymerSyncedObject.canSyncRawToClient(BuiltInRegistries.ITEM, holder.value(), context);
        }).toList();
        if (materials.isEmpty()) return null;
        return materials.size() == repairable.items().size() ? repairable
                : new Repairable(HolderSet.direct(materials));
    }

    private record ItemOverlay(Item fallback) implements PolymerItem {
        @Override
        public Item getPolymerItem(ItemStack stack, PacketContext context) { return nativeClient(context) ? stack.getItem() : fallback; }
        @Override public boolean canSyncRawToClient(PacketContext context) { return nativeClient(context); }
        private static boolean nativeClient(PacketContext context) { return com.thenathe.combinedshim.NativeClients.isNative(context, "simple_smithing_overhaul"); }

        @Override
        public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
            return nativeClient(context) || (context != null && PolymerResourcePackUtils.hasMainPack(context))
                    ? stack.get(DataComponents.ITEM_MODEL)
                    : fallback.components().get(DataComponents.ITEM_MODEL);
        }

        @Override
        public ItemStack getPolymerItemStack(ItemStack stack, TooltipFlag tooltip,
                                            PacketContext context, HolderLookup.Provider lookup) {
            if (nativeClient(context)) return stack;
            var result = PolymerItem.super.getPolymerItemStack(stack, tooltip, context, lookup);
            result.set(DataComponents.ITEM_NAME, TextFallbacks.withFallback(stack.getItem().getName(stack)));
            result.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, stack.hasFoil());
            return result;
        }
    }
}
