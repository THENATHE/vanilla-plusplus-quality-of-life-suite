package me.pajic.tiered_backpacks.compat;

import eu.pb4.trinkets.api.TrinketsApi;
import me.pajic.tiered_backpacks.item.BackpackItem;
import me.pajic.tiered_backpacks.item.BackpackTrinketItem;
import me.pajic.tiered_backpacks.util.BackpackTier;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class TrinketsCompat implements AccessoryUtil {

    @Override
    public BackpackItem makeBackpack(BackpackTier tier) {
        return new BackpackTrinketItem(tier);
    }

    @Override
	public ItemStack getBackpack(LivingEntity entity) {
		var list = TrinketsApi.getAttachment(entity).equipped(stack -> stack.is(BackpackUtil.BACKPACKS), false);
		if (!list.isEmpty()) return list.getFirst().get();
		return ItemStack.EMPTY;
	}
}
