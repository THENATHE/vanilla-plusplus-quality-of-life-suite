package me.pajic.tiered_backpacks.item;

import com.swacky.ohmega.api.IAccessory;
import me.pajic.tiered_backpacks.util.BackpackTier;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class BackpackOhmegaItem extends BackpackItem implements IAccessory {

	public BackpackOhmegaItem(BackpackTier tier) {
		super(tier);
	}

	@Override
	public boolean canUnequip(@NonNull Player player, @NonNull ItemStack stack) {
		return BackpackUtil.canUnequipBackpack(player, stack);
	}
}
