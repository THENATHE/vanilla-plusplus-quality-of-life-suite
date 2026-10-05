package net.atlas.defaulted.component.phantom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EnchantmentOverrides(boolean grindsBackToDefault, boolean isStillEnchantable) {
    public static final Codec<EnchantmentOverrides> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.BOOL.optionalFieldOf("grinds_back_to_default", false).forGetter(EnchantmentOverrides::grindsBackToDefault),
                            Codec.BOOL.optionalFieldOf("is_still_enchantable", false).forGetter(EnchantmentOverrides::isStillEnchantable))
                    .apply(instance, EnchantmentOverrides::new));
}
