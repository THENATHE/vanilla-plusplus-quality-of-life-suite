package com.thenathe.combinedshim;
import net.fabricmc.api.ModInitializer;
import com.thenathe.suite.network.SuiteCapabilities;

/** Optional Polymer registration; original modules remain intact when Polymer is absent. */
public final class CombinedShim implements ModInitializer {
    @Override public void onInitialize() {
        SuiteCapabilities.initialize();
        if (Modules.enabled("simple_smithing_overhaul")) RepairMaterialRecovery.register();
    }
}
