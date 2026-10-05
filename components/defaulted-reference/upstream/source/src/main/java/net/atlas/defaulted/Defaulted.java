package net.atlas.defaulted;

import com.mojang.datafixers.util.Pair;
import net.atlas.defaulted.compat.CodecUICompat;
import net.atlas.defaulted.component.ItemPatches;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.atlas.defaulted.enchantment.EnchantmentBuilder;
import net.atlas.defaulted.enchantment.EnchantmentPatchGenerator;
import net.atlas.defaulted.enchantment.EnchantmentPatches;
//? <26.1 {
/*import net.atlas.defaulted.mixin.ItemAccessor;
*///?}
import net.atlas.defaulted.mixin.MappedRegistryAccessor;
import net.atlas.defaulted.mixin.PatchedDataComponentMapAccessor;
import net.atlas.defaulted.utils.ReferentialDataComponentMap;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
//? >=1.21.5
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

//? <=1.21.1
//import net.minecraft.world.item.ToolMaterials;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.MapCodec;
//? >=1.21.5
import org.jspecify.annotations.NonNull;

public final class Defaulted {
    public static boolean hasOwo = false;
    public static final BiMap<String, /*? >1.21.1 {*/ ToolMaterial /*?} <=1.21.1 {*/ /*Tier *//*?}*/> baseMaterials = HashBiMap.create();
    public static final ToolMaterialWrapper DEFAULT_WRAPPER = new ToolMaterialWrapper(ToolMaterial.DIAMOND, 3, 3);
    //? <26.1 {
    /*public static final Map<Holder<Item>, DataComponentMap> originalComponents = new HashMap<>();
    *///?}
    public static final String MOD_ID = "defaulted";
    public static final Logger LOGGER = LogManager.getLogger("defaulted");
    public static final Map<ResourceKey<Enchantment>, Enchantment> ORIGINAL_ENCHANTMENTS = new HashMap<>();
    public static final ResourceKey<Registry<MapCodec<? extends PatchGenerator>>> PATCH_GENERATOR_TYPE = key("patch_generator");
    public static final ResourceKey<Registry<MapCodec<? extends EnchantmentPatchGenerator>>> ENCHANTMENT_PATCH_GENERATOR_TYPE = key("enchantment_patch_generator");
    public static final ResourceKey<Registry<DataComponentType<?>>> PHANTOM_COMPONENT_TYPE = ResourceKey.createRegistryKey(id("phantom_data_components"));
    public static final ResourceKey<Registry<ItemPatches>> ITEM_PATCHES_TYPE = key("default_component_patches");
    public static final ResourceKey<Registry<EnchantmentPatches>> ENCHANTMENT_PATCHES_TYPE = key("enchantment_patches");
    /**
     * {@link ArrayList} of {@link Consumer}s to run on the sorted collection of {@link ItemPatches} after a reload or resource loading.
     */
    public static final List<Consumer<Collection<ItemPatches>>> EXECUTE_ON_RELOAD = new ArrayList<>();
    /**
     * {@link ArrayList} of {@link Consumer}s for the initial map of all item patches, empty by default, and will be overridden if data is loaded for these.
     */
    static final List<BiConsumer<RegistryAccess, Map<Identifier, ItemPatches>>> ADD_DEFAULT_PATCHES = new ArrayList<>();
    /**
     * {@link ArrayList} of {@link Consumer}s to run on the sorted collection of {@link EnchantmentPatches} after a reload or resource loading.
     */
    public static final List<Consumer<Collection<EnchantmentPatches>>> EXECUTE_ON_ENCHANT_RELOAD = new ArrayList<>();
    /**
     * {@link ArrayList} of {@link Consumer}s for the initial map of all enchantment patches, empty by default, and will be overridden if data is loaded for these.
     */
    static final List<BiConsumer<RegistryAccess, Map<Identifier, EnchantmentPatches>>> ADD_DEFAULT_ENCHANT_PATCHES = new ArrayList<>();

    public static void init() {
        // Write common init code here.
        baseMaterials.put("wood", ToolMaterial.WOOD);
        baseMaterials.put("stone", ToolMaterial.STONE);
        baseMaterials.put("gold", ToolMaterial.GOLD);
        //? >1.21.9
        baseMaterials.put("copper", ToolMaterial.COPPER);
        baseMaterials.put("iron", ToolMaterial.IRON);
        baseMaterials.put("diamond", ToolMaterial.DIAMOND);
        baseMaterials.put("netherite", ToolMaterial.NETHERITE);
        CodecUICompat.registerSchemaHandlers();
    }

    public static <T> ResourceKey<Registry<T>> key(String id) {
        return ResourceKey.createRegistryKey(Defaulted.id(id));
    }

