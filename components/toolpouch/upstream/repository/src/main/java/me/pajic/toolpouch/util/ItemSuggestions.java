package me.pajic.toolpouch.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;

import java.util.ArrayList;
import java.util.List;

public class ItemSuggestions {

	private static final List<String> VALUES = new ArrayList<>();

	public static void update(LevelAccessor level) {
        Registry<Item> registry = level.registryAccess().lookupOrThrow(Registries.ITEM);
		VALUES.clear();
		VALUES.addAll(registry.keySet().stream().map(Identifier::toString).toList());
        registry.listTagIds().forEach(tag -> VALUES.add("#" + tag.location()));
	}

	public static List<String> get() {
		return VALUES;
	}
}
