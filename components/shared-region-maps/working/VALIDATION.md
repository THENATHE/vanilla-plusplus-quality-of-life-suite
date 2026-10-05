# Combo component validation — 2026-10-04

Version **1.0.4-combo.1+mc26.3** built successfully using cached dependencies, Java 25 runtime, and Java 27 compiler targeting Java 25.

- JAR integrity, declared mod ID/version/environment, configured mixins, and removed-integration source scans passed.
- `Region.java`, `RegionIndex.java`, and `MapstitchMaps.java` match the preserved baseline byte for byte.
- One disposable dedicated-server startup passed **605 assertions**. One restart of that same world passed **615 assertions**, including persistent regional IDs and map records.
- Runtime fixture covered ordinary creation, all scales/dimensions, grid alignment, shared terrain/banners, zoom/cartography, locked copies, legacy/custom isolation, and damaged-record protections.

Exact JAR hash and results are in `../verification.json`. The runtime fixture loaded no MapStitch: fresh MapStitch behavior and the overall combined mod must be assessed separately. Singleplayer/integrated-server and graphical clients were not exercised in these focused checks. Pristine historical source/validation is under `../upstream/` and does not count as a fresh combo test.
