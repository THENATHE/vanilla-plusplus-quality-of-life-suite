package me.pajic.simple_death_improvements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.pajic.simple_death_improvements.access.ItemEntityAccess;
import net.minecraft.world.entity.item.ItemEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? >=26.1 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?} else {
/*import net.minecraft.nbt.CompoundTag;
*///?}

@Mixin(ItemEntity.class)
public class ItemEntityMixin implements ItemEntityAccess {

    @Unique private int sdi$lifetime = 6000;

    @Override
    public void sdi$setLifetime(int lifetime) {
        sdi$lifetime = lifetime;
    }

    @ModifyExpressionValue(
            method = "tick",
            //? fabric {
            at = @At(
                    value = "CONSTANT",
                    args = "intValue=6000"
            )
            //?} else {
            /*at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;lifespan:I",
                    opcode = Opcodes.GETFIELD
            )
            *///?}
    )
    private int modifyLifetime(int original) {
        return sdi$lifetime != 6000 ? sdi$lifetime : original;
    }

    //? <26.1 {
    /*@Inject(
            method = "addAdditionalSaveData",
            at = @At("TAIL")
    )
    private void saveLifetime(CompoundTag compound, CallbackInfo ci) {
        if (sdi$lifetime != 6000) compound.putInt("DeathItemLifetime", sdi$lifetime);
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("TAIL")
    )
    private void loadLifetime(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("DeathItemLifetime")) sdi$lifetime = compound.getInt("DeathItemLifetime");
    }
    *///?} else {
    @Inject(
            method = "addAdditionalSaveData",
            at = @At("TAIL")
    )
    private void saveLifetime(ValueOutput output, CallbackInfo ci) {
        if (sdi$lifetime != 6000) output.putInt("DeathItemLifetime", sdi$lifetime);
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("TAIL")
    )
    private void loadLifetime(ValueInput input, CallbackInfo ci) {
        sdi$lifetime = input.getIntOr("DeathItemLifetime", 6000);
    }
    //?}
}