    public static <T> ResourceKey<T> key(ResourceKey<Registry<T>> registry, String id) {
        return ResourceKey.create(registry, Defaulted.id(id));
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void setDurability(int maxDamage, PatchedDataComponentMap patchedDataComponentMap) {
        patchedDataComponentMap.set(DataComponents.MAX_DAMAGE, maxDamage);
        patchedDataComponentMap.set(DataComponents.DAMAGE, 0);
        patchedDataComponentMap.set(DataComponents.MAX_STACK_SIZE, 1);
    }

    @SuppressWarnings("deprecation")
    public static void patchItemComponents(Iterable<ItemPatches> reg) {
        for (Item item : BuiltInRegistries.ITEM) {
            //? <26.1 {
            /*Holder<Item> itemHolder = item.builtInRegistryHolder();
            *///?}
            DataComponentMap originalPrototype = item.components();
            //? <26.1 {
            /*if (Defaulted.originalComponents.containsKey(itemHolder)) {
                originalPrototype = Defaulted.originalComponents.get(itemHolder);
                ((ItemAccessor) item).setComponents(originalPrototype);
            }
            else Defaulted.originalComponents.put(itemHolder, originalPrototype);
            *///?}
            PatchedDataComponentMap newMap = new PatchedDataComponentMap(originalPrototype);
            reg.forEach(itemPatches -> itemPatches.apply(item, newMap));
            reg.forEach(itemPatches -> itemPatches.applyGenerators(item, newMap));
            if (newMap.asPatch().isEmpty()) continue;
            //? >=26.1 {
            item.builtInRegistryHolder().bindComponents(newMap);
            //?} <26.1 {
            /*((ItemAccessor) item).setComponents(newMap);
            *///?}
        }
    }
    public static boolean isOnClientNetworkingThread() {
        return Thread.currentThread().getName().startsWith("Netty") && Thread.currentThread().getName().contains("Client");
    }

    //? >=1.21.5 {
    public static StreamCodec<RegistryFriendlyByteBuf, ItemStack> wrapStreamCodec(StreamCodec<RegistryFriendlyByteBuf, ItemStack> original) {
        return new StreamCodec<>() {
            @Override
            public @NonNull ItemStack decode(RegistryFriendlyByteBuf buffer) {
                return original.decode(buffer);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, ItemStack stack) {
                Defaulted.encode(original::encode, buffer, stack);
            }
        };
    }
    //?}

    public static void encode(BiConsumer<RegistryFriendlyByteBuf, ItemStack> original, RegistryFriendlyByteBuf buffer, ItemStack stack) {
        if (DefaultedPlatform.INSTANCE.isOnClientNetworkingThread()) {
            original.accept(buffer, stack);
            return;
        }
        ItemStack newStack = stack.copy();
        if (!stack.isEmpty()) {
            DataComponentMap prototype = PatchedDataComponentMapAccessor.class.cast(stack.getComponents()).defaulted$getPrototype();
            if (prototype instanceof ReferentialDataComponentMap referentialDataComponentMap && (referentialDataComponentMap.get() instanceof PatchedDataComponentMap prototypeDataComponentMap)) {
                if (newStack.getComponents() instanceof PatchedDataComponentMap patchedDataComponentMap) {
                    patchedDataComponentMap.restorePatch(prototypeDataComponentMap.asPatch());
                    patchedDataComponentMap.applyPatch(stack.getComponentsPatch());
                }
            }
        }
        original.accept(buffer, newStack);
    }
    /**
     * Attaches a {@link Consumer} to run on the intermediary map of item patches (before they get resorted and applied).
     * @param patchApplier The {@link Consumer} to apply onto the intermediary patches.
     */
    public static void builtinPatchCreator(BiConsumer<RegistryAccess, Map<Identifier, ItemPatches>> patchApplier) {
        ADD_DEFAULT_PATCHES.add(patchApplier);
    }
    /**
     * Attaches a {@link Consumer} to run on the intermediary map of enchantment patches (before they get resorted and applied).
     * @param patchApplier The {@link Consumer} to apply onto the intermediary patches.
     */
    public static void builtinEnchantmentPatchCreator(BiConsumer<RegistryAccess, Map<Identifier, EnchantmentPatches>> patchApplier) {
        ADD_DEFAULT_ENCHANT_PATCHES.add(patchApplier);
    }

    public static void patchEnchantments(RegistryAccess registryAccess, List<EnchantmentPatches> reg) {
        var enchantmentLookup = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        Map<Pair<Enchantment, Enchantment>, Holder.Reference<Enchantment>> modified = new HashMap<>();
        enchantmentLookup.listElements().forEach(enchantment -> {
            ResourceKey<Enchantment> key = enchantment.key();
            Enchantment original = enchantment.value();
            Enchantment base = original;
            if (ORIGINAL_ENCHANTMENTS.containsKey(key))
                base = ORIGINAL_ENCHANTMENTS.get(key);
            EnchantmentBuilder enchantmentBuilder = EnchantmentBuilder.of(base);
            reg.forEach(enchantmentPatches -> enchantmentPatches.apply(enchantment, enchantmentBuilder));
            reg.forEach(enchantmentPatches -> enchantmentPatches.applyGenerators(enchantment, enchantmentBuilder));
            if (!enchantmentBuilder.isChanged()) {
                if (ORIGINAL_ENCHANTMENTS.containsKey(key)) enchantment.defaulted$forceBind(ORIGINAL_ENCHANTMENTS.get(key));
                else return;
            } else enchantment.defaulted$forceBind(enchantmentBuilder.build());
            if (!ORIGINAL_ENCHANTMENTS.containsKey(key)) ORIGINAL_ENCHANTMENTS.put(key, base);
            modified.put(Pair.of(original, enchantment.value()), enchantment);
        });
        if (!(/*? >1.21.1 {*/ enchantmentLookup /*?} <=1.21.1 {*/ /*registryAccess.registryOrThrow(Registries.ENCHANTMENT) *//*?}*/ instanceof MappedRegistry<Enchantment> enchantmentRegistry)) return;
        MappedRegistryAccessor<Enchantment> accessor = (MappedRegistryAccessor<Enchantment>) enchantmentRegistry;
        modified.forEach((originalAndNew, holder) -> {
            accessor.getByValue().remove(originalAndNew.getFirst());
            accessor.getByValue().put(originalAndNew.getSecond(), holder);
            int id = accessor.getToId().removeInt(originalAndNew.getFirst());
            accessor.getToId().put(originalAndNew.getSecond(), id);
        });
    }

    public static Enchantment getOriginalEnchantment(Holder.Reference<Enchantment> holder) {
        return ORIGINAL_ENCHANTMENTS.getOrDefault(holder.key(), holder.value());
    }
}
