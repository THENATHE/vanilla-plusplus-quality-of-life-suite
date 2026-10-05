# Merged testing branch

Current version **1.0.2-merged.2+26.3**, branch `merged`. This testing build combines mixed-scale MapStitch and Sensible Stackables while keeping their implementations in separate modules. The final artifact hash and exact fresh verification scope are recorded in [build verification](build-verification.json) and [validation](VALIDATION.md). The merged.1 results retained below belong to that older artifact.

## What changed in merged.2

- **Independent atlas controls:** S changes the world-map viewing layer, M cycles the minimap layer, and 1 / 2 / 4 / 8 / 16 independently enable generation. Multiple generation layers may be enabled; all may be disabled. Tooltips show the selected minimap and generation scales. See [atlas controls](ATLAS_CONTROLS.md).
- **Book ownership:** a persisted book identity keeps an open screen attached to its original inventory/accessory/pouch source. Generation and map ejection may refresh that same book's anchor; replacing the slot with another book disables its controls. Selection requests require matching native negotiation and the book identity.
- **Exploration:** enabled layers continue updating independently, including inventory-held atlases. Creation plays one normal cartography chime to the explorer per batch. Existing maps and shared region/scale IDs remain intact.
- **Mod Menu and settings:** original Pajic mods and Chalk remain individually visible; suite components nest under Vanilla++ Quality of Life Suite, with Colorful Chalk hidden and the coordinator named Vanilla / Polymer Shim. The shared settings page contains functional configurations with consistent names. The optional Open Suite Settings control starts unbound and can be assigned in Controls. See [presentation](MOD_MENU_PRESENTATION.md) and [settings](settings.md).
- **Other atlas integration fixes:** expanded nested-container keep-atlas-on-death handling and synchronized map/detail HUD placement are described in the [changelog](../CHANGELOG.md) and [HUD notes](hud-layout-fix.md).

The root still includes 16 nested modules. The MapStitch addon is **1.0.2-merged.2+26.3**, coordinator **1.0.8-merged.2+26.3**; original MapStitch and original suite inputs are preserved. Defaulted must remain exactly **1.3.8+26.3.dropfix.1**. The paused SSO-port track is preserved. Sensible Stackables' full-count safe fallback behavior is unchanged: native clients retain configured maxima, fallback metadata is capped at 99, and completed inventory gestures receive authoritative server correction.

## Focused verification and your testing

The current pass concentrates on changed atlas controls, ownership, settings/presentation, nested retention, HUD behavior, and a native/Fabric fallback regression. It does not repeat the full prior Stackables matrix or every original mod feature. See [QA commands and evidence](../qa-merged/README.md); final verification distinguishes the tested mechanics candidate from any later presentation-only rebuild rather than treating their JAR hashes as identical.

For your testing, back up your world/configuration, replace the old suite JAR, and restart both server and native client with matching merged.2 builds and documented dependencies. Keep Polymer **0.18.2+26.3** on a server accepting fallback clients.

1. Put existing maps at several scales and several blanks in one atlas. Open its world-map screen. Change S and confirm only the world-map layer changes.
2. Cycle M1 through M16. Verify the minimap and atlas tooltip reflect M while generation choices stay unchanged.
3. Enable 1, 4, and 16; travel into a region without those maps. Confirm up to one blank is consumed per missing enabled scale, all three layers fill, and the atlas tooltip/hover text list 1:1, 1:4, and 1:16. One batch should play one chime when sound is enabled.
4. Turn every generation toggle off. Enter another new region: no blank should be consumed. Existing layers remain stored, and a missing minimap layer stays empty. Re-enable the desired scales.
5. Repeat with the atlas in the pouch and with another atlas also in inventory. Confirm only the opened book's choices change. Close/reopen after moving books to a new source; restart and verify each book's choices persist.
6. Inspect Mods and the suite configuration sidebar. Assign Open Suite Settings in Controls and use it in gameplay. Change Chalk particles through Chalk's config button and confirm its original saved setting persists.
7. Check map/detail HUD corners, atlas retention on death with your ordinary nested containers, and your real sorting/accessory integrations. Those integrations may expose arrangements beyond the automated fixtures.
8. If using uncapped Stackables, try your configured large stacks through ordinary inventory moves on matching native and Fabric fallback clients. Vanilla connection coverage and uninstrumented gameplay limits remain stated in the validation record.

## Completed focused checks — 2026-10-05

Final JAR SHA-256: `9ca677eee6d1c82c117b523e866e244e9bbba1e45cacaafd93ddce0dac76a08f`.

- [Native/Fabric connections and actual controls](../qa-merged/evidence/merged2/connections/result.json) passed, including all five minimap scales, independent S/generation toggles and in-world settings hotkey. Both clients conserved full 2,048-item inventory moves.
- [Map mechanics and restarts](../qa-merged/evidence/merged2/maps/result.json) passed 158 assertions across Polymer/native initial/restart profiles.
- [Final settings](../qa-merged/evidence/merged2/settings/observations.json) opened all 12 functional configs, checked original titles, Mod Menu grouping/hidden Colorful/native Chalk factory, hotkey registration, required settings pack and all 696 original translation entries.
- [Final HUD](../qa-merged/evidence/merged2/hud/observations.json) passed eight actual rendered scenes and synchronized native-file checks. [Screenshots](../qa-merged/evidence/merged2/hud/screenshots/) show both minimap sources with identical anchors and nonoverlapping details.
- [One-off death retention](../qa-death-once/evidence/result.json) passed 25 cases with and without Polymer, 254 assertions total. Optional external accessory slots remain a manual check.

The network/mechanics/death candidate digest is preserved in its own records. [Exact nested-byte comparison](../qa-merged/evidence/merged2/candidate-to-final-equivalence.json) proves all 16 modules unchanged in the final build after the root client title/factory corrections; final settings/HUD were rerun directly. Historical vanilla/client-Polymer/full Stackables tests below were not repeated. Installation pack validation is recorded separately in [its report](installation-pack-verification.json).

# Historical merged.1 verification

Version **1.0.2-merged.1+26.3**, branch `merged`. This combines the two independently published experiments without merging features into main.

Separate modules remain under `components/mapstitch-mixed-scales/working/` and `components/sensible-stackables/{ported,compat}/`. The root includes 16 nested modules. Coordinator version is 1.0.7-merged.1+26.3 and retains Stackables protocol v2 plus addon-aware MapStitch fingerprints. Original MapStitch and all original suite inputs remain unchanged. Required Defaulted is exactly 1.3.8+26.3.dropfix.1. The paused SSO-port track is unchanged.

Native clients must install this matching suite and the documented dependencies. Install Polymer 0.18.2+26.3 on servers accepting vanilla clients. Uncapped Stackables keeps full quantities; fallback maximum metadata is capped at 99, with authoritative menu correction. Configure restart-required uncapping before launching both server and native clients. Vanilla atlas screens remain unavailable as documented by the suite.

### Completed checks — 2026-10-05

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

### Historical test checklist

For your testing, back up your world and configuration, replace your previous suite JAR rather than adding a second copy, and install the exact dependency versions. Try all five scales in one atlas, world-map controls and pouch selection, then repeat inventory moves with potions/enchanted books and your configured larger stacks. Test matching native, Fabric without the suite, and vanilla clients; accept the server Polymer resource pack for Chalk visuals. Restart and confirm atlas contents, active scale and item quantities persist. Try your real inventory sorting/accessory integrations separately.
