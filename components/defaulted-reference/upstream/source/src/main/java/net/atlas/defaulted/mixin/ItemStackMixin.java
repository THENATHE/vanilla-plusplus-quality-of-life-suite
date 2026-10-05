package net.atlas.defaulted.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.compat.OwoCompat;
import net.atlas.defaulted.extension.ItemStackExtensions;
import net.atlas.defaulted.utils.Hooks;
import net.atlas.defaulted.utils.ReferentialDataComponentMap;
//? >=26.1 {
import net.minecraft.core.Holder;
//?}
import net.minecraft.core.component.DataComponentMap;
//? >=1.21.5
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
//? >=1.21.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
//?}
//? >=26.1 {
import net.minecraft.world.item.Item;
//?}
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;

//? <26.1 {
/*import net.minecraft.world.level.ItemLike;
*///?}
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? >=1.21.5 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
//?}
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExtensions {
    @Shadow public abstract boolean isEmpty();

    @Mutable
    @Shadow
    @Final
    PatchedDataComponentMap components;

    @Unique
    boolean defaulted$midUpdate = false;

    @Shadow public abstract DataComponentMap getPrototype();

    //? >=1.21.5 {
    @WrapMethod(method = "createOptionalStreamCodec")
    private static StreamCodec<RegistryFriendlyByteBuf, ItemStack> wrapCodec(StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> streamCodec, Operation<StreamCodec<RegistryFriendlyByteBuf, ItemStack>> original) {
        StreamCodec<RegistryFriendlyByteBuf, ItemStack> result = original.call(streamCodec);
        return Defaulted.wrapStreamCodec(result);
    }
    //?}

    //? >=26.1 {
    @Inject(method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"))
    private void appendStack(Holder<Item> itemHolder, int count, PatchedDataComponentMap components, CallbackInfo ci) {
    //?} <26.1 {
    /*@Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"))
    public void appendStack(ItemLike itemLike, int count, PatchedDataComponentMap patchedDataComponentMap, CallbackInfo ci) {
    *///?}
        this.defaulted$midUpdate = true;
        PatchedDataComponentMapAccessor accessor = PatchedDataComponentMapAccessor.class.cast(this.components);
        boolean mustWrap = false;
        if (Defaulted.hasOwo) {
            DataComponentMap prototype = accessor.defaulted$getPrototype();
            mustWrap = OwoCompat.isDerived(prototype) && !(OwoCompat.unwrapDerivedComponentMap(prototype) instanceof ReferentialDataComponentMap); // If this is false, OwO is going to do this for us, as it is running after us.
        }
        ReferentialDataComponentMap newPrototype = new ReferentialDataComponentMap(this::getPrototype);
        newPrototype.setOriginal(this.components);
        accessor.defaulted$setPrototype(mustWrap ? defaulted$wrapAsDerivedComponentMap(newPrototype) : newPrototype);
        this.defaulted$midUpdate = false;
    }

    @Inject(method = "getComponents", at = @At(value = "HEAD"))
    public void validateReferentialPrototype0(CallbackInfoReturnable<DataComponentMap> cir) {
        this.defaulted$updatePrototype();
    }

    @Inject(method = "getComponentsPatch", at = @At(value = "HEAD"))
    public void validateReferentialPrototype1(CallbackInfoReturnable<DataComponentMap> cir) {
        this.defaulted$updatePrototype();
    }

    @Override
    public void defaulted$updatePrototype() {
        if (!isEmpty() && !this.defaulted$midUpdate) {
            this.defaulted$midUpdate = true;
            PatchedDataComponentMapAccessor accessor = PatchedDataComponentMapAccessor.class.cast(this.components);
            DataComponentMap prototype = accessor.defaulted$getPrototype();
            if (Defaulted.hasOwo) prototype = OwoCompat.unwrapDerivedComponentMap(prototype);
            if (!(prototype instanceof ReferentialDataComponentMap)) {
                ReferentialDataComponentMap newPrototype = new ReferentialDataComponentMap(this::getPrototype);
                newPrototype.setOriginal(this.components);
                accessor.defaulted$setPrototype(Defaulted.hasOwo ? defaulted$wrapAsDerivedComponentMap(newPrototype) : newPrototype);
            }
            this.defaulted$midUpdate = false;
        }
    }

	@Unique
	private DataComponentMap defaulted$wrapAsDerivedComponentMap(DataComponentMap prototype) {
        Field field;
        try {
            field = ItemStack.class.getDeclaredField("owo$derivedMap");
        } catch (NoSuchFieldException e) {
            Defaulted.LOGGER.error("Failed to locate field owo$derivedMap: ", e);
            Defaulted.LOGGER.error("Trying to locate field derivedMap instead.");
            try {
                field = ItemStack.class.getDeclaredField("derivedMap"); // Fallback other potential field
            } catch (NoSuchFieldException ex) {
                Defaulted.LOGGER.error("Failed to locate field derivedMap:", ex);
                return prototype;
            }
        }
        field.setAccessible(true);
        DataComponentMap derived = OwoCompat.deriveComponentMap(ItemStack.class.cast(this), prototype);
        try {
            field.set(this, derived);
        } catch (IllegalAccessException e) {
            Defaulted.LOGGER.error("Failed to store DerivedComponentMap: ", e);
            return derived;
        }
        return derived;
	}

    @WrapOperation(method = "isEnchantable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/ItemEnchantments;isEmpty()Z"))
    private boolean hasAnyEnchantments(ItemEnchantments instance, Operation<Boolean> original) {
        return Hooks.addEnchantableStatusForDefaultEnchantments(original.call(instance), instance, ItemStack.class.cast(this));
    }
}
