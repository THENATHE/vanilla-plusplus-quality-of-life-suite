package me.pajic.tiered_backpacks.item;

//? neoforge {

/*import me.pajic.tiered_backpacks.util.BackpackTier;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class BackpackCurioItem extends BackpackItem implements ICurioItem {

    public BackpackCurioItem(BackpackTier tier) {
        super(tier);
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return slotContext.entity() instanceof Player p ? BackpackUtil.canUnequipBackpack(p, stack) : ICurioItem.super.canUnequip(slotContext, stack);
    }
}
*///?}
