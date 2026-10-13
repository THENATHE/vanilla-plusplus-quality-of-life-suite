package qa.backpackcrafting.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Observation only: records why BRBR cancels a live request without changing its behavior. */
@Pseudo
@Mixin(targets = "marsh.town.brb.InstantCraftingManager", remap = false)
public abstract class CraftingNetworkBrbrTrace {
    @Inject(method = "cancelPending", at = @At("HEAD"))
    private void recordCancellation(CallbackInfo callback) {
        try {
            var field = getClass().getDeclaredField("pending");
            field.setAccessible(true);
            Object pending = field.get(this);
            if (pending != null) {
                System.out.println("CRAFTING_BRBR_CANCEL " + pending);
                new Exception("BRBR request cancellation observer").printStackTrace();
            }
        } catch (ReflectiveOperationException failure) {
            throw new RuntimeException(failure);
        }
    }
}
