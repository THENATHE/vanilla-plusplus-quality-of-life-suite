package net.atlas.defaulted.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.atlas.defaulted.utils.Hooks;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(GrindstoneMenu.class)
public class GrindstoneMenuMixin {
    @WrapOperation(method = "removeNonCursesFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;updateEnchantments(Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/world/item/enchantment/ItemEnchantments;"))
    public ItemEnchantments addDefaultEnchantments(ItemStack itemStack, Consumer<ItemEnchantments.Mutable> consumer, Operation<ItemEnchantments> original) {
        return original.call(itemStack, Hooks.wrapConsumer(consumer, itemStack));
    }
}
