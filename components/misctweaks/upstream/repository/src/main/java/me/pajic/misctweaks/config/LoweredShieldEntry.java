package me.pajic.misctweaks.config;

import me.fzzyhmstrs.fzzy_config.annotations.ClientModifiable;
import me.fzzyhmstrs.fzzy_config.annotations.Translation;
import me.fzzyhmstrs.fzzy_config.util.Walkable;
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedFloat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

@ClientModifiable
@Translation(prefix = "misctweaks.client_config.loweredShieldEntry")
public class LoweredShieldEntry implements Walkable {

	public ValidatedIdentifier id;
	public ValidatedFloat offset;

	public LoweredShieldEntry(Identifier id, float offset) {
		this.id = new ValidatedIdentifier(id);
		this.offset = new ValidatedFloat(offset);
	}

	public LoweredShieldEntry() {
		this.id = ValidatedIdentifier.ofRegistry(Identifier.withDefaultNamespace("shield"), BuiltInRegistries.ITEM);
		this.offset = new ValidatedFloat(-4.0F, 0F, -5.0F);
	}
}
