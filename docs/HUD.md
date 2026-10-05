# Shared atlas and Tool Pouch HUD layout

Stable suite **1.1** on `main` uses one corner for the atlas minimap, ordinary filled-map minimap, and pouch compass/clock details. Changing **MapStitch Client Settings → Minimap → Position**, **Tool Pouch Client Settings → Minimap Overlay Settings → Position**, or **Tool Pouch Client Settings → Info Overlay Settings → Position** adopts that corner in all three controls when applied. Each original configuration file remains authoritative for its own native fields; synchronization saves the matching values to the other original file rather than introducing a separate settings store.

When existing corners differ on first startup, the suite adopts the Tool Pouch information-overlay corner. This preserves the familiar pouch HUD location when switching from a filled map to an atlas. Both map renderers share their map X/Y offsets. Their existing size, background, opacity, zoom, information content, and effect-overlap controls remain available. Tool Pouch detail offsets remain independent padding; vertical padding is applied between the map and the details.

MapStitch permits negative offsets, while Tool Pouch's native offset fields only permit nonnegative values. Negative MapStitch offsets remain saved in MapStitch's original file and are applied to the ordinary map at render time; Tool Pouch's fields show zero for that unrepresentable portion. Choosing a new nonnegative offset from either original editor updates both map offsets normally. This preserves the original validators and signed atlas placement.

## Rendering

`components/toolpouch-atlas-elytra/working/src/main/java/com/thenathe/toolpouchcompat/HudLayout.java` registers a single ordered map layer and the original Tool Pouch details after it. It first attempts the active atlas minimap, then falls back to the ordinary filled-map minimap if no atlas map is displayed. This prevents two minimaps occupying the same corner. Existing HUD layer identifiers remain registered so other integrations still have their anchors.

`AtlasHudLayoutMixin` and `PouchHudLayoutMixin` capture each renderer's actual translation, scale, background extent, and map information text bounds. `PouchInfoLayoutMixin` uses those current-frame bounds for both map types and moves text and its background together. The earlier physical-atlas-in-pouch restriction has been removed, so an inventory atlas cannot cover details supplied by the pouch. Optional Raised offsets apply to either map renderer.

Details appear below the entire map with a gap and configured vertical padding. For a bottom-corner map, the block moves above it if it would otherwise clip the screen bottom. Hidden maps, missing maps, missing player/world, debug/F1 hiding, and world-map screens release the reservation. Extremely large maps or many detail lines can exceed the available viewport; the existing size controls remain available.

`HudLayoutConfigMixin` observes native Fzzy client Apply callbacks for the two original client configurations. During a config GUI preview, the renderer does not synchronize or save pending edits. A reentrancy guard prevents feedback while the matching original file is saved. Outside a GUI, new loaded values are observed as well, so reloads and subsequent sessions retain the shared placement.

## Focused checks

The current `qa-hud` fixture contains eight cases: atlas left, atlas right selected through MapStitch, atlas left selected through Tool Pouch's minimap control, ordinary map at the same top-left anchor, atlas bottom-left selected through the pouch details control, ordinary map at the same bottom-left anchor, preservation of signed atlas offsets on the ordinary map, and the combined settings screen. It observes final text coordinates, checks nonoverlap against actual map bounds, verifies all three position controls, verifies saved original files, and preserves screenshots. Execution results are recorded in the release validation report; this document does not itself claim a completed run.

The earlier independent-position implementation and its historical results remain documented in [hud-layout-fix.md](hud-layout-fix.md). The synchronized behavior above supersedes that earlier presentation preference in the stable suite.

## Updating upstream

Retain these four client mixins and `HudLayout` when importing addon changes. Recheck the original MapStitch and Tool Pouch render signatures, GUI pose methods, native Fzzy `Config.onUpdateClient`, the four matching corner enum names, both native offset validators, and HUD layer identifiers. Run the bounded atlas/ordinary-map graphical fixture against the exact resulting release; compilation alone cannot establish visual spacing.
