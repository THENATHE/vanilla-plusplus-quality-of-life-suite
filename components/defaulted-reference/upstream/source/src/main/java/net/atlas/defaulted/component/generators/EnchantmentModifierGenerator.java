package net.atlas.defaulted.component.generators;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.atlas.defaulted.component.PatchGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record EnchantmentModifierGenerator(Object2IntOpenHashMap<Holder<Enchantment>> entries) implements PatchGenerator {
	private static final Codec<Integer> LEVEL_CODEC = Codec.intRange(0, 255);
    public static final MapCodec<EnchantmentModifierGenerator> CODEC = Codec.unboundedMap(Enchantment.CODEC, LEVEL_CODEC)
        .xmap(Object2IntOpenHashMap::new, Function.identity()).xmap(EnchantmentModifierGenerator::new, EnchantmentModifierGenerator::entries).fieldOf("entries");
    
    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        DataComponentType<ItemEnchantments> eComponentType = item.getDefaultInstance().is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(patchedDataComponentMap.getOrDefault(eComponentType, ItemEnchantments.EMPTY));
        entries.forEach(enchantments::set);
        patchedDataComponentMap.set(eComponentType, enchantments.toImmutable());
    }

    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
    
}
