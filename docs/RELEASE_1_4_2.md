# Vanilla++ 1.4.2+26.3

This update keeps Sorted's global keep-rule suggestions and markers off the temporary backpack crafting grid and output. Alt-click and Alt-scroll on those slots continue through normal crafting handling without creating or editing a global keep rule. Backpack storage, player inventory and ordinary menus retain their rule behavior; saved global rules and Sorted settings remain intact.

The optional bridge scopes suppression to the actual crafting slot and its render, click or scroll operation. The 1.4.1 storage/control integration, floating-panel drop boundaries, standard recipe-book behavior and optional BRBR hover/Pins protection remain included. Original Sorted and other developer JARs are unchanged.

## Installation and versions

Minecraft 26.3, Fabric Loader 0.19.5+, Java 25. Replace the suite JAR on the server and matching native clients. See [installation instructions](INSTALLATION_PACK.md) and [exact dependencies](DEPENDENCIES.md). Runtime libraries and original developer inputs remain unchanged. Sorted remains an optional external client mod and compile-only build input, never bundled. Current optional checks select Sorted 0.1.26, retained 0.1.25, BRBR 1.11.7+26.3 and Hotkey Wheel 1.0.6+26.3.

| Component | Version | Change |
| --- | --- | --- |
| Suite/root | 1.4.2+26.3 | Release version and addon packaging |
| Backpack Crafting | 1.0.2+26.3 | Scoped optional Sorted rule integration |
| Combined compatibility | 1.4.2+26.3 | Matching suite/addon capability record |
| Mixed-scale MapStitch addon | 1.1.11+26.3 | Metadata-only exact coordinator constraint |
| Tool Pouch atlas/Elytra addon | 1.0.10+26.3 | Unchanged |

