package com.thenathe.amethystcursecleanser;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AmethystCurseCleanser implements ModInitializer {
	public static final String MOD_ID = "amethyst_curse_cleanser";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Amethyst Curse Cleanser is ready.");
	}
}
