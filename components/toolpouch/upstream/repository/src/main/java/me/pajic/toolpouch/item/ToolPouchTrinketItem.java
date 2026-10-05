package me.pajic.toolpouch.item;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ToolPouchTrinketItem extends ToolPouchItem implements TrinketCallback {

	public ToolPouchTrinketItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean canUnequip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
		return entity instanceof Player p ? GameplayUtil.canUnequipToolPouch(p, stack) : TrinketCallback.super.canUnequip(stack, slot, entity);
	}
}
