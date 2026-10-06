# Suite 1.1.5 mechanics and SSO verification

Frozen suite SHA-256: `4d0086b3472ea890a9ed6813ef8e32936e4d29b495858c4ca937ede60a16d2f3`. Both dedicated profiles, with and without Polymer, passed and exited zero. [Exact results and provenance](mechanics-and-sso.json).

Each profile covers 752 valid Chalk conversion transitions, 32 calcite recipes, 18 curse-menu assertions, 16 actual item/drop lifecycle cases and 33 SSO assertions. The SSO fixture verifies effective repair getters, live custom material change/clear/reset and actual anvil material repair/rename under native and fallback capability decisions. Three historical Defaulted-only prototype checks are explicitly skipped; the library is compile-only and absent at runtime.

These are actual game-object tests with packet-stub players, not connected-client or GUI tests. Pouch Mending, stack inventory movement and network negotiation are verified separately. Enchantment-upgrade/pinnacle crafting and the entire remote mod stack are outside this fixture's scope. Full disposable logs/worlds stay ignored; this evidence contains sanitized assertions, module hashes and source/fixture hashes.
