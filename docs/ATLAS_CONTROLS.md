# Atlas controls on merged

Suite **1.0.2-merged.3+26.3** keeps its MapStitch changes in the separate `components/mapstitch-mixed-scales/working/` addon. The original MapStitch JAR is unchanged.

Open the world-map screen. A separate group just beneath the top coordinate numbers contains one aligned row of minimap and generation controls. Its right edge lines up with the original sidebar, with four pixels of margin; the buttons begin 16 GUI pixels from the top. The original right-side buttons retain their original positions and spacing:

| Control | Behavior |
| --- | --- |
| S / original scale keys | Switch the world-map viewing layer. This does not change the minimap or generation choices. |
| M1, M2, M4, M8, M16 | Click to cycle the minimap scale. Each atlas remembers its own choice. |
| 1, 2, 4, 8, 16 | Toggle generation at each scale independently. Green means enabled; gray means disabled. Multiple scales may be enabled, or all disabled. |

The minimap and generation buttons' hover text lists what is generating. The atlas tooltip starts with **Stores maps**, then enabled generation scales, then minimap scale; filled-map count, empty-map count, ejection preference and other original lines follow in their existing order. If an enabled scale has no map covering your current position/dimension, one stored blank is consumed for that scale. Existing shared region/scale records may be reused, preserving regional map sharing. Insufficient blanks produce only the layers affordable in ascending scale order. Disabling generation leaves stored maps intact; a missing minimap layer remains empty rather than switching to a different scale. Enabled layers keep updating as you explore even when the minimap uses another scale.

Existing atlases start with generation enabled at their prior active scale. Changing M preserves those generation choices. New per-atlas choices use the namespaced integer `mapstitch_mixed_scales:generation_mask` in vanilla `minecraft:custom_data`. Normal server atlas ticking also assigns a UUID string at `mapstitch_mixed_scales:book_id` if the book has no identity yet. Both tags persist through normal saves and inventory/codec synchronization while retaining unrelated custom tags. Minimap selection continues to use the original `mapstitch:atlas_scale` component; map IDs/contents are preserved. There is no new custom component registry to synchronize to vanilla clients.

Serverbound requests use `mapstitch_mixed_scales:select_scale_v2` and `mapstitch_mixed_scales:select_generation_v2`. Each carries the owned source location/index, current map anchor, and persisted book identity before its scale or generation mask. The server validates native MapStitch negotiation, nonempty matching identity, owned location, map anchor, living player state, and bounds. Empty identity, stale anchor, replaced slot, invalid scale/mask, and unsupported client requests are rejected.

Screens remain bound to their original inventory/accessory/pouch source and book identity. The client may refresh the map anchor after first-map generation or ejection within that same book. It does not adopt the identity of a different atlas that replaces the source slot; the controls disable until the appropriate book is reopened. A newly created or legacy book needs its first server tick and inventory synchronization before native controls can edit it. If opened before that synchronization, reopen the screen once the book is synchronized. Matching client/server suite builds are required for native controls. Standard map creation sends one vanilla cartography sound packet to the explorer per generation batch; a batch creating multiple scales still plays one chime. Sound playback remains subject to the player's sound settings.

Use [merged verification and testing steps](MERGED_TESTING.md) for runtime evidence. The optional original world-map buttons setting hides all these buttons together with the existing controls.
