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
    private static Object read(Object object, String name) throws ReflectiveOperationException {
        var field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    @Inject(method = "cancelPending", at = @At("HEAD"))
    private void recordCancellation(CallbackInfo callback) {
        try {
            var field = getClass().getDeclaredField("pending");
            field.setAccessible(true);
            Object pending = field.get(this);
            if (pending != null) {
                System.out.println("CRAFTING_BRBR_CANCEL " + pending);
                var menu = (net.minecraft.world.inventory.AbstractContainerMenu) read(pending, "menu");
                var result = menu.getSlot((Integer) read(pending, "resultSlot")).getItem();
                System.out.println("CRAFTING_BRBR_STATE result=" + result + " grid=" + ((java.util.function.BooleanSupplier) read(pending, "matchesGrid")).getAsBoolean() + " output=" + ((java.util.function.Predicate) read(pending, "matchesOutput")).test(result));
                for (int i=0; i<10 && i<menu.slots.size(); i++) System.out.println("CRAFTING_BRBR_SLOT " + i + " " + menu.getSlot(i).getItem());
                new Exception("BRBR request cancellation observer").printStackTrace();
            }
        } catch (ReflectiveOperationException failure) {
            throw new RuntimeException(failure);
        }
    }
}
