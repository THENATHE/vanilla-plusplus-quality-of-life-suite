# Release 1.2.1

This local update enlarges the Backpack Collector basket and shows toggle guidance suited to the connected client. Its fixed 16×16 texture now contains 14×14 visible artwork. Matching suite clients see “Toggle with your backpack pickup hotkey.” Vanilla and unsupported clients see “Toggle with /suite-pickup.” Fabric alone does not prove that the collector client is installed.

The existing pickup hotkey and `/suite-pickup [on|off]` controls remain available. The basket tooltip chooses one instruction from the existing connection capability decision. Native clients retain their original item and tooltip; Polymer changes only the fallback guidance and preserves other tooltip lines. Native play without Polymer and singleplayer remain supported.

## Identifiers and scope

The authored root mod ID and item/protocol/config namespace are now `vpp_mod_suite`. Pickup items use `vpp_mod_suite:pouch_magnet` and `vpp_mod_suite:pouch_collector`. No conversion or migration from `thenathe_mod_suite` is included, at the user's request for this testing setup. Original developer module IDs and configuration files remain unchanged. The integrated wrench retains `debugwrench:debug_wrench` and its saved selections.

The suite and combined compatibility component are **1.2.1+26.3**. The mixed-scale addon is **1.1.7+26.3**, with its exact coordinator dependency aligned to this update. Other component release versions and the exact external developer inputs are unchanged. The separate ChatGPT SSO-port track remains paused; the active suite retains official developer SSO 2.10.0.

The replacement basket was generated with the built-in imagegen tool and resized with nearest-neighbor sampling. Both generator outputs remain preserved under `artwork/generated/`; [artwork provenance](../artwork/README.md) identifies them.

## Installation

Use Minecraft **26.3**, Fabric Loader **0.19.5**, Java **25+**, and the same [locked external libraries](DEPENDENCIES.md) as 1.2.0. Replace the old suite with `vanilla-plusplus-quality-of-life-suite-1.2.1+26.3.jar` on the server and matching native clients. Keep separate original feature JARs, old shims and the standalone Polymer Debug Wrench out of the same instance.

For vanilla players, retain full Polymer Bundled **0.18.2+26.3** on the server. Regenerate and host the Polymer resource pack, then accept the updated pack to receive the larger basket artwork. Clients with the matching suite use their native item texture. Existing custom pouch/backpack screens still require their client features.

Local release files belong under `Builds/Minecraft/Vanilla++ Quality of Life Suite/Main Plugin/1.2.1+26.3/`: `README.md`, the suite JAR, `docs.zip`, and `vanilla-plusplus-installation-pack-1.2.1+26.3.zip`, with matching local checksums and release records. [Installer instructions](INSTALLATION_PACK.md) retain side selection and the official Fzzy Config download; fresh installation requires internet access.

This update is local only. No public upload or release is included.

## Verification

Compilation and archive verification pass for the 1.2.1 JAR with SHA-256 `f4a73c3a93c57f9d09e414da21dc79cd2e8eaa927aff73e58121705aa2a04e6a`. Fresh dedicated native/Polymer checks pass **1,344 pickup and 272 wrench assertions**, including the generated collector sprite's occupied bounds. [Dedicated evidence](../qa-pickup/evidence/1.2.1/dedicated/).

All **four physical connection profiles** pass: matching native suite, Fabric-only fallback, unmodified vanilla, and native suite against a server without Polymer. Native and Fabric fallback clients verify both received lore and full tooltips, real pickup, native hotkey or fallback command toggles, and rendered inventory screenshots. The unmodified vanilla profile uses an actual connection and server-side projected-lore evidence; it has no automated vanilla screenshot or client-side tooltip observer. [Physical evidence](../qa-pickup/evidence/1.2.1/physical/).

Both **singleplayer profiles**, with and without Polymer, pass the native item, tooltip and pickup checks. [Integrated evidence](../qa-pickup/evidence/1.2.1/integrated/). The [settings-only fixture](../qa-settings/evidence/1.2.1/) also passes with the renamed authored root identity.

All 13 final installation ZIP archive/installer checks pass, including actual disposable client/server installs using the cached exact official Fzzy file. These results apply to the exact new JAR; [validation](VALIDATION.md) and the [1.2.0 guide](RELEASE_1_2_0.md) preserve broader historical evidence under its original versions and hashes.
