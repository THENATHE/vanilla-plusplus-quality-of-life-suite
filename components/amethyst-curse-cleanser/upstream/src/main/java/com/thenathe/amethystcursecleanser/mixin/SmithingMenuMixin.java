package com.thenathe.amethystcursecleanser.mixin;

import com.thenathe.amethystcursecleanser.CurseCleansing;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin extends ItemCombinerMenu {
	public SmithingMenuMixin(
		final @Nullable MenuType<?> type,
		final int containerId,
		final Inventory inventory,
		final ContainerLevelAccess access
	) {
		super(
			type,
			containerId,
			inventory,
			access,
			ItemCombinerMenuSlotDefinition.create()
				.withSlot(0, 0, 0, stack -> true)
				.withResultSlot(1, 0, 0)
				.build()
		);
	}

	@Inject(
		method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
		at = @At("TAIL")
	)
	private void amethystCurseCleanser$installSlots(
		final int containerId,
		final Inventory inventory,
		final ContainerLevelAccess access,
		final CallbackInfo ci
	) {
		Slot vanillaBaseSlot = this.slots.get(SmithingMenu.BASE_SLOT);
		Slot baseSlot = new Slot(this.inputSlots, SmithingMenu.BASE_SLOT, vanillaBaseSlot.x, vanillaBaseSlot.y) {
			@Override
			public boolean mayPlace(final ItemStack stack) {
				return vanillaBaseSlot.mayPlace(stack) || CurseCleansing.isEligibleCursedGear(stack);
			}
		};
		baseSlot.index = SmithingMenu.BASE_SLOT;
		this.slots.set(SmithingMenu.BASE_SLOT, baseSlot);

		Slot vanillaAdditionSlot = this.slots.get(SmithingMenu.ADDITIONAL_SLOT);
		Slot additionSlot = new Slot(this.inputSlots, SmithingMenu.ADDITIONAL_SLOT, vanillaAdditionSlot.x, vanillaAdditionSlot.y) {
			@Override
			public boolean mayPlace(final ItemStack stack) {
				return vanillaAdditionSlot.mayPlace(stack) || CurseCleansing.isAmethystShard(stack);
			}
		};
		additionSlot.index = SmithingMenu.ADDITIONAL_SLOT;
		this.slots.set(SmithingMenu.ADDITIONAL_SLOT, additionSlot);
	}

	@Inject(method = "createResult", at = @At("TAIL"))
	private void amethystCurseCleanser$createResult(final CallbackInfo ci) {
		if (CurseCleansing.matchesSmithingInputs(this.inputSlots)) {
			this.resultSlots.setRecipeUsed(null);
			this.resultSlots.setItem(0, CurseCleansing.createCleansedCopy(this.inputSlots.getItem(SmithingMenu.BASE_SLOT)));
		}
	}

	@Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
	private void amethystCurseCleanser$takeResult(final Player player, final ItemStack carried, final CallbackInfo ci) {
		if (!CurseCleansing.matchesSmithingInputs(this.inputSlots)) {
			return;
		}

		carried.onCraftedBy(player, carried.getCount());
		CurseCleansing.consumeAndReward(this.inputSlots, SmithingMenu.BASE_SLOT, SmithingMenu.ADDITIONAL_SLOT, player);
		this.access.execute((level, pos) -> level.levelEvent(1044, pos, 0));
		ci.cancel();
	}

	@Inject(method = "canMoveIntoInputSlots", at = @At("HEAD"), cancellable = true)
	private void amethystCurseCleanser$allowQuickMove(final ItemStack stack, final CallbackInfoReturnable<Boolean> cir) {
		if (CurseCleansing.isEligibleCursedGear(stack) && !this.getSlot(SmithingMenu.BASE_SLOT).hasItem()) {
			cir.setReturnValue(true);
		} else if (CurseCleansing.isAmethystShard(stack) && !this.getSlot(SmithingMenu.ADDITIONAL_SLOT).hasItem()) {
			cir.setReturnValue(true);
		}
	}
}
