package me.pajic.tiered_backpacks.item;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import me.pajic.tiered_backpacks.util.BackpackTier;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BackpackTrinketItem extends BackpackItem implements TrinketCallback {

	public BackpackTrinketItem(BackpackTier tier) {
		super(tier);
	}

	@Override
	public boolean canUnequip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
		return entity instanceof Player p ? BackpackUtil.canUnequipBackpack(p, stack) : TrinketCallback.super.canUnequip(stack, slot, entity);
	}
}
