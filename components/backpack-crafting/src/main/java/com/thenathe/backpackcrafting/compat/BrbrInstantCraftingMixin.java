package com.thenathe.backpackcrafting.compat;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.thenathe.backpackcrafting.menu.BackpackCraftingMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.objectweb.asm.Opcodes;

/** BRBR already uses the crafting-menu API after its vanilla-menu eligibility check. */
@Pseudo
@Mixin(targets = "marsh.town.brb.InstantCraftingManager", remap = false)
public abstract class BrbrInstantCraftingMixin {
    @Definition(id = "CraftingMenu", type = CraftingMenu.class)
    @Expression("? instanceof CraftingMenu")
    @ModifyExpressionValue(
            method = {
                    "requestCraft(Lnet/minecraft/world/item/crafting/display/RecipeDisplayId;Z)Z",
                    "requestCraft(Lnet/minecraft/world/item/crafting/display/RecipeDisplayEntry;Ljava/util/function/BooleanSupplier;Z)Z"
            },
            at = @At("MIXINEXTRAS:EXPRESSION"),
            require = 2
    )
    private boolean backpackCrafting$allowBackpackMenu(boolean original) {
        var player = Minecraft.getInstance().player;
        return original || player != null && player.containerMenu instanceof BackpackCraftingMenu;
    }

    @Inject(method = "isPlayerStorage", at = @At("RETURN"), cancellable = true)
    private static void backpackCrafting$includeBackpackStorage(Minecraft client, Slot slot,
                                                               CallbackInfoReturnable<Boolean> cir) {
        if (client.player != null && client.player.containerMenu instanceof BackpackCraftingMenu menu) {
            cir.setReturnValue((cir.getReturnValueZ() || menu.isStorageSlot(slot))
                    && slot.mayPickup(client.player));
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "FIELD",
            target = "Lmarsh/town/brb/InstantCraftingManager$Pending;receivedResult:Z",
            opcode = Opcodes.GETFIELD, ordinal = 2))
    private boolean backpackCrafting$waitForSettledRecipe(@Coerce Object request, Operation<Boolean> original) {
        boolean received = original.call(request);
        var pending = (BrbrPendingAccess) request;
        if (!received || !(pending.backpackCrafting$menu() instanceof BackpackCraftingMenu menu)) return received;
        var result = menu.getSlot(pending.backpackCrafting$resultSlot()).getItem();
        // An exhausted maximum-craft batch must reach BRBR's original refill/stop path.
        if (result.isEmpty() && pending.backpackCrafting$maximum()) return true;
        // Result packets can precede the backpack/grid updates. Retain the acknowledgement
        // until the original predicates match; earlier timeout and user-cancellation checks remain active.
        return pending.backpackCrafting$matchesOutput().test(result)
                && pending.backpackCrafting$matchesGrid().getAsBoolean();
    }
}
