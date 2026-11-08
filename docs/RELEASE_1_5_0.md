# Vanilla++ 1.5.0+26.3 - November 2026 upstream refresh

This release moves the suite onto the newest developer releases of its included mods and libraries for Minecraft 26.3, replaces the unofficial Chalk port with DaFuqs' official Chalk release, and adapts the suite's own additions to the new upstream code. It targets Minecraft 26.3, Java 25 and Fabric Loader 0.19.5. The [2026-11-08 audit](AUDIT-2026-11-08.md) lists every finding, fixed or not, with reasons.

## Why a minor version

Tool Pouch (1.1.10 → 1.2.0) and MapStitch (1.1.7 → 1.2.1) are minor releases that add new saved content (`toolpouch:disabled_slots`, `mapstitch:atlas_pin`, pin data) and changed APIs the suite builds on, and Chalk switches from a port to the official release. Several suite components therefore change behaviour or compatibility contracts, not only fixes, so the suite advances to **1.5.0** rather than 1.4.6.

## Inputs

| Input | 1.4.5 | 1.5.0 | Modrinth version |
| --- | --- | --- | --- |
| Simple Smithing Overhaul | 2.10.0 | 2.10.3 | [bNtv3MPv](https://modrinth.com/mod/simple-smithing-overhaul/version/bNtv3MPv) |
| MapStitch | 1.1.7 | 1.2.1 | [TyJwvZB9](https://modrinth.com/mod/mapstitch/version/TyJwvZB9) |
| Tool Pouch | 1.1.10 | 1.2.0 | [LDL8NLQd](https://modrinth.com/mod/tool-pouch/version/LDL8NLQd) |
| Tiered Backpacks | 1.0.20 | 1.0.21 | [Fh8qqxFD](https://modrinth.com/mod/tiered-backpacks/version/Fh8qqxFD) |
| Simple Death Improvements | 1.6.0 | 1.7.0 | [axbdo6aY](https://modrinth.com/mod/simple-death-improvements/version/axbdo6aY) |
| Sensible Stackables | 3.1.1 | 3.1.2 | [aZbIVjYA](https://modrinth.com/mod/sensible-stackables/version/aZbIVjYA) |
| Chalk | 3.2.1+26.3 (ChatGPT port) | 3.2.0+26.3 (official) | [IfK88aD4](https://modrinth.com/mod/chalk/version/IfK88aD4) |
| Chalk: Colorful Addon | 2.1.1-port.1+26.3 | 2.1.1-port.2+26.3 (metadata-only port; no 26.3 developer release) | — |
| Fabric API | 0.161.0+26.3 | 0.165.2+26.3 | [nJlcvFfj](https://modrinth.com/mod/fabric-api/version/nJlcvFfj) |
| Fabric Language Kotlin | 1.14.1+kotlin.2.4.20 | 1.14.2+kotlin.2.4.21 | [SKW1BTL3](https://modrinth.com/mod/fabric-language-kotlin/version/SKW1BTL3) |
| Polymer (optional) | 0.18.2+26.3 | 0.18.7+26.3 | [pA7tHJZE](https://modrinth.com/mod/polymer/version/pA7tHJZE) |
| Sorted (optional bridge) | 0.1.25 / 0.1.26 | also 0.1.41+26.3 | [PcNoO0No](https://modrinth.com/mod/sorted/version/PcNoO0No) |

Unchanged and still the newest for 26.3: MiscTweaks 1.4.4, Bannerpoint 1.1.2, Fzzy Config 0.7.7+fix3, Mixson 2.2.1, Cloth Config 26.3.159, Mod Menu 21.0.0, ClientSort 3.104.1, CodecUI 26.3-1.4.3. Exact hashes are in `locks/artifacts.json` and the [dependency directory](DEPENDENCIES.md). Original JARs are bundled unchanged.

Component versions: combined compatibility **1.5.0+26.3**, Chalk compatibility **1.2.0-suite.1+26.3**, Shared Region Maps **1.0.6-combo.1+mc26.3**, Tool Pouch atlas/Elytra **1.1.0+26.3**, mixed-scale MapStitch **1.2.0+26.3**, Stackables compatibility **1.0.4+26.3**, Bannerpoint compatibility **1.0.2+26.3** (metadata only), Backpack Crafting **1.1.0+26.3**.

## Chalk: port to official release

The official 3.2.0+26.3 release and the port share all registry IDs, block-state properties, items, recipes, tags and assets (the files differ only in line endings), so existing chalk marks and chalk items load unchanged; the upgrade check below confirms it. The port contained no functional additions. The only code difference is the destroy-effect hook: Minecraft 26.3 split it into a hand-break hook and a `destroyBlock` hook, and the official release overrides only the second. `chalk-compat` now routes hand-breaking through Chalk's own sound and cloud effect, so marks still break the way they did on 26.2 and in 1.4.5. The official JAR is not modified; the port stays in `components/chalk/upstream/` as a historical baseline.

## User-visible changes

See the [changelog](../CHANGELOG.md). Notable:

- Upstream features arrive unchanged: atlas pins and centered minimap (MapStitch), pouch slot toggles (Tool Pouch), keep-damageable-items on death (Simple Death Improvements), many upstream duplication and data-loss fixes.
- Calling a pet now refuses when a player who is neither the caller nor on the caller's scoreboard team is riding it (previously anyone could bind an unowned happy ghast and carry other players along).
- MapStitch 1.2 migrates its server configuration once on first load: it clears the compass/clock item requirements and allows time, coordinates, biome and weather in the minimap. Servers that had customized those four permissions or the requirement lists should re-apply them after updating. This is upstream behaviour; see the parked decision in the audit.

## Installation

Replace `vanilla-plusplus-quality-of-life-suite-1.4.5+26.3.jar` with `vanilla-plusplus-quality-of-life-suite-1.5.0+26.3.jar` on the server and modded clients, and update Fabric API, Fabric Language Kotlin and (server) Polymer to the versions above, or use `vanilla-plusplus-installation-pack-1.5.0+26.3.zip` ([instructions](INSTALLATION_PACK.md)). Install one suite version at a time. Keep worlds and `config/`.

Source: `Minecraft/thenathe-mod-suite/`, branch `update/2026-11-upstream-audit` of [the suite repository](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite) (pending review before merging to main). Local release files: `Builds/Minecraft/Vanilla++ Quality of Life Suite/Main Plugin/1.5.0+26.3/`.

## Final acceptance

Final suite SHA-256: `2bc95d4b0ad0d41a18b1e1100371e548b6c0dc4a994fa057af48fc9029de8c3c` (a clean rebuild with the Microsoft Java 25 JDK reproduces it byte for byte). All checks below ran on that exact JAR, on a private Xvfb display. Records: [verification.json](../qa-release/evidence/1.5.0/verification.json).

| Fresh check | Result |
| --- | --- |
| Connections: native suite, Fabric-only, unmodified vanilla, Polymer | 4 profiles pass |
| Hotkey Wheel 1.0.8, Buildprint 0.1.25, Blocktone 0.1.25, BRBR 1.11.7, atlas cache | 763 native + 763 Polymer checks pass |
| Unified settings with Hotkey Wheel 1.0.8 | pass |
| Sensible Stackables behaviour, conservation and restarts | 519 assertions across six runs |
| Default and uncapped container packet codecs | 4 profiles pass |
| HUD rendering and settings | 30 native + 30 Polymer scenes |
| Recipe/menu/drop/map mechanics | 616 checks incl. restart |
| Mixed-scale atlases | 1,294 checks incl. restart |
| Native features, pickup, pouch Mending, anvil | native and Polymer profiles, incl. restarts |
| Backpack crafting server menus | 192 native + 192 Polymer |
| Sorted 0.1.41 storage controls and cursor recovery | 487 assertions |
| Upgrade from 1.4.5 (release ZIP) | 22 item stacks, 10 chalk marks and the pet index identical; 337 config values checked |
| Installation ZIP | 13 checks, byte-identical rebuild |

Upgrade detail: every suite-owned and other mod setting written by 1.4.5 (including deliberately edited values) survived, except MapStitch's one-time v1→v2 migration described above and two PolyMc options that Polymer 0.18.7 removed.

Not rerun for this release: the Alt keep-rule fixture against Sorted 0.1.41 (it reflects into Sorted internals that 0.1.41 removed; the suite's mixin targets were checked byte-identical by review), BRBR backpack network profiles, physical packet-client receipt, Bannerpoint (unchanged), and the integrated-singleplayer/client-Polymer connection modes. The integration run used Sorted 0.1.26 because Sorted 0.1.41's bundled voxelsprout libraries crash Buildprint 0.1.25's settings screen; this is outside the suite. The paused SSO port was neither built nor tested.
