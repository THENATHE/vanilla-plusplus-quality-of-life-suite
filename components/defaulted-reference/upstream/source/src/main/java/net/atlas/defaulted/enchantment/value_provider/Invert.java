package net.atlas.defaulted.enchantment.value_provider;

import com.mojang.serialization.MapCodec;

import java.util.Optional;

public record Invert(ValueProvider baseProvider) implements ValueProvider {
    public static final MapCodec<Invert> CODEC = ValueProvider.CODEC.fieldOf("invert").xmap(Invert::new, Invert::baseProvider);
    @Override
    public double extractValue(Optional<Integer> input) {
        return -this.baseProvider.extractValue(input);
    }

    @Override
    public MapCodec<? extends ValueProvider> type() {
        return CODEC;
    }
}