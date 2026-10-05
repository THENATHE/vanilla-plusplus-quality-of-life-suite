package com.thenathe.amethystcursecleanser.mixin;

import com.thenathe.amethystcursecleanser.CurseCleansing;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin extends AbstractContainerMenu {
	@Shadow @Final private Container resultSlots;
	@Shadow @Final private Container repairSlots;
	@Shadow @Final private ContainerLevelAccess access;

	protected GrindstoneMenuMixin() {
		super(null, 0);
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
		this.amethystCurseCleanser$replaceInputSlot(0);
		this.amethystCurseCleanser$replaceInputSlot(1);

		Slot vanillaResultSlot = this.slots.get(GrindstoneMenu.RESULT_SLOT);
		Slot resultSlot = new Slot(this.resultSlots, GrindstoneMenu.RESULT_SLOT, vanillaResultSlot.x, vanillaResultSlot.y) {
			@Override
			public boolean mayPlace(final ItemStack stack) {
				return false;
			}

			@Override
			public void onTake(final Player player, final ItemStack carried) {
				CurseCleansing.Match match = CurseCleansing.findGrindstoneMatch(
					GrindstoneMenuMixin.this.repairSlots.getItem(0),
					GrindstoneMenuMixin.this.repairSlots.getItem(1)
				);
				if (match == null) {
					vanillaResultSlot.onTake(player, carried);
					return;
				}

				CurseCleansing.consumeAndReward(
					GrindstoneMenuMixin.this.repairSlots,
					match.gearSlot(),
					match.shardSlot(),
					player
				);
				GrindstoneMenuMixin.this.access.execute((level, pos) -> level.levelEvent(1042, pos, 0));
			}
		};
		resultSlot.index = GrindstoneMenu.RESULT_SLOT;
		this.slots.set(GrindstoneMenu.RESULT_SLOT, resultSlot);
	}

	@Inject(method = "computeResult", at = @At("HEAD"), cancellable = true)
	private void amethystCurseCleanser$createResult(
		final ItemStack input,
		final ItemStack additional,
		final CallbackInfoReturnable<ItemStack> cir
	) {
		CurseCleansing.Match match = CurseCleansing.findGrindstoneMatch(input, additional);
		if (match != null) {
			cir.setReturnValue(CurseCleansing.createCleansedCopy(match.gearSlot() == 0 ? input : additional));
		}
	}

	@Unique
	private void amethystCurseCleanser$replaceInputSlot(final int slotIndex) {
		Slot vanillaSlot = this.slots.get(slotIndex);
		Slot inputSlot = new Slot(this.repairSlots, slotIndex, vanillaSlot.x, vanillaSlot.y) {
			@Override
			public boolean mayPlace(final ItemStack stack) {
				return CurseCleansing.isAmethystShard(stack)
					|| stack.isDamageableItem()
					|| EnchantmentHelper.hasAnyEnchantments(stack);
			}
		};
		inputSlot.index = slotIndex;
		this.slots.set(slotIndex, inputSlot);
	}
}
