package net.atlas.defaulted.enchantment.value_provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.Optional;

public record Constant(double value) implements ValueProvider {
    public static final Constant ZERO = new Constant(0);
    public static final Codec<Constant> CODEC = Codec.DOUBLE.xmap(Constant::new, Constant::value);
    public static final MapCodec<Constant> MAP_CODEC = CODEC.fieldOf("value");
    @Override
    public double extractValue(Optional<Integer> input) {
        return this.value();
    }

    @Override
    public MapCodec<? extends ValueProvider> type() {
        return MAP_CODEC;
    }
}