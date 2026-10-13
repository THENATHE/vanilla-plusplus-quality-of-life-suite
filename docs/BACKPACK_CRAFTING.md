# Backpack Crafting experiment

Branch: `feat/backpack-crafting`. Suite candidate: `1.4.0-beta.1+26.3`. Addon: `vpp_backpack_crafting` `1.0.0-beta.1+26.3`. The existing authored Tool Pouch atlas/Elytra addon advances to `1.0.10-beta.1+26.3` for its ClientSort integration. Combined compatibility advances to `1.4.0-beta.1+26.3` for the capability fingerprint update; the mixed-scale addon becomes `1.1.9-beta.1+26.3` to retain its exact coordinator dependency.

## Using it

Put an Iron Crafting Hammer in your active Tool Pouch to add a 2×2 crafting grid and recipe book when opening a Tiered Backpack. Crafting uses a compact floating panel beside the backpack, with its own border and a gap; the 3×3 version grows only enough to fit its grid. A Diamond Crafting Hammer enables 3×3 recipes and takes priority if both hammers are present. Remove the hammer to return to the original backpack screen. Crafting ingredients left in the grid return to your inventory on close, with ordinary dropped overflow if no space remains.

Recipe placement uses ingredients from **both player inventory and backpack storage**. Crafted shift-click output goes to player inventory first, then backpack space. The open backpack and pouch stay locked while crafting. The collector pauses insertion while this screen is open, matching the original backpack screen policy.

Iron hammer recipe, three iron ingots and two sticks:

```text
III
 S 
 S 
```

Diamond upgrade, four diamonds around an iron hammer:

```text
 D 
DHD
 D 
```

Hammer permissions are added once to Tool Pouch's allowed-item settings. Later administrator changes are preserved. The addon has no separate configuration screen: existing Tool Pouch rules and Tiered Backpacks settings remain authoritative. It appears as a feature beneath the suite in Mod Menu, with a link to the suite configuration hub. Existing ClientSort integration recognizes the enhanced screen and excludes crafting inputs/output from sort and transfer operations.

## Compatibility and provenance

Requires the same Minecraft 26.3/Fabric 0.19.5/Java 25 installation and exact libraries as the suite. Original developer inputs remain Tiered Backpacks 1.0.20 and Tool Pouch 1.1.10, byte-for-byte unchanged. Thanks to **pajic** for [Tiered Backpacks](https://modrinth.com/mod/tiered-backpacks) and [Tool Pouch](https://modrinth.com/mod/tool-pouch). This separate addon is authored by THENATHE.

Polymer remains optional for matching suite clients. Vanilla clients with server-side Polymer can craft, hold and see the hammers, but cannot open the backpack. Matching suite capability fingerprints gate the new native menu; the existing v3 handshake framing is unchanged. Sprites are fixed 16×16 PNGs; resource-pack detection is not used.

Optional [Better Recipe Book Rebetterered](https://github.com/THENATHE/better-recipe-book-rebetterered) integration lives entirely in addon mixins. Neither its local working tree nor its published source/JAR is modified. Native recipe-book search, grouping and settings use BRBR's existing components. Small optional hooks admit the backpack menu and include its storage in BRBR's placement and instant-crafting calculations. The addon waits for consistent grid/result updates before consuming BRBR’s result acknowledgment, retaining its original recipe validation, timeout and user cancellation.

Supported test baselines are GitHub main/release 1.11.1+26.3, commit `8485b2f8c19acf58f51f97d964d7d1e871f2b1e3`, and the local 1.11.2+26.3 candidate. Exact hashes and runtime results are recorded in the verification evidence below. The separate SSO port remains paused; the normal developer-release suite track is used.

## Verification

Candidate SHA-256: `23459d1ba1ffcb87790475e58a8c195f838d879360cbf17bd0295eded45cb40b`.

- Compilation, archive verification and the 19-module bundle check passed. Original developer inputs remain unchanged.
- Native and Polymer server runs each passed **126 checks** on the preceding `0310791d` candidate, including both hammer recipes, tier priority, both grid sizes, combined-inventory placement, remainders, close/invalidation, capacity/overflow, collector interaction and real ClientSort schema/sort execution. [Machine-readable evidence](../qa-backpack-crafting/evidence/clientsort-server/verification.json). The final candidate changes only three optional BRBR client entries; a [recursive byte comparison](../qa-backpack-crafting/evidence/clientsort-server/client-guard-byte-comparison.json) confirms identical server gameplay and ClientSort classes.
- Hotkey Wheel **1.0.6+26.3** passed native and Polymer profiles on the final candidate: activation/selection, overlapping keybindings, suite menus and unified settings. Each profile recorded 62 passing assertions plus one context observation. [Evidence](../qa-backpack-crafting/evidence/wheel/verification.json).
- Existing suite mechanics passed native and Polymer initial/restart regression runs on `aa29bbb1`; the [scope and byte comparison](../qa-backpack-crafting/evidence/suite-regression/verification.json) identify the unchanged server classes used by this candidate.
- All four multiplayer connection profiles and both integrated singleplayer profiles passed on the final candidate, covering native clients with/without Polymer, Fabric-only and unmodified vanilla clients, native/fallback hammer representations, and guarded unsupported backpack opening. [Evidence and limits](../qa-backpack-crafting/evidence/connections/README.md).
- Published BRBR **1.11.1+26.3** and local **1.11.2+26.3** each passed eight connected GUI cases on the final candidate: both hammer tiers, leather/netherite backpacks, ordinary and maximum instant crafting, backpack ingredients and server-confirmed conservation. Four additional cases deliberately withheld the grid-match predicate for three ticks; all waited and completed correctly. [Final BRBR verification](../qa-backpack-crafting/evidence/gui/final-verification.json). Existing timeout and user-cancellation checks remain in code but were not separately forced in these runtime tests.
- The standard recipe book passed four native and four Polymer GUI cases on the compact-panel `aa29bbb1` candidate, including recipe filtering, ghost recipes, click handling and viewport/tab bounds. Later changes were confined to version metadata and optional BRBR integration.
- ClientSort server operations are tested; its client buttons and accelerated network packets are not independently covered by the server fixture.

This branch is an experiment. It does not replace stable main or publish a stable release.
