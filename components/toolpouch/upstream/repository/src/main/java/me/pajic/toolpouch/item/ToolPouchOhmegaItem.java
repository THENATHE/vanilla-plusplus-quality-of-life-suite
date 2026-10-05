package me.pajic.toolpouch.item;

import com.swacky.ohmega.api.IAccessory;
import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class ToolPouchOhmegaItem extends ToolPouchItem implements IAccessory {

	public ToolPouchOhmegaItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean canUnequip(@NonNull Player player, @NonNull ItemStack stack) {
		return GameplayUtil.canUnequipToolPouch(player, stack);
	}

	@Override
	public boolean autoSync(@NonNull Player player, @NonNull ItemStack stack) {
		return true;
	}
}
