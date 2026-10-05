package me.pajic.toolpouch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.pajic.toolpouch.util.ClientUtil;
import me.pajic.toolpouch.util.ItemStackTemplateUtil;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Shadow protected abstract void updateUsingItem(ItemStack useItem);

    @WrapMethod(method = "updatingUsingItem")
    private void useSpyglassFromToolPouch(Operation<Void> original) {
        if (ClientUtil.shouldScope) {
            updateUsingItem(new ItemStack(Items.SPYGLASS));
        }
        else original.call();
    }

	@Inject(
			method = "updateFallFlying",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/stream/Stream;toList()Ljava/util/List;"
			),
			cancellable = true
	)
	private void useElytraFromToolPouch(CallbackInfo ci) {
		if (
				(LivingEntity) (Object) this instanceof Player player &&
				ToolPouchUtil.toolPouchHasItem(player, stack -> ItemStackTemplateUtil.has(stack, DataComponents.GLIDER))
		) {
			ToolPouchUtil.updateElytraInToolPouch(player);
			gameEvent(GameEvent.ELYTRA_GLIDE);
			ci.cancel();
		}
	}

	@ModifyReturnValue(
			method = {"canGlide(Z)Z", "canGlide"},
			at = @At(
					value = "RETURN",
					//? neoforge
					//ordinal = 1
					//? fabric
					ordinal = 2
			),
            require = 1
	)
	private boolean useElytraFromToolPouch(boolean original) {
		if ((LivingEntity) (Object) this instanceof Player player) {
			ItemStackTemplate elytra = ToolPouchUtil.getElytraFromToolPouch(player, false);
			if (elytra != null && !ItemStackTemplateUtil.isEmpty(elytra)) return true;
		}
		return original;
	}

	@ModifyExpressionValue(
			method = "checkTotemDeathProtection",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"
			)
	)
	private ItemStack useTotemFromToolPouch(ItemStack original) {
		if ((LivingEntity) (Object) this instanceof Player player) {
			List<ItemStackTemplate> totems = ToolPouchUtil.getItemsFromToolPouch(player, stack ->
					ItemStackTemplateUtil.has(stack, DataComponents.DEATH_PROTECTION)
			);
			if (!totems.isEmpty()) {
				ItemStackTemplate totem = totems.getFirst();
				ToolPouchUtil.removeItemFromToolPouch(player, totem.item().value(), 1);
				return totems.getFirst().create();
			}
		}
		return original;
	}
}
