package org.sharedregionmaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.datafixers.util.Pair;
import org.sharedregionmaps.mixin.SavedDataStorageAccessor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/** Append-only associations. Never scans, imports, merges, or replaces existing map records. */
public final class RegionIndex extends SavedData {
    private record Entry(Region region, int id) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Region.CODEC.fieldOf("region").forGetter(Entry::region),
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("map_id").forGetter(Entry::id)
        ).apply(i, Entry::new));
    }

    private static final Codec<RegionIndex> RECORD_CODEC = Entry.CODEC.listOf().fieldOf("maps")
            .xmap(RegionIndex::new, RegionIndex::entries).codec();
    // SavedDataStorage accepts resultOrPartial. A partially decoded list would lose
    // associations and later overwrite the original index, so strip all partial results.
    private static final Codec<RegionIndex> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<RegionIndex, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Pair<RegionIndex, T>> decoded = RECORD_CODEC.decode(ops, input);
            if (decoded.error().isPresent())
                return DataResult.error(() -> decoded.error().orElseThrow().message());
            return decoded;
        }

        @Override
        public <T> DataResult<T> encode(RegionIndex input, DynamicOps<T> ops, T prefix) {
            return RECORD_CODEC.encode(input, ops, prefix);
        }
    };
    private static final SavedDataType<RegionIndex> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("shared_region_maps", "regions"), RegionIndex::new, CODEC,
            DataFixTypes.SAVED_DATA_MAP_DATA);
    private final Map<Region, MapId> ids = new HashMap<>();

    public RegionIndex() {}
    private RegionIndex(List<Entry> entries) {
        for (Entry entry : entries) {
            MapId previous = ids.putIfAbsent(entry.region(), new MapId(entry.id()));
            if (previous != null && previous.id() != entry.id())
                throw new IllegalStateException("Conflicting saved shared-map IDs for " + entry.region());
        }
    }
    private List<Entry> entries() {
        return ids.entrySet().stream().map(e -> new Entry(e.getKey(), e.getValue().id())).toList();
    }
    public static RegionIndex get(ServerLevel level) {
        if (!level.getServer().isSameThread()) throw new IllegalStateException("Map creation must run on the server thread");
        var storage = level.getServer().overworld().getDataStorage();
        RegionIndex existing = storage.get(TYPE);
        if (existing != null) return existing;
        Path file = ((SavedDataStorageAccessor) storage).sharedmaps$getDataFile(TYPE.id());
        // get() returns null both for a missing file and for any read/decode failure.
        // Only a positively absent file permits a new index. Never mark a failed
        // load dirty or overwrite its permanent associations during the next save.
        if (!Files.notExists(file))
            throw new IllegalStateException("Cannot read shared-map index " + file
                    + ". Restore the index and restart the server before creating maps.");
        RegionIndex created = new RegionIndex();
        storage.set(TYPE, created);
        return created;
    }

    public boolean contains(MapId id, MapItemSavedData data) {
        return id != null && data != null && id.equals(ids.get(Region.of(data)));
    }

    public MapId getOrCreate(ServerLevel level, Region region) {
        MapId existing = ids.get(region);
        if (existing != null) {
            MapItemSavedData data = level.getMapData(existing);
            // Never silently reassign a permanent key, overwrite a missing file, or blank a changed record.
            if (!SharedMaps.ordinary(data) || !region.equals(Region.of(data)))
                throw new IllegalStateException("Shared map " + existing.id() + " is missing or was changed externally: "
                        + region + ". Restore its saved map data before creating another map here.");
            return existing;
        }
        ServerLevel dimension = level.getServer().getLevel(region.dimension());
        if (dimension == null) throw new IllegalStateException("Map dimension is unavailable: " + region.dimension());
        // The vanilla allocator owns the numeric ID. This call is deliberately NOT globally intercepted.
        ItemStack stack = MapItem.create(dimension, region.centerX(), region.centerZ(), (byte) region.scale(), true, false);
        MapId id = stack.get(DataComponents.MAP_ID);
        MapItemSavedData data = MapItem.getSavedData(stack, dimension);
        if (id == null || !SharedMaps.ordinary(data) || !region.equals(Region.of(data)))
            throw new IllegalStateException("Another mod changed ordinary map allocation");
        ids.put(region, id);
        setDirty();
        return id;
    }
}
