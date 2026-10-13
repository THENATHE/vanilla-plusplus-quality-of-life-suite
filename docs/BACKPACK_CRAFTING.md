# Backpack Crafting

Main suite: `1.4.0+26.3`. Addon: `vpp_backpack_crafting` `1.0.0+26.3`. The existing authored Tool Pouch atlas/Elytra addon advances to `1.0.10+26.3` for its ClientSort integration. Combined compatibility advances to `1.4.0+26.3` for the capability fingerprint update; the mixed-scale addon becomes `1.1.9+26.3` to retain its exact coordinator dependency.

## Using it

Put an Iron Toolbox in your active Tool Pouch to add a 2×2 crafting grid and recipe book when opening a Tiered Backpack. Crafting uses a compact floating panel beside the backpack, with its own border and a gap; the 3×3 version grows only enough to fit its grid. A Netherite Toolbox enables 3×3 recipes and takes priority if both toolboxes are present. Remove the toolbox to return to the original backpack screen. Crafting ingredients left in the grid return to your inventory on close, with ordinary dropped overflow if no space remains.

Recipe placement uses ingredients from **both player inventory and backpack storage**. Crafted shift-click output goes to player inventory first, then backpack space. The open backpack and pouch stay locked while crafting. The collector pauses insertion while this screen is open, matching the original backpack screen policy.

Iron toolbox recipe, seven iron ingots, an iron chain and a crafting table:

```text
ICI
ITI
III
```

`I` = iron ingot, `C` = iron chain, `T` = crafting table.

Netherite upgrade in a smithing table: **Netherite Upgrade template + Iron Toolbox + netherite ingot → Netherite Toolbox**. The four-diamond crafting upgrade is removed.

Toolbox permissions are added once to Tool Pouch's allowed-item settings. Later administrator changes are preserved. The addon has no separate configuration screen: existing Tool Pouch rules and Tiered Backpacks settings remain authoritative. It appears as a feature beneath the suite in Mod Menu, with a link to the suite configuration hub. Existing ClientSort integration recognizes the enhanced screen and excludes crafting inputs/output from sort and transfer operations.

## Compatibility and provenance

