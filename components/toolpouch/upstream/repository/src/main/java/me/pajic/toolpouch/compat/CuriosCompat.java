package me.pajic.toolpouch.compat;

//? neoforge {

/*import me.pajic.toolpouch.item.ToolPouchCurioItem;
import me.pajic.toolpouch.item.ToolPouchItem;
import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

public class CuriosCompat implements AccessoryUtil {

    @Override
    public ToolPouchItem makeToolPouch(Item.Properties properties) {
        return new ToolPouchCurioItem(properties);
    }

    @Override
    public ItemStack tryGetToolPouch(LivingEntity entity) {
        Optional<ICuriosItemHandler> opt = CuriosApi.getCuriosInventory(entity);
        if (opt.isPresent()) {
            ICuriosItemHandler handler = opt.get();
            Optional<SlotResult> res = handler.findFirstCurio(stack -> stack.is(GameplayUtil.TOOL_POUCHES));
            if (res.isPresent()) return res.get().stack();
        }
        return ItemStack.EMPTY;
    }
}
*///?}
