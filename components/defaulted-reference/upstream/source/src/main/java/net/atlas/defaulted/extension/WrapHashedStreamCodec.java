package net.atlas.defaulted.extension;

//? >=1.21.5 {
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.HashCode;
import com.mojang.serialization.DynamicOps;
import net.atlas.defaulted.utils.ComponentMapUtils;
import net.minecraft.core.component.*;
import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.HashOps;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class WrapHashedStreamCodec implements StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> {
    private final StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> original;
    private DynamicOps<HashCode> registryHashOps;

    private final LoadingCache<TypedDataComponent<?>, Integer> cache = CacheBuilder.newBuilder()
            .maximumSize(256L)
            .build(
                    new CacheLoader<>() {
                        public Integer load(TypedDataComponent<?> typedDataComponent) {
                            return typedDataComponent.encodeValue(WrapHashedStreamCodec.this.registryHashOps)
                                    .getOrThrow(string -> new IllegalArgumentException("Failed to hash " + typedDataComponent + ": " + string))
                                    .asInt();
                        }
                    }
            );

    public WrapHashedStreamCodec(StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> original) {
        this.original = original;
    }

    @Override
    public HashedStack.@NonNull ActualItem decode(RegistryFriendlyByteBuf buffer) {
        this.registryHashOps = buffer.registryAccess().createSerializationContext(HashOps.CRC32C_INSTANCE);
        HashedStack.ActualItem stack = original.decode(buffer);
        HashedPatchMap components = stack.components();
        DataComponentMap prototype = ComponentMapUtils.components(stack.item());
        if (prototype instanceof PatchedDataComponentMap prototypeDataComponentMap) {
            DataComponentPatch.SplitResult splitResult = prototypeDataComponentMap.asPatch().split();
            Map<DataComponentType<?>, Integer> added = new IdentityHashMap<>(splitResult.added().size());
            added.putAll(components.addedComponents());
            splitResult.added().forEach((typedDataComponent) -> {
                if (added.containsKey(typedDataComponent.type()) && added.get(typedDataComponent.type()).equals(cache.getUnchecked(typedDataComponent))) added.remove(typedDataComponent.type());
            });
            Set<DataComponentType<?>> removed = new HashSet<>(components.removedComponents());
            removed.removeAll(splitResult.removed());
            stack = new HashedStack.ActualItem(stack.item(), stack.count(), new HashedPatchMap(added, removed));
        }
        return stack;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, HashedStack.ActualItem stack) {
        original.encode(buffer, stack);
    }
}
//?}