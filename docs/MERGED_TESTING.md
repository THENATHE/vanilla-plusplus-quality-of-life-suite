# Merged testing branch

Version **1.0.2-merged.1+26.3**, branch `merged`. This combines the two independently published experiments without merging features into main.

Separate modules remain under `components/mapstitch-mixed-scales/working/` and `components/sensible-stackables/{ported,compat}/`. The root includes 16 nested modules. Coordinator version is 1.0.7-merged.1+26.3 and retains Stackables protocol v2 plus addon-aware MapStitch fingerprints. Original MapStitch and all original suite inputs remain unchanged. Required Defaulted is exactly 1.3.8+26.3.dropfix.1. The paused SSO-port track is unchanged.

Native clients must install this matching suite and the documented dependencies. Install Polymer 0.18.2+26.3 on servers accepting vanilla clients. Uncapped Stackables keeps full quantities; fallback maximum metadata is capped at 99, with authoritative menu correction. Configure restart-required uncapping before launching both server and native clients. Vanilla atlas screens remain unavailable as documented by the suite.

## Completed checks — 2026-10-05

Final JAR SHA-256: `b04ca184a6b8e82700a2b0df96f1fcdbd06e485cc778a6a0fad5666d571bbe9b`.

- Archive/build validation: 16 nested modules, unchanged original inputs and exact Defaulted dropfix. Both experiments are required by root metadata and verified loaded at runtime.
- [Four connection profiles](../qa-merged/evidence/connections/result.json): matching suite with and without server Polymer, Fabric API-only fallback, and genuine zero-mod vanilla. All passed. Vanilla connected and loaded the pack; its uninstrumented profile does not inspect item metadata or automate inventory clicks.
- [Client Polymer](../qa-merged/evidence/client-polymer/result.json): matching suite retained all 11 native capabilities. Native clients in all three tested configurations exercised the actual atlas scale controls across all five scales and the duplicate inventory/pouch selection regression.
- Native and Fabric-only clients picked up and placed all 2,048 stone through real inventory packets with no loss. Native maximum stayed 2,048; fallback maximum metadata was 99 with full counts. Potion count/maximum stayed 3.
- [Map mechanics and restarts](../qa-merged/evidence/maps/result.json): 118 assertions in four Polymer/native initial/restart cases, including insertion, extraction, active scale, codecs and persistence.
- [Stackables mechanics and restarts](../qa-merged/evidence/stackables/result.json): 131 assertions in six default/uncapped Polymer/native initial/restart cases, including server menus and count persistence. These dedicated fixtures use server-side test players, separately from the real-client click tests.
- [Settings](../qa-merged/evidence/settings/observations.json): all 13 configuration screens opened with native widgets. This focused standalone check does not repeat the historical multiplayer permission matrix.

An earlier candidate silently omitted the map addon because its old coordinator dependency no longer matched. That candidate failed map acceptance and is not released. The corrected map addon `1.0.1-merged.1+26.3` requires coordinator `1.0.7-merged.1+26.3`; the root requires both new features, and runtime tests check their presence. Historical individual-branch evidence is retained separately.

- [Installation packs](installation-pack-verification.json): 13 checks passed, including actual official-download client/server installs, all hashes, environment filtering, corrupt-file rejection and reproducible archives. A launcher GUI import was not tested.

## Your test checklist

For your testing, back up your world and configuration, replace your previous suite JAR rather than adding a second copy, and install the exact dependency versions. Try all five scales in one atlas, world-map controls and pouch selection, then repeat inventory moves with potions/enchanted books and your configured larger stacks. Test matching native, Fabric without the suite, and vanilla clients; accept the server Polymer resource pack for Chalk visuals. Restart and confirm atlas contents, active scale and item quantities persist. Try your real inventory sorting/accessory integrations separately.
