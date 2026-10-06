package com.thenathe.multiscale.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.thenathe.multiscale.AtlasOptions;
import com.thenathe.multiscale.AtlasTarget;
import com.thenathe.multiscale.MixedScaleMaps;
import com.thenathe.multiscale.client.MixedScalesClient;
import me.pajic.mapstitch.MapStitchClient;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WorldMapScreen.class, remap = false)
public abstract class WorldMapScaleMixin extends Screen {
    protected WorldMapScaleMixin(Component title) { super(title); }
    @Shadow private int scale;
    @Shadow private static Identifier dimensionId;
    @Unique private int mixedScales$previous;
    @Unique private Identifier mixedScales$previousDimension;
    @Unique private AtlasTarget mixedScales$target;
    @Unique private Button mixedScales$minimap;
    @Unique private Button[] mixedScales$generation;
    @Unique private int mixedScales$minimapScale, mixedScales$mask;
    @Unique private long mixedScales$pendingUntil;
    @Unique private boolean mixedScales$anyAtlasSource;

    @Inject(method = "renderMaps", at = @At("HEAD"))
    private void mixedScales$resetSources(GuiGraphicsExtractor graphics, int minX, int maxX, int minZ, int maxZ, CallbackInfo ci) {
        mixedScales$anyAtlasSource = false;
    }

