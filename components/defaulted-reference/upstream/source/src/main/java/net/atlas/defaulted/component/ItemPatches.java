package net.atlas.defaulted.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonElement;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.base.BasePatches;
import net.atlas.defaulted.codec.HeterogeneousHolderSetListCodec;
import net.atlas.defaulted.codec.TagOnlyHolderSetCodec;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

public record ItemPatches(List<HolderSet<Item>> elements, List<PatchGenerator> generators, DataComponentPatch dataComponentPatch, int priority) implements Comparable<ItemPatches>, BasePatches<Item, PatchGenerator> {
    public static final Codec<ItemPatches> DIRECT_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance ->
            instance.group(HeterogeneousHolderSetListCodec.create(Registries.ITEM, RegistryFixedCodec.create(Registries.ITEM), false).fieldOf("elements").forGetter(ItemPatches::elements))
                    .and(additionalDetails(instance))
                    .apply(instance, ItemPatches::new)));
    public static final Codec<ItemPatches> FALLBACK_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance ->
            instance.group(ExtraCodecs.nonEmptyHolderSet(RegistryCodecs.holderSet(Registries.ITEM)).optionalFieldOf("items", HolderSet.empty()).forGetter(ItemPatches::legacyItems),
                            TagOnlyHolderSetCodec.create(Registries.ITEM).listOf().optionalFieldOf("tags", Collections.emptyList()).forGetter(ItemPatches::legacyTags))
                    .and(additionalDetails(instance))
                    .apply(instance, ItemPatches::createLegacy)));
    public static final Codec<ItemPatches> CODEC = Codec.withAlternative(DIRECT_CODEC, FALLBACK_CODEC);

    private static ItemPatches createLegacy(HolderSet<Item> holders, List<HolderSet<Item>> tags, List<PatchGenerator> patchGenerators, DataComponentPatch dataComponentPatch, Integer integer) {
        List<HolderSet<Item>> items = new ArrayList<>(tags);
        items.add(holders);
        return new ItemPatches(items, patchGenerators, dataComponentPatch, integer);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemPatches> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.<RegistryFriendlyByteBuf, HolderSet<Item>>list().apply(ByteBufCodecs.holderSet(Registries.ITEM)), ItemPatches::elements,
        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.fromCodecWithRegistriesTrusted(PatchGenerator.CODEC)), ItemPatches::generators,
        DataComponentPatch.STREAM_CODEC, ItemPatches::dataComponentPatch,
        ByteBufCodecs.VAR_INT, ItemPatches::priority, ItemPatches::new);
    @Override
    public int compareTo(ItemPatches o) {
        return priority - o.priority;
    }

    public HolderSet<Item> legacyItems() {
        return this.elements.stream().filter(holders -> holders.unwrap().right().isPresent()).findFirst().orElse(HolderSet.empty());
    }

    public List<HolderSet<Item>> legacyTags() {
        return this.elements.stream().filter(holders -> holders.unwrap().left().isPresent()).toList();
    }

    public void apply(Item item, PatchedDataComponentMap newMap) {
        if (!matchItem(item)) return;
        newMap.applyPatch(dataComponentPatch);
    }
    
    public void applyGenerators(Item item, PatchedDataComponentMap newMap) {
        if (!matchItem(item)) return;
        generators.forEach(patchGenerator -> patchGenerator.patchDataComponentMap(item, newMap));
    }

    @SuppressWarnings("deprecation")
    public boolean matchItem(Item item) {
        if (!(this.elements.isEmpty() || this.elements.stream().allMatch(holders -> holders.size() <= 0))) {
            Holder<Item> itemHolder = item.builtInRegistryHolder();
            return this.elements.stream().anyMatch(holders -> holders.contains(itemHolder));
        }
        return true;
    }

    public static Products.P3<RecordCodecBuilder.Mu<ItemPatches>, List<PatchGenerator>, DataComponentPatch, Integer> additionalDetails(RecordCodecBuilder.Instance<ItemPatches> instance) {
        return instance.group(PatchGenerator.CODEC.listOf().optionalFieldOf("patch_generators", Collections.emptyList()).forGetter(ItemPatches::generators),
                DataComponentPatch.CODEC.optionalFieldOf("patch", DataComponentPatch.EMPTY).forGetter(ItemPatches::dataComponentPatch),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("priority", 1000).forGetter(ItemPatches::priority));
    }

    @Override
    public ResourceKey<Registry<ItemPatches>> key() {
        return Defaulted.ITEM_PATCHES_TYPE;
    }

    @Override
    public JsonElement save(RegistryAccess registries) {
        return CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), this).getOrThrow();
    }
}
