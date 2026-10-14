# Vanilla++ 1.4.3+26.3

The [fresh four-mod review](FOUR_MOD_QA_2026_10_13.md) records the later checks with Hotkey Wheel 1.0.7, Blocktone 0.1.24, BRBR 1.11.7 and original Buildprint 0.1.19. The results below retain their original artifact versions and historical scope. The suite binary remains unchanged.

This update restores Sorted's backpack controls above the panel at its right edge and puts player controls below the full backpack frame, with search at its left edge and the other actions at its right. Horizontal anchors exclude the crafting extension and match the original plain backpack. It removes the extra inventory header row and label shift. Sorted's cursor recovery also stays out of the floating backpack crafting grid and output. Interrupted operations and unexpected carried-stack changes must use eligible storage/player destinations rather than returning items to temporary crafting slots. The earlier storage/control, drop-boundary and keep-rule exclusions remain included, with all six Sorted storage/player buttons scoped to those inventories.

The correction is a guarded optional integration in the suite-owned Backpack Crafting addon. Original Sorted and other developer JARs remain unchanged. Normal crafting clicks, saved rules/settings, recipe-book body/tabs and BRBR Pins remain intact.

## Installation and versions

Minecraft 26.3, Fabric Loader 0.19.5+, Java 25. Replace the suite JAR on the server and matching native clients. See [installation instructions](INSTALLATION_PACK.md) and [exact dependencies](DEPENDENCIES.md). Runtime libraries and original developer inputs remain unchanged. Sorted stays an optional external client mod and compile-only build input, never bundled. Current checks select Sorted 0.1.26, retained 0.1.25, BRBR 1.11.7+26.3 and Hotkey Wheel 1.0.6+26.3.

| Component | Version | Change |
| --- | --- | --- |
| Suite/root | 1.4.3+26.3 | Release version and addon packaging |
| Backpack Crafting | 1.0.3+26.3 | Above-backpack/below-inventory controls and optional Sorted recovery guard |
| Combined compatibility | 1.4.3+26.3 | Matching suite/addon capability record |
| Mixed-scale MapStitch addon | 1.1.12+26.3 | Metadata-only exact coordinator constraint |
| Tool Pouch atlas/Elytra addon | 1.0.10+26.3 | Unchanged |

