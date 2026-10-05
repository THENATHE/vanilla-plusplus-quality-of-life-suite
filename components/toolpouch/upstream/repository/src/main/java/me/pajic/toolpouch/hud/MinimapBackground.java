package me.pajic.toolpouch.hud;

import me.fzzyhmstrs.fzzy_config.util.EnumTranslatable;
import org.jetbrains.annotations.NotNull;

public enum MinimapBackground implements EnumTranslatable {
	CLEAR, TEXTURE, NONE;

	@Override @NotNull
	public String prefix() {
		return "toolpouch.minimapBackgroundStyle";
	}
}
