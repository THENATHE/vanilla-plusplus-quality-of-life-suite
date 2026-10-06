# Release 1.1.2

Vanilla++ Quality of Life Suite now includes Bannerpoint on `main`, alongside the existing gameplay modules, automatic client negotiation and Polymer compatibility. This release also lets an atlas mark a banner across all enabled map-generation scales at once. The original Bannerpoint and MapStitch developer JARs remain unchanged; the additions live in separate suite modules.

## Banner waypoints for native and vanilla clients

Bannerpoint puts eligible banners on Minecraft’s locator bar. Named banners and banners marked on maps transmit according to the original server settings and range.

- Clients with Bannerpoint installed use its normal colored icons and custom names. Matching suite clients already include Bannerpoint; untouched original Bannerpoint clients are detected through its original advertised channel.
- Vanilla or Fabric clients without Bannerpoint can receive its icons through Polymer’s generated server resource pack. The server verifies the pack includes the original artwork and waits for successful loading before sending banner waypoints.
- Pending, declined, failed, removed or outdated packs leave those banner waypoints hidden. Ordinary player waypoints remain unchanged.
- Custom banner-name labels require Bannerpoint’s client code; resource packs provide the icon artwork.

Open **Bannerpoint Gameplay Settings** and **Bannerpoint Client Settings** in the combined suite hub for the original options. The native name display uses the original player-list key or sneaking while looking toward the waypoint. See [Bannerpoint support](BANNERPOINT.md) for the complete client matrix and pack checks.

## Mark a banner across enabled atlas scales

Use an atlas on a banner. The operation finds existing maps in the atlas that cover the banner in the current dimension, restricted to the atlas’s enabled **1/2/4/8/16 generation toggles**. The world-map **S** view and minimap **M** selection do not limit marking.

If every eligible map already has the same banner mark, the operation removes it from all those maps. Otherwise it adds or updates the mark across the eligible maps together. Disabled generation scales, other dimensions and maps that do not cover the location remain unchanged. This interaction does not create maps or consume empty maps/paper. Renaming or recoloring a banner updates its mark instead of treating the old mark as a removal. If an eligible map cannot accept the decoration because the banner is at its outermost pixels or its marker limit is reached, the operation leaves the whole eligible set unchanged rather than applying only some scales.

Bannerpoint’s map-linked transmission is reconciled after the operation. A banner remains linked if a disabled scale in the same atlas still carries its mark. This behavior is scoped to the interacted atlas; unrelated books do not gain a new global reference counter. See [mixed-scale implementation](MIXED_SCALES.md) for update boundaries.

## Installation and assets

| Requirement | Exact target |
| --- | --- |
| Suite | `1.1.2+26.3`, stable `main` |
| Minecraft | `26.3` |
| Fabric Loader | `0.19.5` |
| Java | `25` or newer |
| Original Bannerpoint | `1.1.2+26.3`, already nested |
| Bannerpoint compatibility | `1.0.1+26.3`, already nested |
| Mixed-scale atlas addon | `1.1.2+26.3`, already nested |
| Defaulted | Exact `1.3.8+26.3.dropfix.1`, unchanged |
| Polymer for vanilla-client support | Full Polymer Bundled `0.18.2+26.3` on the server |

Use [release downloads](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/releases/tag/v1.1.2%2B26.3) and [installation instructions](INSTALLATION_PACK.md). Replace the previous suite JAR instead of installing both. Do not separately install Bannerpoint, MapStitch or their already included suite addons. Keep worlds and saved configurations. Matching suite clients retain native features; original-only Bannerpoint clients still need Polymer for the other suite systems whose client mods they lack.

The release contains the standard four uploaded files:

- `README.md`
- `vanilla-plusplus-quality-of-life-suite-1.1.2+26.3.jar`
- `docs.zip`
- `vanilla-plusplus-installation-pack-1.1.2+26.3.zip`

The installation ZIP contains the suite, permitted dependencies, notices and client/server installer. It downloads Fzzy Config directly from its official publisher, so fresh installation requires internet access. Generate and host an updated Polymer server pack for players without Bannerpoint, serving the same verified bytes and hash. The suite’s existing dependencies and exact Defaulted dropfix remain unchanged.

## Verification and your testing steps

[Validation](VALIDATION.md) records the final tested hash, completed checks and historical limits. Earlier Bannerpoint testing-release records remain historical and are not relabeled as fresh stable-release checks. The separate ChatGPT SSO port track remains paused.

1. Put covering maps at several scales and an empty map/paper in an atlas. Enable the intended generation scales, then use the atlas on a named colored banner. Check its mark at each enabled scale and confirm blanks/paper were not spent.
2. Click again and check removal from those enabled layers. Disable one marked layer and repeat; that disabled layer stays unchanged, and Bannerpoint remains map-linked while it still contains the mark.
3. Include another dimension’s maps and a same-dimension map that does not cover the banner. Confirm neither is changed. Cycle the world-map view and minimap scale; these choices should not change the marking layers.
4. Join without Bannerpoint and accept the updated Polymer pack. Check the locator-bar icon after successful loading. Reconnect and decline the pack, or test a failed/removed pack; banner icons should be absent while ordinary player waypoints remain available.
5. Open both Bannerpoint settings categories, check original values and permissions, and verify banner persistence after a server restart.

Existing independent minimap/information placement, mixed-dimension atlas filtering, death retention and `/extractmap` commands remain available. See [atlas controls](ATLAS_CONTROLS.md), [HUD settings](HUD.md), [Bannerpoint support](BANNERPOINT.md) and [source/update guide](UPDATING.md).
