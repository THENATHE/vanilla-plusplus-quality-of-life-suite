package net.atlas.defaulted.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//? <1.21.9
//import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.defaulted.extension.ReferenceHolderExtensions;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Holder.Reference.class)
public abstract class ReferenceHolderMixin implements ReferenceHolderExtensions {
    @Shadow
    protected abstract void bindValue(Object value);

    @Unique
    boolean defaulted$forceIntrusiveUpdate = false;

    //? >=1.21.9 {
    @Definition(id = "type", field = "Lnet/minecraft/core/Holder$Reference;type:Lnet/minecraft/core/Holder$Reference$Type;")
    @Definition(id = "INTRUSIVE", field = "Lnet/minecraft/core/Holder$Reference$Type;INTRUSIVE:Lnet/minecraft/core/Holder$Reference$Type;")
    @Expression("this.type == INTRUSIVE")
    //?} <1.21.9 {
    /*@Definition(id = "value", field = "Lnet/minecraft/core/Holder$Reference;value:Ljava/lang/Object;")
    @Definition(id = "object", local = @Local(type = Object.class, argsOnly = true))
    @Expression("this.value != object")
    *///?}
    @ModifyExpressionValue(method = "bindValue", at = @At("MIXINEXTRAS:EXPRESSION"))
    public boolean forceIntrusiveUpdateUnderFlag(boolean original) {
        return original && !this.defaulted$forceIntrusiveUpdate;
    }

    @Override
    public <E> void defaulted$forceBind(E newValue) {
        this.defaulted$forceIntrusiveUpdate = true;
        bindValue(newValue);
        this.defaulted$forceIntrusiveUpdate = false;
    }
}
