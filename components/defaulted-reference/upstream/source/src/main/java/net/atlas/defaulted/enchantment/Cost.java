package net.atlas.defaulted.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.defaulted.enchantment.value_provider.ValueProvider;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Optional;

public record Cost(ValueProvider base, ValueProvider perLevelAboveFirst) {
    public static final Codec<Cost> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                            ValueProvider.CODEC.fieldOf("base").forGetter(Cost::base), ValueProvider.CODEC.fieldOf("per_level_above_first").forGetter(Cost::perLevelAboveFirst)
                    )
                    .apply(i, Cost::new)
    );

    public Enchantment.Cost toCost(Enchantment.Cost original) {
        return new Enchantment.Cost(this.base().roundedValue(Optional.of(original.base())),
                this.perLevelAboveFirst().roundedValue(Optional.of(original.perLevelAboveFirst())));
    }
}