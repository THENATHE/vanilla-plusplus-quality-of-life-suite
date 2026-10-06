package com.thenathe.stackablescompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.pajic.sensible_stackables.handler.StackSizeOverrides;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Keep immutable container templates consistent with upstream's live stack getter. */
@Mixin(ItemStackTemplate.class)
public abstract class ItemStackTemplateMixin {
    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    @SuppressWarnings("unchecked")
    private <T> T suite$effectiveStackLimit(T original, DataComponentType<? extends T> type) {
        if (type != DataComponents.MAX_STACK_SIZE) return original;
        var self = (ItemStackTemplate) (Object) this;
        var patch = self.components().split();
        // Explicit additions and removals have the same precedence as upstream's
        // PatchedDataComponentMap getter. Never rewrite the saved component patch.
        if (patch.added().has(type) || patch.removed().contains(type)) return original;
        int override = StackSizeOverrides.get(self.typeHolder().value());
        return override > 0 ? (T) Integer.valueOf(override) : original;
    }
}
