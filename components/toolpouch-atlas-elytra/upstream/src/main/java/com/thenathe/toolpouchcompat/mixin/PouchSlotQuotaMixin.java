package com.thenathe.toolpouchcompat.mixin;

import me.pajic.toolpouch.menu.ToolPouchSlot;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/** Replacing or topping up the destination does not add another occupied stack there. */
@Mixin(value = ToolPouchSlot.class, remap = false)
public abstract class PouchSlotQuotaMixin {
    @Inject(method = "stackCountCheck", at = @At("HEAD"), cancellable = true)
    private void toolpouchCompat$countOtherSlots(ItemStack stack, int limit, Optional<TagKey<Item>> tag,
                                                CallbackInfoReturnable<Boolean> cir) {
        if (limit == 0) return;
        Slot self = (Slot) (Object) this;
        int count = 0;
        for (int i = 0; i < self.container.getContainerSize(); i++) {
            if (i == self.getContainerSlot()) continue;
            ItemStack existing = self.container.getItem(i);
            if (tag.isPresent() && existing.is(tag.get())) count++;
            else if (existing.is(stack.getItem())) count++;
        }
        cir.setReturnValue(count < limit);
    }
}
