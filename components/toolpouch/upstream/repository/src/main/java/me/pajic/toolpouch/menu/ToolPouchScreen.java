package me.pajic.toolpouch.menu;

import com.mojang.blaze3d.platform.InputConstants;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.keybind.ModKeybinds;
import me.pajic.toolpouch.keybind.SlotToggleKeys;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class ToolPouchScreen extends AbstractContainerScreen<ToolPouchMenu> {

	private static final Identifier GUI_TEXTURE = ToolPouch.id("tool_pouch");
	private static final Identifier SLOT_TEXTURE = Identifier.withDefaultNamespace("container/slot");
	private final int rows;
	private final int columns;
	private int bindingSlot = -1;

	public ToolPouchScreen(ToolPouchMenu menu, Inventory playerInventory, Component ignored) {
		super(
                menu,
                playerInventory,
                menu.getToolPouch().getHoverName(),
                menu.getWidth(ToolPouchUtil.getToolPouchColumns(menu.getToolPouch())),
                menu.getHeight(ToolPouchUtil.getToolPouchRows(menu.getToolPouch()))
        );
		rows = ToolPouchUtil.getToolPouchRows(menu.getToolPouch());
		columns = ToolPouchUtil.getToolPouchColumns(menu.getToolPouch());
		titleLabelY = 6;
		inventoryLabelY = imageHeight - 95;
	}

	@Override
	public void extractBackground(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
		super.extractBackground(guiGraphics, mouseX, mouseY, a);
		int x = (width - imageWidth) / 2;
		int y = (height - imageHeight) / 2;
		guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED, GUI_TEXTURE,
                x, y,
                columns <= 9 ? 176 : 176 + (columns - 9) * 18, 115 + rows * 18
        );
		for (Slot slot : getMenu().slots) {
			guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE,
                    x + slot.x - 1, y + slot.y - 1,
                    18, 18
            );
			if (isToolPouchSlot(slot)) {
				if (slot.getContainerSlot() == bindingSlot) guiGraphics.fill(
                        x + slot.x, y + slot.y,
                        x + slot.x + 16, y + slot.y + 16,
                        0x80FFD700
                );
				else if (ToolPouchUtil.isSlotDisabled(menu.getToolPouch(), slot.getContainerSlot())) guiGraphics.fill(
                        x + slot.x, y + slot.y,
                        x + slot.x + 16, y + slot.y + 16,
                        0x80FF2020
                );
			}
		}
	}

	@Override
	protected void extractSlots(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		super.extractSlots(guiGraphics, mouseX, mouseY);
		guiGraphics.nextStratum();
		for (Slot slot : menu.slots) {
			if (!isToolPouchSlot(slot)) continue;
			String label;
			if (slot.getContainerSlot() == bindingSlot) label = "...";
			else {
				InputConstants.Key key = SlotToggleKeys.getKey(slot.getContainerSlot());
				if (key == null) continue;
				label = font.plainSubstrByWidth(key.getDisplayName().getString(), 30);
			}
			guiGraphics.pose().pushMatrix();
			guiGraphics.pose().translate(slot.x + 1, slot.y + 1);
			guiGraphics.pose().scale(0.5F, 0.5F);
			guiGraphics.text(font, label, 0, 0, 0xFFFFFFFF, true);
			guiGraphics.pose().popMatrix();
		}
	}

	@Override
	public boolean keyPressed(@NotNull KeyEvent event) {
		InputConstants.Key key = InputConstants.getKey(event);
		if (bindingSlot != -1) {
			if (event.key() == InputConstants.KEY_ESCAPE) SlotToggleKeys.unbind(bindingSlot, menu.getToolPouch());
			else SlotToggleKeys.bind(bindingSlot, key);
			bindingSlot = -1;
			return true;
		}
		if (hoveredSlot != null && isToolPouchSlot(hoveredSlot) && ModKeybinds.BIND_SLOT_TOGGLE.matches(event)) {
			bindingSlot = hoveredSlot.getContainerSlot();
			return true;
		}
		if (minecraft.player != null && SlotToggleKeys.toggleSlots(minecraft.player, menu.getToolPouch(), key, false)) return true;
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
		if (bindingSlot != -1) {
			if (event.button() > 1) SlotToggleKeys.bind(bindingSlot, InputConstants.Type.MOUSE.getOrCreate(event.button()));
			bindingSlot = -1;
			return true;
		}
		if (
				event.button() > 1 && minecraft.player != null &&
				SlotToggleKeys.toggleSlots(minecraft.player, menu.getToolPouch(), InputConstants.Type.MOUSE.getOrCreate(event.button()), false)
		) return true;
		return super.mouseClicked(event, doubleClick);
	}

	private boolean isToolPouchSlot(Slot slot) {
		return slot.container == menu.getContainer();
	}
}
