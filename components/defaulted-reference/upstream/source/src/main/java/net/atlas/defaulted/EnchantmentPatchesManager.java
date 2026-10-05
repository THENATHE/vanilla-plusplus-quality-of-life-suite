package net.atlas.defaulted;

//? <=1.21.1 {
/*import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
*///?}
import net.atlas.defaulted.enchantment.EnchantmentPatches;
//? <1.21.11 && fabric {
/*import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
*///?}
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
//? <=1.21.1 {
/*import net.minecraft.resources.RegistryOps;
*///?}
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
//? <=1.21.1 {
/*//? neoforge {
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;
//?}
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
*///?}
//? >26.2 {
import net.minecraft.resources.FileToIdConverter;
//?}
import org.jspecify.annotations.Nullable;

//? <=1.21.1
//import java.util.Optional;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//? >1.21.1 {
public class EnchantmentPatchesManager extends SimpleJsonResourceReloadListener<EnchantmentPatches>
 //?} <=1.21.1 {
/*public class EnchantmentPatchesManager extends SimpleJsonResourceReloadListener
*///?}
//? <1.21.11 && fabric {
        /*implements IdentifiableResourceReloadListener {
*///?} else {
        {
//?}
    //? <=1.21.1 {
    /*private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();
    private HolderLookup.Provider registries = null;
    *///?}
    public static List<EnchantmentPatches> CLIENT_CACHED = null;
    private static EnchantmentPatchesManager INSTANCE;
    private List<EnchantmentPatches> cached = null;
    private Map<Identifier, EnchantmentPatches> intermediary = new HashMap<>();
    //? >1.21.1 {
    public EnchantmentPatchesManager(HolderLookup.Provider arg) {
        //? >26.2 {
        super(EnchantmentPatches.CODEC, FileToIdConverter.registry(Defaulted.ENCHANTMENT_PATCHES_TYPE));
        //?} else {
        /*super(arg, EnchantmentPatches.CODEC, Defaulted.ENCHANTMENT_PATCHES_TYPE);
        *///?}
    //?} <=1.21.1 {
    /*public EnchantmentPatchesManager() {
        super(GSON, "defaulted/enchantment_patches");
        *///?}
        INSTANCE = this;
    }

    //? <=1.21.1 {
    /*public EnchantmentPatchesManager(HolderLookup.Provider registries) {
        this();
        this.registries = registries;
    }
    *///?}

    public void patch(RegistryAccess registryAccess) {
        Defaulted.patchEnchantments(registryAccess, cached);
        Defaulted.EXECUTE_ON_ENCHANT_RELOAD.forEach(collectionConsumer -> collectionConsumer.accept(cached));
    }

    @Override
    protected Map<Identifier, /*? >1.21.1 {*/ EnchantmentPatches /*?} <=1.21.1 {*/ /*JsonElement *//*?}*/> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        clear();
        return super.prepare(resourceManager, profilerFiller);
    }

    @Override
    //? >1.21.1 {
    protected void apply(Map<Identifier, EnchantmentPatches> patches, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
    //?} <=1.21.1 {
    /*protected void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<Identifier, EnchantmentPatches> patches = new HashMap<>();
        RegistryOps<JsonElement> registryOps = makeOps();

        for(Map.Entry<Identifier, JsonElement> entry : map.entrySet()) {
            Identifier loc = entry.getKey();

            try {
                Optional<EnchantmentPatches> optionalEnchantmentPatches = getCodec().parse(registryOps, entry.getValue()).getOrThrow(JsonParseException::new);
                optionalEnchantmentPatches.ifPresentOrElse(enchantmentPatches -> patches.put(loc, enchantmentPatches), () -> LOGGER.debug("Skipping loading enchantment patch {} as its conditions were not met", loc));
            } catch (IllegalArgumentException | JsonParseException runtimeException) {
                LOGGER.error("Parsing error loading enchantment patches {}", loc, runtimeException);
            }
        }
    *///?}
        intermediary = patches;
    }

    //? <=1.21.1 {
    /*//? neoforge {
    public Codec<Optional<EnchantmentPatches>> getCodec() {
        return ConditionalOps.createConditionalCodecWithConditions(EnchantmentPatches.CODEC).xmap(optionalWithConditions -> optionalWithConditions.map(WithConditions::carrier), patches -> patches.map(itemPatches -> WithConditions.builder(itemPatches).build()));
    //?} fabric {
    /^public Codec<Optional<EnchantmentPatches>> getCodec() {
        return EnchantmentPatches.CODEC.xmap(Optional::of, Optional::get);
    ^///?}
    }

    //? neoforge {
    public RegistryOps<JsonElement> makeOps() {
        return makeConditionalOps();
    //?} fabric {
    /^public RegistryOps<JsonElement> makeOps() {
        return registries.createSerializationContext(JsonOps.INSTANCE);
    ^///?}
    }
    *///?}

    public static EnchantmentPatchesManager getInstance() {
        return INSTANCE;
    }

    public static List<EnchantmentPatches> getCached(RegistryAccess registryAccess) {
        if (INSTANCE == null) return null;
        INSTANCE.load(registryAccess);
        return getCached();
    }

    public static @Nullable List<EnchantmentPatches> getCached() {
        if (INSTANCE == null) return null;
        return INSTANCE.cached;
    }

    public void load(RegistryAccess registryAccess) {
        if (cached == null) {
            Defaulted.ADD_DEFAULT_ENCHANT_PATCHES.forEach(collectionConsumer -> collectionConsumer.accept(registryAccess, intermediary));
            cached = intermediary.entrySet().stream().map(entry -> new EnchantmentPatchesEntry(entry.getKey(), entry.getValue())).sorted(Comparator.naturalOrder()).map(EnchantmentPatchesEntry::enchantmentPatches).toList();
            patch(registryAccess);
        }
    }

    public static void clear() {
        if (INSTANCE != null) INSTANCE.cached = null;

        if (CLIENT_CACHED != null) CLIENT_CACHED = null;
    }

    public static void clearClient() {
        clear();
        Defaulted.ORIGINAL_ENCHANTMENTS.clear();
    }

    public static void loadClientCache(RegistryAccess registryAccess, List<EnchantmentPatches> cached) {
        EnchantmentPatchesManager.CLIENT_CACHED = cached;
        Defaulted.patchEnchantments(registryAccess, cached);
    }
    public static void setClientCache(RegistryAccess registryAccess) {
        EnchantmentPatchesManager.CLIENT_CACHED = getCached(registryAccess);
    }

    //? <1.21.11 && fabric {
    /*@Override
    public Identifier getFabricId() {
        return Defaulted.id("enchantment_patches");
    }
    *///?}

    public record EnchantmentPatchesEntry(Identifier id, EnchantmentPatches enchantmentPatches) implements Comparable<EnchantmentPatchesEntry> {
        @Override
        public int compareTo(EnchantmentPatchesEntry other) {
            int priority = enchantmentPatches.compareTo(other.enchantmentPatches);
            return priority == 0 ? id.compareTo(other.id) : priority;
        }
    }
}
