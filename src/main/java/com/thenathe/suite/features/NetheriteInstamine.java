package com.thenathe.suite.features;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class NetheriteInstamine {
    public static final Identifier MODIFIER = Identifier.fromNamespaceAndPath("vpp_mod_suite", "netherite_instamine");

    private NetheriteInstamine() {}

    public static void update(ServerPlayer player) {
        var attribute = player.getAttribute(Attributes.MINING_EFFICIENCY);
        if (attribute == null) return;
        var tool = player.getMainHandItem();
        var haste = player.getEffect(MobEffects.HASTE);
        boolean eligible = FeatureConfig.CONFIG.netheriteInstamine.get() && haste != null && haste.getAmplifier() >= 1
                && (tool.is(Items.NETHERITE_PICKAXE) || tool.is(Items.NETHERITE_AXE))
                && EnchantmentHelper.getItemEnchantmentLevel(player.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), tool) >= 5;
        if (!eligible) {
            attribute.removeModifier(MODIFIER);
            return;
        }
        double amount = tool.is(Items.NETHERITE_PICKAXE) ? FeatureConfig.CONFIG.instaminePickaxeSpeed.get()
                : FeatureConfig.CONFIG.instamineAxeSpeed.get();
        var current = attribute.getModifier(MODIFIER);
        if (current == null || current.amount() != amount) {
            attribute.addOrUpdateTransientModifier(new AttributeModifier(MODIFIER, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
