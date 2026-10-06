# Atlas controls — stable 1.1.5

Suite **1.1.5** keeps its MapStitch changes in the separate `components/mapstitch-mixed-scales/working/` addon. The original MapStitch JAR is unchanged.

Open the world-map screen. A separate group just beneath the top coordinate numbers contains one aligned row of minimap and generation controls. Its right edge lines up with the original sidebar, with four pixels of margin; the buttons begin 16 GUI pixels from the top. The original right-side buttons retain their original positions and spacing:

| Control | Behavior |
| --- | --- |
| S / original scale keys | Switch the world-map viewing layer. This does not change the minimap or generation choices. |
| M1, M2, M4, M8, M16 | Click to cycle the minimap scale. Each atlas remembers its own choice. |
| 1, 2, 4, 8, 16 | Toggle generation at each scale independently. Green means enabled; gray means disabled. Multiple scales may be enabled, or all disabled. |

The minimap and generation buttons' hover text lists what is generating. The atlas tooltip starts with **Stores maps**, then enabled generation scales, then minimap scale; filled-map count, empty-map count, ejection preference and other original lines follow in their existing order. If an enabled scale has no map covering your current position/dimension, one stored blank is consumed for that scale. Existing shared region/scale records may be reused, preserving regional map sharing. Insufficient blanks produce only the layers affordable in ascending scale order. Disabling generation leaves stored maps intact; a missing minimap layer remains empty rather than switching to a different scale. Enabled layers keep updating as you explore even when the minimap uses another scale.

Existing atlases start with generation enabled at their prior active scale. Changing M preserves those generation choices. New per-atlas choices use the namespaced integer `mapstitch_mixed_scales:generation_mask` in vanilla `minecraft:custom_data`. Normal server atlas ticking also assigns a UUID string at `mapstitch_mixed_scales:book_id` if the book has no identity yet. Both tags persist through normal saves and inventory/codec synchronization while retaining unrelated custom tags. Minimap selection continues to use the original `mapstitch:atlas_scale` component; map IDs/contents are preserved. There is no new custom component registry to synchronize to vanilla clients.

## Dimension and scale selection

An atlas can hold maps from multiple dimensions and all five scales together. The world map's selected dimension and **S** scale select only matching stored maps; neither changes the minimap's saved **M** scale or generation choices. The minimap uses maps from your current dimension at its selected scale. Switching dimension or scale clears stale world-map layer caches so another dimension's tiles do not remain on screen.

Vanilla map-update packets omit map dimension and center, so client-side map data could previously inherit the player's current dimension. The addon sends the authoritative map dimension, center, scale and lock state to matching suite clients through `mapstitch_mixed_scales:map_metadata_v1`. These native-only metadata updates correct rendering without replacing stored maps or exploration data. The server checks native MapStitch negotiation and advertised payload support before sending; vanilla, Fabric-only and unsupported clients receive no unknown custom packet.

## Full-screen placement and map ejection

The full-screen atlas positions ordinary tiles and explorer/treasure markers using the synchronized saved map’s center. Missing or stale item-level center components no longer prevent placement or shift a tile into the wrong grid position. Existing dimension and viewing-scale filters still apply.

Use **Ctrl+Q** with the default eject-map binding while pointing at the desired map, or Ctrl plus your configured **Eject Map** key. The original ejection lookup uses the same corrected map positions as rendering. Its normal map/exploration-marker selection behavior is preserved; this change does not replace the original command or key binding.

## Banner markers across scales

Hold the atlas and right-click a banner. The **1/2/4/8/16 generation toggles** also select which existing map scales receive its marker. The operation only affects maps in the current dimension covering the banner; **M** and **S** do not limit it. It never creates maps or spends blanks/paper during the click.

If every selected map already has the current marker, the click removes it from all of them. Otherwise it adds missing markers or updates changed names/colors, retaining matching markers. Disabled scales and other dimensions remain unchanged. All toggles off or no covering maps means no edit. A vanilla map-edge or decoration-limit failure rejects the group before any map changes. Locked maps retain vanilla decoration support.

Bannerpoint's map-linked waypoint remains enabled if any current-dimension map in this atlas still contains the banner, including a disabled scale. Its locator-bar icons remain separate from map decorations. See [Bannerpoint support](BANNERPOINT.md) and [the implementation guide](MIXED_SCALES.md).

## Extract maps from an atlas

Run `/extractmap` in singleplayer or on a server running the suite. Hold the atlas in your main hand or offhand; a main-hand atlas takes priority. If neither hand holds an atlas, the command uses the atlas in your active Tool Pouch. It only edits your own selected atlas and needs no operator permission. A loose atlas elsewhere in your inventory is not selected automatically.

| Command | Extracts |
| --- | --- |
| `/extractmap 1:1` | All filled maps at scale 1:1, across dimensions. |
| `/extractmap minecraft:the_end` | All filled maps from the End, across scales. |
| `/extractmap 1:1 minecraft:the_end` | Only filled maps matching both that scale and dimension. |
| `/extractmap scale 1:1 [dimension]` | The explicit scale form, with an optional dimension filter. |
| `/extractmap dimension minecraft:the_end [ratio]` | The explicit dimension form, with an optional scale filter. |
| `/extractmap empty` | All blank maps and paper, leaving filled maps in the atlas. |

Replace `1:1` with `1:2`, `1:4`, `1:8` or `1:16` as needed. Dimension IDs include `minecraft:overworld`, `minecraft:the_nether` and `minecraft:the_end`; command suggestions include the server's dimensions. Brackets in the table indicate an optional argument and are not typed. Running `/extractmap` alone displays usage help.

