package com.thenathe.suite.pets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** A location index, never a second copy of an animal's saved entity data. */
public final class PetIndex extends SavedData {
    public record Location(UUID uuid, Identifier dimension, int chunkX, int chunkZ) {
        public static final Codec<Location> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(Location::uuid),
            Identifier.CODEC.fieldOf("dimension").forGetter(Location::dimension),
            Codec.INT.fieldOf("chunk_x").forGetter(Location::chunkX),
            Codec.INT.fieldOf("chunk_z").forGetter(Location::chunkZ)
        ).apply(instance, Location::new));

        public ChunkPos chunk() {
            return new ChunkPos(chunkX, chunkZ);
        }
    }

    public static final Codec<PetIndex> CODEC = Location.CODEC.listOf().xmap(PetIndex::new,
        index -> List.copyOf(index.locations.values()));
    public static final SavedDataType<PetIndex> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath("vpp_mod_suite", "called_pets"), PetIndex::new, CODEC, null);
    private final Map<UUID, Location> locations = new HashMap<>();

    public PetIndex() {}

    private PetIndex(List<Location> saved) {
        saved.forEach(location -> locations.put(location.uuid(), location));
    }

    public static PetIndex get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public Location get(UUID uuid) {
        return locations.get(uuid);
    }

    public List<UUID> trackedPets() {
        return List.copyOf(locations.keySet());
    }

    public void update(Entity pet) {
        if (!(pet.level() instanceof ServerLevel level)) return;
        ChunkPos chunk = pet.chunkPosition();
        Location next = new Location(pet.getUUID(), level.dimension().identifier(), chunk.x(), chunk.z());
        if (!next.equals(locations.put(pet.getUUID(), next))) setDirty();
    }

    public void updateIfTracked(Entity pet) {
        if (locations.containsKey(pet.getUUID())) update(pet);
    }
}
