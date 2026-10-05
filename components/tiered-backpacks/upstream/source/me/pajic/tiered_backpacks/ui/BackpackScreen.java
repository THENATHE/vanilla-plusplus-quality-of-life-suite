package me.pajic.tiered_backpacks.ui;

import me.pajic.tiered_backpacks.keybind.ModKeybinds;
import me.pajic.tiered_backpacks.util.BackpackDimensions;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class BackpackScreen extends AbstractContainerScreen<BackpackMenu> {

    private static final Identifier GUI_SPRITE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");
    private final int rows;
    private final int columns;

    public BackpackScreen(BackpackMenu menu, Inventory playerInventory, Component ignored) {
		BackpackDimensions dimensions = BackpackUtil.getBackpackDimensions(menu.getBackpack());
        super(menu, playerInventory, menu.getBackpack().getHoverName(), menu.getWidth(dimensions.columns.get()), menu.getHeight(dimensions.rows.get()));
        rows = dimensions.rows.get();
        columns = dimensions.columns.get();
        titleLabelY = 5;
        titleLabelX = 9;
        inventoryLabelY = imageHeight - 95;
        inventoryLabelX = 9;
    }

	@Override
	public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		int x = leftPos + 1;
		int y = topPos - 1;
        int w = columns <= 9 ? 176 : 176 + (columns - 9) * 18;
        int h = 115 + rows * 18;
        int innerW = w - 2 * 4;
        int innerH = h - 2 * 4;

        part(graphics, x + 4, y + 4, 4, 4, innerW, innerH, 4, 4);

        part(graphics, x + 4, y, 8, 0, innerW, 4, 1, 4);
        part(graphics, x + 4, y + h - 4, 8, 218, innerW, 4, 1, 4);
        part(graphics, x, y + 4, 0, 8, 4, innerH, 4, 1);
        part(graphics, x + w - 4, y + 4, 172, 8, 4, innerH, 4, 1);

        part(graphics, x, y, 0, 0, 4, 4, 4, 4);
        part(graphics, x + w - 4, y, 172, 0, 4, 4, 4, 4);
        part(graphics, x, y + h - 4, 0, 218, 4, 4, 4, 4);
        part(graphics, x + w - 4, y + h - 4, 172, 218, 4, 4, 4, 4);

		for (Slot slot : getMenu().slots) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, x + slot.x - 2, y + slot.y, 18, 18);
		}
	}

    private static void part(GuiGraphicsExtractor g, int x, int y, int u, int v, int w, int h, int rW, int rH) {
        g.blit(RenderPipelines.GUI_TEXTURED, GUI_SPRITE, x, y, u, v, w, h, rW, rH, 256, 256);
    }

	@Override
	public boolean keyPressed(@NotNull KeyEvent event) {
		if (ModKeybinds.OPEN_BACKPACK.matches(event)) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}
}