Requires the same Minecraft 26.3/Fabric 0.19.5/Java 25 installation and exact libraries as the suite. Original developer inputs remain Tiered Backpacks 1.0.20 and Tool Pouch 1.1.10, byte-for-byte unchanged. Thanks to **pajic** for [Tiered Backpacks](https://modrinth.com/mod/tiered-backpacks) and [Tool Pouch](https://modrinth.com/mod/tool-pouch). This separate addon is authored by THENATHE.

Polymer remains optional for matching suite clients. Vanilla clients with server-side Polymer can craft, hold and see the toolboxes, but cannot open the backpack. Matching suite capability fingerprints gate the new native menu; the existing v3 handshake framing is unchanged. Sprites are regenerated iron and netherite toolboxes, fixed 16×16 PNGs; resource-pack detection is not used. Final item and recipe selectors are `vpp_backpack_crafting:iron_toolbox` and `vpp_backpack_crafting:netherite_toolbox`. Testing hammer selectors have been removed without conversion or aliases, as requested.

Optional [Better Recipe Book Rebetterered](https://github.com/THENATHE/better-recipe-book-rebetterered) integration lives entirely in addon mixins. Neither its local working tree nor its published source/JAR is modified. Native recipe-book search, grouping and settings use BRBR's existing components. Small optional hooks admit the backpack menu and include its storage in BRBR's placement and instant-crafting calculations. The addon waits for consistent grid/result updates before consuming BRBR’s result acknowledgment, retaining its original recipe validation, timeout and user cancellation.

Supported test baselines are GitHub main/release 1.11.1+26.3, commit `8485b2f8c19acf58f51f97d964d7d1e871f2b1e3`, and the local 1.11.2+26.3 candidate. Exact hashes and runtime results are recorded in the verification evidence below. The separate SSO port remains paused; the normal developer-release suite track is used.

## Current 1.4.0 verification

See [the release guide](RELEASE_1_4_0.md) for the final artifact and current runtime results.

## Beta 2 verification (historical)

Candidate SHA-256: `7e734d89c51a35e4203a39ab978f16d9db9ec2bb58a9949ef05dd6b204fe787c`.

- Build, original-input checks and 19-module archive verification passed. Both regenerated sprites are exactly 16×16, contain at most 12 colors and use binary alpha. [Artifact record](../qa-backpack-crafting/evidence/toolbox-beta2/artifact.json).
- Native and Polymer server profiles each passed **187 checks** on the final candidate, including the restored vanilla iron pickaxe, unique iron toolbox recipe, removal of the diamond crafting upgrade, actual RecipeManager smithing selection, ordinary/shift netherite output, exact ingredient consumption and ClientSort protection. Each smithing action consumes one iron toolbox, netherite ingot and template; template behavior matches the vanilla netherite-pickaxe control under the same SSO settings. [Evidence](../qa-backpack-crafting/evidence/toolbox-beta2/netherite-server-clientsort/verification.json).
- Recipe/menu screens, BRBR hooks, networking and ClientSort class bytes remain unchanged from beta 1; [comparison](../qa-backpack-crafting/evidence/toolbox-beta2/class-comparison.json) identifies the entrypoint/name and Polymer fallback class changes. Historical BRBR results retain their original hash and scope.
- Hotkey Wheel **1.0.6+26.3** passed native and Polymer profiles on this candidate, with 62 assertions plus one context observation each. [Evidence](../qa-backpack-crafting/evidence/toolbox-beta2/wheel/verification.json).
- All four connection profiles passed on this candidate: matching native clients with/without Polymer, Fabric-only and unmodified vanilla clients. Saved native item IDs, the netherite fallback and unsupported backpack-opening guards were verified. [Evidence](../qa-backpack-crafting/evidence/toolbox-beta2/connections/README.md).

## Beta 1 verification (historical)

Candidate SHA-256: `23459d1ba1ffcb87790475e58a8c195f838d879360cbf17bd0295eded45cb40b`.

- Compilation, archive verification and the 19-module bundle check passed. Original developer inputs remain unchanged.
- Native and Polymer server runs each passed **126 checks** on the preceding `0310791d` candidate, including both hammer recipes, tier priority, both grid sizes, combined-inventory placement, remainders, close/invalidation, capacity/overflow, collector interaction and real ClientSort schema/sort execution. [Machine-readable evidence](../qa-backpack-crafting/evidence/clientsort-server/verification.json). The final candidate changes only three optional BRBR client entries; a [recursive byte comparison](../qa-backpack-crafting/evidence/clientsort-server/client-guard-byte-comparison.json) confirms identical server gameplay and ClientSort classes.
- Hotkey Wheel **1.0.6+26.3** passed native and Polymer profiles on the final candidate: activation/selection, overlapping keybindings, suite menus and unified settings. Each profile recorded 62 passing assertions plus one context observation. [Evidence](../qa-backpack-crafting/evidence/wheel/verification.json).
- Existing suite mechanics passed native and Polymer initial/restart regression runs on `aa29bbb1`; the [scope and byte comparison](../qa-backpack-crafting/evidence/suite-regression/verification.json) identify the unchanged server classes used by this candidate.
- All four multiplayer connection profiles and both integrated singleplayer profiles passed on the final candidate, covering native clients with/without Polymer, Fabric-only and unmodified vanilla clients, native/fallback hammer representations, and guarded unsupported backpack opening. [Evidence and limits](../qa-backpack-crafting/evidence/connections/README.md).
- Published BRBR **1.11.1+26.3** and local **1.11.2+26.3** each passed eight connected GUI cases on the final candidate: both hammer tiers, leather/netherite backpacks, ordinary and maximum instant crafting, backpack ingredients and server-confirmed conservation. Four additional cases deliberately withheld the grid-match predicate for three ticks; all waited and completed correctly. [Final BRBR verification](../qa-backpack-crafting/evidence/gui/final-verification.json). Existing timeout and user-cancellation checks remain in code but were not separately forced in these runtime tests.
- The standard recipe book passed four native and four Polymer GUI cases on the compact-panel `aa29bbb1` candidate, including recipe filtering, ghost recipes, click handling and viewport/tab bounds. Later changes were confined to version metadata and optional BRBR integration.
- ClientSort server operations are tested; its client buttons and accelerated network packets are not independently covered by the server fixture.

Earlier beta records above retain their original testing IDs and artifact hashes; the current main build uses the final toolbox selectors.
