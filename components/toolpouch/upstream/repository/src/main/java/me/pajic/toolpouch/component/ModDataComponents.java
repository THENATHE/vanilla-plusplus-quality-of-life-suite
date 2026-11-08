package me.pajic.toolpouch.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.List;

public class ModDataComponents {

	public static final DataComponentType<DyedItemColor> STORED_TOOL_POUCH_DYE = DataComponentType.<DyedItemColor>builder()
			.persistent(DyedItemColor.CODEC).build();

	public static final DataComponentType<Boolean> IS_NETHERITE_POUCH = DataComponentType.<Boolean>builder()
			.persistent(Codec.BOOL).build();

	public static final DataComponentType<List<Integer>> DISABLED_SLOTS = DataComponentType.<List<Integer>>builder()
			.persistent(Codec.INT.listOf()).build();

	public static void init() {}
}
