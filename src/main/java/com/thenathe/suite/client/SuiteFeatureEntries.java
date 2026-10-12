package com.thenathe.suite.client;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.fabric.FabricIconHandler;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/** Mod Menu descriptions for integrated features, without inventing Fabric loader mods. */
public final class SuiteFeatureEntries {
    public record Feature(String id, String name, String description, String creator, String project,
                          boolean nativeSettings) {}

    public static final List<Feature> FEATURES = List.of(
            feature("drops_shulker", "Drops into Shulker", "Pickups go into your offhand shulker box when space permits.", "onebacon", "https://modrinth.com/mod/drops-into-shulker"),
            feature("beacon_placement", "Better Beacon Placement", "Build and dismantle beacon bases using materials in your inventory.", "Serilum", "https://modrinth.com/mod/better-beacon-placement"),
            feature("conduit_placement", "Better Conduit Placement", "Build and dismantle conduit frames using materials in your inventory.", "Serilum", "https://modrinth.com/mod/better-conduit-placement"),
            feature("grind_enchantments", "Grind Enchantments", "Extract enchantments onto books and transfer enchantments between books at a grindstone.", "mschae23", "https://modrinth.com/mod/grind-enchantments"),
            feature("netherite_instamine", "Netherite Instamine", "Boost qualifying Efficiency V netherite tools while Haste II is active.", "TheLokin", "https://modrinth.com/datapack/netherite-instamine"),
            feature("no_enderman_grief", "No Enderman Grief", "Prevent Endermen from picking up blocks.", "Jodek", "https://modrinth.com/datapack/no-enderman-grief"),
            feature("all_weather_channeling", "Trident All Weather Channeling", "Channeling can summon lightning outside thunderstorms.", "Jodek", "https://modrinth.com/datapack/trident-all-weather-channeling"),
            feature("swift_flight", "Swift Flight", "Happy ghast harness enchantment preserving existing Swift Flight books and equipment.", "Jodek", "https://modrinth.com/datapack/swift-flight"),
            feature("pet_owner_transfer", "Transfer Pet Owner", "Transfer a pet's ownership using a writable book.", "Jodek", "https://modrinth.com/datapack/transfer-pet-owner"),
            feature("call_parrot", "Call Your Parrot", "Bind a goat horn and recall your parrot.", "Jodek", "https://modrinth.com/datapack/call-your-parrot"),
            feature("call_nautilus", "Call Your Nautilus", "Bind a goat horn and recall your nautilus to water.", "Jodek", "https://modrinth.com/datapack/call-your-nautilus"),
            feature("call_horse", "Call Your Horse", "Bind a goat horn and recall your horse or another enabled mount.", "Jodek", "https://modrinth.com/datapack/call-your-horse"),
            feature("call_happy_ghast", "Call Your Happy Ghast", "Bind a goat horn and recall a happy ghast.", "Jodek", "https://modrinth.com/datapack/call-your-happy-ghast"),
            feature("call_dog", "Call Your Dog", "Bind a goat horn and recall your dog.", "Jodek", "https://modrinth.com/datapack/call-your-dog"),
            feature("call_cat", "Call Your Cat", "Bind a goat horn and recall your cat.", "Jodek", "https://modrinth.com/datapack/call-your-cat"),
            authored("pouch_magnet", "Pouch Magnet", "A Tool Pouch upgrade that increases pickup range."),
            authored("pouch_collector", "Pouch Collector", "A toggleable Tool Pouch upgrade that routes pickups into a backpack."),
            authored("debug_wrench", "Debug Wrench", "The integrated block state wrench, with its original recipe and features."));

    private SuiteFeatureEntries() {}

    private static Feature feature(String id, String name, String description, String creator, String project) {
        return new Feature("vpp_feature_" + id, name, description
                + " Independently maintained native implementation inspired by " + creator + ".", creator, project, true);
    }

    private static Feature authored(String id, String name, String description) {
        return new Feature("vpp_feature_" + id, name, description, "THENATHE",
                "https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite", false);
    }

    public static void register() {
        Mod suite = ModMenu.MODS.get(SuiteModPresentation.SUITE_ID);
        if (suite == null) throw new IllegalStateException("Vanilla++ is missing from Mod Menu's loader tree");
        for (Feature feature : FEATURES) {
            Mod entry = new FeatureMod(suite, feature);
            if (ModMenu.MODS.putIfAbsent(feature.id(), entry) == null) ModMenu.PARENT_MAP.put(suite, entry);
        }
        ModMenu.clearModCountCache();
    }

    private static final class FeatureMod implements Mod {
        private final Mod suite;
        private final Feature feature;

        private FeatureMod(Mod suite, Feature feature) {
            this.suite = suite;
            this.feature = feature;
        }

        @Override public String getId() { return feature.id(); }
        @Override public String getName() { return feature.name(); }
        @Override public String getDescription() { return feature.description(); }
        @Override public DynamicTexture getIcon(FabricIconHandler handler, int size) { return suite.getIcon(handler, size); }
        @Override public String getVersion() { return suite.getVersion(); }
        @Override public String getPrefixedVersion() { return suite.getPrefixedVersion(); }
        @Override public List<String> getAuthors() { return List.of("THENATHE"); }
        @Override public ContactInformation getContact(String name) { return suite.getContact(name); }
        @Override public Map<String, Collection<String>> getContributors() { return Map.of(); }
        @Override public SortedMap<String, Set<String>> getCredits() {
            SortedMap<String, Set<String>> credits = new TreeMap<>();
            credits.put("Original feature inspiration", Set.of(feature.creator()));
            return credits;
        }
        @Override public Set<Badge> getBadges() { return Set.of(); }
        @Override public String getWebsite() { return feature.project(); }
        @Override public String getIssueTracker() { return suite.getIssueTracker(); }
        @Override public String getSource() { return suite.getSource(); }
        @Override public String getParent() { return SuiteModPresentation.SUITE_ID; }
        @Override public Set<String> getLicense() { return suite.getLicense(); }
        @Override public Map<String, String> getLinks() { return Map.of("Original project", feature.project()); }
        @Override public boolean isReal() { return false; }
        @Override public boolean allowsUpdateChecks() { return false; }
        @Override public UpdateChecker getUpdateChecker() { return null; }
        @Override public void setUpdateChecker(UpdateChecker checker) {}
        @Override public UpdateInfo getUpdateInfo() { return null; }
        @Override public void setUpdateInfo(UpdateInfo info) {}
        @Override public void setChildHasUpdate() {}
        @Override public boolean getChildHasUpdate() { return false; }
        @Override public boolean isHidden() { return false; }
    }
}
