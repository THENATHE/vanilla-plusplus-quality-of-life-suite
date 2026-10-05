package me.pajic.tiered_backpacks.tooltip;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.color.ColorKey;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import me.pajic.tiered_backpacks.component.ModDataComponents;
import me.pajic.tiered_backpacks.util.BackpackDimensions;
import me.pajic.tiered_backpacks.util.BackpackTier;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BackpackPreviewProvider implements PreviewProvider {

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
        BackpackDimensions dimensions = BackpackUtil.getBackpackDimensions(context.stack());
        return dimensions.rows.get() * dimensions.columns.get();
    }

    @Override
    public int getMaxRowSize(@NotNull PreviewContext context) {
        BackpackDimensions dimensions = BackpackUtil.getBackpackDimensions(context.stack());
        return dimensions.columns.get();
    }

    @Override
    public ColorKey getWindowColorKey(@NotNull PreviewContext context) {
		ItemStack stack = context.stack();
		DyedItemColor dye;
		if (stack.is(ItemTags.CHEST_ARMOR)) dye = stack.get(ModDataComponents.STORED_BACKPACK_DYE);
		else dye = stack.get(DataComponents.DYED_COLOR);
		return dye != null ?
				ColorKey.ofRgb(dye.rgb()) :
				ColorKey.ofRgb(stack.getOrDefault(ModDataComponents.BACKPACK_TIER, BackpackTier.LEATHER).getColor());
    }
}
