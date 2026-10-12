package com.thenathe.suite.features;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Extraction is a transaction: one book, one source, a validated cost, and no XP reward. */
public final class GrindExtraction {
    public record Match(int sourceSlot, int bookSlot, ItemStack sourceRemainder, ItemStack output, int cost) {}

    private GrindExtraction() {}

    public static Match match(Player player, ItemStack top, ItemStack bottom) {
        if (!FeatureConfig.CONFIG.grindEnchantments.get()) return null;
        boolean transfer = top.is(Items.ENCHANTED_BOOK) && (bottom.is(Items.BOOK) || bottom.is(Items.ENCHANTED_BOOK));
        if (transfer && !FeatureConfig.CONFIG.grindTransfer.get()) return null;
        int sourceSlot = 0;
        ItemStack source = top;
        ItemStack book = bottom;
        if (!transfer) {
            if (!FeatureConfig.CONFIG.grindExtraction.get()) return null;
            if (top.is(Items.BOOK)) {
                sourceSlot = 1;
                source = bottom;
                book = top;
            }
            if (!book.is(Items.BOOK) || source.is(Items.ENCHANTED_BOOK)) return null;
        }
        if (source.isEmpty() || source.getCount() != 1 || book.isEmpty()) return null;
        var original = EnchantmentHelper.getEnchantmentsForCrafting(source);
        var candidates = new ArrayList<>(original.keySet());
        if (!FeatureConfig.CONFIG.extractCurses.get()) candidates.removeIf(holder -> holder.is(EnchantmentTags.CURSE));
        if (candidates.isEmpty()) return null;
        List<Holder<Enchantment>> tooltipOrder = new ArrayList<>();
        player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getTagOrEmpty(EnchantmentTags.TOOLTIP_ORDER)
                .forEach(tooltipOrder::add);
        candidates.sort(Comparator.<Holder<Enchantment>>comparingInt(holder -> {
            int index = tooltipOrder.indexOf(holder);
            return index < 0 ? Integer.MAX_VALUE : index;
        }).thenComparing(holder -> holder.unwrapKey().map(key -> key.identifier().toString()).orElse("")));
        if (transfer || FeatureConfig.CONFIG.grindOnlyFirst.get()) candidates.subList(1, candidates.size()).clear();
        var destination = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(book));
        // Combining incompatible book enchantments is useful; silently losing a duplicate is not.
        if (candidates.stream().anyMatch(holder -> destination.getLevel(holder) >= original.getLevel(holder))) return null;
        var remainder = new ItemEnchantments.Mutable(original);
        double units = 0;
        for (Holder<Enchantment> enchantment : candidates) {
            int value = original.getLevel(enchantment);
            destination.upgrade(enchantment, value);
            remainder.set(enchantment, 0);
            units += transfer ? value * (enchantment.is(EnchantmentTags.TREASURE)
                    ? FeatureConfig.CONFIG.grindTransferTreasureFactor.get() : FeatureConfig.CONFIG.grindTransferNormalFactor.get())
                    : enchantment.value().getMinCost(value);
        }
        double rawCost = transfer ? units * FeatureConfig.CONFIG.grindTransferFactor.get() + FeatureConfig.CONFIG.grindTransferOffset.get()
                : units * FeatureConfig.CONFIG.grindExtractionFactor.get() + FeatureConfig.CONFIG.grindExtractionOffset.get();
        int cost = (int) Math.clamp(Math.ceil(rawCost), 0, Integer.MAX_VALUE);
        ItemStack result = book.is(Items.BOOK) ? book.transmuteCopy(Items.ENCHANTED_BOOK, 1) : book.copyWithCount(1);
        EnchantmentHelper.setEnchantments(result, destination.toImmutable());
        ItemStack returned = source.copy();
        EnchantmentHelper.setEnchantments(returned, remainder.toImmutable());
        if (transfer && remainder.toImmutable().isEmpty()) returned = returned.transmuteCopy(Items.BOOK, 1);
        if (!transfer && FeatureConfig.CONFIG.grindConsumeSource.get()) returned = ItemStack.EMPTY;
        return new Match(sourceSlot, 1 - sourceSlot, returned, result, cost);
    }

    public static boolean canAfford(Player player, Match match) {
        return match != null && (player.isCreative() || (FeatureConfig.CONFIG.grindCostInPoints.get()
                ? experiencePoints(player) >= match.cost() : player.experienceLevel >= match.cost()));
    }

    private static int experiencePoints(Player player) {
        int level = player.experienceLevel;
        double base = level <= 16 ? level * level + 6.0 * level
                : level <= 31 ? 2.5 * level * level - 40.5 * level + 360 : 4.5 * level * level - 162.5 * level + 2220;
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(base + player.experienceProgress * player.getXpNeededForNextLevel()));
    }

    public static ItemStack preview(Match match) {
        ItemStack result = match.output().copy();
        var lore = result.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
        result.set(DataComponents.LORE, lore.withLineAdded(Component.literal("Cost: " + match.cost()
                + (FeatureConfig.CONFIG.grindCostInPoints.get() ? " XP points" : " XP levels")).withStyle(ChatFormatting.GRAY)));
        return result;
    }

    public static void consume(Player player, Container inputs, Match match) {
        if (!canAfford(player, match)) return;
        if (!player.isCreative()) {
            if (FeatureConfig.CONFIG.grindCostInPoints.get()) player.giveExperiencePoints(-match.cost());
            else player.giveExperienceLevels(-match.cost());
        }
        inputs.setItem(match.sourceSlot(), match.sourceRemainder().copy());
        inputs.removeItem(match.bookSlot(), 1);
        inputs.setChanged();
    }
}
