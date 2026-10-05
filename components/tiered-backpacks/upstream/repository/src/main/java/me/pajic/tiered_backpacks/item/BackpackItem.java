package me.pajic.tiered_backpacks.item;

import me.pajic.tiered_backpacks.TieredBackpacks;
import me.pajic.tiered_backpacks.platform.MultiLoaderUtil;
import me.pajic.tiered_backpacks.util.BackpackTier;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BackpackItem extends Item {

    public BackpackItem(BackpackTier tier) {
        super(BackpackUtil.createProperties(tier));
    }

    @Override @NotNull
    public InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		if (TieredBackpacks.CONFIG.canOpenWithRightClick.get()) {
			player.startUsingItem(hand);
			player.playSound(SoundEvents.BUNDLE_INSERT);
			MultiLoaderUtil.INSTANCE.openBackpackScreen(player, player.getItemInHand(hand));
			return InteractionResult.SUCCESS;
		}
        return super.use(level, player, hand);
    }
}
