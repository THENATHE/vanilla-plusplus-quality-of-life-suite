# Local archive maintenance — 2026-10-04

The suite remains the active combined project at `Minecraft/thenathe-mod-suite/`. Consolidated standalone projects are preserved locally as complete source snapshots. Their contents, cached dependencies, historical worlds and verification records remain available; archiving does not resume the paused SSO port or establish new runtime verification.

All paths below are relative to the workspace root. Each listed source moves from `Minecraft/<folder>/` to `Backups/General Utilities (Non-WP)/Minecraft/<folder>/Snapshots/2026-10-04-suite-consolidation/`:

| Preserved folder | Role |
| --- | --- |
| `SSO-backpack-toolpouch-mapstitch-shim` | Historical Multi-Shim source and cached compiler dependencies |
| `toolpouch-atlas-elytra-compat-26.3` | Historical addon source and optional ClientSort compile input |
| `simple-smithing-polymer-compat-26.3` | Standalone SSO shim |
| `tiered-backpacks-polymer-compat-26.3` | Standalone backpack shim |
| `toolpouch-polymer-compat-26.3` | Standalone Tool Pouch shim |
| `mapstitch-polymer-compat-26.3` | Standalone MapStitch shim and retained QA launch helper |
| `chalk-polymer-shim` | Standalone Chalk shim and retained cached-classpath helper |
| `shared-region-maps` | Historical standalone map-sharing project |
| `amethyst-curse-cleanser-26.2` | Historical standalone curse-removal project |
| `simple-smithing-overhaul-26.3` | Paused SSO-port source and history |
| `toolpouch` | Retained original checkout |
| `mapstitch` | Retained original checkout |
| `tiered_backpacks` | Retained original checkout |
| `toolpouch-compat-local` | Historical compatibility development |
| `toolpouch-compat-qa` | Historical QA support |
| `toolpouch-polymer-qa` | Historical Polymer QA support |
| `polymer-shim-test-bundle` | Retained staging dependencies used by suite QA |

Archived releases retain their family/component/version hierarchy beneath `Backups/old builds/Minecraft/`. The moved families are Amethyst Curse Cleanser, Shared Region Maps, Simple Smithing Overhaul, Tiered Backpacks, Tool Pouch, MapStitch and Multi-Shim. Chalk's two Polymer Compatibility Shim components and Polymer's combined-shim and Local Shim Test Bundle components move there too. Chalk's base mod and Colorful Addon, Polymer Main Plugin, the suite's releases and Map Atlases remain active.

`tools/workspace_paths.py` first uses an existing `Minecraft/<folder>` directory, then the exact dated source snapshot above. The connection/mechanics runners resolve the retained Chalk helper, combined-shim Loom cache and test-bundle staging inputs through it. The settings/HUD runners resolve the retained MapStitch helper and reset its workspace and Map Atlases audit globals in memory, since deriving those paths from the moved helper would otherwise point inside `Backups`. Helper loading does not write Python bytecode into archived snapshots. New QA outputs remain in the suite's own ignored run directories.

`tools/stage-inputs.py --workspace /path/to/workspace` searches the corresponding preserved source inputs plus both active and archived release hierarchies. Every staged artifact still must match `locks/artifacts.json`; archiving does not permit substituted dependencies. Public checkout rebuilding through `--bundle`, `--inputs` and pinned downloads remains available without this local archive.

The two cached launch audit files remain under active `Minecraft/map-atlases-26.3/`: `build/port3-polymer-final/packaged-launch-audit.json` and `qa/registry-fix/vanilla/vanilla-launch-audit.json`. Their current classpaths reference active Map Atlases files and the user's Gradle cache, not the moved projects. The resolver also translates missing classpath/assets entries to an existing corresponding source/release archive path in memory, so an older audit can retain its original location records. Historical audits are preserved without rewriting. The accepted EULA read by the settings/HUD helper stays with the archived MapStitch project's `qa/runs/server/eula.txt`.

This maintenance change receives Python syntax and path/import checks only. It changes local tooling and documentation; it does not modify production sources, rebuild release artifacts, or claim a new gameplay run. Keep the snapshots, existing Gradle cache and active Map Atlases support files when using these local QA helpers. If those inputs are later relocated, update the resolver and repeat path checks before launching QA.

Post-move checks passed for all 17 source mappings and 24 archived release component directories, syntax of the six changed Python files, real imports of both retained helpers, corrected workspace/audit globals, all 114 cached audit classpath entries, Chalk's required cache files, the retained accepted EULA, and server/native command construction without launching them. Temporary fixtures also checked active-path preference and archived source/release path fallback. Helper scripts and historical audits retained their pre-check hashes. The production suite JAR remains SHA-256 `8e4783b66633a7f6f5cfa38e285b530638d4d82326d5eca15376f7b698d0791a`.
