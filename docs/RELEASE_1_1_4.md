# Release 1.1.4

Vanilla++ Quality of Life Suite extends the atlas center-metadata fix to the full-screen world map and its existing map-ejection controls. `/atlas fix` now resynchronizes the selected atlas and inventory even when the server reports zero centers repaired. Maintenance commands now share the `/atlas` command, and a cartography/command copy operation duplicates the atlas’s stored filled maps using one ordinary book. The release also adds enchantment-aware pouch flight durability and optional SSO automatic repair for stored Elytra. Existing minimap repair, diagnostics, deduplication, Bannerpoint and other gameplay features remain available.

## Full-screen atlas placement and Ctrl+Q

The full-screen atlas previously relied on the center component stored on each map item. If that component was absent, a valid map could be skipped; if it was stale, the map could be placed in the wrong grid position even though the saved map data was correct.

The screen now uses the synchronized saved map center for ordinary tiles and explorer/treasure markers. It keeps the existing dimension and viewing-scale filters. The original ejection lookup uses those same corrected screen positions: point at a map and press **Ctrl+Q** with the default binding, or Ctrl plus your configured **Eject Map** key.

A later empty atlas in the inventory or active Tool Pouch also no longer hides earlier filled atlases in the full-screen map. Original source scan locations and order remain intact.

The fix changes representation and lookup, preserving stored map IDs, exploration pixels, banner decorations and atlas options. It does not regenerate maps or make unexplored terrain appear.

## Repair even when zero centers need correction

Hold the atlas in your main hand or offhand, or use the atlas in your active Tool Pouch. Main hand takes priority, then offhand, then active pouch. These commands select only your own atlas and need no operator permission.

```text
/atlas fix check
/atlas fix
/atlas repair check
/atlas repair
```

`/atlas repair` is an alias for `/atlas fix`, and both accept `check`. `check` remains inspection-only and reports coverage, selected layer, center issues and missing/blank records. `/atlas fix` corrects stored centers as needed, resends saved map pixels and decorations, and sends a fresh atlas/inventory snapshot. It performs that resynchronization even if the server’s center metadata is already correct and the summary says **0 centers repaired**.

This handles a stale client representation without pretending the server map itself needs regeneration. Missing saved records and genuinely unexplored maps still require separate investigation; the command does not recreate records or invent exploration. Native custom metadata and cache-refresh packets remain gated to negotiated client support and advertised channels. See [atlas controls](ATLAS_CONTROLS.md) for diagnostics, `/atlas dedupe` and `/extractmap`.

## Copy an atlas with a book

Use an atlas with one ordinary book in a cartography table, or hold/use your active-pouch atlas with one ordinary book in inventory and run:

```text
/atlas makecopy
```

Both methods keep the original atlas and copy every filled/explorer map at all scales, dimensions and entry counts. The copy retains atlas names/options, map IDs and markers, and gets a new atlas identity. Empty maps and paper stay only in the original; they are not duplicated into the copy. Copies share the existing map records, so matching map IDs continue sharing terrain and banner data.

The cartography operation consumes one book when the output is taken. The command consumes one book from inventory and returns the new atlas to inventory first, dropping overflow. `/atlas dedupe` retains its same-ID cleanup behavior, and `/extractmap` commands remain unchanged. See [atlas controls](ATLAS_CONTROLS.md) for the full command and copying details.

## Elytra enchantments and repair in the Tool Pouch

Flight durability now goes through Minecraft’s normal enchantment-aware damage calculation, so Unbreaking works while the Elytra is stored in the pouch. Changes update the selected usable Elytra and preserve other stored items.

Regular XP Mending uses collected XP left after equipped Mending items, when the server permits regular Mending. With all four SSO Mending settings enabled, this behavior remains available. The server’s settings apply in multiplayer; a client-side setting alone does not enable server repair. This also applies to a netherite pouch attached to Mending netherite leggings: damaged leggings can spend all the orb’s XP first, while fully repaired leggings leave XP for the stored Elytra.

