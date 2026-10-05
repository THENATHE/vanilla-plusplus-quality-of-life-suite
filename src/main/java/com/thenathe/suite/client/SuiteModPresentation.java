package com.thenathe.suite.client;

import java.util.Set;

/** Display-only grouping; Fabric identities and gameplay registrations stay intact. */
public final class SuiteModPresentation {
    public static final String SUITE_ID = "thenathe_mod_suite";

    private static final Set<String> CHILDREN = Set.of(
            "sso_backpack_toolpouch_mapstitch_shim", "chalk_polymer_compat",
            "shared_region_maps", "toolpouch_atlas_elytra_compat", "mapstitch_mixed_scales",
            "sensible_stackables_polymer_compat", "amethyst_curse_cleanser", "chalk-colorful-addon");
    private static final Set<String> ORIGINAL_FEATURES = Set.of(
            "simple_smithing_overhaul", "mapstitch", "toolpouch", "tiered_backpacks",
            "misctweaks", "simple_death_improvements", "sensible_stackables", "chalk");

    private SuiteModPresentation() {}

    public static boolean isSuiteChild(String id) { return CHILDREN.contains(id); }
    public static boolean isOriginalFeature(String id) { return ORIGINAL_FEATURES.contains(id); }
    public static boolean isHiddenAddon(String id) { return "chalk-colorful-addon".equals(id); }
}
