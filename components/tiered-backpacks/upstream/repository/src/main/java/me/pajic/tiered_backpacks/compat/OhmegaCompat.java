package me.pajic.tiered_backpacks.compat;

import com.swacky.ohmega.api.AccessoryHelper;
import me.pajic.tiered_backpacks.item.BackpackItem;
import me.pajic.tiered_backpacks.item.BackpackOhmegaItem;
import me.pajic.tiered_backpacks.util.BackpackTier;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class OhmegaCompat implements AccessoryUtil {

    @Override
    public BackpackItem makeBackpack(BackpackTier tier) {
        return new BackpackOhmegaItem(tier);
    }

    @Override
	public ItemStack getBackpack(LivingEntity entity) {
		if (entity instanceof Player player) {
			var list = AccessoryHelper.getAccessoryStacks(player).stream().filter(stack -> stack.is(BackpackUtil.BACKPACKS)).toList();
			if (!list.isEmpty()) return list.getFirst();
		}
		return ItemStack.EMPTY;
	}
}
