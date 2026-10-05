package me.pajic.misctweaks.config;

import me.fzzyhmstrs.fzzy_config.annotations.Action;
import me.fzzyhmstrs.fzzy_config.annotations.RequiresAction;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.ValidatedField;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedMap;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedSet;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import me.pajic.misctweaks.MiscTweaks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@Version(version = 1)
public class ModClientConfig extends Config {

	private final ValidatedAny<LoweredShieldEntry> loweredShieldEntry = new ValidatedAny<>(new LoweredShieldEntry());

    public ModClientConfig() {
        super(MiscTweaks.id("client_config"));
		ValidatedField.Companion.attachProvider(
				loweredShieldEntry,
				Provider.Companion.getWIDGET_TITLE(),
				(entry, c) -> Component.literal(entry.id.get().getPath())
		);
    }

    @RequiresAction(action = Action.RELOAD_RESOURCES)
    public ValidatedBoolean lowerShield = new ValidatedBoolean();
    @RequiresAction(action = Action.RELOAD_RESOURCES)
    public ValidatedSet<LoweredShieldEntry> shields = loweredShieldEntry.toSet(
			new LoweredShieldEntry(Identifier.withDefaultNamespace("shield"), -4.0F)
	);
	@RequiresAction(action = Action.RELOAD_RESOURCES)
	public ValidatedInt raiseHotbarPixels = new ValidatedInt(2, 24, 0);
	@SuppressWarnings({"unchecked", "rawtypes"})
	public ValidatedMap<String, Integer> perDimensionBrightness = (new ValidatedMap.Builder())
			.keyHandler(new ValidatedString())
			.valueHandler(new ValidatedInt(50, 100, -1))
			.build();
}
