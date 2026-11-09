# Vanilla++ 1.5.1+26.3 - review follow-up to the November 2026 refresh

1.5.0 was prepared but never published; its local build folder is kept unchanged. 1.5.1 contains everything in the [1.5.0 guide](RELEASE_1_5_0.md) (new upstream releases, official Chalk, audit fixes) plus the follow-up below, decided after review. Targets, libraries and original inputs are the same as 1.5.0: Minecraft 26.3, Java 25, Fabric Loader 0.19.5, Fabric API 0.165.2, Polymer 0.18.7.

## Changes since 1.5.0

- **Swift Flight 1.2.0 parity.** The suite's Swift Flight data now matches Jodek's v1.2.0: the enchantment is part of `#minecraft:non_treasure` (so it appears in enchanting tables, librarian trades and random enchanted loot like other ordinary enchantments), its minimum cost is 10 + 10 per level with harnesses as primary items, and the 32 vanilla harness and harness-dye recipes are replaced with v1.2.0's copies, which differ from vanilla only by giving the crafted harness `minecraft:enchantable` 10. The lower cost also lowers grindstone extraction (Swift Flight III: 31 → 17 levels). The identifier `swift_flight:swift_flight` is unchanged, so existing books and enchanted harnesses keep working; harnesses crafted before the update are not table-enchantable, as in upstream. The suite's End City book chance stays at the documented 8%. The suite setting still turns the flight-speed effect off; the enchantment itself stays registered.
- **Sorted public API.** Backpack Crafting 1.1.1 registers the crafting menu, classifies its storage slots and publishes its button placement through Sorted's public `SortedStorage` API instead of mixins. Keep-rule suppression, portable-menu status and cursor recovery keep targeted mixins because Sorted has no public API for them; the [provenance record](PROVENANCE.md) lists each case. The compile input stays the unchanged Sorted 0.1.25 JAR, whose public API is identical to 0.1.41's. The runtime gate admits 0.1.25, 0.1.26 and 0.1.41+26.3.
- **Sorted permission.** The user states that they know Sorted's author and that he is fine with the suite's use; this is recorded without a written grant.
- **MapStitch pin guard.** MapStitch 1.2.1 copies a container into a fixed 27-slot list when it takes a pin for placement or sharing (only when *Atlas pins require items* is on) and writes the list back, which deleted everything past slot 26 of larger backpacks and pouches and hid pins stored there. A narrow suite mixin sizes that copy to the stored contents, never below 27, so shulker boxes behave exactly as before. It applies only to MapStitch 1.2.1; the official JAR is unchanged.
- **Decisions kept.** MapStitch 1.2's one-time configuration migration is accepted and documented; the pet-call team rule stays. The SSO overlay strings remain open and unchanged.

Components: suite root **1.5.1+26.3**, Backpack Crafting **1.1.1+26.3**, combined compatibility **1.5.1+26.3** and mixed-scale MapStitch **1.2.1+26.3** (metadata only, for its exact coordinator dependency); every other nested module is byte-identical to 1.5.0.

## Installation

Replace any earlier suite JAR with `vanilla-plusplus-quality-of-life-suite-1.5.1+26.3.jar` on the server and modded clients, with the libraries listed in the [dependency directory](DEPENDENCIES.md), or use `vanilla-plusplus-installation-pack-1.5.1+26.3.zip` ([instructions](INSTALLATION_PACK.md)). Coming from 1.4.5, also read the 1.5.0 notes about MapStitch's configuration migration.

Source: branch `update/2026-11-upstream-audit` of [the suite repository](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite), pending review. Local release files: `Builds/Minecraft/Vanilla++ Quality of Life Suite/Main Plugin/1.5.1+26.3/`.

## Final acceptance

Final suite SHA-256: `00aa245b80aa62cdca2cee5a7a3cbdf30ec0c7e0df37b71cea8a146ba9f8328c`. All checks below ran on that exact JAR on a private Xvfb display; records are in [verification.json](../qa-release/evidence/1.5.1/verification.json).

| Fresh check | Result |
| --- | --- |
| Connections: native suite, Fabric-only, unmodified vanilla, Polymer | 4 profiles pass |
| Hotkey Wheel 1.0.9, Buildprint 0.1.38, Blocktone 0.1.38, BRBR 1.11.7, Sorted 0.1.41, atlas cache | 782 native + 782 Polymer checks pass |
| Unified settings with Hotkey Wheel 1.0.9 | pass |
| Native features incl. Swift Flight 1.2.0 (tags, costs, enchantable crafted harness, table offers on harness and book) | native and Polymer, initial and restart |
| Sorted 0.1.41 keep-rule markers and Alt edits | 146 assertions (fixture restored for 0.1.41) |
| Sorted storage controls and cursor recovery | 487 assertions on 0.1.41 and 487 on 0.1.26 |
| MapStitch pin guard: shulker, early in a 60-slot backpack, bundle inside it, only past slot 27; placement and share payment | 8 cases × native and Polymer pass; the same fixture on 1.5.0 reproduces the loss (5 stored entries become 2) and the unusable pin |
| Upgrade from 1.4.5 | 23 stacks (incl. a Swift Flight book and harness), 10 chalk marks, pet index identical; 337 config values, only the 12 accepted MapStitch/Polymer migrations change |
| Upgrade from 1.5.0 | identical world data; 345 config values unchanged |
| Installation ZIP | 13 checks, byte-identical rebuild |

HUD, Stackables, packet-codec, mechanics, mixed-scale, pickup, Mending and anvil checks were not rerun: those modules are byte-identical to 1.5.0 (mixed-scale MapStitch changes only its metadata version), so their 1.5.0 results stand. Pin sharing is exercised through its payment step, not the full share command with a second online player. The paused SSO port was neither built nor tested.
