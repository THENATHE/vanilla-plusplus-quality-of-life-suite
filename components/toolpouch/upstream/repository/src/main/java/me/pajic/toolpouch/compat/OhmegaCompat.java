package me.pajic.toolpouch.compat;

import com.swacky.ohmega.api.AccessoryHelper;
import me.pajic.toolpouch.item.ToolPouchItem;
import me.pajic.toolpouch.item.ToolPouchOhmegaItem;
import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class OhmegaCompat implements AccessoryUtil {

    @Override
    public ToolPouchItem makeToolPouch(Item.Properties properties) {
        return new ToolPouchOhmegaItem(properties);
    }

    @Override
    public ItemStack tryGetToolPouch(LivingEntity entity) {
        if (entity instanceof Player player) {
            var list = AccessoryHelper.getAccessoryStacks(player).stream().filter(stack -> stack.is(GameplayUtil.TOOL_POUCHES)).toList();
            if (!list.isEmpty()) return list.getFirst();
        }
        return ItemStack.EMPTY;
    }
}
