package com.thenathe.backpackcrafting.client;

import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import com.thenathe.backpackcrafting.mixin.client.RecipeBookBoundsAccessor;
import me.pajic.tiered_backpacks.keybind.ModKeybinds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.CraftingRecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/** Uses the vanilla recipe-book component, including compatible BRBR extensions. */
public final class BackpackCraftingScreen extends AbstractRecipeBookScreen<BackpackCraftingMenu> {
    private static final Identifier SLOT = Identifier.withDefaultNamespace("container/slot");
    private final BackpackRecipeBook backpackRecipeBook;
    private ItemContainerContents lastContents;

    public BackpackCraftingScreen(BackpackCraftingMenu menu, Inventory inventory, Component title) {
        this(menu, inventory, new BackpackRecipeBook(menu));
    }

    private BackpackCraftingScreen(BackpackCraftingMenu menu, Inventory inventory, BackpackRecipeBook recipeBook) {
        super(menu, recipeBook, inventory, menu.getBackpack().getHoverName());
        backpackRecipeBook = recipeBook;
        titleLabelX = 8;
        titleLabelY = 5;
        inventoryLabelX = menu.getStorageWidth() / 2 - 80;
        inventoryLabelY = menu.getInventoryY() - 11;
    }

    @Override
    protected ScreenPosition getRecipeBookButtonPosition() {
        return new ScreenPosition(leftPos + menu.getStorageWidth() + 8 + (menu.getCraftingPanelWidth() - 20) / 2,
                topPos + menu.getCraftingPanelY() + menu.getCraftingPanelHeight() - 25);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        double x = mouseX - left;
        double y = mouseY - top;
        boolean insideStorage = x >= 0 && x < menu.getStorageWidth() && y >= 0 && y < imageHeight;
        int craftingLeft = menu.getStorageWidth() + 8;
        int craftingTop = menu.getCraftingPanelY();
        boolean insideCrafting = x >= craftingLeft && x < craftingLeft + menu.getCraftingPanelWidth()
                && y >= craftingTop && y < craftingTop + menu.getCraftingPanelHeight();
        // Only painted panels protect the cursor. The gap and the space above or
        // below the floating crafting panel are ordinary outside-click drop areas.
        // Keep extension controls, including BRBR's separate Pins tab, protected too.
        return !insideStorage && !insideCrafting && !backpackRecipeBook.isMouseOver(mouseX, mouseY)
                && backpackRecipeBook.hasClickedOutside(mouseX, mouseY, leftPos, topPos,
                        menu.getStorageWidth(), imageHeight);
    }

    @Override
    public void containerTick() {
        ItemContainerContents contents = menu.getBackpack().get(DataComponents.CONTAINER);
        if (contents != lastContents) {
            lastContents = contents;
            // Vanilla refreshes ingredient counts when this local revision changes.
            // Keep ghost recipes intact instead of synthesizing a slot click.
            minecraft.player.getInventory().setChanged();
        }
        super.containerTick();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        drawPanel(graphics, leftPos, topPos, menu.getStorageWidth(), imageHeight);
        drawPanel(graphics, leftPos + menu.getStorageWidth() + 8, topPos + menu.getCraftingPanelY(),
                menu.getCraftingPanelWidth(), menu.getCraftingPanelHeight());
        for (Slot slot : menu.slots) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT,
                    leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18);
        }
        graphics.text(font, Component.translatable("container.crafting"), leftPos + menu.getStorageWidth() + 18,
                topPos + menu.getCraftingPanelY() + 6, 0xff404040, false);
        int arrowX = leftPos + menu.getStorageWidth() + 16 + menu.getGridWidth() * 18 + 4;
        int arrowY = topPos + menu.getCraftingPanelY() + 20 + menu.getGridHeight() * 9 - 4;
        graphics.fill(arrowX, arrowY + 2, arrowX + 12, arrowY + 6, 0xff777777);
        for (int offset = 0; offset < 4; offset++) {
            graphics.fill(arrowX + 12 + offset, arrowY + offset,
                    arrowX + 13 + offset, arrowY + 8 - offset, 0xff777777);
        }
    }

    private static void drawPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xff373737);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xff555555);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xffc6c6c6);
        graphics.fill(x + 3, y + 3, x + width - 3, y + 4, 0xffffffff);
        graphics.fill(x + 3, y + 3, x + 4, y + height - 3, 0xffffffff);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!isInputCaptured() && ModKeybinds.OPEN_BACKPACK.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private static final class BackpackRecipeBook extends CraftingRecipeBookComponent {
        private final BackpackCraftingMenu backpackMenu;

        private BackpackRecipeBook(BackpackCraftingMenu menu) {
            super(menu);
            backpackMenu = menu;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!isVisible()) return false;
            if (super.isMouseOver(mouseX, mouseY)) return true;
            var bounds = (RecipeBookBoundsAccessor) (RecipeBookComponent<?>) this;
            int x = bounds.backpackCrafting$getBookX();
            int y = bounds.backpackCrafting$getBookY();
            if (mouseX >= x && mouseX < x + RecipeBookComponent.IMAGE_WIDTH
                    && mouseY >= y && mouseY < y + RecipeBookComponent.IMAGE_HEIGHT) return true;
            for (var tab : bounds.backpackCrafting$getTabs()) {
                if (tab.visible && tab.isMouseOver(mouseX, mouseY)) return true;
            }
            return false;
        }

        @Override
        protected boolean isCraftingSlot(Slot slot) {
            return backpackMenu.isStorageSlot(slot) || super.isCraftingSlot(slot);
        }
    }
}
