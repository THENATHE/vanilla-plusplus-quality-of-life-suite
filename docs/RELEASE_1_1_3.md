# Release 1.1.3

Vanilla++ Quality of Life Suite fixes a reproduced blank-minimap case caused by missing map-item center metadata and adds atlas maintenance commands. Bannerpoint, coordinated banner marking, independent information/minimap placement, mixed-scale atlases and the existing gameplay modules remain included.

## The minimap fix

A stored map can have a valid ID and explored pixels while its atlas item lacks `mapstitch:map_center`. In the reproduced case, the client selected no map and made zero minimap rendering calls. The minimap now falls back to the saved map’s center and keeps filtering by your current dimension and selected **M** scale.

This addresses the confirmed metadata-dependent failure. A blank minimap may still mean the selected scale has no covering map, the saved map record is missing or its terrain is unexplored. The diagnostic command helps distinguish those cases; this release does not claim every cause of a blank minimap has been reproduced or repaired.

A separate server-side reproduction without Polymer found stale center components being trusted merely because they were present. The shared-map helper now validates those stored values against the saved map record as well. That defect can misplace maps and is distinct from the missing-center blank-rendering case.

## Inspect and repair your atlas

Hold the atlas in your main hand or offhand, or use the atlas in your active Tool Pouch. The main hand takes priority, then offhand, then active pouch. Commands apply only to your own selected atlas and need no operator permission.

```text
/repairmaps check
/repairmaps
```

`check` reports the selected minimap scale/current dimension, covering maps, missing saved records, blank/locked maps and duplicate references without changing contents. `/repairmaps` repairs stored map-item center components from authoritative saved data and resends the complete stored pixels and decorations to a matching native client.

Repair includes stored map-ID entries such as explorer/treasure maps and corrects missing or stale center components. It keeps map IDs, exploration pixels, banner decorations, atlas options and unrelated item data. It does not generate replacements or reconstruct a missing saved record. Native metadata and refresh packets are sent only to clients with the negotiated support and advertised channels; fallback clients receive no unsupported custom payload. See [atlas commands](ATLAS_CONTROLS.md) for usage and interpretation.

## Remove duplicate atlas copies

```text
/dedupemaps
```

The command selects the same held/active-pouch atlas. It keeps one copy per exact map ID and returns one empty map for each removed extra copy. Empty maps enter your inventory first; overflow drops at your feet. Different map IDs remain separate even if they cover the same dimension/region/scale. This removes duplicate atlas items without deleting saved exploration or banner records, selecting a “most explored” map or merging independently saved maps.

## Installation and assets

| Requirement | Exact target |
| --- | --- |
| Suite | `1.1.3+26.3`, stable `main` |
| Minecraft | `26.3` |
| Fabric Loader | `0.19.5` |
| Java | `25` or newer |
| Mixed-scale atlas addon | `1.1.3+26.3`, already nested |
| Shared Region Maps | `1.0.5-combo.1+mc26.3`, already nested |
| Bannerpoint compatibility | `1.0.1+26.3`, unchanged and already nested |
| Defaulted | Exact `1.3.8+26.3.dropfix.1`, unchanged |
| Polymer for clients without the suite | Full Polymer Bundled `0.18.2+26.3` on the server |

Replace the older suite JAR with [the release download](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/releases/tag/v1.1.3%2B26.3); do not install two suite versions or duplicate nested feature mods. Keep your worlds and saved configuration files. Install the matching version on the server and native suite clients. The full unchanged dependency list and client/server setup are in [installation instructions](INSTALLATION_PACK.md).

The release has four public assets:

- `README.md`
- `vanilla-plusplus-quality-of-life-suite-1.1.3+26.3.jar`
- `docs.zip`
- `vanilla-plusplus-installation-pack-1.1.3+26.3.zip`

The installation ZIP includes the suite, permitted dependencies, licenses and installer; it downloads Fzzy Config from its official publisher, so fresh installation requires internet access. Existing Polymer pack hosting and Bannerpoint’s successful-load requirement remain unchanged.

## Verification and your testing steps

[Validation](VALIDATION.md) records the exact tested hash, fresh checks and remaining limits. Earlier results retain their original artifact versions and are not relabeled as fresh 1.1.3 verification. Original developer JARs, dependencies, settings and saved-data identities remain preserved. The distinct ChatGPT SSO version-port track remains paused.

1. Select an atlas with known explored terrain at your current location and minimap scale. Run `/repairmaps check` and compare its coverage/missing-record report with the minimap.
2. Run `/repairmaps`. Confirm the explored minimap displays without creating new maps or spending blanks. Check the atlas’s map IDs, banners and scale/generation options remain intact.
3. Repeat with the atlas in the offhand and active Tool Pouch. Switch dimension and minimap scale; only covering maps from that layer should render.
4. Add extra copies of one map ID, plus a different ID for the same region. Run `/dedupemaps` and check that one same-ID copy remains, the different ID remains untouched and one blank is returned per removed copy. Repeat with a full inventory to check overflow drops.
5. Test an atlas containing a genuinely unexplored map or missing saved record. Diagnostics should identify the problem; repair should not invent terrain or replace its map ID.

See [atlas controls](ATLAS_CONTROLS.md), [mixed-scale implementation](MIXED_SCALES.md), [Bannerpoint support](BANNERPOINT.md), [HUD settings](HUD.md) and [source/update guide](UPDATING.md).
