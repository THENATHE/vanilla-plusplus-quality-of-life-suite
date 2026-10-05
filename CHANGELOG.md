# Changes

## 1.1.2-bannerpoint.1+26.3 — 2026-10-05 (experimental branch)

- Integrate Pajic’s original Bannerpoint developer release unchanged as a separate nested module on `feat/bannerpoint`; stable `main` remains 1.1.1.
- Add a separate compatibility component that contributes Bannerpoint’s waypoint style and four locator-bar sprites to Polymer’s generated resource pack.
- Send banner waypoint icons to clients with Bannerpoint installed, or to other clients only after Polymer confirms its main pack loaded successfully. Pending, declined, failed or removed packs hide banner waypoints instead of showing missing-texture squares.
- Send the original custom banner-name payload only to clients advertising its original channel. Resource-pack-only clients receive icons without Bannerpoint’s custom name overlay.
- Preserve original player waypoints, banner identities, persistence, configuration files and native functionality. Include Bannerpoint gameplay/client settings in the combined settings hub.
- Keep the existing libraries, Defaulted dropfix and four-asset packaging procedure. See [Bannerpoint support](docs/BANNERPOINT.md) and [validation](docs/VALIDATION.md) for the exact implementation and verification records.

## 1.1.1+26.3 — 2026-10-05

- Restore independent placement for Tool Pouch's information overlay: put the minimap on the right and compass/clock text on the left, or choose other corners independently.
- Keep atlas and ordinary filled-map minimap positions and map offsets synchronized through their original settings. Information-overlay position and offsets stay separate.
- Preserve measured map bounds, text/background alignment and overlap protection when maps and details share a side, including bottom corners.
- Fix world-map and minimap dimension/scale selection for atlases containing mixed map sizes and multiple dimensions. Matching native clients receive each map’s authoritative dimension and center through a guarded metadata payload; vanilla/fallback clients receive no unsupported packet.
- Add `/extractmap` to extract filled maps by scale, dimension or both, and `/extractmap empty` for blank maps and paper. Use a held atlas first, otherwise the active pouch atlas; return items to the owner’s inventory and drop only overflow. No operator permission is required.
- Retain the original settings files, separate feature modules, exact dependencies, saved data and automatic native/Polymer negotiation.
- Update the settings directions, source/update guide and four-asset release documentation. See [the 1.1.1 release guide](docs/RELEASE_1_1_1.md) and [validation](docs/VALIDATION.md) for installation and the exact test scope.

## 1.1.0+26.3 — 2026-10-05

- Promote tested mixed-scale MapStitch and Sensible Stackables modules, automatic negotiation and Polymer support to main. Keep both additions separately maintained for upstream updates.
- Include independent world-map/minimap/generation controls, the top-right layout and ordered tooltip, map-creation sound, synchronized atlas/ordinary-map HUD, nested atlas death retention, Mod Menu grouping and Configure... settings actions.
- Combine Chalk and its Colorful Addon into one README feature section with both original links and credits. Present wholly authored THENATHE features directly without a My tweaks subsection. Document Sensible Stackables alongside the other included feature mods.
- Use stable suite/coordinator/map-addon versions 1.1.0+26.3, retain original mod IDs and all saved configuration/data formats, exact Defaulted dropfix and separate historical compatibility tracks.
- Standardize user-facing documentation, canonical front-page dependency links, four public assets, version-only release titles and preservation-aware cleanup of finished branches. Include permitted dependencies/notices/corresponding sources in one client/server installation ZIP; Fzzy remains an official manifest download.


## 1.0.2-merged.3+26.3 — 2026-10-05 (testing branch)

- Move minimap and generation controls into an aligned, right-aligned group immediately beneath the top coordinate numbers; restore every original right-side button position and gap.
- Reorder atlas tooltips: storage introduction, enabled generation scales, minimap scale, then unchanged original counts/ejection information.
- Use Configure... for settings navigation buttons while retaining original category names, settings, and behavior.

## 1.0.2-merged.2+26.3 — 2026-10-05 (testing branch)

- Separate world-map viewing, minimap scale, and per-scale map generation. Add compact controls below S, per-atlas persistence, and generation tooltips.
- Restore a vanilla cartography sound sent to the explorer once per generation batch.
- Extend keep-atlas-on-death to extract only atlases from nested pouch/backpack/shulker/bundle contents, including cursor containers; preserve other ordinary drops and handle retained-atlas overflow.
- Synchronize Tool Pouch and MapStitch HUD corner settings and use shared minimap/detail spacing for atlas and ordinary map sources.
- Keep original mods individually visible in Mod Menu, hide the Colorful addon entry, and group authored components under the suite. Rename the coordinator to Vanilla / Polymer Shim. Original mod JARs remain unchanged.
- Remove placeholder settings entries, give real configuration screens consistent titles, and add a rebindable Open Suite Settings hotkey (initially unbound).


## 1.0.2-merged.1+26.3 — 2026-10-05 (testing branch)

- Merge mixed-scale MapStitch and Sensible Stackables as separate modules on `merged`.
- Retain both addon-aware MapStitch negotiation and Stackables protocol v2, including safe fallback metadata and full server counts.
- Bundle all 16 feature/compatibility modules, keep exact Defaulted dropfix, and provide fresh combined validation.
- Update the map addon’s coordinator dependency to the merged version and require all three new modules in root metadata, preventing Fabric from silently omitting an experiment.


## 1.0.2-stackables.1+26.3 — 2026-10-04 (experimental branch)

- Integrate Sensible Stackables as an independently compiled unofficial 26.3 port and separate Polymer module. Preserve original 26.2 source/binary/dependencies and verification.
- Keep default stack rules, configurable uncapping, menu fixes, throw cooldown, original configuration and native count presentation.
- Synchronize stack defaults through Polymer. Preserve full counts above 99 while advertising safe fallback maximum metadata; correct completed inventory actions.
- Add independently negotiated Stackables capability and its original settings to the grouped hub.

At this historical release they were not yet on main; stable 1.1 now includes them. Exact branch validation is in [VALIDATION.md](docs/VALIDATION.md).

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
