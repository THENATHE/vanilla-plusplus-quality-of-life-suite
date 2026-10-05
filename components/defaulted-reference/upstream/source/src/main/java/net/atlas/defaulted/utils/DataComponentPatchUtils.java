package net.atlas.defaulted.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import net.atlas.defaulted.mixin.DataComponentPatchAccessor;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

import java.util.Map;

//? >26.2 {
import net.minecraft.core.component.Removed;
//?} else {
/*import java.util.Optional;
*///?}

import static net.minecraft.core.component.DataComponentPatch.EMPTY;

public class DataComponentPatchUtils {
    public static Codec<DataComponentPatch> codec(Registry<DataComponentType<?>> registry) {
        return Codec.dispatchedMap(PatchKey.codec(registry), PatchKey::valueCodec).xmap((data) -> {
            if (data.isEmpty()) {
                return EMPTY;
            } else {
                //~ if >26.2 'Optional<?>> map' -> 'Object> map'
                Reference2ObjectMap<DataComponentType<?>, Object> map = new Reference2ObjectArrayMap<>(data.size());

                for(Map.Entry<PatchKey, ?> entry : data.entrySet()) {
                    PatchKey key = entry.getKey();
                    if (key.removed()) {
                        //~ if >26.2 'Optional.empty()' -> 'Removed.INSTANCE'
                        map.put(key.type(), Removed.INSTANCE);
                    } else {
                        //~ if >26.2 'Optional.of(entry.getValue())' -> 'entry.getValue()'
                        map.put(key.type(), entry.getValue());
                    }
                }

                return DataComponentPatchAccessor.create(map);
            }
        }, (patch) -> {
            Reference2ObjectMap<PatchKey, Object> map = new Reference2ObjectArrayMap<>(getMap(patch).size());

            //~ if >26.2 'Optional<?>> entry' -> 'Object> entry'
            for (Reference2ObjectMap.Entry<DataComponentType<?>, Object> entry : Reference2ObjectMaps.fastIterable(getMap(patch))) {
                DataComponentType<?> type = entry.getKey();
                if (!type.isTransient()) {
                    //~ if >26.2 'Optional<?> value' -> 'Object value'
                    //~ if >26.2 'entry.getValue()' -> 'Removed.removedToNull(entry.getValue())'
                    Object value = Removed.removedToNull(entry.getValue());
                    //~ if >26.2 'value.isPresent()' -> 'value != null'
                    if (value != null) {
                        //~ if >26.2 'value.get()' -> 'value'
                        map.put(new PatchKey(type, false), value);
                    } else {
                        map.put(new PatchKey(type, true), Unit.INSTANCE);
                    }
                }
            }

            //noinspection rawtypes,unchecked
            return (Map) map;
        });
    }
    //~ if >26.2 'Optional<?>> getMap' -> 'Object> getMap'
    public static Reference2ObjectMap<DataComponentType<?>, Object> getMap(DataComponentPatch patch) {
        return ((DataComponentPatchAccessor) (Object) patch).getMap();
    }
    private record PatchKey(DataComponentType<?> type, boolean removed) {
        public static Codec<PatchKey> codec(Registry<DataComponentType<?>> registry) {
            return Codec.STRING.flatXmap((string) -> {
                boolean removed = string.startsWith("!");
                if (removed) {
                    string = string.substring("!".length());
                }

                Identifier id = Identifier.tryParse(string);
                DataComponentType<?> type = /*? >1.21.1 {*/ registry.getValue(id) /*?} <=1.21.1 {*/ /*registry.get(id) *//*?}*/;
                if (type == null) {
                    return DataResult.error(() -> "No component with type: '" + id + "'");
                } else {
                    return type.isTransient() ? DataResult.error(() -> "'" + id + "' is not a persistent component") : DataResult.success(new PatchKey(type, removed));
                }
            }, (key) -> {
                DataComponentType<?> type = key.type();
                Identifier id = registry.getKey(type);
                return id == null ? DataResult.error(() -> "Unregistered component: " + type) : DataResult.success(key.removed() ? "!" + id : id.toString());
            });
        }

        public Codec<?> valueCodec() {
            return this.removed ? Codec.EMPTY.codec() : this.type.codecOrThrow();
        }
    }
}
