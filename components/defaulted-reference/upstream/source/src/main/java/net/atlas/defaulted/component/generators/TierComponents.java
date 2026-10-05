package net.atlas.defaulted.component.generators;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum TierComponents implements StringRepresentable {
    DURABILITY("durability"),
    ENCHANTABLE("enchantable"),
    REPAIRABLE("repairable"),
    TOOL("tool");
    public static final Codec<TierComponents> CODEC = StringRepresentable.fromEnum(TierComponents::values);
    public final String name;
    TierComponents(String name) {
        this.name = name;
    }
    @Override
    public String getSerializedName() {
        return name;
    }
}