Extracted items go to your inventory first. Any overflow drops at your feet; the command reports how many items were added and dropped. Nonmatching contents remain stored, and the atlas's active-map selection refreshes after extraction. Scale/generation choices, names and other unrelated item data stay with the atlas. If nothing matches, the command reports that without removing contents. When both scale and dimension are specified, both must match; invalid arguments are rejected rather than broadening the selection.

## Check or repair a blank minimap

Use the same atlas selection as `/extractmap`: main hand first, then offhand, otherwise the active Tool Pouch atlas. A loose atlas elsewhere in your inventory is not automatically selected. These commands edit or inspect only your own selected atlas and require no operator permission.

| Command | Behavior |
| --- | --- |
| `/atlas fix check` | Inspect the atlas and report the minimap’s selected scale/current dimension, map coverage, missing saved records, blank/locked maps and duplicate references. It does not change map contents. |
| `/atlas repair check` | Alias for `/atlas fix check`; the same read-only report. |
| `/atlas repair` | Alias for `/atlas fix`; the same repair and resynchronization. |
| `/atlas fix` | Repair stored map-item center components from authoritative server saved data and resend the full stored map pixels and decorations to a matching native client. |

A reproduced failure involved a map with valid saved pixels whose atlas item lacked `mapstitch:map_center`. The client minimap used that absent item component to choose maps and rendered no map at all. The minimap now falls back to the actual saved map center, checks the current dimension, and preserves the selected scale. This fixes the reproduced metadata-dependent case; a blank selected layer may still mean no covering map, no saved record or unexplored terrain.

Repair also sends a fresh atlas/inventory snapshot even when the report says **0 centers repaired**, so a stale client copy can receive the correct server representation. `check` remains inspection-only. Repair applies to stored entries with a map ID, including explorer/treasure maps. It also validates already present center components against server saved data, correcting stale values. Repair preserves map IDs, pixels, banners, atlas options and unrelated contents. It does not explore new terrain, generate replacement maps, delete saved records or fill a genuinely unexplored map. Missing saved records cannot be reconstructed by this command. Native metadata/refresh packets are sent only when the client’s MapStitch support and advertised channels permit them; fallback clients receive no unsupported payload.

## Remove duplicate map copies

Run `/atlas dedupe` while holding the atlas, or using its active Tool Pouch location. It uses the same main-hand → offhand → active-pouch priority and requires no operator permission.

The command keeps one copy of each **exact map ID** in that atlas. Each removed extra copy returns one empty map to your inventory; overflow drops at your feet. This includes repeated copies represented by a stack count, not just separate entries. The command reports its result and preserves the retained copy’s item data, atlas settings and unrelated contents.

Maps with different IDs stay separate even when their dimension, center and scale match. Exploration, banners and the world’s saved map records are not deleted. Shared Region Maps normally lets copies of one region use the same record, which makes exact-ID duplicate removal appropriate; the command does not choose a “most explored” map or merge independently saved records. Existing `/extractmap` filters remain available when you want to remove an entire scale or dimension instead.

## Copy an atlas

Put an atlas and one **ordinary book** in a cartography table, or run:

```text
/atlas makecopy
```

The command selects the atlas in your main hand, then offhand, otherwise your active Tool Pouch. It requires and consumes **one ordinary book from your inventory**, keeps the source atlas, and puts the copy into inventory first; overflow drops at your feet. No operator permission is required.

Both methods copy all stored filled/explorer maps, across every scale and dimension, retaining each entry’s count. The copy preserves the atlas’s name and options, map IDs, item data and markers, but receives a new atlas identity. Empty maps and paper are excluded: supply the new atlas with its own exploration materials if you want it to generate further maps. Copying does not deduplicate its contents. Normal atlas click behavior still applies when taking the result; a primary click can clear the selected map slot without removing its stored maps.

The copied maps reference their original saved map records. Terrain exploration and saved banner-marker changes are therefore shared between matching map IDs; this is another atlas of those maps, not an independent frozen terrain snapshot. The original atlas and its empty maps/paper remain intact. The cartography operation consumes the book when its output is taken, keeping the original atlas as the source.

## Control networking and source identity

Serverbound requests use `mapstitch_mixed_scales:select_scale_v2` and `mapstitch_mixed_scales:select_generation_v2`. Each carries the owned source location/index, current map anchor, and persisted book identity before its scale or generation mask. The server validates native MapStitch negotiation, nonempty matching identity, owned location, map anchor, living player state, and bounds. Empty identity, stale anchor, replaced slot, invalid scale/mask, and unsupported client requests are rejected.

Screens remain bound to their original inventory/accessory/pouch source and book identity. The client may refresh the map anchor after first-map generation or ejection within that same book. It does not adopt the identity of a different atlas that replaces the source slot; the controls disable until the appropriate book is reopened. A newly created or legacy book needs its first server tick and inventory synchronization before native controls can edit it. If opened before that synchronization, reopen the screen once the book is synchronized. Matching client/server suite builds are required for native controls. Standard map creation sends one vanilla cartography sound packet to the explorer per generation batch; a batch creating multiple scales still plays one chime. Sound playback remains subject to the player's sound settings.

Use [stable installation and testing steps](RELEASE_1_1_4.md) and [current validation](VALIDATION.md) for the released artifact. Earlier merged-branch records remain [historical evidence](MERGED_TESTING.md). The optional original world-map buttons setting hides all these buttons together with the existing controls.
