package com.thenathe.suite.features.mixin;

import com.thenathe.suite.features.FeatureConfig;
import com.thenathe.suite.features.GrindExtraction;
import me.pajic.simple_smithing_overhaul.SSO;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The outermost wrappers delegate every non-book operation to the installed SSO/curse slots. */
@Mixin(value = GrindstoneMenu.class, priority = 1100)
public abstract class GrindstoneExtractionMixin extends AbstractContainerMenu {
    @Shadow @Final private Container resultSlots;
    @Shadow @Final private Container repairSlots;
    @Shadow @Final private ContainerLevelAccess access;
    @Unique private Player suite$owner;
    @Unique private GrindExtraction.Match suite$shownMatch;

    protected GrindstoneExtractionMixin() { super(null, 0); }

    @Unique private GrindExtraction.Match suite$match() {
        return suite$owner == null ? null : GrindExtraction.match(suite$owner, repairSlots.getItem(0), repairSlots.getItem(1));
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void suite$installBookSlots(int id, Inventory inventory, ContainerLevelAccess levelAccess, CallbackInfo callback) {
        suite$owner = inventory.player;
        for (int index = 0; index < 2; index++) {
            Slot installed = slots.get(index);
            boolean additionalInput = index == 1;
            Slot input = new Slot(repairSlots, index, installed.x, installed.y) {
                @Override public boolean mayPlace(ItemStack stack) {
                    // The bundled curse-cleanser replaces SSO's additional-slot predicate.
                    return FeatureConfig.CONFIG.grindEnchantments.get() && stack.is(Items.BOOK)
                            || additionalInput && stack.is(Items.NETHERITE_SCRAP)
                            && SSO.CONFIG.grindstoneImprovements.repairCostReductionRecipe.get()
                            || installed.mayPlace(stack);
                }
            };
            input.index = index;
            slots.set(index, input);
        }
        Slot installed = slots.get(GrindstoneMenu.RESULT_SLOT);
        Slot result = new Slot(resultSlots, 2, installed.x, installed.y) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) {
                var match = suite$match();
                if (suite$shownMatch != null && (match == null || match.cost() != suite$shownMatch.cost()
                        || !ItemStack.matches(match.output(), suite$shownMatch.output()))) return false;
                return match == null ? installed.mayPickup(player) : GrindExtraction.canAfford(player, match);
            }
            @Override public ItemStack remove(int amount) {
                var match = suite$match();
                var taken = super.remove(amount);
                return match == null || taken.isEmpty() ? taken : match.output().copyWithCount(taken.getCount());
            }
            @Override public void onTake(Player player, ItemStack carried) {
                var match = suite$match();
                if (match == null) {
                    installed.onTake(player, carried);
                    return;
                }
                GrindExtraction.consume(player, repairSlots, match);
                access.execute((level, pos) -> level.levelEvent(1042, pos, 0));
            }
        };
        result.index = 2;
        slots.set(2, result);
    }

    @Inject(method = "computeResult", at = @At("HEAD"), cancellable = true)
    private void suite$preview(ItemStack top, ItemStack bottom, CallbackInfoReturnable<ItemStack> callback) {
        var match = suite$owner == null ? null : GrindExtraction.match(suite$owner, top, bottom);
        suite$shownMatch = match;
        if (match != null) callback.setReturnValue(GrindExtraction.preview(match));
    }

    @Inject(method = "createResult", at = @At("HEAD"))
    private void suite$clearPreviousOperation(CallbackInfo callback) {
        suite$shownMatch = null;
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void suite$quickMoveBook(Player player, int index, CallbackInfoReturnable<ItemStack> callback) {
        if (index != 2) return;
        var match = suite$match();
        if (match == null) {
            if (suite$shownMatch != null) callback.setReturnValue(ItemStack.EMPTY);
            return;
        }
        if (suite$shownMatch == null || match.cost() != suite$shownMatch.cost()
                || !ItemStack.matches(match.output(), suite$shownMatch.output())) {
            callback.setReturnValue(ItemStack.EMPTY);
            return;
        }
        if (!GrindExtraction.canAfford(player, match)) {
            callback.setReturnValue(ItemStack.EMPTY);
            return;
        }
        ItemStack output = match.output().copy();
        ItemStack original = output.copy();
        if (!moveItemStackTo(output, 3, 39, true) || !output.isEmpty()) {
            callback.setReturnValue(ItemStack.EMPTY);
            return;
        }
        GrindExtraction.consume(player, repairSlots, match);
        access.execute((level, pos) -> level.levelEvent(1042, pos, 0));
        callback.setReturnValue(original);
    }
}