    @WrapOperation(method = "renderMaps", at = @At(value = "INVOKE",
            target = "Lme/pajic/mapstitch/worldmap/WorldMapScreen;prepareAtlas(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean mixedScales$collectSources(WorldMapScreen screen, ItemStack atlas, Operation<Boolean> original) {
        // Scan every native/accessory/pouch source in its original order. An empty
        // later book must not hide earlier maps; reset the accumulator each frame.
        mixedScales$anyAtlasSource |= original.call(screen, atlas);
        return mixedScales$anyAtlasSource;
    }

    @WrapOperation(method = "prepareAtlas", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStackTemplate;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object mixedScales$authoritativeWorldCenter(ItemStackTemplate map, DataComponentType<?> component,
                                                       Operation<Object> original) {
        if (component == me.pajic.mapstitch.component.ModDataComponents.MAP_CENTER) {
            var level = Minecraft.getInstance().level;
            var id = map.get(DataComponents.MAP_ID);
            var data = level == null || id == null ? null : level.getMapData(id);
            if (data != null && data.scale >= 0 && data.scale <= 4
                    && dimensionId.equals(data.dimension.identifier())) {
                // Use the same saved map for tile placement and Ctrl+Q's grid lookup.
                // Item centers can be absent/stale even when pixels are synchronized.
                return new Vector2i(data.centerX, data.centerZ);
            }
        }
        return original.call(map, component);
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Tooltip;create(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/client/gui/components/Tooltip;", ordinal = 3), index = 0)
    private Component mixedScales$scaleHelp(Component original) {
        return mixedScales$target == null ? original : Component.translatable("mapstitch_mixed_scales.screen.scale_help");
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mixedScales$bindBook(int scaleOverride, CallbackInfo ci) {
        mixedScales$target = MixedScalesClient.openTarget(scaleOverride);
        // Opening uses the normal world-map view, independently of saved minimap options.
        mixedScales$previous = scale;
        mixedScales$previousDimension = dimensionId;
        WorldMapScreen.clearMaps();
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void mixedScales$buttons(CallbackInfo ci) {
        mixedScales$minimap = null;
        mixedScales$generation = null;
        if (mixedScales$target == null || !MapStitchClient.CONFIG.worldMap.buttons.get()) return;
        var player = Minecraft.getInstance().player;
        var handle = player == null ? null : mixedScales$target.resolve(player);
        if (handle == null) return;
        mixedScales$minimapScale = MixedScaleMaps.activeScale(handle.atlas());
        mixedScales$mask = AtlasOptions.generationMask(handle.atlas());
        mixedScales$pendingUntil = 0;
        // One aligned strip below the coordinate readout; native sidebar positions stay untouched.
        int controlsX = width - 152;
        int controlsY = 16;
        mixedScales$minimap = addRenderableWidget(Button.builder(Component.empty(), b -> {
            int next = (mixedScales$minimapScale + 1) % 5;
            if (MixedScalesClient.select(mixedScales$target, next)) {
                mixedScales$minimapScale = next;
                mixedScales$pendingUntil = System.nanoTime() + 2_000_000_000L;
                mixedScales$labels();
            }
        }).pos(controlsX, controlsY).size(32, 16).build());
        mixedScales$generation = new Button[5];
        for (int layer = 0; layer < 5; layer++) {
            final int selected = layer;
            mixedScales$generation[layer] = addRenderableWidget(Button.builder(Component.empty(), b -> {
                int next = mixedScales$mask ^ (1 << selected);
                if (MixedScalesClient.selectGeneration(mixedScales$target, next)) {
                    mixedScales$mask = next;
                    mixedScales$pendingUntil = System.nanoTime() + 2_000_000_000L;
                    mixedScales$labels();
                }
            }).pos(controlsX + 40 + layer * 22, controlsY).size(20, 16).build());
        }
        mixedScales$labels();
    }

    @Unique private void mixedScales$labels() {
        if (mixedScales$minimap == null) return;
        Component generating = mixedScales$mask == 0 ? Component.translatable("mapstitch_mixed_scales.screen.none")
                : Component.literal(AtlasOptions.generationLabel(mixedScales$mask));
        mixedScales$minimap.setMessage(Component.literal("M" + (1 << mixedScales$minimapScale)));
        mixedScales$minimap.setTooltip(Tooltip.create(Component.translatable("mapstitch_mixed_scales.screen.minimap_help", 1 << mixedScales$minimapScale, generating)));
        for (int layer = 0; layer < 5; layer++) {
            var button = mixedScales$generation[layer];
            boolean enabled = (mixedScales$mask & (1 << layer)) != 0;
            button.setMessage(Component.literal(Integer.toString(1 << layer)).withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            button.setTooltip(Tooltip.create(Component.translatable("mapstitch_mixed_scales.screen.generation_help", 1 << layer,
                    Component.translatable(enabled ? "mapstitch_mixed_scales.screen.enabled" : "mapstitch_mixed_scales.screen.disabled"), generating)));
        }
    }

    @Unique private void mixedScales$refresh() {
        if (mixedScales$minimap == null) return;
        var player = Minecraft.getInstance().player;
        AtlasTarget.Handle handle = null;
        if (player != null) for (var target : AtlasTarget.all(player)) {
            if (target.location() == mixedScales$target.location() && target.index() == mixedScales$target.index() && target.identity().equals(mixedScales$target.identity())) {
                handle = target.resolve(player); break;
            }
        }
        mixedScales$minimap.active = handle != null && MixedScalesClient.canEdit();
        for (var button : mixedScales$generation) button.active = handle != null && MixedScalesClient.canEdit();
        if (handle == null) return;
        int currentScale = MixedScaleMaps.activeScale(handle.atlas()), currentMask = AtlasOptions.generationMask(handle.atlas());
        if (System.nanoTime() >= mixedScales$pendingUntil || (currentScale == mixedScales$minimapScale && currentMask == mixedScales$mask)) {
            mixedScales$minimapScale = currentScale; mixedScales$mask = currentMask;
            mixedScales$pendingUntil = 0; mixedScales$labels();
        }
    }

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    private void mixedScales$controlsArea(GuiGraphicsExtractor graphics, int x, int y, float delta, CallbackInfo ci) {
        if (mixedScales$minimap == null) return;
        int left = mixedScales$minimap.getX(), top = mixedScales$minimap.getY();
        graphics.fill(left - 4, top - 4, left + 152, top + 20, 0xb0000000);
        graphics.fill(left + 35, top, left + 36, top + 16, 0x80ffffff);
    }

    @Unique private void mixedScales$changed() {
        if (scale == mixedScales$previous && java.util.Objects.equals(dimensionId, mixedScales$previousDimension)) return;
        mixedScales$previous = scale;
        mixedScales$previousDimension = dimensionId;
        WorldMapScreen.clearMaps();
    }
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void mixedScales$renderScale(GuiGraphicsExtractor graphics, int x, int y, float delta, CallbackInfo ci) {
        mixedScales$changed(); mixedScales$refresh();
    }
    @Inject(method = {"keyPressed", "mouseClicked"}, at = @At("RETURN"))
    private void mixedScales$inputScale(CallbackInfoReturnable<Boolean> cir) { mixedScales$changed(); }
}
