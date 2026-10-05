package me.pajic.toolpouch.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;

import java.util.function.Predicate;

public class ClientUtil {

	public static boolean shouldScope = false;
	public static float zoomModifier = 1.0F;

	@SuppressWarnings("unchecked")
	public static final EntityType<Player> PLAYER = (EntityType<Player>) BuiltInRegistries.ENTITY_TYPE
			.getValueOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("player")));

	public static Predicate<ItemStackTemplate> getSupportedLanterns() {
		return stack -> stack.is(BlockItemTags.LANTERNS.item()) || Block.byItem(stack.item().value()) instanceof LanternBlock;
	}
}
