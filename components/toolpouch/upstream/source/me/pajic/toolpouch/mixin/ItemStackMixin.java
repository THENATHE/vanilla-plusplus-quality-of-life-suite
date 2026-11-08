package me.pajic.toolpouch.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.util.GameplayUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;


@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@Shadow public abstract ItemStack copy();
	@Shadow public abstract boolean isEmpty();

	@Inject(
			//? fabric
			method = "applyDamage",
			//? neoforge
			//method = "applyDamage(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
			at = @At(
					value = "INVOKE",
                    //~ if >26.2 'getItem()Lnet/minecraft/world/item/Item;' -> 'copy()Lnet/minecraft/world/item/ItemStack;'
                    target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"
			)
	)
	private void saveItem(CallbackInfo ci, @Share("stack") LocalRef<ItemStack> stackRef) {
		stackRef.set(copy());
	}

	@Inject(
			//? fabric
			method = "applyDamage",
			//? neoforge
			//method = "applyDamage(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"
			)
	)
	private void detachToolPouchOnBreak(
			int newDamage,
			@Nullable /*? fabric {*/ServerPlayer/*?} else {*//*LivingEntity*//*?}*/ player,
			Consumer<Item> onBreak,
			CallbackInfo ci,
			@Share("stack") LocalRef<ItemStack> stackRef
	) {
		ItemStack savedStack = stackRef.get();
		if (isEmpty() && GameplayUtil.isLeggingsWithPouchAttached(savedStack) && player != null) {
			ItemStack toolPouch = GameplayUtil.detachToolPouch(savedStack);
			/*? fabric {*/if (!player.addItem(toolPouch) && player.level() instanceof ServerLevel serverLevel)/*?} else {*//*if (!(player instanceof ServerPlayer sp && sp.addItem(toolPouch)) && player.level() instanceof ServerLevel serverLevel)*//*?}*/
				player.spawnAtLocation(serverLevel, toolPouch);
		}
	}

	//? fabric {
	@WrapMethod(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V")
	private void detachToolPouchOnMobBreak(int amount, LivingEntity owner, EquipmentSlot slot, Operation<Void> original) {
		ItemStack savedStack = GameplayUtil.isLeggingsWithPouchAttached((ItemStack) (Object) this) ? copy() : ItemStack.EMPTY;
		original.call(amount, owner, slot);
		if (!savedStack.isEmpty() && isEmpty() && !(owner instanceof ServerPlayer) && owner.level() instanceof ServerLevel serverLevel) {
			owner.spawnAtLocation(serverLevel, GameplayUtil.detachToolPouch(savedStack));
		}
	}
	//?}

	@Inject(
			method = "getTooltipLines",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;addDetailsToTooltip(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;Ljava/util/function/Consumer;)V"
			)
	)
	private void showExactItemCountIfLarge(
			CallbackInfoReturnable<List<Component>> cir,
			@Local(name = "lines") List<Component> lines
	) {
		ItemStack stack = (ItemStack) (Object) this;
		if (
				ToolPouch.CONFIG.equippableItemTooltip.get() &&
				ToolPouch.CONFIG.allowedItems.stream().anyMatch(allowedItem ->
						GameplayUtil.itemMatches(stack, allowedItem.id.get()).rightBoolean())
		) {
			lines.add(Component.translatable("text.toolpouch.equippable").withStyle(ChatFormatting.GRAY));
		}
	}
}
