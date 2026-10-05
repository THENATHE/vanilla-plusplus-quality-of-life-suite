package net.atlas.defaulted.mixin;

import net.atlas.defaulted.extension.DataComponentUpdateConsumer;
import net.atlas.defaulted.extension.PatchedDataComponentMapExtensions;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? <26.3
//import java.util.Optional;

@Mixin(PatchedDataComponentMap.class)
public class PatchedDataComponentMapMixin implements PatchedDataComponentMapExtensions {
    @Unique
    public DataComponentUpdateConsumer defaulted$callback;

    @Override
    public void defaulted$setCallback(DataComponentUpdateConsumer callback) {
        this.defaulted$callback = callback;
    }

    @Inject(method = "set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", at = @At("HEAD"))
    public <T> void setCallback(DataComponentType<T> type, @Nullable T value, CallbackInfoReturnable<T> cir) {
        if (this.defaulted$callback == null) return;
        this.defaulted$callback.set(type, value);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    public <T> void removeCallback(DataComponentType<T> type, CallbackInfoReturnable<T> cir) {
        if (this.defaulted$callback == null) return;
        this.defaulted$callback.remove(type);
    }

    @Inject(method = "applyPatch(Lnet/minecraft/core/component/DataComponentPatch;)V", at = @At("HEAD"))
    public void applyPatchCallback(DataComponentPatch patch, CallbackInfo ci) {
        if (this.defaulted$callback == null) return;
        this.defaulted$callback.applyPatch(patch);
    }

    //~ if >26.2 'util/Optional' -> 'lang/Object'
    @Inject(method = "applyPatch(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)V", at = @At("HEAD"))
    //~ if >26.2 'Optional<?> value' -> 'Object value'
    public void applyPatchCallback(DataComponentType<?> type, Object value, CallbackInfo ci) {
        if (this.defaulted$callback == null) return;
        this.defaulted$callback.applyPatch(type, value);
    }

    @Inject(method = "restorePatch", at = @At("HEAD"))
    public void restorePatchCallback(DataComponentPatch patch, CallbackInfo ci) {
        if (this.defaulted$callback == null) return;
        this.defaulted$callback.restorePatch(patch);
    }

    //? >1.21.1 {
    @Inject(method = "clearPatch", at = @At("HEAD"))
    public void clearPatchCallback(CallbackInfo ci) {
        if (this.defaulted$callback == null) return;
        this.defaulted$callback.clearPatch();
    }
    //?}

    @Inject(method = "setAll", at = @At("HEAD"))
    public void setAllCallback(DataComponentMap components, CallbackInfo ci) {
        if (this.defaulted$callback == null) return;
        this.defaulted$callback.setAll(components);
    }
}
