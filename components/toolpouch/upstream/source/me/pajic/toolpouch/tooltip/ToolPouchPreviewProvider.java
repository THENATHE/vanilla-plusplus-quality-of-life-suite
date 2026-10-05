package me.pajic.toolpouch.tooltip;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.color.ColorKey;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ToolPouchPreviewProvider implements PreviewProvider {

    @Override
    public boolean shouldDisplay(@NotNull PreviewContext context) {
        return !getInventory(context).stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public List<ItemStack> getInventory(@NotNull PreviewContext context) {
        ItemContainerContents contents = context.stack().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        //~ if >26.2 'allItemsCopyStream' -> 'itemCopies'
        return contents.itemCopies().toList();
    }

    @Override
    public int getInventoryMaxSize(@NotNull PreviewContext context) {
        return ToolPouchUtil.getToolPouchSize(context.stack());
    }

    @Override
    public int getMaxRowSize(@NotNull PreviewContext context) {
        return ToolPouchUtil.getToolPouchColumns(context.stack());
    }

    @Override
    public ColorKey getWindowColorKey(@NotNull PreviewContext context) {
        return ColorKey.ofRgb(DyedItemColor.getOrDefault(context.stack(), -6265536));
    }
}
