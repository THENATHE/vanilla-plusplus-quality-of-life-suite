package me.pajic.misctweaks.config;

import me.fzzyhmstrs.fzzy_config.annotations.Translation;
import me.fzzyhmstrs.fzzy_config.util.AllowableStrings;
import me.fzzyhmstrs.fzzy_config.util.Walkable;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import me.pajic.misctweaks.util.ModUtil;

@Translation(prefix = "misctweaks.config.flammableBlockEntry")
public class FlammableBlockEntry implements Walkable {

	public ValidatedString id;
	public ValidatedInt igniteOdds;
	public ValidatedInt burnOdds;

	public FlammableBlockEntry(String id, int igniteOdds, int burnOdds) {
		this.id = new ValidatedString(id);
		this.igniteOdds = new ValidatedInt(igniteOdds);
		this.burnOdds = new ValidatedInt(burnOdds);
	}

	public FlammableBlockEntry() {
		this.id = new ValidatedString("", new AllowableStrings(ModUtil.BLOCK_SUGGESTIONS::contains, () -> ModUtil.BLOCK_SUGGESTIONS));
		this.igniteOdds = new ValidatedInt(60, 600, 5);
		this.burnOdds = new ValidatedInt(100, 1000, 5);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof FlammableBlockEntry fbe && fbe.id.equals(id);
	}
}
