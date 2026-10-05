package me.pajic.tiered_backpacks.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.pajic.tiered_backpacks.component.ModDataComponents;
import me.pajic.tiered_backpacks.util.BackpackUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

//? neoforge
//import net.minecraft.world.entity.LivingEntity;

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
	private void detachBackpackOnBreak(
			int newDamage,
			@Nullable /*? fabric {*/ServerPlayer/*?} else {*//*LivingEntity*//*?}*/ player,
			Consumer<Item> onBreak,
			CallbackInfo ci,
			@Share("stack") LocalRef<ItemStack> stackRef
	) {
		ItemStack savedStack = stackRef.get();
		if (isEmpty() && BackpackUtil.isChestplateWithBackpackAttached(savedStack) && player != null) {
			ItemStack backpack = BackpackUtil.stackByTier(savedStack);
			backpack.set(DataComponents.CONTAINER, savedStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
			DyedItemColor dye = savedStack.get(ModDataComponents.STORED_BACKPACK_DYE);
			if (dye != null) backpack.set(DataComponents.DYED_COLOR, dye);
			/*? fabric {*/player/*?} else {*//*if (player instanceof ServerPlayer sp) sp*//*?}*/.addItem(backpack);
		}
	}
}