SSO’s automatic repair on break also checks the active pouch’s Elytra. It uses SSO’s original repair implementation: carry a working, compatible whetstone and the Elytra’s valid repair material in your inventory. SSO requires the whetstone to contain each enchantment present on the Elytra. The original resource costs and configuration controls apply. Keeping an Elytra in the pouch does not make repair free.

## Installation and assets

| Requirement | Exact target |
| --- | --- |
| Suite | `1.1.4+26.3`, stable `main` |
| Minecraft | `26.3` |
| Fabric Loader | `0.19.5` |
| Java | `25` or newer |
| Mixed-scale atlas addon | `1.1.4+26.3`, already nested |
| Shared Region Maps | `1.0.5-combo.1+mc26.3`, unchanged |
| Tool Pouch atlas/Elytra addon | `1.0.8-suite.1+26.3`, already nested |
| Shared coordinator | `1.1.0+26.3`, unchanged |
| Bannerpoint compatibility | `1.0.1+26.3`, unchanged |
| Defaulted | Exact `1.3.8+26.3.dropfix.1`, unchanged |
| Polymer for clients without the suite | Full Polymer Bundled `0.18.2+26.3` on the server |

Replace the previous suite JAR using [release downloads](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/releases/tag/v1.1.4%2B26.3), and use matching suite files on the server and native clients. Keep saved worlds and configurations; do not install duplicate suite versions or separate copies of already nested feature mods. External dependencies remain unchanged. [Installation instructions](INSTALLATION_PACK.md) explain the exact client/server setup.

The standard four release assets are:

- `README.md`
- `vanilla-plusplus-quality-of-life-suite-1.1.4+26.3.jar`
- `docs.zip`
- `vanilla-plusplus-installation-pack-1.1.4+26.3.zip`

The installation ZIP includes the suite, permitted dependencies, notices and installer. Fresh installation requires internet access to download Fzzy Config directly from its official publisher. Existing Polymer hosting and Bannerpoint’s verified successful-load requirement remain unchanged.

## Verification and your testing steps

[Validation](VALIDATION.md) records the exact final hash, fresh runtime checks and remaining limits. Previous releases retain their original documentation and evidence. The optional Remapped integration retains its own existing packet path; no new full-RGB runtime coverage is implied by normal-map checks. Original developer artifacts, dependency integrations and saved-data formats remain intact. The separate ChatGPT SSO version-port track remains paused.

1. Open an atlas with known explored maps, including explorer/treasure markers. Check the selected dimension and scale place its tiles and markers correctly.
2. Use Ctrl plus your configured eject-map key over the intended map. Confirm the original ejection action targets the map shown at that position.
3. With the atlas selected, run `/atlas fix` when `/atlas fix check` reports no center issue. Confirm the client receives the atlas refresh and renders its stored terrain; zero repairs should not prevent synchronization.
4. Repeat repair with an offhand atlas and an atlas in the active Tool Pouch. Check saved scale/generation options, map IDs, banner data and unrelated contents remain intact.
5. Copy a mixed-scale, mixed-dimension atlas containing explorer maps and stored blanks/paper in a cartography table and with `/atlas makecopy`. Check one book is consumed, the original remains intact, every filled-map count is copied, blanks/paper are absent from the copy, and the copy keeps names/options with a distinct atlas identity.
6. Check `/atlas fix check` leaves contents unchanged and still distinguishes missing records/unexplored maps from stale presentation.
7. Compare pouch flight durability with and without Unbreaking. Collect XP with a damaged Mending Elytra in the pouch; test SSO automatic repair with its server settings enabled, a whetstone and repair material. Repeat with an attached pouch and after closing its menu.

See [atlas controls](ATLAS_CONTROLS.md), [mixed-scale implementation](MIXED_SCALES.md), [Bannerpoint support](BANNERPOINT.md), [HUD settings](HUD.md) and [source/update guide](UPDATING.md).