Item/recipe identifiers remain `vpp_backpack_crafting:iron_toolbox` and `vpp_backpack_crafting:netherite_toolbox`. Existing items, contents, pouch rules and other configuration retain their identities. Thanks to **pajic** for [Tiered Backpacks](https://modrinth.com/mod/tiered-backpacks) and [Tool Pouch](https://modrinth.com/mod/tool-pouch). The addon and optional rule correction are suite-owned code. Original author credits/licenses remain intact, no Sophisticated source/assets are included, and the separate SSO port remains paused.

Polymer 0.18.2+26.3 stays optional for matching clients and permits vanilla connectivity/item display on the server. Custom backpack screens require native clients. The original plain backpack continues using Sorted's own built-in support and remains in regression coverage.

## Verification

Final JAR SHA-256: `7c85a7ae934917cb1c5427e18fa00e68b3943064c12c1b74b7c8a3d31511c24b`. The clean build executed all **55 tasks**, and archive verification passed. [Native and Polymer final-server regressions](../qa-backpack-crafting/evidence/1.4.2/server/result.json) each passed **198 assertions**.

The [independent exported-source build](../qa-backpack-crafting/evidence/1.4.2/reports/independent-build.json) passed. All **130 non-directory ZIP entries** match the candidate byte-for-byte. Four empty directory entries differ: `assets/tiered_backpacks/`, its `lang/` directory, `data/swift_flight/loot_table/`, and its `chests/` directory. The independent archive SHA-256 is `0d6b0080b3388cba7aa7f0968bb596614a554e6ac9bcafaab1c9329f781910ed`; whole-archive byte identity is not claimed.

[All recorded runtime profiles passed on this final JAR](../qa-backpack-crafting/evidence/1.4.2/verification.json):

| Check / profile | Current result |
| --- | --- |
| [Sorted 0.1.26 rules, native](../qa-backpack-crafting/evidence/1.4.2/rules/sorted26-native/result.json) | Passed 146 assertions across six menus: plain, 2×2 and 3×3 at 27/78 storage slots. Exercised 60 Alt-edit callbacks, 24 protected input/result actions and 36 positive storage/player actions, plus 20 marker traces and six screen captures. |
| [BRBR 1.11.7 with Sorted 0.1.26, native](../qa-backpack-crafting/evidence/1.4.2/recipes/native/result.json) | Passed eight recipe cases, four ordinary and four maximum, using backpack ingredients. |
| [Sorted 0.1.26 rules, Polymer](../qa-backpack-crafting/evidence/1.4.2/rules/sorted26-polymer/result.json) | Passed 146 assertions across six menus, 60 Alt-edit callbacks, 20 marker traces and six captures. |
| [Sorted 0.1.25 rules, native without BRBR](../qa-backpack-crafting/evidence/1.4.2/rules/sorted25-native/result.json) | Passed 146 assertions across six menus, 60 Alt-edit callbacks, 20 marker traces and six captures. |
| [Sorted 0.1.26 layout/drop regression, native with BRBR](../qa-backpack-crafting/evidence/1.4.2/layout/sorted26-native/result.json) | Passed 590 assertions across 20 states, 128 cursor clicks and eight Pins checks, with actual storage commands and server-confirmed item conservation. |
| [BRBR 1.11.7 with Sorted 0.1.26, Polymer](../qa-backpack-crafting/evidence/1.4.2/recipes/polymer/result.json) | Passed eight cases, four ordinary and four maximum, using backpack ingredients. |
| [Standard recipe book, native without Sorted or BRBR](../qa-backpack-crafting/evidence/1.4.2/recipes/no-sorted/result.json) | Passed four cases using backpack ingredients. |
| Hotkey Wheel 1.0.6, [native](../qa-backpack-crafting/evidence/1.4.2/wheel/native/result.json) and [Polymer](../qa-backpack-crafting/evidence/1.4.2/wheel/polymer/result.json) | Passed 62 assertions plus one context observation per profile; 15 functional configuration screens and 18 captures each. |
| [Installation package](installation-pack-verification.json) | Passed all 13 checks: actual disposable client/server installs, exact hashes, side selection, notices/sources, corruption/path rejection, idempotency, Fzzy binary exclusion and a byte-identical kit rebuild. |

The three completed rule profiles total **438 assertions across 18 menus, 180 Alt-edit callbacks, 60 marker traces and 18 captures**. They verify full in-memory configuration and persisted-rule preservation for protected slots, target-only saved edits on storage/player slots, and restoration of the fixture's configuration in all six cases per profile. They dispatch real Alt-click/scroll callbacks with native input constants and development-only SDL focus events. Installation ZIP SHA-256: `1e99e0e39157fd06712a0edda6b126b80771af2e56e325776c3bcc45259ea998`. Hotkey Wheel 1.0.6 SHA-256: `1e7ee2c1306bb9a0de2eb5da3075a42a50396ae8bd3298c48f48c337e984f9d1`. Native and Polymer BRBR profiles each execute two cycles and verify four ordinary and four actual maximum cases; the standard native profile without Sorted/BRBR verifies four recipe cases, for **20 recipe cases total**. Fresh layout acceptance covers the native profile; prior Polymer layout results retain their 1.4.1 artifact and scope.

Historical [1.4.1 acceptance](RELEASE_1_4_1.md) keeps its exact artifact and scope; it does not verify the new rule integration.

Verification scope:

- Clean build, locked inputs, nested module/archive verification, optional loading with Sorted absent, and unchanged original binaries. The independent source comparison above records actual file-entry equality and whole-archive differences.
- Actual Sorted 0.1.26 and retained 0.1.25 keep markers/suggestions, Alt-click and Alt-scroll on crafting input/output: no global rule creation or edits, normal crafting callbacks, unchanged saved rules/settings and preserved storage/player/plain-menu rule behavior.
- Both 2×2/3×3 layouts, backpack capacities, recipe-book side/narrow states, native and Polymer profiles; original plain backpack regression; storage sorting/quick stack/data filtering and crafting-side drops; recipe-book body/tabs and optional BRBR Pins; server-confirmed conservation.
- Standard recipe book without BRBR and BRBR 1.11.7 with backpack ingredients, ordinary/maximum crafting and normal output clicks on the exact final suite JAR.
- Hotkey Wheel 1.0.6 activation/selection, overlapping keybindings, suite menus and unified configuration on the exact final suite JAR in native and Polymer profiles.
- Installation ZIP integrity, exact included hashes, side selection, notices/sources, actual disposable client/server installs, corruption/path rejection, idempotency and official-download-only Fzzy behavior all passed. Launcher GUI import was not separately tested.

See [backpack records](BACKPACK_CRAFTING.md) and [validation](VALIDATION.md) for current status and preserved historical evidence. Sorted's transfer/filter/cache storage-data view remains separate from global Alt keep rules; temporary crafting inputs/output are excluded from both through their respective integration paths.
