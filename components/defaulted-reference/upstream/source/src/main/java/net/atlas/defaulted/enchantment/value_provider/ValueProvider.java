package net.atlas.defaulted.enchantment.value_provider;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.Optional;

public interface ValueProvider {
    Codec<ValueProvider> DISPATCH_CODEC = ValueProviders.INSTANCE.getRegistry().byNameCodec()
            .dispatch(ValueProvider::type, mapCodec -> mapCodec);
    Codec<ValueProvider> CODEC = Codec.either(Constant.CODEC, DISPATCH_CODEC)
            .xmap((either) -> either.map((left) -> left, (right) -> right), (valueProvider) -> valueProvider instanceof Constant constant ? Either.left(constant) : Either.right(valueProvider));
    double extractValue(Optional<Integer> input);
    default int roundedValue(Optional<Integer> value) {
        return Math.toIntExact(Math.round(extractValue(value)));
    }
    MapCodec<? extends ValueProvider> type();
}