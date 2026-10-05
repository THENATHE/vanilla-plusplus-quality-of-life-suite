package me.pajic.toolpouch.item;

//? neoforge {

/*import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ToolPouchCurioItem extends ToolPouchItem implements ICurioItem {

    public ToolPouchCurioItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return slotContext.entity() instanceof Player p ? GameplayUtil.canUnequipToolPouch(p, stack) : ICurioItem.super.canUnequip(slotContext, stack);
    }
}
*///?}
