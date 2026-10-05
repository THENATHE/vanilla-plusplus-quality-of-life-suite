package com.thenathe.suite;
import com.thenathe.suite.network.SuiteCapabilities;
import net.fabricmc.api.ModInitializer;
public final class Suite implements ModInitializer {
 @Override public void onInitialize() { SuiteCapabilities.initialize(); }
}
