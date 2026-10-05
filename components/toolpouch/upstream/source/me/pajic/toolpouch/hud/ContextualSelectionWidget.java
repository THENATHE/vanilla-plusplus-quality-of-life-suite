package me.pajic.toolpouch.hud;

import me.pajic.toolpouch.ToolPouchClient;
import me.pajic.toolpouch.keybind.ModKeybinds;
import me.pajic.toolpouch.keybind.ScrollHandler;
import me.pajic.toolpouch.network.ModPayloads;
import me.pajic.toolpouch.platform.MultiLoaderUtil;
import me.pajic.toolpouch.renderer.WidgetRenderer;
import me.pajic.toolpouch.util.GameplayUtil;
import me.pajic.toolpouch.util.ItemStackTemplateUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.List;

public class ContextualSelectionWidget {

    public static boolean widgetOpen = false;
    private static final Minecraft MC = Minecraft.getInstance();

    public static void render(GuiGraphicsExtractor guiGraphics) {
        if (MC.player != null && MC.level != null) {
			if (GameplayUtil.isHoldingProjectileWeapon(MC.player)) {
				List<ItemStackTemplate> ammo = ToolPouchUtil.getItemsFromToolPouch(MC.player, GameplayUtil.getSupportedAmmo(MC.player));
				if (!ammo.isEmpty()) {
					int count = ammo.size();
					if (ScrollHandler.selectedArrowSlot >= count) ScrollHandler.selectedArrowSlot = 0;
					if (ItemStackTemplateUtil.isEmpty(ammo.get(ScrollHandler.selectedArrowSlot))) {
						do {
							ScrollHandler.selectedArrowSlot++;
							if (ScrollHandler.selectedArrowSlot >= count) {
								ScrollHandler.selectedArrowSlot = 0;
							}
						} while (ItemStackTemplateUtil.isEmpty(ammo.get(ScrollHandler.selectedArrowSlot)));
						MultiLoaderUtil.INSTANCE.sendToServer(new ModPayloads.C2SSyncArrowSlot(ScrollHandler.selectedArrowSlot));
					}
					if (widgetOpen || (ToolPouchClient.CONFIG.quickSelect.get() && !MC.player.isUsingItem() && !MC.gui.hud.isHidden() && ModKeybinds.OPEN_WIDGET.isDown())) {
						if (ToolPouchClient.CONFIG.quickSelect.get()) widgetOpen = true;
						WidgetRenderer.renderCenterSlot(MC, guiGraphics);
						for (int i = 0; i < count; i++) {
							ItemStack arrow = ammo.get(i).create();
							if (!arrow.isEmpty()) {
								WidgetRenderer.renderItemStack(
										MC, guiGraphics, arrow,
										i - ScrollHandler.selectedArrowSlot
								);
							}
							if (i == ScrollHandler.selectedArrowSlot)
								WidgetRenderer.renderCenterText(MC, arrow.getHoverName(), guiGraphics, -48);
						}
						if (ToolPouchClient.CONFIG.showUIHints.get()) {
							Component scrollHint = Component.translatable("gui.toolpouch.hint_arrow_scroll");
							Component exitHint = Component.translatable(
									"gui.toolpouch.hint_arrow_exit",
									Component.keybind(ModKeybinds.OPEN_WIDGET.getName())
							);
							WidgetRenderer.renderCenterText(MC, scrollHint, guiGraphics, 12);
							WidgetRenderer.renderCenterText(MC, exitHint, guiGraphics, 24);
						}
					} else {
						widgetOpen = false;
					}
				}
			} else {
				List<ItemStackTemplate> shulkers = ToolPouchUtil.getItemsFromToolPouch(MC.player, stack -> stack.is(ItemTags.SHULKER_BOXES));
				if (!shulkers.isEmpty()) {
					int count = shulkers.size();
					if (ScrollHandler.selectedShulkerSlot >= count) ScrollHandler.selectedShulkerSlot = 0;
					if (ItemStackTemplateUtil.isEmpty(shulkers.get(ScrollHandler.selectedShulkerSlot))) {
						do {
							ScrollHandler.selectedShulkerSlot++;
							if (ScrollHandler.selectedShulkerSlot >= count) {
								ScrollHandler.selectedShulkerSlot = 0;
							}
						} while (ItemStackTemplateUtil.isEmpty(shulkers.get(ScrollHandler.selectedShulkerSlot)));
						MultiLoaderUtil.INSTANCE.sendToServer(new ModPayloads.C2SSyncShulkerSlot(ScrollHandler.selectedShulkerSlot));
					}
					if (widgetOpen || (ToolPouchClient.CONFIG.quickSelect.get() && !MC.gui.hud.isHidden() && ModKeybinds.OPEN_WIDGET.isDown())) {
						if (count == 1) {
							MC.player.playSound(SoundEvents.SHULKER_BOX_OPEN);
                            MultiLoaderUtil.INSTANCE.sendToServer(new ModPayloads.C2SOpenShulkerBoxPayload(ScrollHandler.selectedShulkerSlot));
							widgetOpen = false;
							if (ToolPouchClient.CONFIG.quickSelect.get()) ModKeybinds.OPEN_WIDGET.setDown(false);
						} else {
							if (ToolPouchClient.CONFIG.quickSelect.get()) widgetOpen = true;
							WidgetRenderer.renderCenterSlot(MC, guiGraphics);
							for (int i = 0; i < count; i++) {
								ItemStack shulker = shulkers.get(i).create();
								if (!shulker.isEmpty()) WidgetRenderer.renderItemStack(
										MC, guiGraphics, shulker,
										i - ScrollHandler.selectedShulkerSlot
								);
								if (i == ScrollHandler.selectedShulkerSlot)
									WidgetRenderer.renderCenterText(MC, shulker.getHoverName(), guiGraphics, -48);
							}
							if (ToolPouchClient.CONFIG.showUIHints.get()) {
								Component scrollHint = Component.translatable("gui.toolpouch.hint_shulker_scroll");
								Component tooltipHint = Component.translatable(
										"gui.toolpouch.hint_shulker_tooltip",
										Component.keybind(MC.options.keyShift.getName())
								);
								Component exitHint = ToolPouchClient.CONFIG.quickSelect.get() ? Component.translatable(
										"gui.toolpouch.hint_shulker_exit_quick",
										Component.keybind(ModKeybinds.OPEN_WIDGET.getName())
								) : Component.translatable(
										"gui.toolpouch.hint_shulker_exit",
										Component.keybind(ModKeybinds.OPEN_WIDGET.getName())
								);
								WidgetRenderer.renderCenterText(MC, scrollHint, guiGraphics, 12);
								WidgetRenderer.renderCenterText(MC, tooltipHint, guiGraphics, 24);
								WidgetRenderer.renderCenterText(MC, exitHint, guiGraphics, 36);
							}
							if (MC.player.isShiftKeyDown()) {
								ItemStack stack = shulkers.get(ScrollHandler.selectedShulkerSlot).create();
								List<ClientTooltipComponent> list = Screen.getTooltipFromItem(MC, stack).stream()
										.map(Component::getVisualOrderText)
										.map(ClientTooltipComponent::create)
										.collect(Util.toMutableList());
								stack.getTooltipImage().ifPresent(tooltipComponent ->
										list.add(list.isEmpty() ? 0 : 1, ClientTooltipComponent.create(tooltipComponent))
								);
								guiGraphics.tooltip(
										MC.font, list,
										MC.getWindow().getGuiScaledWidth() / 2,
										MC.getWindow().getGuiScaledHeight() / 2,
										DefaultTooltipPositioner.INSTANCE,
										stack.get(DataComponents.TOOLTIP_STYLE)
                                        //? >26.2
                                        , false
								);
							}
						}
					}
				}
			}
        }
    }
}
