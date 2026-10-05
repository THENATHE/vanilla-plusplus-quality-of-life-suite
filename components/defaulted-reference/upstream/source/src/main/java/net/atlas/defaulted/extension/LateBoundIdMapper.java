package net.atlas.defaulted.extension;

//? <1.21.4 {
/*import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.codecui.CodecUI;
import net.mehvahdjukaar.codecui.SchemaContext;
import net.mehvahdjukaar.codecui.internal.WrappedEnumerableCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@SuppressWarnings("unused")
public class LateBoundIdMapper<I, V> {
	public static <I, E> Codec<E> idResolverCodec(Codec<I> codec, Function<I, E> function, Function<E, I> function2) {
		return codec.flatXmap(object -> {
			E object2 = function.apply(object);
			return object2 == null ? DataResult.error(() -> "Unknown element id: " + object) : DataResult.success(object2);
		}, object -> {
			I object2 = function2.apply(object);
			return object2 == null ? DataResult.error(() -> "Element with unknown id: " + object) : DataResult.success(object2);
		});
	}
	private final BiMap<I, V> idToValue = HashBiMap.create();

	public Codec<V> codec(Codec<I> codec) {
		BiMap<V, I> biMap = this.idToValue.inverse();
		return new WrappedEnumerableCodec<>(idResolverCodec(codec, this.idToValue::get, biMap::get), () -> {
            DynamicOps<JsonElement> ops = SchemaContext.getRegistries().createSerializationContext(JsonOps.INSTANCE);
            Map<String, V> vMap = new HashMap<>();
            this.idToValue.forEach((id, value) -> {
                String key;
                try {
                    key = codec.encodeStart(ops, id).flatMap(element -> {
                        if (!element.isJsonPrimitive())
                            return DataResult.success(CodecUI.GSON.toJson(element));
                        return DataResult.success(element.getAsString());
                    }).getOrThrow();
                } catch (Exception e) {
                    key = id.toString();
                    CodecUI.LOGGER.error("Error encoding {} as string, falling back to {}. Error: {}", id, key, e);
                }
                vMap.put(key, value);
            });
            return vMap;
        });
	}

	public LateBoundIdMapper<I, V> put(I object, V object2) {
		Objects.requireNonNull(object2, () -> "Value for " + object + " is null");
		this.idToValue.put(object, object2);
		return this;
	}
}
*///?}