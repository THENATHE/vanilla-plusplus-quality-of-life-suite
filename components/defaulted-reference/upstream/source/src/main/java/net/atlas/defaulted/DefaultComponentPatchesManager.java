package net.atlas.defaulted;

//? <=1.21.1
//import java.util.Optional;
//? <1.21.11 && fabric
//import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//? <=1.21.1 {
/*import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
*///?}
import net.atlas.defaulted.component.ItemPatches;
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

//? >1.21.1 {
public class DefaultComponentPatchesManager extends SimpleJsonResourceReloadListener<ItemPatches>
//?} <=1.21.1 {
/*public class DefaultComponentPatchesManager extends SimpleJsonResourceReloadListener
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
    public static List<ItemPatches> CLIENT_CACHED = null;
    private static DefaultComponentPatchesManager INSTANCE;
    private List<ItemPatches> cached = null;
    private Map<Identifier, ItemPatches> intermediary = new HashMap<>();
    //? >1.21.1 {
    public DefaultComponentPatchesManager(HolderLookup.Provider arg) {
        //? >26.2 {
        super(ItemPatches.CODEC, FileToIdConverter.registry(Defaulted.ITEM_PATCHES_TYPE));
        //?} else {
        /*super(arg, ItemPatches.CODEC, Defaulted.ITEM_PATCHES_TYPE);
         *///?}
    //?} <=1.21.1 {
    /*public DefaultComponentPatchesManager() {
        super(GSON, "defaulted/default_component_patches");
    *///?}
        INSTANCE = this;
    }

    //? <=1.21.1 {
    /*public DefaultComponentPatchesManager(HolderLookup.Provider registries) {
        this();
        this.registries = registries;
    }
    *///?}

    public void patch() {
        Defaulted.patchItemComponents(cached);
        Defaulted.EXECUTE_ON_RELOAD.forEach(collectionConsumer -> collectionConsumer.accept(cached));
    }

    @Override
    protected Map<Identifier, /*? >1.21.1 {*/ ItemPatches /*?} <=1.21.1 {*/ /*JsonElement *//*?}*/> prepare(ResourceManager resourceManager,
            ProfilerFiller profilerFiller) {
        clear();
        return super.prepare(resourceManager, profilerFiller);
    }

    @Override
    //? >1.21.1 {
    protected void apply(Map<Identifier, ItemPatches> patches, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
    //?} <=1.21.1 {
    /*protected void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<Identifier, ItemPatches> patches = new HashMap<>();
        RegistryOps<JsonElement> registryOps = makeOps();

        for(Map.Entry<Identifier, JsonElement> entry : map.entrySet()) {
            Identifier loc = entry.getKey();

            try {
                Optional<ItemPatches> optionalItemPatches = getCodec().parse(registryOps, entry.getValue()).getOrThrow(JsonParseException::new);
                optionalItemPatches.ifPresentOrElse(itemPatches -> patches.put(loc, itemPatches), () -> LOGGER.debug("Skipping loading item components patch {} as its conditions were not met", loc));
            } catch (IllegalArgumentException | JsonParseException runtimeException) {
                LOGGER.error("Parsing error loading item patches {}", loc, runtimeException);
            }
        }
    *///?}
        intermediary = patches;
    }

    //? <=1.21.1 {
    /*//? neoforge {
    public Codec<Optional<ItemPatches>> getCodec() {
        return ConditionalOps.createConditionalCodecWithConditions(ItemPatches.CODEC).xmap(optionalWithConditions -> optionalWithConditions.map(WithConditions::carrier), patches -> patches.map(itemPatches -> WithConditions.builder(itemPatches).build()));
    //?} fabric {
    /^public Codec<Optional<ItemPatches>> getCodec() {
        return ItemPatches.CODEC.xmap(Optional::of, Optional::get);
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

    public static DefaultComponentPatchesManager getInstance() {
        return INSTANCE;
    }

    public static List<ItemPatches> getCached(RegistryAccess registryAccess) {
        if (INSTANCE == null) return null;
        INSTANCE.load(registryAccess);
        return getCached();
    }

    public static @Nullable List<ItemPatches> getCached() {
        if (INSTANCE == null) return null;
        return INSTANCE.cached;
    }

    public void load(RegistryAccess registryAccess) {
        if (cached == null) {
            Defaulted.ADD_DEFAULT_PATCHES.forEach(collectionConsumer -> collectionConsumer.accept(registryAccess, intermediary));
            cached = intermediary.entrySet().stream().map(entry -> new ItemPatchesEntry(entry.getKey(), entry.getValue())).sorted(Comparator.naturalOrder()).map(ItemPatchesEntry::itemPatches).toList();
            patch();
        }
    }

    public static void clear() {
        if (INSTANCE != null) INSTANCE.cached = null;

        if (CLIENT_CACHED != null) CLIENT_CACHED = null;
    }

    public static void clearClient() {
        clear();
    }

    public static void loadClientCache(List<ItemPatches> cached) {
        DefaultComponentPatchesManager.CLIENT_CACHED = cached;
        Defaulted.patchItemComponents(cached);
    }
    public static void setClientCache(RegistryAccess registryAccess) {
        DefaultComponentPatchesManager.CLIENT_CACHED = getCached(registryAccess);
    }

    //? <1.21.11 && fabric {
    /*@Override
    public Identifier getFabricId() {
        return Defaulted.id("default_component_patches");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return List.of(Defaulted.id("enchantment_patches"));
    }
    *///?}

    public record ItemPatchesEntry(Identifier id, ItemPatches itemPatches) implements Comparable<ItemPatchesEntry> {
        @Override
        public int compareTo(ItemPatchesEntry other) {
            int priority = itemPatches.compareTo(other.itemPatches);
            return priority == 0 ? id.compareTo(other.id) : priority;
        }
    }
}
