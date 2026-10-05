package me.pajic.toolpouch.util;

import me.fzzyhmstrs.fzzy_config.annotations.Translation;
import me.fzzyhmstrs.fzzy_config.util.AllowableStrings;
import me.fzzyhmstrs.fzzy_config.util.Walkable;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;

@Translation(prefix = "toolpouch.config.allowedItem")
public class AllowedItem implements Walkable {

	public ValidatedString id;
	public ValidatedInt maxStackSize;
	public ValidatedInt maxStackCount;

	public AllowedItem(String id, int maxStackSize, int maxStackCount) {
		this.id = new ValidatedString(id);
		this.maxStackSize = new ValidatedInt(maxStackSize);
		this.maxStackCount = new ValidatedInt(maxStackCount);
	}

	public AllowedItem() {
		this.id = new ValidatedString("", new AllowableStrings(ItemSuggestions.get()::contains, ItemSuggestions::get));
		this.maxStackSize = new ValidatedInt(1, 64, 0);
		this.maxStackCount = new ValidatedInt(1, 64, 0);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof AllowedItem ai && ai.id.get().equals(this.id.get());
	}
}
