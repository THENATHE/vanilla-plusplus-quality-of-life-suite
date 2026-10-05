package me.pajic.misctweaks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.pajic.misctweaks.MiscTweaks;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? >=26.1 {
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
//?}

@SuppressWarnings("ConstantValue")
@Mixin(Mob.class)
public class MobMixin {

    @WrapMethod(method = "canHoldItem")
    private boolean allowAnimalFoodPickup(ItemStack itemStack, Operation<Boolean> original) {
        if (MiscTweaks.CONFIG.animalsSearchForFood.get() && (Mob) (Object) this instanceof Animal animal) {
            return !animal.isBaby() && animal.getAge() == 0 && animal.canFallInLove() && animal.isFood(itemStack);
        }
        return original.call(itemStack);
    }

    @WrapWithCondition(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    //? >=26.1
                    target = "Lnet/minecraft/world/entity/Mob;pickUpItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/item/ItemEntity;)V"
                    //? <26.1
                    //target = "Lnet/minecraft/world/entity/Mob;pickUpItem(Lnet/minecraft/world/entity/item/ItemEntity;)V"
            )
    )
    private boolean animalPickupFood(Mob instance,/*? >=26.1 {*/ ServerLevel level,/*?}*/ ItemEntity entity) {
        if (MiscTweaks.CONFIG.animalsSearchForFood.get() && instance instanceof Animal animal && animal.isFood(entity.getItem())) {
            if (!animal.isInLove()) {
                ItemStack itemStack = entity.getItem();
                animal.onItemPickup(entity);
                animal.take(entity, 1);
                itemStack.shrink(1);
                if (itemStack.isEmpty()) entity.discard();
                animal.setInLove(entity.getOwner() instanceof Player player ? player : null);
            }
            return false;
        }
        return true;
    }

    @WrapMethod(method = "canReplaceCurrentItem")
    private boolean preventBadCheck(ItemStack newItemStack, ItemStack currentItemStack,/*? >=26.1 {*/ EquipmentSlot slot,/*?}*/ Operation<Boolean> original) {
        if (MiscTweaks.CONFIG.animalsSearchForFood.get() && (Mob) (Object) this instanceof Animal animal && animal.isFood(newItemStack)) return false;
        return original.call(newItemStack, currentItemStack/*? >=26.1 {*/, slot/*?}*/);
    }
}
