package com.thenathe.multiscale.mixin;

import com.thenathe.multiscale.MixedScaleMaps;
import java.util.ArrayList;
import java.util.List;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.AtlasItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AtlasItem.class, remap = false)
public abstract class AtlasMixedScalesMixin {
    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    private static void mixedScales$describeLayers(ItemStack atlas, CallbackInfoReturnable<List<Component>> cir) {
        var lines = new ArrayList<>(cir.getReturnValue());
        if (atlas.getOrDefault(ModDataComponents.ATLAS_SCALE, -1) != -1 && !lines.isEmpty())
            lines.set(0, Component.translatable("mapstitch_mixed_scales.tooltip.active_scale", 1 << MixedScaleMaps.activeScale(atlas)).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("mapstitch_mixed_scales.tooltip.layers").withStyle(ChatFormatting.GRAY));
        cir.setReturnValue(lines);
    }

    @Inject(method = "isValidItemForAtlas", at = @At("RETURN"), cancellable = true)
    private void mixedScales$acceptLayers(ItemStack stack, ItemStack atlas, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (stack.has(DataComponents.MAP_ID)) {
            var data = MapItem.getSavedData(stack, level);
            if (data != null && data.scale >= 0 && data.scale <= 4) cir.setReturnValue(true);
        }
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void mixedScales$selectBeforeTick(ItemStack atlas, ServerLevel level, Entity owner, EquipmentSlot slot, CallbackInfo ci) {
        MixedScaleMaps.selectActive(atlas, level, owner, true);
    }

    @Inject(method = "inventoryTick", at = @At("RETURN"))
    private void mixedScales$keepSelectedLayer(ItemStack atlas, ServerLevel level, Entity owner, EquipmentSlot slot, CallbackInfo ci) {
        MixedScaleMaps.selectActive(atlas, level, owner, false);
    }

    @Inject(method = "updateActiveMap", at = @At("HEAD"), cancellable = true)
    private void mixedScales$scaleAwareSelection(ItemStack atlas, BundleContents ignoredSnapshot, int x, int z, Level level, Entity owner, CallbackInfo ci) {
        if (level instanceof ServerLevel server) MixedScaleMaps.selectActive(atlas, server, owner, true);
        ci.cancel();
    }
}
