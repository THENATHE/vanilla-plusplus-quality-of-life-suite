# Release 1.1.6

Vanilla++ Quality of Life Suite fixes disconnects when a Tool Pouch contains a shulker with items stacked above their original vanilla defaults. A pouch containing a shulker of three potions can trigger the problem; shulkers inside other shulkers are not required.

## Stored items and client compatibility

Sensible Stackables applies configured limits to live item stacks. Stored-item templates now consult the same effective limits, so valid stacks remain valid when their containing shulker is sent to a client. Explicit item overrides retain priority, and ordinary strict validation remains enabled. The fix lives in the separate suite compatibility module; the original developer mods remain unchanged.

Matching native clients keep full previews and configured limits. Clients without Stackables support retain full quantities with maximum-stack prediction metadata capped at 99. If a nested preview contains a count above 99, that outgoing preview component is hidden because vanilla cannot materialize it. The server retains all actual contents and counts, and supported inventory menus still deliver full top-level quantities. Safe smaller previews remain visible. This also covers clients that support a carrier mod, such as Tool Pouch, but lack Stackables.

No saved item, map, configuration or world format changes. Elytra Mending with Clumps, Unbreaking, atlas commands/copying, mixed-scale maps, Bannerpoint and independent minimap/information placement remain available. Defaulted remains absent from runtime installation. See the [packet-fix analysis](PACKET_FIX_1_1_6.md) for the reproduced failure and exact evidence.

## Installation

The final installation ZIP passes [13 fresh installer/archive checks](installation-pack-verification.json), including actual client/server installs, exact hashes, side filtering, official Fzzy download, preservation of existing files and reproducible ZIP output. These checks do not claim launcher GUI import.

| Requirement | Exact target |
| --- | --- |
| Suite | `1.1.6+26.3`, stable `main` |
| Minecraft / Fabric Loader / Java | `26.3` / `0.19.5` / `25+` |
| Stackables compatibility | `1.0.2+26.3`, nested |
| Official Sensible Stackables | `3.1.1`, unchanged nested developer input |
| Mixed-scale atlas addon / coordinator | `1.1.5+26.3` / `1.1.1+26.3`, unchanged |
| Other nested modules | All 17 byte-identical to release 1.1.5 |
| External dependencies | Unchanged from 1.1.5; [exact dependency list](DEPENDENCIES.md) |
| Polymer for fallback clients | Full Polymer Bundled `0.18.2+26.3` on the server |

Stop and back up your instance, replace its suite JAR, and install the matching version on native clients. Keep exactly one suite JAR and avoid separate copies of nested feature mods. Preserve existing configuration and world files. Existing 1.1.5 external libraries remain compatible; this fix needs no settings reset or resource-pack rebuild. Fresh setups should use the [installation pack instructions](INSTALLATION_PACK.md). Rollback consists of stopping the instance and restoring its previous suite JAR.

[Release downloads](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/releases/tag/v1.1.6%2B26.3) use the standard four assets:

- `README.md`
- `vanilla-plusplus-quality-of-life-suite-1.1.6+26.3.jar`
- `docs.zip`
- `vanilla-plusplus-installation-pack-1.1.6+26.3.zip`

The installation ZIP contains permitted dependencies, notices and its client/server helper. Fzzy Config is obtained directly from its official publisher; fresh installation needs internet access. The corresponding local release folder is `Builds/Minecraft/Vanilla++ Quality of Life Suite/Main Plugin/1.1.6+26.3/`.

## Verification

The release preserves the exact runtime-tested JAR, SHA-256 `ea383c52581970f5b69eb0db0cffdc9c26a0a0efb2f80e0ab82f8b8f9d2a5f86`; promotion does not rebuild it. Fresh acceptance covers **28,156 packet/template assertions**, **519 Stackables assertions across six phases**, and **two physical clients**, native suite and Fabric API-only fallback. Both receive the affected pouch/shulker/potion arrangement intact and move full 2,048-item stacks through real inventory packets. Clean-build/archive checks verify 18 modules, original input hashes and notices, exact internal dependencies and no Defaulted runtime requirement.

The broad **55,911-check** 1.1.5 sanity pass remains historical evidence for its own exact JAR. It did not include this newly reproduced nested-template failure. The remote server/world, every tooltip integration and a fresh pure-vanilla GUI are not claimed as tested. Final evidence, preliminary candidates and frozen failing baselines remain separately identified in [validation](VALIDATION.md).

## Your server check

1. Rejoin with matching suite files and the affected pouch still present. Open and close its pouch and stored shulker screens; confirm quantities remain intact and the packet exception stops.
2. Test potion stacks of three and other configured stack sizes, including a netherite pouch attached to leggings.
3. If uncapping is enabled, verify native clients retain complete previews. Clients without Stackables support should retain all real items/counts through supported menus; an unsafe above-99 nested preview is hidden.
