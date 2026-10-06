package com.thenathe.stackablescompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.thenathe.stackablescompat.FallbackContainerPreview;
import com.thenathe.stackablescompat.StackablesCompat;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Also covers native carrier mods on a client that lacks native Stackables support. */
@Mixin(targets = "eu.pb4.polymer.core.api.item.PolymerItemUtils", remap = false)
public abstract class PolymerPreviewMixin {
    @ModifyReturnValue(method = "getPolymerItemStack(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/TooltipFlag;Lnet/fabricmc/fabric/api/networking/v1/context/PacketContext;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
    private static ItemStack suite$safePreview(ItemStack outgoing, ItemStack original,
            TooltipFlag tooltip, PacketContext context, HolderLookup.Provider lookup) {
        var connection = context == null ? null : context.get(PacketContext.CONNECTION);
        if (connection == null || connection.getReceiving() != PacketFlow.SERVERBOUND) return outgoing;
        return StackablesCompat.nativeClient(context) ? outgoing
                : FallbackContainerPreview.project(original,
                        StackablesCompat.projectStackLimit(original, outgoing, false));
    }
}
