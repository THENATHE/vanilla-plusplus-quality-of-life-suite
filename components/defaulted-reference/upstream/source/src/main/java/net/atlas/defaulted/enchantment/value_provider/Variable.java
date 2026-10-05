package net.atlas.defaulted.enchantment.value_provider;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Optional;

public enum Variable implements StringRepresentable, ValueProvider {
    INPUT("input");
    public final String name;
    public static final MapCodec<Variable> CODEC = StringRepresentable.fromEnum(Variable::values).optionalFieldOf("var", INPUT);

    Variable(String name) {
        this.name = name;
    }

    @Override
    public double extractValue(Optional<Integer> input) {
        return switch (this) {
            case INPUT -> input.orElse(0);
        };
    }

    @Override
    public MapCodec<? extends ValueProvider> type() {
        return CODEC;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}