# Vanilla++ 1.4.1+26.3

This update corrects click/drop boundaries and adds an optional Sorted bridge for the floating toolbox crafting screens. The original plain backpack screen remains unchanged and is included in regression checks through Sorted's built-in support. Visible storage and crafting panels protect held items. The gap between panels and blank space above or below the crafting panel retain normal outside-click drops unless a visible recipe-book region or control overlaps the point. Recipe-book protection retains its native outside-click helper and supplements it with actual visible body/tab bounds, including the narrow overlay layout. The native helper keeps its conservative book-side protection. Standard recipe-book hover works without BRBR; optional BRBR hover and its separate Pins controls remain protected.

Sorted controls use the actual backpack storage and player inventory regions. Crafting inputs and output remain outside storage sorting, quick-stack transfers and the storage-data filtering view used for transfers and cache collection. Each crafting slot retains its own row and column coordinates. Global Alt keep rules continue using Sorted's separate existing behavior.

## Installation and versions

Minecraft 26.3, Fabric Loader 0.19.5+, Java 25. Replace the suite JAR on the server and matching native clients. See [installation instructions](INSTALLATION_PACK.md) and [exact dependencies](DEPENDENCIES.md). Runtime libraries and original developer inputs remain unchanged. Polymer 0.18.2+26.3 remains optional for matching clients and permits vanilla connectivity/item display on the server. Backpack screens require native clients. The separate SSO port remains paused.

| Component | Version | Change |
| --- | --- | --- |
| Suite/root | 1.4.1+26.3 | Release version and addon packaging |
| Backpack Crafting | 1.0.1+26.3 | Floating-panel drop bounds and optional Sorted bridge |
| Combined compatibility | 1.4.1+26.3 | Matching suite/addon capability record |
| Mixed-scale MapStitch addon | 1.1.10+26.3 | Metadata-only exact coordinator constraint |
| Tool Pouch atlas/Elytra addon | 1.0.10+26.3 | Unchanged |

Sorted **0.1.26** is the current installed optional input; the retained **0.1.25** API is compatible with the bridge. Sorted remains an unchanged external client mod, guarded and compile-only for the suite build. It is never bundled or required to launch. Existing optional ClientSort and BRBR hooks remain available. Fresh integration checks select **BRBR 1.11.7+26.3** and **Hotkey Wheel 1.0.6+26.3**; their own sources and JARs remain unchanged.

