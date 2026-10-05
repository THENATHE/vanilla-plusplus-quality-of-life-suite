package me.pajic.toolpouch.menu;

import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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

	public ToolPouchScreen(ToolPouchMenu menu, Inventory playerInventory, Component ignored) {
		super(menu, playerInventory, menu.getToolPouch().getHoverName(), menu.getWidth(ToolPouchUtil.getToolPouchColumns(menu.getToolPouch())), menu.getHeight(ToolPouchUtil.getToolPouchRows(menu.getToolPouch())));
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
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y, columns <= 9 ? 176 : 176 + (columns - 9) * 18, 115 + rows * 18);
		for (Slot slot : getMenu().slots) {
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, x + slot.x - 1, y + slot.y - 1, 18, 18);
		}
	}
}
