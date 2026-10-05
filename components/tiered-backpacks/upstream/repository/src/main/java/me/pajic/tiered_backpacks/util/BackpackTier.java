package me.pajic.tiered_backpacks.util;

import me.fzzyhmstrs.fzzy_config.util.EnumTranslatable;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum BackpackTier implements StringRepresentable, EnumTranslatable {
    LEATHER("leather", -6265536),
    COPPER("copper", -2790072),
    IRON("iron", -3947581),
    GOLDEN("golden", -1659115),
    DIAMOND("diamond", -11932970),
    NETHERITE("netherite", -11777972);

    private final String name;
	private final int color;

    BackpackTier(String name, int color) {
        this.name = name;
		this.color = color;
    }

	public int getColor() {
		return color;
	}

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    @Override
    public @NotNull String prefix() {
        return "tiered_backpacks.backpackTier";
    }
}
