package net.atlas.defaulted.base;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.atlas.defaulted.codec.HeterogeneousHolderSetListCodec;
import net.atlas.defaulted.component.ItemPatchesBuilder;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.enchantment.Cost;
import net.atlas.defaulted.enchantment.EnchantmentPatchGenerator;
import net.atlas.defaulted.enchantment.EnchantmentPatchesBuilder;
import net.atlas.defaulted.enchantment.value_provider.ValueProvider;
import net.atlas.defaulted.utils.CommonUtils;
import net.atlas.defaulted.utils.DataComponentPatchUtils;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class PatchType<T, D, G extends BasePatchGenerator<G>, B extends BasePatchesBuilder<T, G, ?, ?>> implements StringRepresentable {
    private static final HashMap<Identifier, PatchType<?, ?, ?, ?>> CURRENT_BUILDERS = new HashMap<>();
    private static final List<PatchType<?, ?, ?, ?>> TYPES = new ArrayList<>();
    public static final PatchType<Item, DataComponentMap, PatchGenerator, ItemPatchesBuilder> ITEM = new PatchType<>("item",
            ItemPatchesBuilder::new,
            Registries.ITEM,
            PatchGenerator.CODEC,
            Util.make(new HashMap<>(), map -> map.put("patch", DataComponentPatch.CODEC)),
            Util.make(new HashMap<>(), map -> map.put("components", DataComponentMap.CODEC)),
            (str, map) -> map);
    public static final PatchType<Enchantment, Enchantment, EnchantmentPatchGenerator, EnchantmentPatchesBuilder> ENCHANTMENT = new PatchType<>("enchantment",
            EnchantmentPatchesBuilder::new,
            Registries.ENCHANTMENT,
            EnchantmentPatchGenerator.CODEC,
            Util.make(new HashMap<>(), map -> {
                map.put("patch", DataComponentPatchUtils.codec(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE));
                map.put("description", ComponentSerialization.CODEC);
                map.put("supported_items", RegistryCodecs.holderSet(Registries.ITEM));
                map.put("primary_items", RegistryCodecs.holderSet(Registries.ITEM));
                map.put("force_primary_items_replacement", Codec.BOOL);
                map.put("weight", ValueProvider.CODEC);
                map.put("max_level", ValueProvider.CODEC);
                map.put("min_cost", Cost.CODEC);
                map.put("max_cost", Cost.CODEC);
                map.put("anvil_cost", ValueProvider.CODEC);
                map.put("slots", EquipmentSlotGroup.CODEC.listOf());
                map.put("added_slots", EquipmentSlotGroup.CODEC.listOf());
                map.put("removed_slots", EquipmentSlotGroup.CODEC.listOf());
                map.put("exclusive_set", RegistryCodecs.holderSet(Registries.ENCHANTMENT));
            }),
            Util.make(new HashMap<>(), map -> {
                map.put("description", ComponentSerialization.CODEC);
                map.put("supported_items", RegistryCodecs.holderSet(Registries.ITEM));
                map.put("primary_items", Codec.either(RegistryCodecs.holderSet(Registries.ITEM).xmap(Optional::of, Optional::get), Unit.CODEC));
                map.put("weight", Codec.INT);
                map.put("max_level", Codec.INT);
                map.put("min_cost", Enchantment.Cost.CODEC);
                map.put("max_cost", Enchantment.Cost.CODEC);
                map.put("anvil_cost", Codec.INT);
                map.put("slots", EquipmentSlotGroup.CODEC.listOf());
                map.put("exclusive_set", RegistryCodecs.holderSet(Registries.ENCHANTMENT));
                map.put("effects", EnchantmentEffectComponents.CODEC);
                map.put("full", Enchantment.DIRECT_CODEC);
            }),
            (str, enchantment) -> switch (str) {
                case "description" -> enchantment.description();
                case "supported_items" -> enchantment.definition().supportedItems();
                case "primary_items" -> enchantment.definition().primaryItems();
                case "weight" -> enchantment.definition().weight();
                case "max_level" -> enchantment.definition().maxLevel();
                case "min_cost" -> enchantment.definition().minCost();
                case "max_cost" -> enchantment.definition().maxCost();
                case "anvil_cost" -> enchantment.definition().anvilCost();
                case "slots" -> enchantment.definition().slots();
                case "exclusive_set" -> enchantment.exclusiveSet();
                case "effects" -> enchantment.effects();
                default -> enchantment;
            });

    private final String name;
    private final BasePatchesBuilder.Factory<T, B> factory;
    private final Codec<List<HolderSet<T>>> elementsCodec;
    private final HashMap<Identifier, B> map = new HashMap<>();
    private final Codec<G> generatorCodec;
    private final Map<String, Codec<?>> builderArgs;
    private final Map<String, Codec<?>> readArgs;
    private final BiFunction<String, D, Object> valueGetter;
    @SuppressWarnings("RedundantCast")
    public static Codec<PatchType<?, ?, ?, ?>> CODEC = StringRepresentable.fromValues(() -> (PatchType<?, ?, ?, ?>[]) TYPES.toArray(new PatchType[0]));

    public PatchType(String name, BasePatchesBuilder.Factory<T, B> factory, ResourceKey<? extends Registry<T>> registry, Codec<G> generatorCodec, Codec<Holder<T>> elementCodec, Map<String, Codec<?>> builderArgs, Map<String, Codec<?>> readArgs, BiFunction<String, D, Object> valueGetter) {
        this.name = name;
        this.factory = factory;
        this.generatorCodec = generatorCodec;
        this.builderArgs = builderArgs;
        this.readArgs = readArgs;
        this.valueGetter = valueGetter;
        this.elementsCodec = HeterogeneousHolderSetListCodec.create(registry, elementCodec, false);
        TYPES.add(this);
    }

    public PatchType(String name, BasePatchesBuilder.Factory<T, B> factory, ResourceKey<? extends Registry<T>> registry, Codec<G> generatorCodec, Map<String, Codec<?>> builderArgs, Map<String, Codec<?>> readArgs, BiFunction<String, D, Object> valueGetter) {
        this(name, factory, registry, generatorCodec, RegistryFixedCodec.create(registry), builderArgs, readArgs, valueGetter);
    }

    public static Stream<String> values() {
        return TYPES.stream().map(PatchType::getSerializedName);
    }

    public static Stream<Identifier> builders() {
        return CURRENT_BUILDERS.keySet().stream();
    }

    public Codec<G> generatorCodec() {
        return this.generatorCodec;
    }

    public boolean addBuilder(Identifier id) {
        if (CURRENT_BUILDERS.containsKey(id)) return false;
        CURRENT_BUILDERS.put(id, this);
        this.map.put(id, this.factory.create());
        return true;
    }

    public boolean addBuilder(Identifier id, StringReader reader, RegistryAccess registryAccess) throws CommandSyntaxException {
        return addBuilder(id, CommonUtils.parse(reader, registryAccess, this.elementsCodec));
    }

    private boolean addBuilder(Identifier id, List<HolderSet<T>> elements) {
        if (CURRENT_BUILDERS.containsKey(id)) return false;
        CURRENT_BUILDERS.put(id, this);
        this.map.put(id, this.factory.create(elements));
        return true;
    }

    public static PatchType<?, ?, ?, ?> forId(Identifier id) {
        return CURRENT_BUILDERS.get(id);
    }

    public B get(Identifier id) {
        return this.map.get(id);
    }

    public void removeBuilder(Identifier id) {
        CURRENT_BUILDERS.remove(id);
        this.map.remove(id);
    }

    public Codec<Object> forArg(String arg) {
        //noinspection unchecked
        return (Codec<Object>) this.builderArgs.get(arg);
    }

    public Codec<Object> forReadArg(String arg) {
        //noinspection unchecked
        return (Codec<Object>) this.readArgs.get(arg);
    }

    public String[] args() {
        return this.builderArgs.keySet().toArray(new String[0]);
    }

    public String[] readArgs() {
        return this.readArgs.keySet().toArray(new String[0]);
    }

    public Object get(String arg, D input) {
        return this.valueGetter.apply(arg, input);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
