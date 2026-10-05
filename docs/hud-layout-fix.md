# MapStitch / Tool Pouch HUD layout fix

## Component and baseline

This change belongs to `components/toolpouch-atlas-elytra/working/`, declared addon version **1.0.5-suite.1+26.3**. The untouched local `Minecraft/toolpouch-atlas-elytra-compat-26.3` source at revision `25dfaf8bff9f51d8cfa29be9ca34c71a29cac2c0` is preserved as `components/toolpouch-atlas-elytra/upstream/`. It originally declared **1.0.4+26.3** and MIT. Its full license, notices, existing integrations, and separate compiler-input hashes remain available. The addon retains the existing ID `toolpouch_atlas_elytra_compat`.

## Cause

Tool Pouch `me.pajic.toolpouch.hud.InfoOverlays.renderLines` reserves space for its own minimap only. It does not know the size or location of MapStitch's `me.pajic.mapstitch.minimap.MinimapOverlay`. An atlas supplied through the pouch therefore causes MapStitch's map and the pouch's compass/clock details to occupy the same area.

## Change

- `HudLayout.java` holds current-frame bounds only. On client startup, after every client entrypoint has registered its layers, it replaces the original `toolpouch:info_overlay` callback with an empty callback and registers the same unmodified `InfoOverlays.render` after `mapstitch:minimap`. The original placeholder identifier is retained. No duplicate details are drawn.
- `AtlasHudLayoutMixin` resets bounds at every MapStitch render call. It records the real pose translation and scale, actual background bounds, and the extents of every information line MapStitch actually draws. Map geometry is not estimated from configuration defaults or a fixed information-row count.
- `PouchInfoLayoutMixin` shifts both the original pouch text and its background together. It preserves original horizontal alignment, colors, shadow, field contents, ordering, configured offsets, optional Raised offsets, and localization.
- Shifting applies only if an atlas is in the active pouch, MapStitch actually rendered its minimap that frame, and both map and details occupy the same horizontal side. Opposite sides retain their ordinary coordinates. Hidden/toggled-off maps, GUI hiding, F3, missing atlas, and missing world/player clear or bypass the layout reservation.
- Details appear below the full minimap/background/information extent with a small gap. If the whole details block would extend past the screen bottom, it is placed above the minimap instead. Extremely large minimaps or many detail lines may leave insufficient space on either side; reduce map size or move the overlays in that case.

## Settings retained independently

MapStitch client config still owns `minimap.position`, `size`, `xOffset`, `yOffset`, `background`, `preventEffectOverlap`, and all `minimapInfo` fields. Tool Pouch client config still owns `infoOverlaySettings.position`, `offsetX`, `offsetY`, field presentation, text background, opacity, and shadow. Tool Pouch's own minimap controls are unchanged. Existing server restrictions remain authoritative. No new settings, library, or replaced configuration store is introduced. MiscTweaks remains unchanged; its hotbar movement is unrelated to these overlays.

## Other addon functions preserved

Elytra toggle/preferences, atlas lookup and insertion/ejection support, map-center cache repair, pouch capacities and tier handling, XP/mending behavior, quota checks, ClientSort compatibility and optional acceleration, payloads, and all prior feature gates retain their source. Only the client startup handler additionally registers the HUD layout callback. If MapStitch is absent, the new mixins and callback are disabled, preserving standalone pouch/elytra behavior.

## Build and provenance

`components/toolpouch-atlas-elytra/upstream-manifest.json` records the pristine snapshot's hashes. `verification.json` records the exact JAR, compiler dependencies, Minecraft/loader/API and checks. Build with Java 25 Gradle runtime and Java 27 compiler targeting Java 25:

```sh
cd components/toolpouch-atlas-elytra/working
JAVA_HOME=/usr/lib/jvm/java-25-openjdk bash ./gradlew build --offline -Pjavac=/usr/lib/jvm/java-27-openjdk/bin/javac
```

`libs/` are compile-only original input JARs. They are not packed into this addon; the suite bundles the relevant components separately. ClientSort remains optional. No additional dependencies were introduced.

## Focused verification and remaining manual checks

Compilation, archive integrity, mixin inventory, and target signatures against MapStitch 1.1.6 / Tool Pouch 1.1.10 passed. Graphical suite testing is recorded separately; these static checks alone do not establish visual correctness.

In a disposable world, put an atlas, compass, and clock in the selected pouch. Set map and details both top-left, then both top-right; verify all detail text/background is below the map and its own enabled information rows. Change size, GUI scale, offsets, background and map information fields; verify spacing adapts. Move details to the opposite side and confirm their original placement. Toggle the minimap off, remove the atlas, press F1/F3, switch worlds, and re-enable; verify space is released immediately. Check bottom corners: use below when there is room and above when the screen bottom would clip the block. Verify pouch clocks/compass content and independent position controls remain usable. Confirm Elytra and ordinary ClientSort still work in the combined distribution.

## Updating upstream

Keep the pristine source separate, bring new upstream changes into the working addon, and retain these two client mixins and the startup registration. Recheck `MinimapOverlay.render`, `InfoOverlays.renderLines/renderLine`, both HUD layer identifiers, and pose/text method signatures before building. A changed upstream renderer can invalidate captured bounds even when compilation passes; repeat the corner/size/toggle smoke after updates.

## Completed graphical smoke — 2026-10-04

The exact suite candidate `ee06bdf23509e0a2f0c619b72126b8d875c42f2d77dba84fc6d89bb799791936` passed ten cases in one disposable localhost world and a real native graphical suite client, without client Polymer. Its nested addon hash matches `verification.json`. Final renderer text arguments and screenshots confirm both top corners put text below the minimap; opposite sides retain the original top offset; a larger map with three enabled map information lines increases the reservation; toggling or removing the atlas immediately restores the ordinary position; restoring it reserves space again; bottom-corner text sits above the map when needed. The combined Fzzy settings screen opened and its six original module config keys were present.

Example GUI-coordinate measurements: minimap bottom 110.6 → details top 115; larger map/information bottom 209.8 → details top 214; hidden/opposite-side details top 4; bottom map top 339.8 → details bottom 335. All six original pouch information lines remained present. Reviewed images confirm text/background appear together and do not overlap the map.

Evidence: `qa-hud/evidence/2026-10-04/`, with exact launch/dependency hashes, observations, ten screenshots and visual-review provenance. This smoke used a 1280×900 window at GUI scale 2. It does not claim configuration edit/sync, full gameplay, or an exhaustive resolution matrix. Fixture construction and whitelist issues were corrected before this successful run; the first-launch suite settings directory failure was fixed separately by the settings component owner.
