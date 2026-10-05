# Mixed-scale atlas branch

On the current `merged` branch, both experiments are included in suite **1.0.2-merged.1+26.3**. Coordinator is **1.0.7-merged.1+26.3**; the map addon is **1.0.1-merged.1+26.3** with its matching dependency pin. See `docs/MERGED_TESTING.md` for combined acceptance. Individual-branch versions and evidence below are retained history.


Suite version: `1.0.2-multiscale.1+26.3`. Branch: `feat/mapstitch-mixed-scales`. Separate addon: `mapstitch_mixed_scales`, version `1.0.0+26.3`. Coordinator variant: `1.0.7-multiscale.1+26.3`.

This branch adds one nested addon under `components/mapstitch-mixed-scales/working/`. It uses the original developer MapStitch Fabric `1.1.6+26.3` artifact, SHA-256 `e1b768bbd1ae06f83305eeba3ab19bfe4eb99bf21de12d57364d04ce9a1cde81`, without rewriting that JAR or its preserved sources. There is no separate ported MapStitch counterpart for this feature. The paused SSO-port track is not built or tested. Other exact library inputs, including Defaulted dropfix, stay pinned by `locks/artifacts.json`.

## Player behavior

- Insert maps at any of the five scales into the same atlas using either existing inventory insertion direction. Existing capacity, blank-map/paper admission, Globetrotter, ejection and item-stack rules remain in effect.
- The world-map screen displays the scale selected by its existing button or scale keys. Changing that selector also persists the active exploration/minimap scale on the atlas that opened the screen. Opening through the global world-map key uses the minimap's selected atlas, or the first configured world-map atlas when no minimap atlas is available. Other carried books keep their own active scale.
- Opening a book uses its recorded active scale. Its tooltip labels this as **Active scale** and states that maps of all five scales can be stored. The scale button explains that it also selects exploration/minimap scale.
- The minimap uses a map from that active scale covering the player's current position and dimension. If that layer has no covering map, normal atlas ticking can consume one existing blank and create/reuse the regional map at that scale. Another scale's wider coverage does not suppress this. With no blank, the selected layer has no active map; the addon does not silently choose another scale.
- Maps keep their original IDs, scales, centers, contents and item components. Existing independent/locked maps are not imported into the shared regional index. Shared creation still reuses the same dimension/region/scale record. A scale change does not zoom existing maps, reveal distant regions or combine records.
- Stored blanks present before enchanting an atlas remain usable, matching upstream behavior. New paper/blank insertion still follows MapStitch's original Globetrotter rules. Generation replaces exactly one blank item, retains other stack counts/order and the selected bundle entry, and preserves total item count.
- Atlases held in the active Tool Pouch use the existing atlas bridge and its saveback rules. Lookup binds the source location in the established order: configured accessory, active pouch, configured main hand, then configured inventory slots. Identical copies in other locations do not redirect scale changes. Targeted scale packets address a player-owned location and carry a map-ID anchor to reject stale replacement targets. Each selection refreshes that anchor from the same bound location, so ejecting the original anchor map or generating the first map does not break an already-open screen. The addon does not mutate every atlas scanned by the world-map screen.

MapStitch already indexes its world-map tiles by position **and scale**, and its renderer already filters by the selected scale. The addon retains that rendering path. It clears the upstream map/decoration cache when the scale changes, preventing markers from the previous layer from appearing on a new grid. Existing exploration-marker projection and per-map ejection remain upstream behavior. Multiple independent maps with identical dimension/center/scale remain separate stored items; the upstream screen still displays one tile at each grid key.

## Native negotiation and persistence

MapStitch's existing negotiated fingerprint now includes this addon, the atlas/Elytra integration and Shared Region Maps. The fixed ten-module negotiation layout is unchanged. An untouched MapStitch client's original advertised channels no longer qualify it for native MapStitch on this branch; matching addon support is required. With server Polymer, that client receives the existing safe fallback. Vanilla clients also retain fallback. Native operation without Polymer requires the matching suite and dependencies on both sides.

The addon registers one serverbound selection payload, `mapstitch_mixed_scales:select_scale_v1`. Clients send it only after native MapStitch negotiation and when the server advertises its receiver. The server validates negotiation, player state, scale bounds and the owned atlas target before applying it. It sends no new custom clientbound state. Scale changes use the existing synchronized/persistent `mapstitch:atlas_scale` component; map IDs, bundle contents, the existing active-map component and the shared regional index retain their existing formats. There is no migration, map rescan, new world index or custom persistent identity.

Back up the world and inventories before trying this independent feature branch. Returning to the baseline suite preserves stored records but restores its original same-scale insertion and selection rules; use the feature branch to separate mixed-scale contents first if that baseline behavior is required.

## Maintenance targets

| Source | Responsibility |
| --- | --- |
| `MixedScaleMaps` | Scale-specific active-map selection, exact coverage bounds and conservation-preserving blank replacement |
| `AtlasTarget` | Inventory/accessory/pouch target lookup and saveback |
| `MixedScales` | Validated serverbound selection payload |
| `MixedScalesClient` | Bind the screen to its opened/selected atlas and send negotiated selections |
| `AtlasMixedScalesMixin` | Permit all scales at insertion; select before upstream map ticking; replace the single-scale `updateActiveMap` policy; tooltip |
| `AtlasUseClientMixin` | Remember which inventory book initiated the screen |
| `WorldMapScaleMixin` | Observe original scale controls, retain the selected atlas scale and clear old-layer decorations |
| `SuiteCapabilities.dependencies/classify` | Require addon support for the MapStitch native decision |

For a later MapStitch update, compare its exact published source against the retained baseline. Recheck `AtlasItem.isValidItemForAtlas`, `inventoryTick`, `updateActiveMap`, `getTooltip` and `use`, plus the world-map constructor, input handlers, render callback, scale field and scale-button tooltip call. Check upstream grid keys, minimap active-ID behavior and Tool Pouch bridge signatures before adjusting these small adapters. Update the pinned artifact/version and addon version, then rerun actual insertion, every selected scale, generation boundaries, ejection, pouch saveback, codec/restart persistence and native/fallback negotiation checks. Do not merge upstream classes into this addon or replace library dependencies to pass a build.

## Build and evidence

From the suite root, stage the exact inputs using `tools/stage-inputs.py`, then run:

```sh
JAVA_HOME=/usr/lib/jvm/java-25-openjdk ./gradlew --offline build -PcompilerVersion=27 -Pjavac=/usr/lib/jvm/java-27-openjdk/bin/javac --console=plain
python3 tools/verify-bundle.py
```

The archive verifier checks all 14 nested mod IDs, declared component versions, unchanged upstream input hashes and the exact Defaulted requirement. Build/archive checks establish compilation and packaging only. Focused actual-client/server and persistence evidence is maintained separately under `qa-multiscale/`; inherited 1.0.1 validation records are baseline history, not proof of this feature branch.

The frozen candidate `0c590629dd4ba3f3a4cd21f0705430775799e8e096211419db7921910b185ddd` passed the focused dedicated-server run `qa-multiscale/runs/final-source-priority-0c590629/result.json`: 56 initial assertions and 3 restart assertions in each of the Polymer and no-Polymer profiles (118 total). This includes an identical inventory/pouch atlas regression verifying that the pouch remains the selected source and only its scale changes. Actual client controls and network profiles are recorded separately by the release QA.
