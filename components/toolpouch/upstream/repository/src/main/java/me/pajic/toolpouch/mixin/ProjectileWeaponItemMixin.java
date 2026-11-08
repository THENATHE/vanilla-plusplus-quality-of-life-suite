package me.pajic.toolpouch.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {

    @ModifyReturnValue(
            method = "getHeldProjectile",
            at = @At(value = "RETURN")
    )
    private static ItemStack getAmmoFromToolPouch(ItemStack original, @Local(argsOnly = true, name = "entity") LivingEntity entity) {
        if (entity instanceof Player player && original.isEmpty()) {
			ItemStackTemplate ammo = ToolPouchUtil.getSelectedAmmo(player);
			if (ammo != null) return ammo.create();
        }
        return original;
    }

    @WrapMethod(method = "useAmmo")
    private static ItemStack useAmmoFromToolPouch(
            ItemStack weapon,
            ItemStack projectile,
            LivingEntity holder,
            boolean forceInfinite,
            Operation<ItemStack> original
    ) {
		if (holder instanceof Player player && !forceInfinite && !isInInventory(player, projectile)) {
			ItemStackTemplate ammo = ToolPouchUtil.getSelectedAmmo(player);
			if (ammo != null) {
				int i = !player.hasInfiniteMaterials() && player.level() instanceof ServerLevel serverLevel ?
						EnchantmentHelper.processAmmoUse(serverLevel, weapon, projectile, 1) : 0;
				ToolPouchUtil.removeItemFromToolPouch(player, ammo, i);
				return original.call(weapon, ammo.create(), player, false);
			}
		}
        return original.call(weapon, projectile, holder, forceInfinite);
    }

	@Unique
	private static boolean isInInventory(Player player, ItemStack stack) {
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			if (inventory.getItem(i) == stack) return true;
		}
		return false;
	}
}
