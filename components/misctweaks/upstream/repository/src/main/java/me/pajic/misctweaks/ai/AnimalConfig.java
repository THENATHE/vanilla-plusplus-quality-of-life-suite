package me.pajic.misctweaks.ai;

import me.fzzyhmstrs.fzzy_config.annotations.Translation;
import me.fzzyhmstrs.fzzy_config.util.Walkable;
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

@Translation(prefix = "misctweaks.config.animalConfig")
public class AnimalConfig implements Walkable {

	public ValidatedIdentifier id;
	public ValidatedBoolean doSearch;
	public ValidatedInt searchRange;
	public ValidatedDouble searchSpeed;
	public ValidatedInt searchGoalPriority;

	public AnimalConfig(Identifier id, boolean doSearch) {
		this.id = new ValidatedIdentifier(id);
		this.doSearch = new ValidatedBoolean(doSearch);
		this.searchRange = new ValidatedInt(16, 64, 1);
		this.searchSpeed = new ValidatedDouble(1.25, 2, 1);
		this.searchGoalPriority = new ValidatedInt(2, Integer.MAX_VALUE, 0);
	}

	public AnimalConfig() {
		this.id = ValidatedIdentifier.ofRegistry(Identifier.withDefaultNamespace("cow"), BuiltInRegistries.ENTITY_TYPE);
		this.doSearch = new ValidatedBoolean();
		this.searchRange = new ValidatedInt(16, 64, 1);
		this.searchSpeed = new ValidatedDouble(1.25, 2, 1);
		this.searchGoalPriority = new ValidatedInt(2, Integer.MAX_VALUE, 0);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof AnimalConfig ac && ac.id.equals(id);
	}
}
