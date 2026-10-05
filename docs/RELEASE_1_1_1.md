# Release 1.1.1

Vanilla++ Quality of Life Suite combines the existing gameplay modules with automatic native/Polymer negotiation. This update lets the minimap and Tool Pouch information use different corners, repairs atlas filtering across dimensions and scales, and adds map extraction commands.

## Place the map right and information left

Open the suite settings using your configured hotkey or Mod Menu. In **MapStitch Client Settings**, set the minimap position to **Top Right**. In **Tool Pouch Client Settings**, set the **info overlay** position to **Top Left**. Apply each configuration. Their offsets remain independent. Atlas and ordinary-map minimaps continue sharing the map position; text moves around a map only when they actually overlap on the same side.

At first startup, the saved MapStitch minimap position/offsets initialize the shared map placement. The saved information-overlay settings remain unchanged.

## Extract atlas contents

Hold the atlas in either hand, or use the atlas in your active Tool Pouch. No operator permission is required. The main hand takes priority, followed by the offhand, then the pouch.

```text
/extractmap 1:1 minecraft:the_end
/extractmap 1:1
/extractmap minecraft:the_end
/extractmap empty
/extractmap scale 1:16
/extractmap dimension minecraft:the_nether
```

The first command extracts only End maps at 1:1. Scale alone selects every dimension; dimension alone selects every scale. `empty` extracts all blank maps and paper. Supported ratios are 1:1, 1:2, 1:4, 1:8 and 1:16. Explicit scale/dimension forms also accept the other filter afterward. Extracted items go into inventory first; overflow drops at your feet. Stored map data and unselected contents remain intact.

## Installation and assets

| Requirement | Exact target |
| --- | --- |
| Suite | `1.1.1+26.3` |
| Minecraft | `26.3` |
| Fabric Loader | `0.19.5` |
| Java | `25` or newer |
| Defaulted | `1.3.8+26.3.dropfix.1`, exact retained dropfix |

Use the [release downloads](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/releases/tag/v1.1.1%2B26.3) and [installation guide](INSTALLATION_PACK.md) for the full dependency list. Replace the previous suite JAR instead of installing both. Matching clients and server need this suite version for native atlas metadata; clients without the suite keep the existing Polymer fallback behavior.

The release has four uploaded files: `README.md`, the suite JAR, `docs.zip`, and `vanilla-plusplus-installation-pack-1.1.1+26.3.zip`. The installation ZIP contains the suite and redistributable dependencies, with the installer and exact manifests. It downloads Fzzy Config directly from its official URL, so a fresh installation needs internet access.

## Verification and your testing steps

Executed tests, exact hashes, screenshots and historical limits are recorded in [validation](VALIDATION.md). Original developer JARs, library dependencies, all settings and the separate module layout remain preserved. The distinct ChatGPT SSO port track remains paused.

1. Put a compass/clock and atlas in a Tool Pouch. Apply right minimap and left information, then restart the client to check persistence. Swap the atlas for a regular map and check the same placement.
2. Open an atlas with Overworld, Nether and End maps at several scales. Cycle S through each layer, then travel between dimensions; only maps from the viewed dimension/scale should render.
3. Try the extraction commands on a spare atlas, including `empty`, with both free inventory space and a full inventory. Check item counts, preserved atlas settings and overflow drops.

See [controls](ATLAS_CONTROLS.md), [HUD settings](HUD.md), and [maintenance details](MIXED_SCALES.md).
