package com.thenathe.backpackcrafting.compat;

import com.thenathe.backpackcrafting.client.SortedRuleContext;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractContainerScreen.class)
public abstract class SortedRuleScreenMixin {
    @Shadow @Final protected AbstractContainerMenu menu;
    @Shadow protected Slot hoveredSlot;

    @WrapMethod(method = "extractSlot")
    private void backpackCrafting$craftingMarkers(GuiGraphicsExtractor graphics, Slot slot,
                                                  int mouseX, int mouseY, Operation<Void> original) {
        if (SortedRuleContext.isCraftingSlot(menu, slot)) {
            SortedRuleContext.withoutRules(() -> original.call(graphics, slot, mouseX, mouseY));
        } else {
            original.call(graphics, slot, mouseX, mouseY);
        }
    }

    @WrapMethod(method = "mouseClicked")
    private boolean backpackCrafting$craftingRuleClick(MouseButtonEvent event, boolean doubleClick,
                                                      Operation<Boolean> original) {
        if (SortedRuleContext.isCraftingSlot(menu, hoveredSlot)) {
            return SortedRuleContext.withoutRules(() -> original.call(event, doubleClick));
        }
        return original.call(event, doubleClick);
    }

    @WrapMethod(method = "mouseScrolled")
    private boolean backpackCrafting$craftingRuleScroll(double mouseX, double mouseY,
                                                       double horizontal, double vertical,
                                                       Operation<Boolean> original) {
        if (SortedRuleContext.isCraftingSlot(menu, hoveredSlot)) {
            return SortedRuleContext.withoutRules(() -> original.call(mouseX, mouseY, horizontal, vertical));
        }
        return original.call(mouseX, mouseY, horizontal, vertical);
    }
}
