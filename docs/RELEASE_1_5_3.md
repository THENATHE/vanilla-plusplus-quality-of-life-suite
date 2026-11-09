# Vanilla++ 1.5.3+26.3 - Switched-off pouch Elytra keep repairing

1.5.2 stopped pouch XP Mending and SSO auto-repair for an Elytra whose Tool Pouch slot is switched off. At the user's decision (2026-11-08) 1.5.3 reverses that: a switched-off Elytra is repaired exactly as when its slot is on, and the switch only stops pouch flight. Targets, libraries and original inputs are unchanged from [1.5.2](RELEASE_1_5_2.md).

## Changes

- **Repair continues while switched off.** Pouch XP Mending and SSO's auto-repair-on-break now include an Elytra in a switched-off pouch slot. Equipped-item XP priority, SSO's regular-Mending setting and its whetstone/material rules are unchanged. The slot stays off after the repair: Tool Pouch finds no glider in it, and flight returns only when the slot is switched back on. This replaces the 1.5.2 line saying the Elytra "is not worn, mended or SSO-repaired"; it is still not worn.
- **No slot command.** As decided for 1.5.2, there is still no command for switching other pouch slots from a vanilla client.
- **Hotkey Wheel 1.0.11.** The current wheel lists **Toggle pouch Elytra** with its other Tool Pouch slot toggles. A wheel slot saved with the removed suite control (`key.toolpouch.toggle_elytra`) now switches the pouch Elytra through Tool Pouch instead of reporting the control as unavailable.

Components: suite **1.5.3+26.3** and Tool Pouch Atlas and Elytra **1.2.1+26.3** (only `PouchMending`, `PouchSsoMending`, `PouchItems` and its version change). Combined compatibility stays **1.5.2+26.3**; it and every other nested module, the root classes and the locks are byte-identical to 1.5.2.

## Installation

Replace the previous suite JAR with `vanilla-plusplus-quality-of-life-suite-1.5.3+26.3.jar` on the server and modded clients, or use `vanilla-plusplus-installation-pack-1.5.3+26.3.zip` ([instructions](INSTALLATION_PACK.md)). Source: branch `feature/pouch-slot-toggle-binds` of [the suite repository](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite), pending review. Local release files: `Builds/Minecraft/Vanilla++ Quality of Life Suite/Main Plugin/1.5.3+26.3/`.

## Final acceptance

Final suite SHA-256: `807083d8c7fb232c7616004575f36145562b17b46477c870cf4c5888fe3a2b39`. All checks below ran on that exact JAR on a private Xvfb display; records are in [verification.json](../qa-release/evidence/1.5.3/verification.json).

| Fresh check | Result |
| --- | --- |
| Pouch XP and SSO Mending with the Elytra slot switched off and on | 71 native + 74 Polymer + 46 no-SSO checks; a switched-off Elytra is repaired by XP (40 → 26 damage, no XP left) and by SSO auto-repair, also with the pouch menu open; the slot stays off and Tool Pouch finds no glider until it is switched back on |
| Slot-toggle key presses (Elytra with the old key carried over to J, atlas, magnet, collector) plus Hotkey Wheel 1.0.11's Toggle pouch Elytra and a wheel slot saved with the removed control, each off and on | 60 checks pass in the native and the Polymer profile |
| Suite wheel integration: Hotkey Wheel 1.0.11, Buildprint 0.1.38, Blocktone 0.1.38, BRBR 1.11.7, Sorted 0.1.41, atlas cache | 842 native + 842 Polymer checks pass |
| Hotkey Wheel's own suite profiles with this JAR (BRBR 1.11.8), including its 60 Tool Pouch slot-toggle checks | 818 + 818 (Polymer) checks pass |
| Connections: native suite, Fabric-only, unmodified vanilla, Polymer | 4 profiles pass |
| Unified settings with Hotkey Wheel 1.0.11 | pass |
| Upgrade from 1.4.5 | 23 stacks, 10 chalk marks, pet index identical; 337 config values, only the 12 accepted MapStitch/Polymer migrations change; Elytra-off player gets a disabled Elytra slot, Elytra-on player keeps flight |
| Upgrade from 1.5.0, 1.5.1 and 1.5.2 | 24 stacks incl. a pouch with switched-off slots, identical world data; 345 config values unchanged; same Elytra player results |
| Restart on 1.5.3 | identical world and config; switched-off slots persist on the pouch |
| Installation ZIP | 13 checks, byte-identical rebuild |

Native features, Sorted keep-rule/storage and the MapStitch pin guard were not rerun: the root classes, combined compatibility, Backpack Crafting and mixed-scale MapStitch are byte-identical to 1.5.2, so its results stand; HUD, Stackables, packet-codec, mechanics, mixed-scale, pickup and anvil checks keep their 1.5.0 results for the same reason. The slot-toggle client test runs in an integrated (singleplayer) world, so its Polymer profile means Polymer with a modded client; vanilla clients on a Polymer server are covered by the connection profiles and the Mending passive guard. The older component-level Mending fixture (`components/toolpouch-atlas-elytra/working/qa/pouch-mending`, built on the retired Multi-Shim 1.0.3 bundle) already expects a switched-off Elytra to mend and was not run. Hotkey Wheel is unchanged, so no wheel build was made. The paused SSO port was neither built nor tested.
