# Source map and updating upstream modules

The workspace source remains at `Minecraft/thenathe-mod-suite/`. The public repository and outer artifact use `vanilla-plusplus-quality-of-life-suite`. The internal suite mod ID and protocol/config namespace remain `thenathe_mod_suite`; branding does not rename original mod IDs or saved configuration keys.

## Where things live

| Path | Purpose |
| --- | --- |
| src/main/java/com/thenathe/suite/Suite.java | Root common entrypoint |
| src/main/java/com/thenathe/suite/network/ | Shared capability protocol; compiled into combined-compat only |
| src/main/java/com/thenathe/suite/client/ | Unified Fzzy settings screen, Chalk settings bridge, client command and Mod Menu integration |
| src/main/resources/ | Root identity, nested feature JAR list and translations |
| components/sso, mapstitch, toolpouch, tiered-backpacks, misctweaks, simple-death-improvements | Immutable published source/binary records plus upstream repository snapshots and staging scripts |
| components/shared-region-maps/upstream | Original 1.0.3 source baseline |
| components/shared-region-maps/working | Vanilla/MapStitch-only modified module |
| components/toolpouch-atlas-elytra/upstream | Original local addon 1.0.4 source baseline |
| components/toolpouch-atlas-elytra/working | Preserved addon features plus HUD spacing fix |
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
| licenses/ | Complete original copyright and permission notices, copied into the bundle |
| qa/ | Connection/capability smoke, final native/fallback packet checks and disposable runs |
| qa-settings/, qa-hud/, qa-mechanics/ | Unified settings/permissions screenshots, actual HUD observations, recipe/menu/drop/map persistence regressions |
| tools/package-release.py | Package a committed, verified version into the required release hierarchy |
| build/libs/ | Development output, separate from released artifacts |

The historical projects outside this suite remain preserved. Consolidated local source/build locations and QA helper resolution are documented in [local archive maintenance](LOCAL_ARCHIVE.md). Suite releases use `Builds/Minecraft/Vanilla++ Quality of Life Suite/<Component>/<Release Version>/`, with the installable JAR, checksums, source archive and release notes together.

## Updating Pajic's mods

1. Identify the exact developer release for the target Minecraft version. Record its Modrinth version ID, original binary hash, published-source hash and repository commit. `tools/fetch-upstream.py` fetches the locked components; a GitHub HEAD snapshot is context, not proof that a published binary contains that exact source.
2. Put new snapshots in a new versioned baseline rather than overwriting the recorded old inputs. Compare published source trees and original metadata/dependencies. Modules that are bundled unchanged should remain unchanged.
3. For Shared Region Maps, apply upstream vanilla/MapStitch changes to working while preserving its documented adapter removals. For the atlas/Elytra addon, inspect Tool Pouch and MapStitch HUD method signatures, corner enums, graphics transforms and info-overlay registration. See docs/hud-layout-fix.md.
4. Review all affected mixins and networking payloads in both compatibility components. Check new items, components, recipe serializers, menus and block states; new registry entries may need Polymer mappings. A version fingerprint change selects fallback until both clients and server match, but does not implement compatibility for newly added content.
5. Preserve external libraries and optional integrations. Update versions within the same library, pin hashes and document the change. Do not bypass Defaulted, Fzzy, Cloth, Mixson or another dependency to make compilation pass. Reapply or retire the Defaulted drop fix only after confirming the upstream implementation handles the same regression.
6. Update unified settings registration and translations only where upstream configuration IDs/layouts change. Keep original paths, validation and Fzzy permissions/synchronization. The settings adapter uses pinned Fzzy internals; check it against a changed Fzzy release.
7. Update the nested JAR metadata list, locks/artifacts.json, attribution, source manifests and version. Build, verify archive integrity/nested IDs/dependencies, then run the focused profiles against the final hash. Perform the manual feature checks in docs/VALIDATION.md.
8. Package the release under the required versioned build hierarchy, record checksums, and publish the standalone repository/release after actual compatibility verification. Older Minecraft releases are retained.

## Compatibility tracks

The suite is a mixed-origin 26.3 distribution: developer releases where available, the existing local Chalk port, locally authored additions and suite variants of the existing shims. It is not a claim that all nested modules are official developer releases. Consult docs/PROVENANCE.md for each exact target.

The original Chalk 26.2 developer/shim track remains in its historical project and release folders. This suite targets the local 26.3 port explicitly. The separately named SSO port track is paused and must not be built, tested or overwritten automatically. New suite work does not erase historical release provenance or license notices.

## Rebuilding inputs

The feature modules that are unchanged are staged from verified JARs rather than merged into one source package. `tools/fetch-upstream.py` preserves published-source authority for Pajic modules. Local modules have retained source snapshots and their original build scripts. Use their declared versions and dependency locks; do not invent new versions for a byte-identical snapshot.

After fetching the official snapshots, run `python3 tools/stage-inputs.py --workspace /path/to/existing/workspace` or `--inputs /path/to/verified/jars`. It stages only exact hash matches and reports every missing input. A fresh checkout requires the retained local Chalk/SSO/library binaries as well as upstream downloads; it never silently replaces local ports with different official releases.

Root `locks/artifacts.json` is the authority for staged input bytes, including Defaulted dropfix. Compile-only Kotlin stdlib is extracted unchanged from the pinned Fabric Language Kotlin archive. The patched Defaulted source and original input hash are documented in components/defaulted-dropfix/README.md; a reproducible patch requires the original locked input and ASM 9.10.1, never an arbitrary latest Defaulted binary.

The atlas/Elytra component uses the same staged root inputs as the other suite projects. Its optional ClientSort integration also requires the exact ClientSort 3.104.1+26.3 JAR at compile time. The lock records its SHA-256 and [official version](https://modrinth.com/mod/clientsort/version/UWMryUad). Supply it in `--inputs`, or add `--download-public` to staging to fetch missing publicly downloadable inputs with recorded URLs and verify their hashes. ClientSort is staged under `libs/compile-only/`; it is neither bundled nor installed by the suite QA runtime profiles. ClientSort remains an optional separately installed runtime integration. No component-local `libs` directory is needed for a suite build.

## Rebuilding a public checkout from release inputs

Download this version’s feature JAR and optional local-library ZIP from the public release. Extract the ZIP into an input directory, and put the exact remaining publisher library JARs listed in README/locks there too. Run `python3 tools/stage-inputs.py --bundle /path/to/vanilla-plusplus-quality-of-life-suite-1.0.1+26.3.jar --inputs /path/to/inputs --download-public`. The script verifies the outer release hash and stages only nested originals that match the artifact lock; suite-modified compatibility/HUD/map modules are rebuilt from working source. It also recursively finds the local-library archive’s `mods/` directory and rejects unavailable/mismatched inputs. `tools/fetch-upstream.py` remains available to retrieve official source/reference inputs.

After changing source, build and run the applicable checks. Update the build and validation records with the exact tested hash, commit source/docs/evidence, then run `python3 tools/package-release.py` from a clean checkout. The packager creates the installable JAR, optional MIT-only library archive, tracked-source archive, release notes and checksums. It excludes QA fixtures and external public libraries. A new version requires updated metadata/locks/verification and a new tag; preserve older releases.
