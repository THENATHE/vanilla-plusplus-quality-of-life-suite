package com.thenathe.suite;
import com.thenathe.suite.network.SuiteCapabilities;
import net.fabricmc.api.ModInitializer;
public final class Suite implements ModInitializer {
 @Override public void onInitialize() {
  SuiteCapabilities.initialize();
  com.thenathe.suite.features.FeatureConfig.initialize();
  com.thenathe.suite.wrench.DebugWrench.initialize();
  SuiteItems.initialize();
  com.thenathe.suite.features.CoreFeatures.initialize();
  com.thenathe.suite.pets.PetFeatures.initialize();
 }
}
