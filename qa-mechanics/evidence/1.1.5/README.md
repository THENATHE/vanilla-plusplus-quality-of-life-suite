# Suite 1.1.5 mechanics and SSO verification

Frozen suite SHA-256: `67be52fd07c2d3dcfda4045dff5463e45bc0d91d37d3f759f4ebb3be043cd8f5`. Both dedicated profiles, with and without Polymer, passed and exited zero. [Exact results and provenance](mechanics-and-sso.json).

Each profile covers 752 valid Chalk conversion transitions, 32 calcite recipes, 18 curse-menu assertions, 16 actual item/drop lifecycle cases and 33 SSO assertions. The SSO fixture verifies effective repair getters, live custom material change/clear/reset and actual anvil material repair/rename under native and fallback capability decisions. Three historical Defaulted-only prototype checks are explicitly skipped; the library is compile-only and absent at runtime.

These are actual game-object tests with packet-stub players, not connected-client or GUI tests. Pouch Mending, stack inventory movement and network negotiation are verified separately. Enchantment-upgrade/pinnacle crafting and the entire remote mod stack are outside this fixture's scope. Full disposable logs/worlds stay ignored; this evidence contains sanitized assertions, module hashes and source/fixture hashes.

The previous passing candidate is retained under [candidate-83fc3a](candidate-83fc3a/mechanics-and-sso.json). These current results freshly rerun both profiles against the rebuilt final JAR; they do not relabel the candidate evidence. Earlier passing candidates remain in their own preserved subdirectories.
