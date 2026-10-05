package clientsortqa.mixin;
import clientsortqa.SortClientQa;

import dev.terminalmc.clientsort.client.inventory.operator.server.ServerOperator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Observes transport selection only; never modifies arguments, state or return values. */
@Mixin(value=ServerOperator.class,remap=false)
public class SortClientOperatorTraceMixin {
    @Inject(method="<init>",at=@At("RETURN"))
    private void record(CallbackInfo ci){SortClientQa.serverOperatorCount++;}
}
