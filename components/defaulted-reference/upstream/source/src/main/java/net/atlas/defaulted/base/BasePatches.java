package net.atlas.defaulted.base;

import com.google.gson.JsonElement;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

import java.util.List;

public interface BasePatches<T, G extends BasePatchGenerator<G>> {
    List<HolderSet<T>> elements();
    List<G> generators();
    int priority();
    ResourceKey<? extends Registry<?>> key();
    JsonElement save(RegistryAccess registries);
}
