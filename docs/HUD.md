# Minimap placement and independent Tool Pouch information

Stable suite **1.1.1** on `main` shares one position between the atlas minimap and the ordinary filled-map minimap. Tool Pouch's compass/clock information has its own position, so the minimap can sit on the right while text stays on the left.

## Set the map on the right and text on the left

Open the suite settings through Mod Menu, `/suite-settings`, or **Open Suite Settings** if you have bound that key in Minecraft Controls.

1. Open **MapStitch Client Settings → Minimap → Position** and choose **Top Right**. You can instead use **Tool Pouch Client Settings → Minimap Overlay Settings → Position**; the two map controls synchronize when applied.
2. Open **Tool Pouch Client Settings → Info Overlay Settings → Position** and choose **Top Left**.
3. Apply your changes. Switching between an atlas and an ordinary filled map keeps the map corner; changing the map position does not move the text, and changing the text position does not move either map.

Choose other corners independently if preferred. Map size, background, opacity, zoom, effect-overlap controls and displayed information remain in their original sections. Tool Pouch's info X/Y offsets stay independent of map offsets.

## Saved settings and synchronization

Each original configuration file remains authoritative for its native fields. Map position and map X/Y offsets synchronize between MapStitch and Tool Pouch by saving matching values to the other original file. No separate settings store is introduced. `infoOverlaySettings.position`, `offsetX` and `offsetY` are not part of this synchronization.

When the two saved map positions or offsets differ on first startup, the suite adopts MapStitch's minimap position and X/Y offsets. The saved Tool Pouch information position remains untouched. Upgrading from 1.1.0 preserves the information corner that version saved; move it once through Info Overlay Settings to choose a different corner.

MapStitch permits negative offsets, while Tool Pouch's native offset fields only permit nonnegative values. Negative MapStitch offsets remain saved in MapStitch's original file and are applied to the ordinary map at render time; Tool Pouch's fields show zero for that unrepresentable portion. Choosing a new nonnegative offset from either original editor updates both map offsets normally. This preserves the original validators and signed atlas placement.

`HudLayoutConfigMixin` observes native Fzzy client Apply callbacks for the two original client configurations. During a config GUI preview, the renderer does not synchronize or save pending edits. A reentrancy guard prevents feedback while the matching original file is saved. Outside a GUI, new loaded values are observed as well, so reloads and subsequent sessions retain the saved placement.

## Rendering and overlap protection

`components/toolpouch-atlas-elytra/working/src/main/java/com/thenathe/toolpouchcompat/HudLayout.java` registers a single ordered map layer and the original Tool Pouch details after it. It first attempts the active atlas minimap, then falls back to the ordinary filled-map minimap if no atlas map is displayed. This prevents two minimaps occupying the same corner. Existing HUD layer identifiers remain registered so other integrations still have their anchors.

`AtlasHudLayoutMixin` and `PouchHudLayoutMixin` capture each renderer's actual translation, scale, background extent and map information text bounds. `PouchInfoLayoutMixin` uses those current-frame bounds for both map types and moves text and its background together. An inventory atlas receives the same spacing protection as an atlas in the pouch. Optional Raised offsets apply to either map renderer.

Opposite-side maps and details keep their original positions. Details on the same side also keep their chosen corner when their natural text bounds do not intersect the map: for example, a top-right map and bottom-right text can remain at opposite ends of that side. When an intersection occurs, details move below the full map bounds with a gap and their configured vertical padding; they move above instead if the below-map position would clip the screen bottom. Hidden maps, missing maps, missing player/world, debug/F1 hiding and world-map screens release the reservation. Extremely large maps or many detail lines can exceed the available viewport; use the existing size and offset controls in that case.

This integration targets Tool Pouch's own information overlay. An external replacement such as Immersive Overlays controls its own layout.

## Focused verification

The graphical `qa-hud` fixture exercises both minimap sources, map and information positions changed through the native configuration Apply callbacks, independent opposite-side placement, same-side collision handling, bottom corners, signed map offsets, saved original files and combined settings access. Execution results, exact artifact hashes, observations and screenshots are recorded in the release validation report. This guide describes expected behavior; it does not itself claim a completed run.

Earlier implementations and their historical graphical results remain documented in [hud-layout-fix.md](hud-layout-fix.md). The independent info settings and synchronized map settings above describe the current stable release.

## Updating upstream

Retain the four client mixins and `HudLayout` when importing addon changes. Recheck original MapStitch and Tool Pouch render signatures, GUI pose methods, native Fzzy `Config.onUpdateClient`, matching corner enum names, both native map-offset validators and HUD layer identifiers. Keep the information position out of map synchronization. Verify that same-side separation only applies when natural text bounds intersect the rendered map. Run the bounded atlas/ordinary-map graphical fixture against the exact resulting release; compilation alone cannot establish visual spacing.
