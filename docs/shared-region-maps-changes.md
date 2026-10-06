# Shared Region Maps combo variant

## Source and provenance

- Original local repository path: `Minecraft/shared-region-maps/`. Its unchanged historical source is now archived as documented in [LOCAL_ARCHIVE.md](LOCAL_ARCHIVE.md).
- Historical original repository URL: https://github.com/THENATHE/shared-region-maps-shim (retained as provenance; the active source is in this suite).
- Original clean revision: `338270a70c14be19ba79b535b40fac980e414470`, version `1.0.3+mc26.3`.
- Exact clean source copy: `components/shared-region-maps/upstream/`.
- Per-file SHA-256 baseline: `components/shared-region-maps/upstream-manifest.json`.
- Modified source: `components/shared-region-maps/working/`.
- Declared component version: `1.0.5-combo.1+mc26.3`.

This component was written locally, rather than being a Pajic repository. Its existing All-Rights-Reserved declaration is retained. The task owner authorized combining it; no license for unrelated work has been inferred.

## Retained behavior

All ordinary vanilla creation, dimension/region/scale sharing, enrolled zooming, banner and terrain sharing, locking, legacy-map isolation, index corruption protection, saved-data format, and MapStitch integrations remain unchanged. MapStitch uses an atlas item itself; references to that supported MapStitch item remain intentionally present.

No new settings were introduced: the original component has no configuration entries. The suite's settings page therefore has no Shared Region Maps options to migrate or conceal.

## Explicit removals

1. Deleted `AtlasMapTypeMixin.java`, its mixin registration, and its conditional activation for `map_atlases`.
2. This also removes its Supplementaries antique-ink exclusion, which existed only inside the Map Atlases adapter. Vanilla safety checks are retained.
3. Removed the `fabric.mod.json` `breaks` entry for `improved-maps`. There was no Improved Maps implementation to copy or replace. Compatibility with external map-ID allocators is not claimed.
4. Removed the Interdimensional Map Markers presence check and red/blue-marker exception from `SharedMaps.ordinary`. Such custom decorations now receive the ordinary conservative special-map exclusion.
5. Removed Map Atlases reflection tests from the modified standalone QA fixture; pristine baseline tests remain in `upstream/`.
6. Replaced working documentation so it describes this variant's actual supported integrations.

## Bundle changes

The component keeps the `shared_region_maps` ID, Java package names, regional-key codec, `regions` SavedData identifier, and existing map-ID handling. Its metadata now allows both physical sides (`environment: "*"`). The common mixins operate through existing server-only methods taking `ServerLevel`; clients gain no independent map-ID allocation or custom synchronization. This also allows singleplayer/integrated-server map sharing in the bundle. Integrated-server runtime testing is listed separately from dedicated-server evidence.

The Gradle version was updated and an optional explicit compiler path was added for the installed Java 25 Gradle runtime / Java 27 compiler with `--release 25`. Dependencies remain Minecraft 26.3, Fabric Loader 0.19.5, and compile/QA Fabric API 0.161.0+26.3. No runtime library was added. The working build uses Loom 1.17.20, matching the suite; the original snapshot uses 1.17.21. The initial combo release had a manifest-only normalization built without recompiling its unchanged classes; that historical fixture evidence and both hashes remain in verification.json. The current suite has separate final-artifact verification linked below.

## Atlas center repair in suite 1.1.3

The MapStitch helper now validates an existing center against the authoritative saved map record instead of trusting any present center component. This corrects persisted stale values on servers without Polymer as well as with it. Map-ID-bearing entries include the distinct vanilla explorer/treasure map items; their centers can be repaired without replacing their item type, explored pixels, markers or IDs. Atlas item order, counts and selected entry remain intact. No saved-data format, regional index, map allocation or dependency changes are involved.

The separate mixed-scale addon owns `/repairmaps`, read-only `/repairmaps check` and `/dedupemaps`; the shared-map helper supplies center correction only. Duplicate cleanup compares exact IDs rather than merging different records for the same region. See [atlas controls](ATLAS_CONTROLS.md) and [mixed-scale source guide](MIXED_SCALES.md).

## Persistence and upgrades

Save identifier and index format are unchanged. Back up `<world>/dimensions/minecraft/overworld/data/shared_region_maps/regions.dat`, vanilla map records, and the map-ID counter together. This variant never scans or merges unknown existing maps. Preserving the index resumes sharing; removing the mod leaves existing vanilla IDs usable. Custom-marker maps previously accepted through the removed marker integration can now be conservatively rejected for future reuse/zoom; their stored pixels and IDs are not deleted.

## Bringing in a later upstream update

1. Copy the later clean local upstream revision into a new preserved baseline; record its revision, license, and per-file hashes.
2. Compare old `upstream/` to new upstream, then merge that change into `working/`.
3. Retain the five explicit removals above and the bundle's `environment: "*"` change.
4. Treat any changes to `Region.java`, `RegionIndex.java`, SavedData codecs, allocation, or lock behavior as persistence-sensitive. Check existing worlds on a copy.
5. Confirm the MapStitch adapter's target signatures against the exact MapStitch source bundled by the suite. Preserve authoritative validation for present/missing centers and all map-ID-bearing atlas entries; keep original selected-slot/order/count semantics.
6. Build, run the focused checks below, and record the new revision/artifact hashes.

## Manual checks

In a disposable world, create ordinary maps twice for the same region/scale (including two players) and compare their IDs; explore or add a named banner and verify both copies update. Zoom two enrolled maps to the same scale; lock one and verify it becomes independent. Restart and recreate the region to confirm the ID persists. Insert maps into MapStitch and verify rendering; cross a region edge with automatic exploration and verify map centers. Check singleplayer separately. Do not install another Shared Region Maps JAR alongside the suite.

## Verification

Focused build/static/dedicated-server evidence is recorded in `components/shared-region-maps/verification.json`. Historical upstream `VALIDATION.md` remains part of the pristine snapshot and is not proof of a fresh combo test. The modified working copy will contain a brief current validation document.

Current suite 1.1.3 acceptance is recorded independently in [validation](VALIDATION.md), including the no-Polymer stale-center reproduction, explorer-map cases and final repair/deduplication results. Earlier standalone results are historical evidence for their own artifacts.
