package me.pajic.toolpouch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {

    public AbstractArrowMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(
            method = "tryPickup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean addArrowToToolPouch(
            Inventory instance,
			ItemStack itemStack,
			Operation<Boolean> original,
			@Local(argsOnly = true, name = "player") Player player
    ) {
        if (itemStack.is(ItemTags.ARROWS)) {
			if (ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(ItemTags.ARROWS))) {
				ItemStack updated = ToolPouchUtil.addItemToToolPouch(player, itemStack);
				if (updated.isEmpty()) {
					player.take((AbstractArrow) (Object) this, 1);
					discard();
					return false;
				}
				else return original.call(instance, updated);
			}
        }
        return original.call(instance, itemStack);
    }
}
