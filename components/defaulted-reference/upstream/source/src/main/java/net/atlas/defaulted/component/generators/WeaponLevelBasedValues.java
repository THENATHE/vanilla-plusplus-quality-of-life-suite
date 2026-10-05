package net.atlas.defaulted.component.generators;

import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.init.registry.Bootstrapper;
import net.atlas.defaulted.component.generators.WeaponLevelBasedValue.*;

public class WeaponLevelBasedValues extends Bootstrapper<MapCodec<? extends WeaponLevelBasedValue>> {
    public static final WeaponLevelBasedValues INSTANCE = new WeaponLevelBasedValues();

    public WeaponLevelBasedValues() {
        super(Defaulted.key("weapon_level_based_values"), "minecraft");
    }

    public void bootstrap() {
        register("constant", () -> Constant.CODEC);
        register("lookup", () -> Lookup.CODEC);
        register("linear", () -> Linear.CODEC);
    }
}
