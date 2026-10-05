# Release validation

## Shared Region Maps 1.0.3+mc26.3

Validated on 2026-09-30 using the packaged dedicated-server JAR, Minecraft 26.3, Fabric Loader 0.19.5, and Java 25.

```text
9590cb9e4e3e73f1edc226b13055ce27f92524af14263d33f71b0022597c8453  shared-region-maps-1.0.3+mc26.3.jar
```

| Configuration | Passing assertions |
| --- | ---: |
| Standalone first launch / restart | 605 / 615 |
| Upgrade from a copy of a 1.0.2 test world | 615 |
| Map Atlases stack, first launch / restart | 621 / 631 |
| Map Atlases stack + Polymer, first launch / restart | 621 / 631 |
| Corrupt index bytes / partially decodable index | 4 / 4 |
| Interdimensional Map Markers present / absent control | 142 / 31 |
| MapStitch metadata, four configurations | 1,623 each |
| MapStitch sharing, four configurations | 579 each |

**Total: 13,328 assertions.** Successful server runs completed normal saves and exited with code 0. Separate startup/save checks passed without Fabric API or either atlas mod; Fabric also correctly rejected the competing Improved Maps allocator with all dependencies present.

## Tested integrations

The Atlas stack used Map Atlases custom port 26.3-6.7.3-port.5, Moonlight 26.3-4.0.8-port.3, CodecUI 26.3-1.4.3-port.1, Accurate Maps 1.1.0-port.2+26.3, and Fabric API 0.161.0+26.3. Polymer checks added Polymer Bundled 0.18.2+26.3. Marker tests used Interdimensional Map Markers 1.0.5.

MapStitch checks covered original 1.1.6+26.3 and Toolpouch-patch.1, each with and without Polymer and MapStitch Polymer Compatibility 1.0.1+26.3. The patched + Polymer sharing run also loaded the Atlas stack, exercising MapStitch sharing while both atlas adapters were active. Map Atlases behavior was separately exercised by its own fixture.

Other MapStitch dependencies were Fzzy Config 0.7.7+fix2+26.3 and Fabric Language Kotlin 1.14.1+kotlin.2.4.20. Tested dependency JARs retained their hashes.

## Coverage

- Grid alignment and boundaries, negative coordinates, all five scales, all three built-in dimensions, and allocation without discarded IDs.
- Real empty-map creation, cartography pickup and shift-click, crafting postprocessing, shared zooming, and independent locked snapshots.
- Terrain sampling, named banners, vanilla update packets, saved IDs/pixels/banner persistence, and upgrading an existing index.
- Legacy, custom-data, target-decorated, unknown-allocator, unsupported, missing, locked, and corrupted-record protections.
- MapStitch automatic atlas generation for two simulated players, both creation orders, insertion/ejection, shared pixels, independent-map scaling, and map-center refresh/repair.
- Preservation of duplicate order, counts, names, decorations, selection, IDs and pixel data when repairing missing centers.
- Real interdimensional marker packets, Nether/End coordinate projection, canonical reuse/zoom, and negative controls for unsupported or spoofed decorations.
- Archive integrity, production metadata/version, all mixin classes present, removal of prior branding from decompressed artifact bytes, and exclusion of QA classes.

## Limits and reproducibility

These are real dedicated-server engine calls with simulated players. This release did not repeat graphical-client tests, Tool Pouch gameplay, or every optional mod integration. Supplementaries-specific content and unsupported special maps remain excluded. Known nonfatal diagnostics from the tested Atlas stack include absent optional Twilight Forest targets and empty Moonlight extension registries.

`src/qa` contains the standalone acceptance fixture; build it with `bash ./gradlew qaJar`. The production JAR excludes that source set. `qa.gradle` supports an explicitly supplied existing accepted EULA file and a disposable server run. External integration harnesses, raw server worlds, account/session data, and machine-specific logs are retained locally and are not included in this repository; the full cross-mod matrix is a recorded acceptance result, not a bundled one-command test suite.
