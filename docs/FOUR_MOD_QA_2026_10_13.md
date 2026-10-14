# Fresh four-mod review, 2026-10-13

This review began with the current released artifacts together and found a real suite HUD placement defect. The final suite advances to 1.4.4; the other mod binaries remain unchanged. Baseline and final-candidate results are recorded separately. The reviewed installation targets Minecraft 26.3, Fabric Loader 0.19.5 and Java 25. Original developer JARs, saved identifiers and the separately paused SSO port are preserved.

| Component | Exact candidate |
| --- | --- |
| Vanilla++ Quality of Life Suite | `1.4.4+26.3`, SHA-256 `e4663dddf2e176c3d5d7a669f59822dc76286ed28e195cc1a77b1bb1b3e7d269` |
| Hotkey Wheel | `1.0.7+26.3`, SHA-256 `3095b28e981dd995aabc582b7abe785baeccfdc4ac00128eabd6bda0d5a55fd6` |
| Blocktone for Buildprint | `0.1.24`, SHA-256 `b9c42fd4679f9c70c584a6f8a888e51911c0dfe90f10dddd92c4e427efec5940` |
| Better Recipe Book: Rebetterered | `1.11.7+26.3`, SHA-256 `28868f38950057d850d634364881855503093dcd8e70064faf7febcecb81621f` |
| Original Buildprint | `0.1.19+26.x`, SHA-256 `bd60814e7ba944d7a67bfbf9831a8fa156223277345dc87569cd358a326e3f1f` |
| Suite-supported Polymer server profile | `0.18.2+26.3` |

## Confirmed suite HUD correction

A rendered reproduction on the exact released 1.4.3 JAR showed the ordinary map at X=262..398 and information at X=13..81. Their horizontal bounds did not overlap, yet the information moved from its natural Y=17 to Y=155. The old layout compared configured corners and vertical positions, without horizontal intersection. The correction measures native map canvas/background/caption bounds, both scales and origins, and the complete information block before deciding whether to move it. Independent saved preferences remain intact.

The earlier caption-shadow fixture used the font advance/line-height box as an approximation of pixel bounds. The final native prepared-glyph observer showed that its caption ends at Y=149 and information starts at Y=149: touching rather than physical overlap. That earlier “shadow overlap” is an observer false positive and is retained as such. The expanded fixture checks a genuine one-pixel overlap separately from this touching-boundary control.

A later renderer audit established that decoration icons and named labels can extend beyond the canvas/background. The intermediate `ca8a1f0c…` candidate reproduced a real named white-banner label extending the ordinary clear-background map union left to X=74, while information remained at X=13..81: seven pixels of native prepared-bounds overlap. The final correction records native submitted icon/text bounds within the owned minimap draw scope, including glyph and shadow extents. [Final native submission-bounds acceptance](../qa-hud/evidence/2026-10-13-final-1.4.4-native-bounds/README.md) passed 30 native and 30 client-Polymer scenes on the exact final candidate. The fixture checks real named-marker overlap on ordinary and atlas maps with clear/no backgrounds, preserved disjoint/touching placements, genuine caption overlap, frame scope cleanup, saved preferences and the settings screen. [Release scope](RELEASE_1_4_4.md) identifies the only changed component, Tool Pouch Atlas and Elytra compatibility 1.0.11.

## Baseline suite code and server review

Fresh source review covered backpack menu permissions and identity, grid capacity, recipe placement across backpack/player inventories, result synchronization, shift-click routing, crafting remainders and close cleanup. It also checked BRBR's optional eligibility/placement/pending-request adapters, Sorted's crafting-slot exclusions and interrupted-operation destination guard, pickup routing and collector pause while a portable menu is open. The additional HUD audit established the defect above; the reviewed storage and crafting code remains unchanged.

The exact released **1.4.3** suite passed these fresh baseline standalone server profiles:

| Fresh check | Result |
| --- | --- |
| Backpack crafting and ClientSort server schema | 198 assertions with Polymer and 198 without it |
| Broader SSO, storage and Chalk | 140 SSO + 180 storage + 15,587 Chalk assertions with Polymer; 105 + 180 + 15,587 without it |
| Suite feature mechanics and real restart | Four successful initial/restart profiles, with 250/259 recorded checks in each native and Polymer pair |
| Pickup utilities, wrench and real restart | Four successful initial/restart profiles; pickup assertions 68/71 native and 601/604 Polymer, plus wrench assertions 60/60 and 76/76 |
| MiscTweaks and death improvements | 154 assertions in each native and Polymer profile |
| Actual nested inventory packet codecs | Default limits: 7,103 Polymer / 6,202 native assertions; uncapped 2,048-item limits: 7,728 / 7,123; no failures |

