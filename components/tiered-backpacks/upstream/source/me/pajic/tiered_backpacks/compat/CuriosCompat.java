package me.pajic.tiered_backpacks.compat;

//? neoforge {

/*import me.pajic.tiered_backpacks.item.BackpackCurioItem;
import me.pajic.tiered_backpacks.item.BackpackItem;
import me.pajic.tiered_backpacks.util.BackpackTier;
import me.pajic.tiered_backpacks.util.BackpackUtil;import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

public class CuriosCompat implements AccessoryUtil {

    @Override
    public BackpackItem makeBackpack(BackpackTier tier) {
        return new BackpackCurioItem(tier);
    }

    @Override
    public ItemStack getBackpack(LivingEntity entity) {
        Optional<ICuriosItemHandler> opt = CuriosApi.getCuriosInventory(entity);
        if (opt.isPresent()) {
            ICuriosItemHandler handler = opt.get();
            Optional<SlotResult> res = handler.findFirstCurio(stack -> stack.is(BackpackUtil.BACKPACKS));
            if (res.isPresent()) return res.get().stack();
        }
        return ItemStack.EMPTY;
    }
}
*///?}
