# Full-screen atlas, Ctrl+Q and source acceptance

Passed against atlas candidate `1.1.4+26.3`, SHA-256 `96bdb9d63210f1fce11d361a054b590dda2a6d828dfe7d2732b20d1f42e6be84`, using Minecraft 26.3, Fabric Loader 0.19.5, OpenJDK 25.0.4.1 and the real Vulkan graphical backend. Run label: `worldmap-center-1.1.4-final-02`.

Twelve full-screen cases used the real MapStitch renderer with deliberately missing or wrong atlas item centers. Each case verified the tile cache at the saved map's authoritative grid, rendered the selected scale/dimension, then dispatched Ctrl+Q through the original `WorldMapScreen.keyPressed`. A separate observer captured the original outgoing `C2SEjectMap` ID. The real server then dropped exactly one matching map item, removed it from the atlas and retained the locked saved map and pixels. Coverage includes all five Overworld scales, an End 1:16 view and a Nether 1:8 view; every case uses a negative grid coordinate on at least one axis. Pointers are placed at the center of the viewed map immediately before key dispatch. Named Minecraft SDL key/modifier constants are used.

Five source cases passed:

- A filled inventory atlas followed by an empty inventory atlas still renders.
- Replacing the filled atlas with empty atlases on the same screen shows the original no-sources message and zero tiles, proving the accumulated source flag resets between frames.
- An empty pouch atlas appended after a filled inventory atlas does not hide its tiles.
- A filled active pouch renders with empty inventory atlases.
- When both sources contain filled maps, the active pouch retains native minimap selection priority over the inventory atlas.

The previous acceptance cases also passed: four minimap center/dimension regressions; `/atlas fix` restoring all 16,384 pixels of each of 15 client maps; native cache refresh; preservation of existing map objects, pixels and banners; current-dimension exploration markers; 21 world-map scale/dimension views; and two real dimension transfers. The fixture uses deterministic locked maps and a disposable offline local server, not the user's exact world. Remapped was not installed.

`observations.json` contains 47 rows. `result.json` identifies the precise candidate hash, coverage and 22 unmodified screenshots. Side-specific audits preserve loaded mod hashes, runtime versions and relevant launch arguments while removing machine-specific paths/classpaths and synthetic offline identifiers. `nested-modules.json` records the 18 bundled modules. Normal Minecraft chat-trust/tutorial notifications may partially cover the minimap.

`baseline-1.1.3/` separately records the expected full-screen reproducer failure against published 1.1.3, SHA-256 `0492e88a51fb88cc5dbc0d815022d3d5a39f095fa299c5069f040ad5be63c69b`: valid synchronized map data at `(-2048, -2048)` cannot be indexed when the atlas item's center is missing. The already-fixed minimap renders that map; this is a separate full-screen lookup defect.

Reproduce with `python3 qa-multiscale/run-client.py --label <unique-label> --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.1.4+26.3.jar`. The historical baseline uses `--baseline-1.1.3` and the published 1.1.3 JAR. Full raw logs remain in ignored local run folders. Spectator mode isolates movement and death from these checks; dropped item entities are restored/cleared only inside the disposable QA world.

This record accepts the identified atlas candidate. If another module changes the final release JAR, the new artifact needs its own recorded verification; this hash must not be presented as that later binary.
