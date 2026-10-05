package net.atlas.defaulted.utils;

import it.unimi.dsi.fastutil.objects.*;
import net.atlas.defaulted.extension.DataComponentUpdateConsumer;
import net.atlas.defaulted.extension.PatchedDataComponentMapExtensions;
import net.atlas.defaulted.mixin.DataComponentPatchAccessor;
import net.minecraft.core.component.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

//? <26.3
//import java.util.Optional;

public class ReferentialDataComponentMap implements DataComponentMap, DataComponentUpdateConsumer {
    private final Supplier<DataComponentMap> parentGetter;
    //~ if >26.2 'Optional<?>> patch' -> 'Object> patch'
    private Reference2ObjectMap<DataComponentType<?>, Object> patch = new Reference2ObjectOpenHashMap<>();
    private DataComponentMap cached = null;
    private PatchedDataComponentMap original = null;
    private boolean copyOnWrite;

    public ReferentialDataComponentMap(Supplier<DataComponentMap> parentGetter) {
        this.parentGetter = () -> {
            DataComponentMap newValue = parentGetter.get();
            if (newValue != this.cached && this.original != null) {
                DataComponentPatch patch = this.asPatch();
                this.cached = newValue;
                this.original.restorePatch(patch);
            } else this.cached = newValue;
            return this.cached;
        };
    }

    public DataComponentMap get() {
        return this.parentGetter.get();
    }

    public DataComponentPatch asPatch() {
        this.copyOnWrite = true;
        return DataComponentPatchAccessor.create(this.patch);
    }

    public void setOriginal(PatchedDataComponentMap original) {
        this.original = original;
        this.restorePatch(original.asPatch());
        ((PatchedDataComponentMapExtensions)(Object)original).defaulted$setCallback(this);
    }

    @Override
    public @NonNull Set<DataComponentType<?>> keySet() {
        return this.parentGetter.get().keySet();
    }

    @Override
    public @Nullable <T> T get(@NonNull DataComponentType<? extends T> dataComponentType) {
        return this.parentGetter.get().get(dataComponentType);
    }

    @Override
    public <T> void set(DataComponentType<T> type, T value) {
        ensureMapOwnership();
        //~ if >26.2 'Optional.ofNullable(value)' -> 'Removed.nullToRemoved(value)'
        this.patch.put(type, Removed.nullToRemoved(value));
    }

    public <T> void set(TypedDataComponent<T> value) {
        this.set(value.type(), value.value());
    }

    @Override
    public <T> void remove(DataComponentType<T> type) {
        ensureMapOwnership();
        //~ if >26.2 'Optional.empty()' -> 'Removed.INSTANCE'
        this.patch.put(type, Removed.INSTANCE);
    }

    @Override
    public void applyPatch(DataComponentPatch patch) {
        ensureMapOwnership();
        //~ if >26.2 'Optional<?>> entry' -> 'Object> entry'
        for (Reference2ObjectMap.Entry<DataComponentType<?>, Object> entry : Reference2ObjectMaps.fastIterable(((DataComponentPatchAccessor)(Object)patch).getMap())) {
            this.applyPatch(entry.getKey(), entry.getValue());
        }
    }

    @Override
    //~ if >26.2 'Optional<?> value' -> 'Object value'
    public void applyPatch(DataComponentType<?> type, Object value) {
        ensureMapOwnership();
        this.patch.put(type, value);
    }

    @Override
    public void restorePatch(DataComponentPatch patch) {
        ensureMapOwnership();
        this.patch.clear();
        this.patch.putAll(((DataComponentPatchAccessor)(Object)patch).getMap());
    }

    @Override
    public void clearPatch() {
        ensureMapOwnership();
        this.patch.clear();
    }

    @Override
    public void setAll(DataComponentMap components) {
        for (TypedDataComponent<?> entry : components) {
            this.set(entry);
        }
    }

    private void ensureMapOwnership() {
        if (this.copyOnWrite) {
            this.patch = new Reference2ObjectArrayMap<>(this.patch);
            this.copyOnWrite = false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReferentialDataComponentMap that)) {
            if (o instanceof DataComponentMap that) return Objects.equals(this.parentGetter.get(), that);
            return false;
        }
        return Objects.equals(this.parentGetter.get(), that.parentGetter.get());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.parentGetter.get());
    }
}
