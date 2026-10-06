# Source map and updating upstream modules

The active stable source is at `Minecraft/thenathe-mod-suite/`, branch `main`, suite **1.1.5+26.3**. The earlier Bannerpoint and atlas/stackables testing releases remain historical references. Stable release files belong under `Builds/Minecraft/Vanilla++ Quality of Life Suite/Main Plugin/1.1.5+26.3/`; preserve earlier testing-component release folders rather than relabeling their artifacts. The public repository and outer artifact use `vanilla-plusplus-quality-of-life-suite`. The internal suite mod ID and protocol/config namespace remain `thenathe_mod_suite`; branding does not rename original mod IDs or saved configuration keys.

## Where things live

| Path | Purpose |
| --- | --- |
| src/main/java/com/thenathe/suite/Suite.java | Root common entrypoint |
| src/main/java/com/thenathe/suite/network/ | Shared capability protocol; compiled into combined-compat only |
| src/main/java/com/thenathe/suite/client/ | Unified Fzzy settings screen, Chalk settings bridge, client command and Mod Menu integration |
| src/main/resources/ | Root identity, nested feature JARs, client mixin registration and always-enabled settings translation pack |
| components/sso, mapstitch, toolpouch, tiered-backpacks, misctweaks, simple-death-improvements | Immutable published source/binary records plus upstream repository snapshots and staging scripts |
| components/shared-region-maps/upstream | Original 1.0.3 source baseline |
| components/shared-region-maps/working | Vanilla/MapStitch-only modified module |
| components/toolpouch-atlas-elytra/upstream | Original local addon 1.0.4 source baseline |
| components/toolpouch-atlas-elytra/working | Preserved addon features plus synchronized minimaps and independent details; HudLayout and four client mixins capture map bounds, move details, order rendering and observe native config Apply. |
| components/bannerpoint/ | Unchanged official Bannerpoint 1.1.2+26.3, published sources, repository/reference records and exact staged-input lock |
| components/bannerpoint-compat/ | Separate native-channel detection, Polymer waypoint-asset contribution and per-player banner-waypoint gating; original Bannerpoint JAR remains unchanged |
| components/chalk/upstream | Exact local Chalk Fabric 26.3 port source snapshot |
| components/chalk-colorful/upstream | Original colorful addon and its metadata-only 26.3 port source |
| components/amethyst-curse-cleanser/upstream | Existing 26.3 curse-removal source snapshot |
| components/codecui-reference/, defaulted-reference/, fzzy-config-reference/ | Pinned publisher source/API context and public/private library provenance; reference commits do not invent exact private binary build revisions |
| components/defaulted-dropfix/ | Exact Defaulted patcher; it preserves unrelated upstream entries |
| components/combined-compat/ | Suite variant of the original four-mod shim; original packages and mod ID preserved |
| components/chalk-compat/ | Suite Chalk compatibility variant, recipes and post-registry state confirmation |
| components/*/ORIGIN.json or *manifest.json | Baseline/modified source hashes and provenance |
| locks/ | Exact artifact hashes, runtime requirements and build provenance |
| libs/ | Ignored local build inputs; do not commit dependencies |
| libs/installation/ | Ignored redistributable publisher binaries and corresponding source/license inputs for the installation ZIP |
| tools/stage-installation.py | Fetch and verify the permitted installation dependencies and corresponding sources; excludes Fzzy binary redistribution |
| docs/RELEASE_WORKFLOW.md | Standing four-asset packaging, validation, publication and branch-cleanup procedure |
| licenses/ | Complete original copyright and permission notices, copied into the bundle |
| qa/ | Connection/capability smoke, final native/fallback packet checks and disposable runs |
| qa-settings/, qa-hud/, qa-mechanics/ | Unified settings/permissions screenshots, actual HUD observations, recipe/menu/drop/map persistence regressions |
| tools/package-release.py | Package a committed, verified version into the required release hierarchy |
| components/mapstitch-mixed-scales/working/ | Separate mixed-scale atlas addon: world-map UI, independent minimap/generation, location selection, guarded packets, authoritative minimap/full-screen saved-center placement and eject lookup, center repair/diagnostics with forced atlas resynchronization, same-ID atlas deduplication, cartography/command atlas copying and coordinated banner marking across existing enabled layers in the current dimension. AtlasDeathRetention and death/respawn mixins add container-safe retention. |
| components/sensible-stackables/upstream/ | Untouched 26.2 publisher source/binary records and independently tested baseline |
| components/sensible-stackables/ported/ | Preserved historical independent 26.3 port; not the current suite input |
| components/sensible-stackables/developer-release/ | Current unchanged official 3.1.1+26.3 source/binary records and exact input lock |
| components/sensible-stackables/compat/ | Separate Polymer stack defaults, safe metadata and authoritative menu correction |
| qa-merged/ | Historical combined-branch network, maps, stackables and settings evidence; current release scope is in docs/VALIDATION.md |
| qa-multiscale/ | Mixed-scale mechanics/restart fixture and original experiment evidence |
| qa-stackables/ | Actual menu/count conservation, drop, codec and restart evidence for both targets |
| build/libs/ | Development output, separate from released artifacts |

The historical projects outside this suite remain preserved. Consolidated local source/build locations and QA helper resolution are documented in [local archive maintenance](LOCAL_ARCHIVE.md). Suite releases use `Builds/Minecraft/Vanilla++ Quality of Life Suite/<Component>/<Release Version>/`. Each completed release folder contains the four public assets — `README.md`, the suite JAR, `docs.zip` and the installation ZIP — plus local checksum/publication records. Technical documentation, locks, notices and evidence are collected in `docs.zip`; GitHub provides its own source downloads. See [RELEASE_WORKFLOW.md](RELEASE_WORKFLOW.md) for the standing procedure.

## Updating Pajic's mods

1. Identify the exact developer release for the target Minecraft version. Record its Modrinth version ID, original binary hash, published-source hash and repository commit. `tools/fetch-upstream.py` fetches the locked components; a GitHub HEAD snapshot is context, not proof that a published binary contains that exact source.
2. Put new snapshots in a new versioned baseline rather than overwriting the recorded old inputs. Compare published source trees and original metadata/dependencies. Modules that are bundled unchanged should remain unchanged.
3. For Shared Region Maps, apply upstream vanilla/MapStitch changes to working while preserving its documented adapter removals. For the atlas/Elytra addon, inspect Tool Pouch and MapStitch HUD method signatures, corner enums, graphics transforms and info-overlay registration. See docs/HUD.md for shared map placement, independent information placement, ordered render layers, original Apply callbacks and captured bounds. Keep infoOverlaySettings.position out of minimap synchronization; changing a map must not move the text, or vice versa.
4. Review all affected mixins and networking payloads in both compatibility components. Check new items, components, recipe serializers, menus and block states; new registry entries may need Polymer mappings. A version fingerprint change selects fallback until both clients and server match, but does not implement compatibility for newly added content.
5. Preserve external libraries and optional integrations. Update versions within the same library, pin hashes and document the change. Do not bypass an active dependency to make compilation pass. Legitimate upstream dependency removal requires checking every consumer, root/component metadata, runtime profiles, build locks and installer manifests. SSO 2.10.0 and Stackables 3.1.1 removed Defaulted; retain its exact historical compile input for guarded optional compatibility while excluding it from runtime/install sets. CodecUI remains a separately retained runtime input. Mixson is optional for Stackables but still required elsewhere in the suite.
6. Update unified settings registration and translations only where upstream configuration IDs/layouts change. Keep original paths, validation and Fzzy permissions/synchronization. The settings adapter uses pinned Fzzy internals; check it against a changed Fzzy release. Refresh the complete English language copies in `src/main/resources/resourcepacks/settings_titles/assets/` from the exact matching original artifacts before applying title overrides; verify all original entries survive.
7. When updating MapStitch, recheck minimap template selection with absent `mapstitch:map_center`, authoritative saved centers and current-dimension filtering. Validate already-present stored centers against saved data with Polymer both present and absent. Recheck full-screen tile/exploration-marker grid preparation and Ctrl plus the configured eject-map key using missing and stale item centers. Verify `/atlas fix` sends the owning atlas/inventory snapshot even when the server center repair count is zero. Keep deduplication scoped to exact map IDs, return one blank per removed copy and never delete global map records or silently merge same-region maps with different IDs. Keep `/atlas fix` and its `/atlas repair` alias equivalent, with read-only `check` forms; preserve `/atlas dedupe` and `/atlas makecopy` target priority and normal `/extractmap` filters. Recheck atlas+ordinary-book cartography previews and output/quick-move consumption: keep the source, spend one book, copy every filled/explorer entry count while excluding blanks/paper, preserve options and assign a fresh result identity. Keep maintenance commands separate from upstream code, preserve item components/pixels/decorations, and verify full native resends plus client cache refresh without generating replacement maps.
8. Recheck atlas banner interaction against the original MapStitch `tryMarkBanner`/map toggle path and Bannerpoint’s map-linked transmission hook. Keep the all-enabled-layer operation in the separate mixed-scale addon; preserve current-dimension/coverage filtering, add/remove consistency, original map data and the rule that marking spends no blanks.
9. For Bannerpoint, compare its published waypoint connection and naming code, original channel ID, style JSON/sprites and saved-banner lifecycle against `components/bannerpoint-compat/`. Preserve player waypoints and native clients; test Polymer pack success, decline, failure and removal separately. Refresh the original Bannerpoint resources included in the generated pack without modifying the official JAR.
10. Update the nested JAR metadata list, locks/artifacts.json, attribution, source manifests and version. Build, verify archive integrity/nested IDs/dependencies, then run the focused profiles against the final hash. Perform the manual feature checks in docs/VALIDATION.md.
11. Package the release under the required versioned build hierarchy, record checksums, and publish the standalone repository/release after actual compatibility verification. Older Minecraft releases are retained.

## Compatibility tracks

The suite is a mixed-origin 26.3 distribution: developer releases where available, the existing local Chalk port, locally authored additions and suite variants of the existing shims. It is not a claim that all nested modules are official developer releases. Consult docs/PROVENANCE.md for each exact target.

The original Chalk 26.2 developer/shim track remains in its historical project and release folders. This suite targets the local 26.3 port explicitly. The separately named SSO port track is paused and must not be built, tested or overwritten automatically. New suite work does not erase historical release provenance or license notices.

## Rebuilding inputs

The feature modules that are unchanged are staged from verified JARs rather than merged into one source package. `tools/fetch-upstream.py` preserves published-source authority for Pajic modules. Local modules have retained source snapshots and their original build scripts. Use their declared versions and dependency locks; do not invent new versions for a byte-identical snapshot.

After fetching the official snapshots, run `python3 tools/stage-inputs.py --workspace /path/to/existing/workspace` or `--inputs /path/to/verified/jars`. It stages only exact hash matches and reports every missing input. A fresh checkout requires the retained local Chalk/library binaries as well as upstream downloads; it never silently replaces local ports with different official releases.

Root `locks/artifacts.json` is the authority for staged input bytes, including the compile-only historical Defaulted dropfix. Compile-only Kotlin stdlib is extracted unchanged from the pinned Fabric Language Kotlin archive. The patched Defaulted source and original input hash are documented in components/defaulted-dropfix/README.md; a reproducible patch requires the original locked input and ASM 9.10.1, never an arbitrary latest Defaulted binary.

The atlas/Elytra component uses the same staged root inputs as the other suite projects. Its optional ClientSort integration also requires the exact ClientSort 3.104.1+26.3 JAR at compile time. The lock records its SHA-256 and [official version](https://modrinth.com/mod/clientsort/version/UWMryUad). Supply it in `--inputs`, or add `--download-public` to staging to fetch missing publicly downloadable inputs with recorded URLs and verify their hashes. ClientSort is staged under `libs/compile-only/`; it is neither bundled nor installed by the suite QA runtime profiles. ClientSort remains an optional separately installed runtime integration. No component-local `libs` directory is needed for a suite build.

## Rebuilding a public checkout from release inputs

Download the suite JAR and `vanilla-plusplus-installation-pack-1.1.5+26.3.zip` from the public release, and extract the installation ZIP into an input directory. The kit includes the suite and exact CodecUI file under `overrides/mods/` and six permitted publisher binaries under `downloads/mods/`. Fzzy Config is deliberately absent; staging obtains its locked official file when needed. Compile-only Defaulted remains a separate historical input: retrieve the exact locked dropfix from retained prior-release inputs when building its optional compatibility hook; do not install it merely to build current runtime profiles. Run:

```sh
python3 tools/stage-inputs.py --bundle /path/to/vanilla-plusplus-quality-of-life-suite-1.1.5+26.3.jar --inputs /path/to/extracted-installation-kit --download-public
```

The script verifies the outer release hash and recursively stages only original inputs matching the artifact lock; suite-modified compatibility/HUD/map modules are rebuilt from working source. It rejects unavailable or mismatched inputs. `tools/fetch-upstream.py` remains available to retrieve official source/reference inputs. The release has no separate local-library ZIP or uploaded tracked-source archive; use the repository or GitHub’s generated source download for the working checkout.

For installation-kit packaging, run `python3 tools/stage-installation.py`. It stages permitted publisher binaries under ignored `libs/installation/` and corresponding source/license inputs from `docs/dependency-distribution.lock.json`, verifying their locked sizes and hashes. Fzzy remains an official manifest download and must never be staged into the distribution. This installation cache is separate from normal compile/runtime inputs.

After changing source, build and run the applicable checks. Update the build and validation records with the exact tested hash, rebuild and test the installation ZIP, then commit source/docs/evidence. Run `python3 tools/package-release.py` from a clean committed checkout. It creates exactly four public assets: `README.md`, the installable suite JAR, `docs.zip` and the installation ZIP, with local checksums/publication records alongside them. A new version requires updated metadata/locks/verification and a new tag; preserve older releases. Follow [RELEASE_WORKFLOW.md](RELEASE_WORKFLOW.md) for exact staging, installation checks and publication requirements.

### Pouch Elytra durability and repair

Implementation lives in `components/toolpouch-atlas-elytra/working/src/main/java/com/thenathe/toolpouchcompat/`: `PouchDurability` and its utility mixin replace raw flight wear, `PouchItems` handles active/live contents, and `PouchSsoMending` with its guarded inventory mixin delegates SSO material repair. `PouchMending`/`PouchMendingExperienceMixin` retain the separate XP path. Focused fixtures are in `qa-release/pouch-mending/`, with historical results under `qa-release/evidence/1.1.4/pouch-mending/` and current-release scope in [validation](VALIDATION.md).

The suite’s pouch addon `1.0.9-suite.1+26.3` owns enchantment-aware flight wear and optional SSO repair integration. When Tool Pouch or SSO updates, trace `ToolPouchUtil.updateElytraInToolPouch`, active-pouch selection, `ExperienceOrb.repairPlayerItems`, and SSO’s `InventoryMixin`/`ModUtil.tryRepairItem`. Preserve Unbreaking, equipped-item XP priority, server-side Mending settings, original whetstone enchantment compatibility and material costs, correct usable-Elytra selection and live open-menu saveback. Verify enabled/disabled and missing-resource cases, attached pouches, menu close persistence and unrelated-item conservation. Keep SSO optional for this component and retain its original recipe rather than recreating its repair rules.


### Repair and stack synchronization without Defaulted

SSO 2.10.0 uses `simple_smithing_overhaul:repairables`; Sensible Stackables 3.1.1 uses `sensible_stackables:stack_sizes`. Audit their server send paths and registration on each update. Both payloads require supported native-client guards. Stackables now supplies limits through override getters, so copying stored default components alone is insufficient: project the effective limits into fallback item metadata, retain full counts and cap the fallback prediction maximum at 99. Test actual native and Fabric-without-suite joins as well as menu moves and configuration changes.

Pouch XP repair wraps the whole `ExperienceOrb.repairPlayerItems` operation. Clumps can cancel/return from that method before a return-value modifier runs; an ordinary return-only injection passed the isolated 1.1.4 fixture while failing this real mod combination. Recheck Clumps with standalone and attached netherite pouches, equipment consuming all or part of an orb, SSO regular-Mending disabled, and optional Armored Elytra/Inventory Mending paths. Preserve the original equipped-item priority and XP conservation; do not disable another mod to obtain a passing test.