[Collected server evidence](../qa-four-mods/evidence/2026-10-13/server/verification.json) includes results, exact launch inputs, executed fixture class hashes and source records. These are standalone suite checks; combined client acceptance is recorded separately. Counts include repeated configurations and loot/geometry cases, rather than representing distinct features.

The baseline build with the complete Microsoft Java 25 JDK reran all 46 tasks and reproduced the released 1.4.3 JAR byte-for-byte. The system Java 25 installation lacks a compiler, and substituting Java 27's compiler changes class bytes despite targeting Java 25. Those exploratory outputs are not release candidates. Use a complete Java 25 JDK when reproducing the release.

The **baseline 1.4.3** reviewed installation pack passed all 13 archive/installer checks after refreshing optional integration documentation. Its SHA-256 is `a37fff16ec416f9e307973585f294564203ddce94c85adf357ebb50b528f53bb`. All embedded binaries are unchanged. Actual disposable client/server installs, side filtering, official Fzzy downloads, checksum/path/corruption rejection and idempotency passed. The [installation record](../qa-four-mods/evidence/2026-10-13/installation.json) keeps the earlier pack hash for comparison; launcher GUI import was not separately tested.

## Intermediate ca8a suite archive, server and installer acceptance

The intermediate `ca8a1f0c…` Java 25 production rebuild executed 46 tasks. Archive verification passed; [comparison with 1.4.3](../qa-four-mods/evidence/2026-10-13/intermediate-1.4.4-ca8a/artifact-diff.json) confirms that only the HUD component changes. The other 18 nested modules and root classes remain byte-identical. An [independent fresh source build](../qa-four-mods/evidence/2026-10-13/intermediate-1.4.4-ca8a/source-rebuild.json) matches all 130 non-directory file entries, including nested JARs. Four outer empty directory entries differ; whole-archive byte identity is not claimed.

[Intermediate server checks](../qa-four-mods/evidence/2026-10-13/intermediate-1.4.4-ca8a/server/verification.json) passed 198 backpack/crafting assertions per native and Polymer profile. Both profiles also passed native feature mechanics and real restart, with 250 initial and 259 restart checks each. These fresh results use the exact intermediate `ca8a1f0c…` binary; they do not establish acceptance of the later complete-render-bounds candidate. The broader baseline suites above retain their 1.4.3 identities.

The [intermediate 1.4.4 installation pack](../qa-four-mods/evidence/2026-10-13/intermediate-1.4.4-ca8a/installation.json) passed all 13 checks, including real client/server installation, exact hashes and side filtering, preservation on mismatched files, corruption/path rejection, Fzzy exclusion, idempotency and byte-identical kit rebuild. Pack SHA-256: `70974e88afd292a40de71f02460a3a5bbb208583f09708f12b72f84f0c6b0000`. Launcher GUI import was not separately tested.

## Final complete native-render-bounds candidate

The exact final `e4663ddd…` build passed archive checks and all 46 production rebuild tasks. [Artifact comparison](../qa-four-mods/evidence/2026-10-13/final-1.4.4/artifact-diff.json) confirms the other 18 nested modules and root classes match released 1.4.3 byte-for-byte. The only changed module is the HUD addon, SHA-256 `08fb7ee7565102cdecc97659703aca8dd7c037f7401d48bbf7d736c06219efb8`.

An [independent source rebuild](../qa-four-mods/evidence/2026-10-13/final-1.4.4/source-rebuild.json) matches all 130 non-directory file entries, including complete nested archives. Four outer empty directory entries differ; whole-archive byte identity is not claimed. [Final server acceptance](../qa-four-mods/evidence/2026-10-13/final-1.4.4/server/verification.json) passed 198 backpack/crafting and ClientSort schema assertions each in native and Polymer profiles. The broader baseline/intermediate mechanics results above keep their original hashes.