Item/recipe identifiers remain `vpp_backpack_crafting:iron_toolbox` and `vpp_backpack_crafting:netherite_toolbox`. Existing toolboxes, backpack contents, pouch rules and other settings retain their identities. Thanks to **pajic** for [Tiered Backpacks](https://modrinth.com/mod/tiered-backpacks) and [Tool Pouch](https://modrinth.com/mod/tool-pouch). The correction and optional adapter are suite-owned code. A read-only Sophisticated Backpacks/Core comparison informed the region/storage design; no source or assets from those projects are copied or bundled.

## Verification

Final JAR SHA-256: `2928783da8e2b532a102045a8abab9dd91c49813ae451062b2198010619ed36b`. The final clean build executed all **55 tasks**, and archive/bundle verification passed. An independent clean source build has byte-identical non-directory ZIP entries; four empty directory entries differ, so whole-archive byte identity is not claimed.

Fresh results on this exact artifact:

| Check / profile | Current result |
| --- | --- |
| [Dedicated server regressions](../qa-backpack-crafting/evidence/1.4.1/server/verification.json), native and Polymer | Passed 198 checks each; toolbox/crafting/menu/persistence and ClientSort schema/permutations. Sorted is not installed on these servers. |
| [Sorted 0.1.26, native](../qa-backpack-crafting/evidence/1.4.1/layout/sorted26-native/result.json) | Passed 590 assertions across 20 states, 128 cursor clicks and eight Pins checks. |
| [Sorted 0.1.26, server Polymer](../qa-backpack-crafting/evidence/1.4.1/layout/sorted26-polymer/result.json) | Passed 590 assertions across 20 states, 128 cursor clicks and eight Pins checks. |
| [Sorted 0.1.25 without BRBR](../qa-backpack-crafting/evidence/1.4.1/layout/sorted25-native/result.json) | Passed 550 assertions across 20 states and 120 cursor clicks. |
| BRBR 1.11.7 recipes with Sorted 0.1.26, [native](../qa-backpack-crafting/evidence/1.4.1/gui/brbr-sorted26-native/result.json) and [Polymer](../qa-backpack-crafting/evidence/1.4.1/gui/brbr-sorted26-polymer/result.json) | Passed eight connected cases per profile: four ordinary and four maximum, both toolbox grid sizes and leather/netherite capacities, using backpack ingredients. |
| Hotkey Wheel 1.0.6, [native](../qa-backpack-crafting/evidence/1.4.1/wheel/native/observations.json) and [Polymer](../qa-backpack-crafting/evidence/1.4.1/wheel/polymer/observations.json) | Passed 62 assertions plus one context observation per profile, 63 observations each. |
| [Installation package](installation-pack-verification.json) | Passed all 13 checks, including actual disposable client/server installs, side selection, exact hashes, official-download-only Fzzy, corrupt/unsafe input rejection, idempotency and a byte-identical kit rebuild. |

The completed Sorted profiles exercise its actual `/sorted storage` and `/sorted storage add` commands, storage classification, `MenuStorageView` data selection, actual sort and quick-stack callbacks, held-stack panel/book protection, blank crafting-side drops and server-confirmed item conservation. Filtering coverage is the storage-data view used by transfer/filter/cache operations; global Alt keep rules remain separate from those storage controls. Sorted 0.1.26 SHA-256 is `df080bab56e7f6c6f836533f403fe192a04019d9004e1c8d3acac5b6fe77edd5`; retained 0.1.25 is `44cc1f50724264a37319dcb4358d071cb8e20a6a324ac595e68ba887750e7a71`. The latter profile uses the standard recipe book without BRBR.

All seven connected runtime profiles passed on the final JAR. The three layout profiles total **1,730 assertions across 60 states and 376 cursor clicks**. Recipe profiles verify four ordinary and four maximum cases each, with authoritative craft counts `2, 2, 2, 2, 3, 3, 3, 3`; maximum cases use the actual BRBR maximum flag. Installation ZIP SHA-256: `83d3bbd104cbf95a58c68afc9e4e90e6cd3eb44eb1a23ec1486701f08dc99183`.

Verification scope:

- Clean build, locked-input/archive verification and independent source build cover the final artifact. Original developer inputs remain unchanged; Sorted remains absent from the nested archive. Fresh server and wheel profiles also exercise the suite with Sorted absent.
- Native and server-Polymer connected profiles with actual Sorted controls for plain, iron-toolbox 2×2, and netherite-toolbox 3×3 menus. Plain menu identifier is `tiered_backpacks:backpack_menu`; both crafting sizes share `vpp_backpack_crafting:crafting`. Registered configuration preserves that shared identity while using each actual menu's slot layout.
- Storage/player control placement across backpack capacities, sorting, quick-stack transfers and storage-data filtering/cache selection; protected crafting grid/output; correct per-row coordinates; held-stack drops in the panel gap and blank margins; protected clicks inside panels, the visible recipe-book body/tabs and BRBR Pins; item conservation.
- Standard recipe-book behavior without BRBR, plus current BRBR 1.11.7 recipe placement/instant crafting with backpack ingredients and the final screen bounds. Actual book body and visible tabs are covered in side and narrow overlay layouts; BRBR source and binary remain unchanged.
- Current Hotkey Wheel 1.0.6 activation/selection, overlapping keybindings, suite menus and unified configuration on the exact final suite JAR, with native and Polymer profiles.
- Installation ZIP checks passed for integrity, exact included hashes, side selection, sources/notices, idempotency, rejected corrupt/unsafe inputs and official-download-only Fzzy behavior. The installer ZIP is verified through its helper; launcher GUI import was not separately tested.

Historical [1.4.0 results](RELEASE_1_4_0.md), [backpack crafting evidence](BACKPACK_CRAFTING.md) and [validation](VALIDATION.md) keep their original artifacts and scope. Earlier ClientSort server tests do not establish current Sorted client behavior; matching optional APIs alone does not establish runtime acceptance.
