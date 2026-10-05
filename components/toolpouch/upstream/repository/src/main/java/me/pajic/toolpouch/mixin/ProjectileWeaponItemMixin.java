package me.pajic.toolpouch.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.toolpouch.util.GameplayUtil;
import me.pajic.toolpouch.util.PlayerExtension;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {

    @ModifyReturnValue(
            method = "getHeldProjectile",
            at = @At(value = "RETURN")
    )
    private static ItemStack getAmmoFromToolPouch(ItemStack original, @Local(argsOnly = true, name = "entity") LivingEntity entity) {
        if (entity instanceof Player player && original.isEmpty()) {
			int slot = ((PlayerExtension) player).toolpouch$getArrowSlot();
			List<ItemStackTemplate> ammo = ToolPouchUtil.getItemsFromToolPouch(player, GameplayUtil.getSupportedAmmo(player));
			if (!ammo.isEmpty()) return ammo.get(slot).create();
        }
        return original;
    }

    @SuppressWarnings({"MixinExtrasOperationParameters", "resource"})
	@WrapMethod(method = "useAmmo")
    private static ItemStack useAmmoFromToolPouch(
            ItemStack weapon,
            ItemStack projectile,
            LivingEntity holder,
            boolean forceInfinite,
            Operation<ItemStack> original
    ) {
		if (holder instanceof Player player) {
			int i = !forceInfinite && !player.hasInfiniteMaterials() && player.level() instanceof ServerLevel serverLevel ?
					EnchantmentHelper.processAmmoUse(serverLevel, weapon, projectile, 1) : 0;
			int slot = ((PlayerExtension) player).toolpouch$getArrowSlot();
			List<ItemStackTemplate> ammo = ToolPouchUtil.getItemsFromToolPouch(player, GameplayUtil.getSupportedAmmo(player));
			if (!ammo.isEmpty()) {
				ItemStackTemplate arrow = ammo.get(slot);
				ToolPouchUtil.removeItemFromToolPouch(player, arrow.item().value(), i);
				return original.call(weapon, ammo.get(slot).create(), player, forceInfinite);
			}
		}
        return original.call(weapon, projectile, holder, forceInfinite);
    }
}