Final item/recipe IDs remain `vpp_backpack_crafting:iron_toolbox` and `vpp_backpack_crafting:netherite_toolbox`. Existing toolbox items, backpack contents, pouch rules and other configuration retain their identities. Thanks to **pajic** for [Tiered Backpacks](https://modrinth.com/mod/tiered-backpacks) and [Tool Pouch](https://modrinth.com/mod/tool-pouch). The correction is suite-owned; original author notices remain intact and no Sophisticated source/assets are included. The separate SSO port remains paused.

Polymer 0.18.2+26.3 stays optional for matching clients and permits vanilla connectivity/item display on the server. Custom backpack screens require native clients. The original plain backpack keeps Sorted's built-in support and remains in regression coverage.

## Verification

Final JAR SHA-256: `321925c18ea28f2894821011c12904917ab0e5e3f44a2145eda01dfcc647bf80`. The clean build executed all **55 tasks**, archive verification passed, and [Fresh native/Polymer final-server profiles](../qa-backpack-crafting/evidence/1.4.3/server/result.json) each passed **198 assertions**. ClientSort coverage is schema/permutation regression; its fill handlers and client buttons are not claimed as executed acceptance.

The [independent exported-source build](../qa-backpack-crafting/evidence/1.4.3/reports/independent-build.json) passed. All main/source JAR file entries, **130/80**, match byte-for-byte including ZIP metadata, compressed streams and nested archives. Both whole archives differ only through four empty directory entries; whole-archive byte identity is not claimed.

[The placement preview](../qa-backpack-crafting/evidence/1.4.3/preview/result.json) passed **19 assertions** against actual original plain backpacks at **27/78/117 slots**. All seven offsets match at each capacity, the disposable client configuration was restored, and ten screen captures were collected. This preview performs **zero storage operations and zero recovery probes**; it verifies placement without claiming operation/data acceptance. Earlier candidate results remain superseded.

The verified placement contract uses main-frame width for the header's right edge, header y offset `-20`, bottom-left x `0`, bottom-right x `menu.getStorageWidth()`, and bottom y `menu.getHeight() + 1`, all relative to the GUI origin. The extension containing the crafting panel is excluded from these x bounds. Search is at the bottom row's far left; other actions align to the frame's right edge.

Tests compare all **seven control offsets** with an actual original plain backpack at the matching tier/configuration, covering both header and bottom row. An ordinary chest may supply a common vertical-row reference, but chest-relative x equality is not the intended contract.

Executed capacity coverage includes leather **27 slots**, default netherite **78 slots**, and an enlarged netherite backpack with an actual temporary **117-slot (9×13) configuration**, restored after disposable testing. Both 2×2 and 3×3 crafting sizes produce six crafting menus per profile. Each storage profile executed **48 actual button callbacks** and **42 direct recovery-selector probes** per profile, with **487 passed assertions**, including original configuration restoration. Native and Polymer layout runs each cover **30 states each**, including plain-menu references, widths 640/427, GUI height 300 for ordinary capacities and 400 for the enlarged backpack. All three storage, both layout, both recipe and both wheel profiles passed on the final JAR.

[The unchanged published 1.4.2 baseline](../qa-backpack-crafting/evidence/1.4.3/baseline/recovery/result.json), SHA-256 `7c85a7ae934917cb1c5427e18fa00e68b3943064c12c1b74b7c8a3d31511c24b`, reproduces the unsafe recovery boundary. Its **26 assertions across six probes** confirm that original-grid, matching-grid and empty-grid scenarios each select crafting index 1 in both 2×2 and 3×3 menus. The reproducer passed by demonstrating the old defect; it is not safe-boundary acceptance.

Fresh connected progress on this exact artifact:

| Check / profile | Current result |
| --- | --- |
| [Sorted 0.1.26 controls/recovery selector, native](../qa-backpack-crafting/evidence/1.4.3/storage/sorted26-native/result.json) | Passed 487 assertions across six crafting menus at 27/78/117 slots, 48 actual widget callbacks and 42 direct recovery-selector probes. All seven offsets match three actual plain-backpack references; client configuration restoration passed. Ten screenshots were collected. |
| [Sorted 0.1.26 controls/recovery selector, Polymer](../qa-backpack-crafting/evidence/1.4.3/storage/sorted26-polymer/result.json) | Passed 487 assertions across six 27/78/117-slot crafting menus, 48 widget callbacks and 42 direct selector probes; all seven offsets match three plain-backpack references, configuration restoration passed, and ten captures were collected. |
| [Sorted 0.1.25 controls/recovery selector, native without BRBR](../qa-backpack-crafting/evidence/1.4.3/storage/sorted25-native/result.json) | Passed 487 assertions across six 27/78/117-slot crafting menus, 48 widget callbacks and 42 direct selector probes; all seven offsets match three plain references, configuration restoration passed, and ten captures were collected. |
| [Sorted 0.1.26 layout, native](../qa-backpack-crafting/evidence/1.4.3/layout/sorted26-native/result.json) | Passed 886 assertions across 30 states, 192 cursor clicks and 12 Pins callbacks; client size configuration was restored. |
| [Sorted 0.1.26 layout, Polymer](../qa-backpack-crafting/evidence/1.4.3/layout/sorted26-polymer/result.json) | Passed 886 assertions across 30 states, 192 cursor clicks and 12 Pins callbacks; client size configuration was restored. |
| [BRBR 1.11.7 with Sorted 0.1.26, native recipes](../qa-backpack-crafting/evidence/1.4.3/recipes/native/result.json) | Passed eight connected cases, four ordinary and four maximum, with backpack ingredients. |
| [BRBR 1.11.7 with Sorted 0.1.26, Polymer recipes](../qa-backpack-crafting/evidence/1.4.3/recipes/polymer/result.json) | Passed eight connected cases, four ordinary and four maximum, with backpack ingredients. |
| [Hotkey Wheel 1.0.6, native](../qa-backpack-crafting/evidence/1.4.3/wheel/native/result.json) | Passed 62 assertions plus one context observation, with 15 functional configurations and 18 captures. |
| [Hotkey Wheel 1.0.6, Polymer](../qa-backpack-crafting/evidence/1.4.3/wheel/polymer/result.json) | Passed 62 assertions plus one context observation, with 15 functional configurations and 18 captures. |
| [Installation package](installation-pack-verification-1.4.3.json) | Passed all 13 checks: actual disposable client/server installs, exact hashes, side filtering, notices/sources, corruption/path rejection, idempotency, Fzzy binary exclusion and byte-identical kit rebuild. |

The three storage profiles total **1,461 assertions across 18 crafting menus, 144 widget callbacks and 126 direct selector probes**, with nine actual plain-backpack capacity references, three configuration restorations and 30 captures. All exercise all six action buttons plus Shift Quick Dump/Quick Empty. They verify unchanged grid/result stacks, client/server components and counts, conservation and held-cursor acknowledgments. Both layout profiles total **1,772 assertions across 60 states, 384 cursor clicks and 24 Pins callbacks**, with restored size configuration. Both recipe profiles passed **16 connected cases total**, four ordinary and four maximum per profile. Both wheel profiles passed 62 assertions plus one unflagged context observation each. Across server, storage, layout, wheel and preview checks, the final record totals **3,772 declared assertions plus 16 recipe cases**. Installation ZIP SHA-256: `d1cd98caf03932a63ff8f8c6b340592bf67d0ed9a28976f0f0619f9703ed20a7`. [The collected runtime record](../qa-backpack-crafting/evidence/1.4.3/verification.json) includes 11 current runs and **110 screen captures**. The historical unsafe baseline's 26 assertions and two captures are separate. Recovery probes invoke Sorted's actual private destination selector against prepared states. They do not exercise a full interrupted-sort timeline, so full abort-to-recovery behavior is not claimed as runtime acceptance. Earlier height-240 layout results retain their historical scope.

Historical [1.4.2 acceptance](RELEASE_1_4_2.md) retains its exact artifact and scope; it does not establish the new recovery behavior.

Verification scope:

- Clean build, locked-input/archive checks, unchanged original binaries, optional loading without Sorted and independent exported-source build comparison.
- Actual behavior of all six Sorted storage/player controls with Sorted 0.1.26 native and Polymer profiles, and retained 0.1.25 native. Check both toolbox grid sizes and backpack capacities, normal sort/fill/transfer scopes, bottom-row search/actions aligned to the full main frame, all seven offsets matching an actual plain backpack at the corresponding tier/configuration, and server-confirmed conservation.
- Seven direct recovery-selector cases across each profile's six crafting menus, including prepared cursor-mismatch states, occupied/empty crafting cells and result exclusion. This verifies the destination boundary rather than a full interrupted-sort timing path; the unchanged 1.4.2 baseline above demonstrates the original leak.
- Preserve the rule-marker/Alt-edit exclusions, native recipe-book and optional BRBR body/tab/Pins protection, and actual crafting-side drop bounds.
- Current ordinary/maximum BRBR recipes with backpack ingredients, native and Polymer; standard recipe evidence retains its historical artifact/scope. Hotkey Wheel 1.0.6 activation, overlapping keys, suite menus and unified configuration on the exact final candidate.
- Native/Polymer server regressions and all 13 installation checks passed, including actual disposable installs, exact hashes, side selection, notices/sources, corruption/path rejection, idempotency and official-download-only Fzzy. Launcher GUI import was not separately tested.

The ClientSort integration remains separate. Static inspection of ClientSort 3.104.1+26.3 and the retained compatibility source confirms crafting exclusions in client source/destination collection and accelerated server schemas; fill/transfer payloads validate both arrays. Existing QA executes sort permutations and schema rejection, without fresh client fill/transfer or accelerated-payload acceptance. The custom fill/transfer operator returns leftovers only to its original filtered source, so this audit found no equivalent arbitrary-slot return scan there.

See [backpack records](BACKPACK_CRAFTING.md) and [validation](VALIDATION.md) for current status and preserved historical evidence.
