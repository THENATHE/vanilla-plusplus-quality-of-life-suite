package net.atlas.defaulted.component.generators;

import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.generators.WeaponLevelBasedValue.LevelCondition;
import net.atlas.defaulted.component.generators.WeaponLevelBasedValue.*;
import net.atlas.defaulted.init.registry.Bootstrapper;

public class LevelConditions extends Bootstrapper<MapCodec<? extends LevelCondition>> {
    public static final LevelConditions INSTANCE = new LevelConditions();

    public LevelConditions() {
        super(Defaulted.key("weapon_level_conditions"), "minecraft");
    }

    public void bootstrap() {
        register("clamped", () -> ClampedCondition.CODEC);
        register("list", () -> ListCondition.CODEC);
    }
}
