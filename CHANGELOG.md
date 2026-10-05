# Changes

## 1.0.2-multiscale.1+26.3 — 2026-10-04 (experimental branch)

- Add a separately built mixed-scale MapStitch addon; original MapStitch JAR stays byte-identical.
- Store maps at all five scales in one atlas; persist active exploration/minimap scale using original world-map controls.
- Preserve map IDs and exactly-once blank consumption, shared scale-specific regions, pouch saveback and restart state.
- Bind scale selection to the owned physical atlas location, including identical inventory/pouch copies. Require addon-aware MapStitch negotiation.

These features are not merged into main. Exact branch validation is in [VALIDATION.md](docs/VALIDATION.md).

## 1.0.1+26.3 — 2026-10-04

- Matching suite clients now receive SSO’s actual broken-anvil block and facing. Clients without native SSO support continue to receive a safe damaged-anvil representation through Polymer. Native block registry synchronization, confirmed state IDs and palette width now cover SSO and Chalk together, including independent module fallback.
- Reopening unified settings keeps pending forwarded approval requests. A server update refreshes the grouped configuration manager while carrying pending requests forward and restoring every module’s routing. Changing connections clears pending requests and cached permissions so they cannot cross servers.
- Public source builds now use the staged root dependency directory for the atlas/Elytra addon. ClientSort 3.104.1+26.3 is pinned and staged as a compile-only input; it stays optional at runtime and is not bundled. Input verification runs before all component compilation.
- Gradle now tracks component version changes lazily when generating metadata. Archive verification rejects a nested component whose declared version does not match its build, preventing stale version labels after incremental builds.
- Added a linked Modrinth dependency and optional-integration directory to the GitHub README and local release documentation. Publisher availability, local-only inputs and actual runtime coverage are distinguished.

Updated internal components: combined compatibility **1.0.6-suite.1+26.3**, Chalk compatibility **1.1.1-suite.1+26.3**. Existing feature mod inputs and the required Defaulted **1.3.8+26.3.dropfix.1** stay pinned. No saved-data identifiers or configuration IDs change. Replace the previous suite JAR with this one; keep the documented dependency versions. Older suite clients may negotiate fallback for changed components; install the matching suite version on native clients.

Regression coverage and exact artifact hashes are in [VALIDATION.md](docs/VALIDATION.md). Source and dependency setup are in [UPDATING.md](docs/UPDATING.md) and [DEPENDENCIES.md](docs/DEPENDENCIES.md).

## 1.0.0+26.3 — 2026-10-04

Initial modular suite release with shared client/server negotiation, optional Polymer compatibility, unified settings, Chalk recolor/glow crafting, amethyst curse removal, vanilla/MapStitch-only map sharing, and pouch/minimap HUD spacing. Historical validation remains linked in the repository and the 1.0.0 release.
