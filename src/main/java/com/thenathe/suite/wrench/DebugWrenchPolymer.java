package com.thenathe.suite.wrench;

import com.thenathe.suite.network.SuiteCapabilities;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Loaded only when Polymer is present. Both clients use the same fixed 16x16 sprite. */
final class DebugWrenchPolymer {
    private DebugWrenchPolymer() {}

    static void initialize() {
        PolymerResourcePackUtils.addModAssets("vpp_mod_suite");
        PolymerResourcePackUtils.markAsRequired();
        PolymerItem.registerOverlay(DebugWrench.WRENCH, new WrenchOverlay());
    }

    private static final class WrenchOverlay implements PolymerItem {
        private boolean nativeClient(PacketContext context) {
            return SuiteCapabilities.isNative(context, DebugWrench.MOD_ID);
        }

        @Override
        public boolean canSyncRawToClient(PacketContext context) {
            return nativeClient(context);
        }

        @Override
        public Item getPolymerItem(ItemStack stack, PacketContext context) {
            return nativeClient(context) ? stack.getItem() : Items.IRON_NUGGET;
        }

        @Override
        public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
            return DebugWrench.ITEM_ID;
        }

        @Override
        public ItemStack getPolymerItemStack(ItemStack stack, TooltipFlag tooltip,
                PacketContext context, HolderLookup.Provider lookup) {
            return nativeClient(context) ? stack : PolymerItem.super.getPolymerItemStack(stack, tooltip, context, lookup);
        }

        @Override
        public boolean handleMiningOnServer(ItemStack tool, BlockState state, BlockPos pos, ServerPlayer player) {
            return true;
        }

        @Override
        public boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand,
                ItemStack stack, ServerLevel level, BlockHitResult hit, InteractionResult result) {
            // Consume Polymer's acknowledgement so a vanilla client cannot retry with its offhand.
            return true;
        }
    }
}
