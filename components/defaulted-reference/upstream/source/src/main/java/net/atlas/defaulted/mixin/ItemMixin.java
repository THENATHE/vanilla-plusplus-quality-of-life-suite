package net.atlas.defaulted.mixin;

//? <=1.21.1 {
/*import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
*///?}
import net.atlas.defaulted.component.ToolMaterialWrapper;
//? <=1.21.1 {
/*import net.atlas.defaulted.component.backport.Enchantable;
import net.atlas.defaulted.component.backport.PhantomDataComponents;
*///?}
import net.atlas.defaulted.extension.ItemExtensions;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
//? <=1.21.1
//import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
//? <=1.21.1
//import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
//? <=1.21.1
//import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemExtensions {
    //? <=1.21.1 {
    /*@Shadow
    public abstract int getEnchantmentValue();
    *///?}
    @Unique
    public PatchedDataComponentMap defaulted$phantomComponentMap = new PatchedDataComponentMap(DataComponentMap.builder().build());

    @Unique
    public ToolMaterialWrapper defaulted$toolMaterialWrapper = null;

    @Override
    public <T> @Nullable T defaulted$get(DataComponentType<T> phantomType) {
        return defaulted$phantomComponentMap.get(phantomType);
    }
    @Override
    public <T> @NonNull T defaulted$getOrDefault(DataComponentType<T> phantomType, T defaultValue) {
        return defaulted$phantomComponentMap.getOrDefault(phantomType, defaultValue);
    }

    @Override
    public boolean defaulted$has(DataComponentType<?> phantomType) {
        return defaulted$get(phantomType) != null;
    }

    @Override
    public <T> void defaulted$set(DataComponentType<T> phantomType, T phantom) {
        defaulted$phantomComponentMap.set(phantomType, phantom);
    }

    @Override
    public PatchedDataComponentMap defaulted$getPhantomComponentMap() {
        return defaulted$phantomComponentMap;
    }

    @Override
    public ToolMaterialWrapper defaulted$getToolMaterial() {
        return defaulted$toolMaterialWrapper;
    }

    @Override
    public void defaulted$setToolMaterial(ToolMaterialWrapper newTier) {
        defaulted$toolMaterialWrapper = newTier;
    }
    //? <=1.21.1 {
    /*@ModifyReturnValue(method = "isEnchantable", at = @At("RETURN"))
    public boolean useComponent(boolean original) {
        int val;
        if (!defaulted$has(PhantomDataComponents.ENCHANTABLE.get()) && (val = getEnchantmentValue()) != 0) defaulted$set(PhantomDataComponents.ENCHANTABLE.get(), new Enchantable(val));
        return original || defaulted$has(PhantomDataComponents.ENCHANTABLE.get());
    }

    @WrapMethod(method = "isValidRepairItem")
    public boolean useComponent(ItemStack itemStack, ItemStack itemStack2, Operation<Boolean> original) {
        return itemStack.getItem().defaulted$has(PhantomDataComponents.REPAIRABLE.get()) ? itemStack2.getItem().defaulted$get(PhantomDataComponents.REPAIRABLE.get()).isValidRepairItem(itemStack2) : original.call(itemStack, itemStack2);
    }

    @WrapMethod(method = "getEnchantmentValue")
    public int useComponent(Operation<Integer> original) {
        int val;
        if (!defaulted$has(PhantomDataComponents.ENCHANTABLE.get()) && (val = original.call()) != 0) defaulted$set(PhantomDataComponents.ENCHANTABLE.get(), new Enchantable(val));
        return defaulted$getOrDefault(PhantomDataComponents.ENCHANTABLE.get(), Enchantable.EMPTY).value();
    }
    *///?}
}
