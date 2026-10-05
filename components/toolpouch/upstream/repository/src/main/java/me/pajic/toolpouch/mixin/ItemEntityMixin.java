package me.pajic.toolpouch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.toolpouch.util.GameplayUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract ItemStack getItem();
    @Shadow public abstract void setItem(ItemStack itemStack);

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
        if (ammo.test(ItemStackTemplate.fromNonEmptyStack(itemStack))) {
			if (ToolPouchUtil.toolPouchHasItem(player, ammo)) {
				ItemEntity itemEntity = (ItemEntity) (Object) this;
				int i = itemStack.getCount();
				ItemStack updated = ToolPouchUtil.addItemToToolPouch(player, itemStack);
				setItem(updated);
				player.take(itemEntity, i);
				player.awardStat(Stats.ITEM_PICKED_UP.get(itemStack.getItem()), i);
				player.onItemPickup(itemEntity);
				if (updated.isEmpty()) {
					discard();
					return false;
				}
				else return original.call(instance, updated);
			}
        }
        return original.call(instance, itemStack);
    }
}
