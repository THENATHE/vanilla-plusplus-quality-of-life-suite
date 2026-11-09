# Vanilla++ 1.5.2+26.3 - Tool Pouch slot toggles for suite items

Tool Pouch 1.2 lets players bind a key to any pouch slot and switch that item's pouch functions off and on. 1.5.2 retires the suite's older, Elytra-only toggle and makes every suite feature that works from a pouch slot follow Tool Pouch's switch. Targets, libraries and original inputs are unchanged from [1.5.1](RELEASE_1_5_1.md).

## Changes

- **Elytra toggle retired.** The Tool Pouch Atlas and Elytra addon (now **1.2.0+26.3**) no longer registers *Toggle Tool Pouch Elytra* (`key.toolpouch.toggle_elytra`), its two network payloads or the per-player preference. The pouch Elytra is switched with Tool Pouch's own *Bind slot toggle to key*, which stores `toolpouch:disabled_slots` on the pouch.
- **Suite items follow Tool Pouch's switch.** With a slot switched off, the pouch atlas no longer drives the minimap, world map or atlas ticking; the Elytra no longer gives flight and is not worn, mended or SSO-repaired; the Pouch Magnet and Pouch Collector stop pulling items; ClientSort leaves the slot alone. Switching the slot back on restores each one. Tool Pouch shows its own "… disabled" / "… enabled" message. Toolboxes (backpack items), the Debug Wrench and Swift Flight (a harness enchantment) do not act from pouch slots.
- **Earlier choices carried over.**
  - Up to 1.5.1 the off state was saved in player data as `ToolPouchElytraEnabled = false`. It now disables the Elytra slots of the player's active, worn and inventory pouches the first time one of them holds an Elytra, and stays in player data until then, so nobody starts gliding unexpectedly.
  - A key bound to the old control in `options.txt` becomes the slot-toggle bind for each Elytra slot in the pouch (never replacing an existing bind). It is remembered in `config/vpp_mod_suite/legacy-elytra-key.txt` until that happens, because Minecraft drops the unknown option on its next save. Tool Pouch binds keys to slot positions, so the bind follows the slot, not the item, if the Elytra is later moved.
- **Commands and vanilla clients.** `/toolpouch-elytra`, `/toolpouch-elytra on` and `/toolpouch-elytra off` remain and switch the Elytra slots of the player's active pouch. On Polymer servers, players without the client mod still get no pouch flight (unchanged since Tool Pouch's passive effects need the client mod), while the Pouch Magnet and Collector keep working for them; the pouch they see now names any switched-off items ("Turned off: …"). `toolpouch:disabled_slots` itself stays server-side. There is deliberately no command for switching other slots from a vanilla client (user's decision, 2026-11-08).
- **Behaviour change.** An Elytra whose slot is switched off is no longer repaired by pouch XP Mending or SSO auto-repair until it is switched back on (previously the flight toggle left repair running). [1.5.3](RELEASE_1_5_3.md) reverses this at the user's decision: repair continues while the slot is switched off.
- **Mixed versions.** The removed payloads were only sent when the other side advertised them, so 1.5.1 and 1.5.2 clients and servers still connect; capability negotiation treats the changed addon as a version mismatch and falls back where needed.

Components: suite **1.5.2+26.3**, Tool Pouch Atlas and Elytra **1.2.0+26.3**, combined compatibility **1.5.2+26.3**, Backpack Crafting **1.1.2+26.3** and mixed-scale MapStitch **1.2.2+26.3** (both metadata-only, for their exact coordinator dependency). Other nested modules are byte-identical to 1.5.1.

## Installation

Replace the previous suite JAR with `vanilla-plusplus-quality-of-life-suite-1.5.2+26.3.jar` on the server and modded clients, or use `vanilla-plusplus-installation-pack-1.5.2+26.3.zip` ([instructions](INSTALLATION_PACK.md)). Source: branch `feature/pouch-slot-toggle-binds` of [the suite repository](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite), pending review. Local release files: `Builds/Minecraft/Vanilla++ Quality of Life Suite/Main Plugin/1.5.2+26.3/`.

## Final acceptance

Final suite SHA-256: `490cd993ee52d8bdaa868554c257d73dc9989b3470f4b0a583c979332b162d65`. All checks below ran on that exact JAR on a private Xvfb display; records are in [verification.json](../qa-release/evidence/1.5.2/verification.json).

| Fresh check | Result |
| --- | --- |
| Slot-toggle key presses: Elytra (old key carried over to J), atlas, magnet and collector each switched off and on; server state, server and client behaviour, Tool Pouch's own message | 43 checks pass in the native and the Polymer profile |
| Hotkey Wheel 1.0.10 slot still naming `key.toolpouch.toggle_elytra` | wheel opens, choosing the slot reports "control is unavailable", no crash, Elytra slot unchanged |
| Hotkey Wheel 1.0.10, Buildprint 0.1.38, Blocktone 0.1.38, BRBR 1.11.7, Sorted 0.1.41, atlas cache | 825 native + 825 Polymer checks pass |
| Connections: native suite, Fabric-only, unmodified vanilla, Polymer | 4 profiles pass |
| Unified settings with Hotkey Wheel 1.0.10 | pass |
| Native features | native and Polymer, initial and restart |
| Pouch XP and SSO Mending with the Elytra slot switched off | 62 native + 65 Polymer checks; a switched-off Elytra keeps its damage and the XP |
| Sorted 0.1.41 keep-rule markers and Alt edits | 146 assertions |
| Sorted storage controls and cursor recovery | 487 assertions on 0.1.41 and 487 on 0.1.26 |
| MapStitch pin guard | native and Polymer pass |
| Upgrade from 1.4.5 | 23 stacks, 10 chalk marks, pet index identical; 337 config values, only the 12 accepted MapStitch/Polymer migrations change; Elytra-off player gets a disabled Elytra slot, Elytra-on player keeps flight, old flag no longer saved |
| Upgrade from 1.5.0 and 1.5.1 | 24 stacks incl. a pouch with switched-off slots, identical world data; 345 config values unchanged; same Elytra player results |
| Restart on 1.5.2 | identical world and config; switched-off slots persist on the pouch |
| Installation ZIP | 13 checks, byte-identical rebuild |

HUD, Stackables, packet-codec, mechanics, mixed-scale, pickup and anvil checks were not rerun: those modules are byte-identical to 1.5.1 (Backpack Crafting and mixed-scale MapStitch change only their metadata version), so their 1.5.0 results stand. The slot-toggle client test runs in an integrated (singleplayer) world, so the Polymer profile there means Polymer installed with a modded client; vanilla clients on a Polymer server are covered by the connection profiles and the Mending passive guard. Persistence across a relog or restart is shown by the restart harness and the pouch component, not by a client relog inside the key-press test. The paused SSO port was neither built nor tested.