The [final installation pack](installation-pack-verification.json) passed all 13 archive/installer checks on actual disposable client/server installs. Its SHA-256 is `64c904347d1ef05459d3f4359c40d3888d5f40c45f267c366dc98a05163ca985`. Side filtering, official Fzzy download, exact dependency/source hashes, file preservation, corruption/path rejection, idempotency and byte-identical rebuild passed. Launcher GUI import was not separately tested.

## Buildprint API review

All ten linked official documentation pages were read and compared with signatures from the exact original 0.1.19 JAR. The [developer overview](https://voxelsprout.com/buildprint/wiki/api) lists nine API areas; Editor and Format contain several interfaces. It is not a list of twelve independent requirements.

Hotkey Wheel uses the public editor lifecycle, listener/screen-recognition and HUD interfaces for its current controls. Blocktone uses the applicable editor, selection/capture, placement/snapshot/live-verdict, preview and rules interfaces. Their detailed audits distinguish necessary integration from optional new features. Blocktone retains focused bridges where the public APIs cannot preserve its current named-region export/persistence transaction, large sparse capture, visible slices or independent mining attempt cap. Library/Format calls are not equivalent replacements for those transactions.

Vanilla++ and BRBR do not operate on Buildprint blueprints or register Buildprint editor tools. They need coexistence checks, rather than a new dependency on those APIs. Buildprint and Blocktone remain separate optional client installs and are not nested suite modules or installer dependencies.

## Combined acceptance and publication

[Final combined Hotkey Wheel acceptance](../qa-four-mods/evidence/2026-10-13/final-1.4.4/wheel/BUILD-REPORT.json) passed **655 native + 655 supported-Polymer assertions** against the exact final suite. Wheel activation/selection, registered unbound/conflicting controls, original-opener closure of owned editor/child sessions, native save/cancel/validation, suite settings/menu navigation and BRBR coexistence passed. Each profile includes 27 BRBR shared-key pin/configuration Save/Cancel assertions. The unchanged-wheel minimal 232 and Buildprint/Blocktone integrated 445 checks remain valid separate profiles, for 1,987 current matrix assertions. All exact installed hashes are recorded in per-profile provenance.

[Final packaged BRBR acceptance](../qa-four-mods/evidence/2026-10-13/final-1.4.4/brbr/full-client/clientqa-result.txt) passed **1,968 recorded checks**, exit 0, with all four exact installable mods together. Coverage includes recipe/creative grouping, pins, both crafting methods, 2x2/3x3 server correction, overflow/remainders, workstation persistence, lifecycle and all 15 settings at three sizes. Correction tests prove crafting is pending immediately before the authoritative correction, then verify cancellation and client/server grid/inventory/cursor conservation afterward.

All four connected backpack profiles passed: native and supported Polymer 0.18.2 servers, each with SINGLE_BATCHES and FILL_GRID. Across the four profiles, 32 repeated tier/grid cases and 32 prepared-cake remainder/overflow policy cases passed. Each maximum case crafts/stores 68 tables from 272 ingredients; each fill-grid maximum uses two real result QUICK_MOVE packets and no PICKUP packets. Both fill-grid profiles additionally exercise the suite acknowledgement guard with 48 delayed-grid predicate checks each. All runner processes exited 0. [Detailed connected receipts](../qa-four-mods/evidence/2026-10-13/final-1.4.4/brbr/connected-summary.json) and the [final verification record](../qa-four-mods/evidence/2026-10-13/final-1.4.4/verification.json) identify the exact inputs and outcomes.

Fresh BRBR source review and its complete Java 25 independent build reproduce the unchanged main/sources JARs byte-for-byte. Older synthetic fixtures required Minecraft namespace scoping for vanilla anvil variants and the `minecraft:` recipe-search dataset. Those fixture assumptions were corrected without changing production behavior; unrecorded original recipe IDs are not presented as a verified failure cause. Failed environment/fixture attempts remain separate from accepted final runtime results.

Vanilla++ advances to 1.4.4 for the verified HUD correction. The unchanged wheel, Blocktone and BRBR versions receive fresh QA/documentation records. Existing binary releases and their original source tags remain intact. Final main-branch commits, release documentation and separately named review evidence identify the new verification. Blocktone's reviewed release stays local at the user's request.

Historical acceptance and its limitations remain in the [1.4.3 guide](RELEASE_1_4_3.md). In particular, direct Sorted recovery-selector checks are not a complete interrupted-sort timeline, and ClientSort's client fill/transfer UI remains outside those earlier checks.
