package me.pajic.toolpouch.compat;

import eu.pb4.trinkets.api.TrinketsApi;
import me.pajic.toolpouch.item.ToolPouchItem;
import me.pajic.toolpouch.item.ToolPouchTrinketItem;
import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TrinketsCompat implements AccessoryUtil {

    @Override
    public ToolPouchItem makeToolPouch(Item.Properties properties) {
        return new ToolPouchTrinketItem(properties);
    }

    @Override
    public ItemStack tryGetToolPouch(LivingEntity entity) {
        var list = TrinketsApi.getAttachment(entity).equipped(stack -> stack.is(GameplayUtil.TOOL_POUCHES), false);
        if (!list.isEmpty()) return list.getFirst().get();
        return ItemStack.EMPTY;
    }
}
