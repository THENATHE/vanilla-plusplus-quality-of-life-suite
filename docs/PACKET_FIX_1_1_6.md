# Stored-item packet fix — release 1.1.6

This release fixes a reproduced disconnect when a Tool Pouch contains a shulker with items stacked above their unchanged vanilla defaults. The arrangement is **pouch → shulker → stored items**; it does not require shulker boxes inside other shulker boxes. The stable release promotes the exact tested local artifact without rebuilding it.

## What failed

The supplied log reports `clientbound/minecraft:container_set_content`, followed by `ItemStackTemplate.fromNonEmptyStack` and `IllegalStateException: Stack must be non-empty` inside Polymer's template codec. Sparse empty slots are represented by absent optional templates and do not cause this failure.

Official Sensible Stackables 3.1.1 moved limits into its own override table. Live item stacks consult that table, but immutable stored-item templates did not. A valid potion stack of three, saddle stack of sixteen or enchanted-book stack of sixty-four therefore looked oversized when vanilla strictly validated its containing shulker. Materializing that shulker template returned an empty stack; Polymer then tried to encode it as non-empty.

Frozen 1.1.5 reproduces the same exception with a netherite Tool Pouch containing such a shulker, including native-suite, fallback and partial-native contexts. The no-Polymer fixture independently demonstrates invalid template materialization. This establishes a matching defect and mechanism; the user's remote world and exact shulker contents were not available for replay.

## Changes

- The separate Stackables compatibility module now makes templates consult the same effective override table as live stacks on both client and server. Explicit component additions/removals retain priority, live changes remain visible, and invalid damageable stacks remain invalid.
- Existing empty-stack guards remain intact. Original developer JARs, saved items, quantities, names and component patches are unchanged.
- Matching native clients retain complete nested previews and configured limits.
- Vanilla/Fabric clients without Stackables support receive safe prediction maxima of at most 99 while retaining full top-level quantities. If a preview component contains a descendant stack above 99, only that outgoing preview component is omitted: vanilla cannot materialize those templates even with safe maximum metadata. Actual server contents, item counts and Polymer recovery data remain intact. Safe smaller previews remain available.
- Final Polymer projection also covers clients that support the carrier mod, such as Tool Pouch, but lack Stackables. It applies only to server connections, and optional Polymer is not required by the common template fix.

This is a suite integration fix in `components/sensible-stackables/compat/`, not a replacement original Stackables JAR. [Source/update map](UPDATING.md) identifies the new mixins/helper. [Artifact comparison](packet-fix-previous-build-comparison-1.1.6.json) verifies **17 unchanged nested modules** and identical root Java classes. The separately paused SSO port remains untouched.

## Installation

| Item | Exact target |
| --- | --- |
| Suite release | `1.1.6+26.3` |
| Minecraft / Fabric Loader / Java | `26.3` / `0.19.5` / `25+` |
| Stackables compatibility | `1.0.2+26.3`, already nested |
| Original Stackables | Unchanged official `3.1.1`, already nested |
| External dependencies | Identical to 1.1.5; see [dependencies](DEPENDENCIES.md) |
| Polymer for fallback clients | Full Polymer Bundled `0.18.2+26.3` on the server |

Stable release files belong under `Builds/Minecraft/Vanilla++ Quality of Life Suite/Main Plugin/1.1.6+26.3/`, relative to the workspace root. The standard four assets are the suite JAR, `README.md`, `docs.zip` and `vanilla-plusplus-installation-pack-1.1.6+26.3.zip`, accompanied locally by checksums and publication records. The earlier candidate folder remains a separate prepublication record. See the [release guide](RELEASE_1_1_6.md).

Back up the server world and keep the previous suite JAR separately. Stop the server/client, replace the old suite JAR with this one, and use matching files on native clients. Keep exactly one suite JAR per instance. No settings reset, inventory clearing, resource-pack rebuild or dependency replacement is needed for this fix. The [1.1.6 installation pack](INSTALLATION_PACK.md) supplies this suite JAR and the unchanged external dependency selection for a fresh setup. To roll back, stop the instance and restore its previous suite JAR. This fix adds no new saved-data format.

## Verification

The exact final artifact SHA-256 is `ea383c52581970f5b69eb0db0cffdc9c26a0a0efb2f80e0ab82f8b8f9d2a5f86`. Its Java 27 compiler targets Java 25, with Java 25 runtime QA. The clean offline Gradle build and 18-module archive/input/license verifier passed. Runtime results and exact scope are recorded in the [validation report](VALIDATION.md); preliminary candidate evidence remains separately labeled and is not relabeled as final-artifact acceptance.

Focused coverage includes real nested container packet codecs, native/fallback/partial-native recipients, effective template limits, explicit additions/removals, live override clearing, valid sparse slots, nested shulkers/pouches/attached storage, bundles, projectile/remainder templates, safe and uncapped counts, strict rejection of invalid damageable templates, wire/source identity and quantity conservation. Physical native-suite and Fabric fallback clients receive a netherite pouch containing a shulker of three potions; high-count inventory moves and connection negotiation are checked separately.

The inaccessible full remote mod stack and its world are not tested. A fresh pure-vanilla GUI, every nested tooltip mod and an exhaustive unrelated gameplay round are not claimed; broad 1.1.5 acceptance remains historical for its exact artifact.

## Server check

1. Rejoin with the matching build and the original affected pouch still in inventory. Open/close its pouch and stored shulker screens; check that the contents and quantities are unchanged and that the packet exception stops.
2. Test potion stacks of three and any other configured stack sizes in that shulker, including the netherite pouch attached to leggings.
3. If uncapping is enabled, verify matching clients retain complete previews and limits. Clients without Stackables support retain full items/counts through supported menus, but an unsafe above-99 nested preview is hidden.
