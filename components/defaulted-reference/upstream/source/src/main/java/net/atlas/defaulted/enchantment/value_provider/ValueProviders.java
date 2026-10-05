package net.atlas.defaulted.enchantment.value_provider;

import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.init.registry.Bootstrapper;

public class ValueProviders extends Bootstrapper<MapCodec<? extends ValueProvider>> {
    public static final ValueProviders INSTANCE = new ValueProviders();

    public ValueProviders() {
        super(Defaulted.key("value_providers"), "minecraft");
    }

    public void bootstrap() {
        register("constant", () -> Constant.MAP_CODEC);
        register("function", () -> MathFunction.CODEC);
        register("invert", () -> Invert.CODEC);
        register("variable", () -> Variable.CODEC);
    }
}
