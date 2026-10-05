package net.atlas.defaulted.component.backport;

//? <=1.21.1 {
/*import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;

public record Enchantable(int value) {
    public static final Enchantable EMPTY = new Enchantable(0);
    public static final Codec<Enchantable> CODEC = ExtraCodecs.POSITIVE_INT.xmap(Enchantable::new, Enchantable::value).fieldOf("value").codec();
}
*///?}