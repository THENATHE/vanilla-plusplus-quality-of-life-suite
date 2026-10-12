package com.thenathe.suite.features;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;

public final class AllWeatherChanneling {
    private AllWeatherChanneling() {}

    /** Retain every other enchantment effect while owning Channeling's single strike. */
    public static ItemStack vanillaAttackWeapon(ItemStack original) {
        if (!FeatureConfig.CONFIG.allWeatherChanneling.get()) return original;
        ItemStack copy = original.copy();
        EnchantmentHelper.updateEnchantments(copy, entries -> entries.removeIf(holder -> holder.is(Enchantments.CHANNELING)));
        return copy;
    }

    public static boolean strike(ThrownTrident trident, net.minecraft.world.entity.Entity target) {
        if (!FeatureConfig.CONFIG.allWeatherChanneling.get() || !(trident.level() instanceof ServerLevel level)
                || !target.isAlive()) return false;
        var channeling = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.CHANNELING);
        if (EnchantmentHelper.getItemEnchantmentLevel(channeling, trident.getWeaponItem()) <= 0) return false;
        var lightning = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (lightning == null) return false;
        lightning.snapTo(Vec3.atBottomCenterOf(target.blockPosition()));
        if (trident.getOwner() instanceof ServerPlayer player) lightning.setCause(player);
        if (!level.addFreshEntity(lightning)) return false;
        level.playSound(null, trident.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 5, 1);
        return true;
    }
}
