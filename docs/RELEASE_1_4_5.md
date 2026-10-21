# Vanilla++ 1.4.5+26.3 - atlas and stack-limit allocation fixes

This update addresses two interacting allocation hotspots observed in the supplied Spark recordings. It targets Minecraft 26.3, Java 25, Fabric Loader 0.19.5, and the same locked libraries and original developer mods as 1.4.4. The exact requirements remain in [DEPENDENCIES.md](DEPENDENCIES.md).

## Changes

The client atlas bridge now materializes pouch atlases once per player/world, active pouch object, immutable container component and live Stackables override-table identity. Supported stack-limit updates replace that table and invalidate validation results immediately. The optional public snapshot API is resolved once through a method handle, calling the API without per-lookup reflective invocation. Without Stackables the table key stays stable; older optional Stackables versions lacking the public snapshot API use uncached materialization. It resolves the original active-pouch policy on every lookup, so configuration, inventory priority and accessory changes remain effective immediately. Each caller receives detached ItemStack copies; mutating a result cannot alter the stored pouch or the cached snapshot. The open-pouch-menu guard remains. Authoritative server ticking, saving and ejection continue to use uncached stacks.

The Sensible Stackables compatibility getter now checks one component directly through the public component-patch API. A private sentinel prototype distinguishes an absent entry from an explicit removal without allocating added/removed maps or sets. The sentinel is kept as an erased Object and cannot escape as a component value. Explicit additions/removals, live override-table replacement/clear, item prototypes and strict validation retain their existing behavior.

The suite advances to **1.4.5+26.3**, Tool Pouch Atlas/Elytra compatibility to **1.0.12+26.3**, and Sensible Stackables compatibility to **1.0.3+26.3**. All seventeen other nested modules and all fifty-six root Java classes match 1.4.4 byte for byte. Original developer JARs and library locks remain unchanged. No new dependency, setting, identifier or data migration is introduced. The SSO port remains paused; original developer SSO 2.10.0 remains in the tested suite.

## Evidence and performance scope

[The original allocation profile](https://spark.lucko.me/ZMTDDkGijz) attributes about 47.4 percent of sampled allocated bytes to AtlasBridge.atlases and about 47.3 percent to component-patch splitting beneath its stack validation. The [new user capture](https://spark.lucko.me/c0brPnfWy3) still invokes the minimap and atlas bridge but does not sample that expensive chain; changed settings/inventory/execution conditions are not established. These user captures are not a controlled before/after code-fix comparison. [Recorded comparisons](../qa-performance/evidence/1.4.5/spark-comparison.json) distinguish allocation bytes from the earlier execution-time samples.

Controlled regression measurements isolate each optimization. The transformed stack getter performs 200,000 nonempty-patch lookups against the previous split expression. The client fixture performs 400 lookups of a 1,024-map atlas against the previous uncached lookup expression, with the optimized stack getter present on both paths. These are narrow allocation measurements, not predicted FPS gains or a claim that all garbage collection pauses disappear.

Current candidate-specific acceptance and exact hashes are in [verification.json](../qa-performance/evidence/1.4.5/verification.json). It links Stackables conservation/restarts, default/uncapped scoped native/fallback packet codecs, client cache behavior/allocation, current wheel integration, actual HUD rendering, connected client coverage, source export and installation acceptance. Historical broader gameplay evidence keeps its own original version/hash.

The atlas fixture validates source/container replacement, added/removed/reordered mixed-scale atlases, detached mutation, policy/config changes, live atlas/nested-map stack-limit changes without replacing pouch contents, open menus and separate cache instances. Its level-key check uses live-level/null/live-level identity transitions; this is not an actual dimension-travel or reconnect claim.

## Installation and source

Replace the previous suite JAR with `vanilla-plusplus-quality-of-life-suite-1.4.5+26.3.jar` on the participating clients/server. Install one suite version at a time and keep the required publisher libraries. [The installation ZIP](INSTALLATION_PACK.md) includes permitted dependencies and a side-aware installer; Fzzy Config remains an official manifest download.

Source: `Minecraft/thenathe-mod-suite/`, main branch of [the suite repository](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite). Local release files: `Builds/Minecraft/Vanilla++ Quality of Life Suite/Main Plugin/1.4.5+26.3/`. The requested main-branch push is separate from creating a public GitHub release. Prior versions, tags and evidence remain available.

## Final acceptance

Final suite SHA-256: `2b8da007d555b55512c1e8fb08ce5db3be419bb5f3a47d61a1e13d577f67b97b`.

| Fresh check | Result |
| --- | --- |
| Stackables behavior, conservation and restart | 519 assertions across six native/Polymer runs |
| Default and uncapped scoped packet codecs | 28,156 assertions across four profiles |
| Current wheel, BuildPrint, Blocktone, BRBR and atlas-cache integration | 762 native plus 762 Polymer checks |
| Actual HUD rendering/settings | 30 native plus 30 Polymer scenes |
| Native, Fabric-only and unmodified vanilla connection smoke | Four profiles, including native server without Polymer |
| Disposable client/server installation and archive safety | 13 checks, including byte-identical installation ZIP rebuild |
| Independent source export | All 130 packaged file entries identical; ZIP directory records differ |

Across all six Stackables runs, the 200,000-lookup measurement recorded zero bytes for the optimized getter versus approximately 664-738 bytes per legacy lookup. The atlas benchmark recorded 502,400 to 248,896 bytes in the native profile (50.46 percent less), and 550,400 to 264,912 bytes with Polymer (51.87 percent less), per 400 lookups. These atlas comparisons retain the optimized stack getter on both paths.

Native integration completed in one process. Polymer acceptance combines three completed fixture reports with four clean isolated fixture sessions. Two batched attempts deadlocked in Fabric GameTest world-teardown coordination; their thread dumps and reports are preserved separately and those interrupted processes are not counted as full passes. All final scoped assertions, inputs and product bytes remain identified in the integration record.

Only the supported suite configuration is runtime-tested here. The optional standalone behavior without Stackables or with older versions lacking the snapshot API was reviewed in source, and is not presented as separate runtime acceptance. Launcher GUI import was not separately tested. Broader historical gameplay records retain their original versions and hashes.
