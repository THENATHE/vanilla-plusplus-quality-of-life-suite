package me.pajic.toolpouch.item;

import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.platform.MultiLoaderUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ToolPouchItem extends Item {

	public ToolPouchItem(Properties properties) {
		super(properties);
	}

	@Override
	public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		if (ToolPouch.CONFIG.canOpenWithRightClick.get()) {
			player.playSound(SoundEvents.BUNDLE_INSERT);
			MultiLoaderUtil.INSTANCE.openToolPouchScreen(player, player.getItemInHand(hand));
			return InteractionResult.SUCCESS;
		}
		return super.use(level, player, hand);
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}
}
