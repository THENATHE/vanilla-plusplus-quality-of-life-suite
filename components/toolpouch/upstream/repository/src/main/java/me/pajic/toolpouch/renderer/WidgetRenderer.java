package me.pajic.toolpouch.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class WidgetRenderer {

    public static void renderItemStack(Minecraft mc, GuiGraphicsExtractor guiGraphics, ItemStack stack, int offset) {
        int stackX = mc.getWindow().getGuiScaledWidth() / 2 - 8 + 24 * offset;
        int stackY = mc.getWindow().getGuiScaledHeight() / 2 - 28;
        guiGraphics.fakeItem(stack, stackX, stackY);
        guiGraphics.itemDecorations(mc.font, stack, stackX, stackY);
    }

    public static void renderCenterSlot(Minecraft mc, GuiGraphicsExtractor guiGraphics) {
        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                Identifier.withDefaultNamespace("hud/hotbar_offhand_right"),
                mc.getWindow().getGuiScaledWidth() / 2 - 18,
                mc.getWindow().getGuiScaledHeight() / 2 - 32,
                29, 24
        );
    }

    public static void renderCenterText(Minecraft mc, Component text, GuiGraphicsExtractor guiGraphics, int offset) {
        guiGraphics.text(
                mc.font, text,
                mc.getWindow().getGuiScaledWidth() / 2 - mc.font.width(text) / 2,
                mc.getWindow().getGuiScaledHeight() / 2 + offset,
                0xffffffff
        );
    }
}
