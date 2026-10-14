# Vanilla++ 1.4.4+26.3

Tool Pouch information now moves only when its rendered bounds overlap the displayed minimap in both dimensions. Previously, the shared HUD layout used configured corners and vertical overlap without checking horizontal bounds. A fresh rendered reproduction placed an ordinary map at X=260 and information at X=11; despite no overlap, version 1.4.3 moved the first information row from Y=17 to Y=155.

The correction keeps information placement and saved offsets independent of map preferences. Maps with horizontal offsets can leave information in its normal position, and actual overlap is detected even when the configured corners differ. The map renderers continue sharing their map placement preferences. Native submitted bounds also include map decoration icons, names and caption shadows, so clear or backgroundless maps do not leave protruding labels unaccounted for.

Only the outer suite version and the Tool Pouch Atlas and Elytra component advance: suite **1.4.4+26.3**, component **1.0.11+26.3**. Combined compatibility remains **1.4.3+26.3**, mixed-scale MapStitch remains **1.1.12+26.3**, and Backpack Crafting remains **1.0.3+26.3**. Original developer JARs, saved IDs, dependencies and the paused SSO-port track remain unchanged.

## Installation

Use Minecraft **26.3**, Fabric Loader **0.19.5**, Java **25+**, the matching suite on participating native clients/server, and the exact libraries in the installation pack. Polymer **0.18.2+26.3** is the supported server profile. [Dependency details](DEPENDENCIES.md) and [installation instructions](INSTALLATION_PACK.md) preserve side filtering and the official-download-only Fzzy Config requirement.

The current combined review uses Hotkey Wheel **1.0.7+26.3**, original Buildprint **0.1.19+26.x**, local Blocktone **0.1.24**, and Better Recipe Book: Rebetterered **1.11.7+26.3**. The four client integrations are separate installs, not new nested suite dependencies. Blocktone's release remains local.

## Verification

The [four-mod review](FOUR_MOD_QA_2026_10_13.md) separates the exact 1.4.3 baseline from final-candidate checks and records the rendered reproduction, fixed HUD acceptance, combined native/Polymer profiles, archive identity and installation checks. All final runtime gates passed on SHA-256 `e4663dddf2e176c3d5d7a669f59822dc76286ed28e195cc1a77b1bb1b3e7d269`: 30 native + 30 client-Polymer HUD scenes, 655 native + 655 Polymer all-four wheel assertions, 1,968 packaged BRBR checks, four native/Polymer backpack crafting profiles, and 198 standalone crafting/server assertions per native/Polymer profile. The final installer passed all 13 checks on real disposable client/server installs. No earlier artifact's result is relabeled as acceptance of this build.

The production rebuild executed 46 tasks. The independent source export matches every actual file and nested archive; four empty outer directory entries differ, so whole-archive byte identity is not claimed. The other 18 nested modules and suite root classes remain unchanged. Exact hashes, source/fixture records, screenshots and scope are in the linked review and evidence.

Historical [1.4.3 acceptance](RELEASE_1_4_3.md) remains available. Its direct Sorted recovery-selector checks do not establish a complete interrupted-sort timeline, and ClientSort's client fill/transfer UI was not independently exercised. The 1.4.4 correction changes HUD placement, not those storage contracts.
