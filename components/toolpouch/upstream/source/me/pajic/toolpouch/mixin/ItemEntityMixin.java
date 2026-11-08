package me.pajic.toolpouch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.toolpouch.util.GameplayUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(
            method = "playerTouch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean addAmmoToToolPouch(
            Inventory instance,
			ItemStack itemStack,
			Operation<Boolean> original,
			@Local(argsOnly = true, name = "player") Player player
    ) {
		Predicate<ItemStackTemplate> ammo = GameplayUtil.getSupportedAmmo(player);
        if (
				!itemStack.isEmpty() && ammo.test(ItemStackTemplate.fromNonEmptyStack(itemStack)) &&
				ToolPouchUtil.toolPouchHasItem(player, ammo)
		) {
			int count = itemStack.getCount();
			itemStack.setCount(ToolPouchUtil.addItemToToolPouch(player, itemStack).getCount());
			if (itemStack.isEmpty()) return true;
			return original.call(instance, itemStack) || itemStack.getCount() < count;
        }
        return original.call(instance, itemStack);
    }
}
