package me.pajic.tiered_backpacks.component;

import me.pajic.tiered_backpacks.util.BackpackTier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.component.DyedItemColor;

public class ModDataComponents {

	public static final DataComponentType<BackpackTier> BACKPACK_TIER = DataComponentType.<BackpackTier>builder()
			.persistent(StringRepresentable.fromEnum(BackpackTier::values)).build();

	public static final DataComponentType<DyedItemColor> STORED_BACKPACK_DYE = DataComponentType.<DyedItemColor>builder()
			.persistent(DyedItemColor.CODEC).build();

	public static void init() {}
}
