package net.atlas.defaulted.mixin;

//? <=1.21.1 && neoforge {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.defaulted.component.backport.Enchantable;
import net.atlas.defaulted.component.backport.PhantomDataComponents;
import net.minecraft.world.item.Item;
*///?}
import net.atlas.defaulted.utils.Hooks;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
//? <=1.21.1 && neoforge
//import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    //? <=1.21.1 && neoforge {
    /*@ModifyExpressionValue(method = "getEnchantmentCost", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getEnchantmentValue()I"))
    private static int getEnchantmentValue(int original, @Local(ordinal = 0, argsOnly = true) ItemStack stack) {
        Item item = stack.getItem();
        int val;
        if (!item.defaulted$has(PhantomDataComponents.ENCHANTABLE.get()) && (val = original) != 0) item.defaulted$set(PhantomDataComponents.ENCHANTABLE.get(), new Enchantable(val));
        return item.defaulted$getOrDefault(PhantomDataComponents.ENCHANTABLE.get(), Enchantable.EMPTY).value();
    }

    @ModifyExpressionValue(method = "selectEnchantment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getEnchantmentValue()I"))
    private static int getEnchantmentValue1(int original, @Local(ordinal = 0, argsOnly = true) ItemStack stack) {
        Item item = stack.getItem();
        int val;
        if (!item.defaulted$has(PhantomDataComponents.ENCHANTABLE.get()) && (val = original) != 0) item.defaulted$set(PhantomDataComponents.ENCHANTABLE.get(), new Enchantable(val));
        return item.defaulted$getOrDefault(PhantomDataComponents.ENCHANTABLE.get(), Enchantable.EMPTY).value();
    }
    *///?}

    @WrapMethod(method = "hasAnyEnchantments")
    private static boolean hasAnyEnchantments(ItemStack itemStack, Operation<Boolean> original) {
        return Hooks.considerNonEnchantedIfMatchingDefaultEnchantments(original.call(itemStack), itemStack);
    }
}