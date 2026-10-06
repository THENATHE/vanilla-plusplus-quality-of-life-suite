# Native minimap rendering and repair acceptance

Passed against suite `1.1.3+26.3`, SHA-256 `0492e88a51fb88cc5dbc0d815022d3d5a39f095fa299c5069f040ad5be63c69b`, with Minecraft 26.3, Fabric Loader 0.19.5 and OpenJDK 25.0.4.1. Run label: `minimap-repair-1.1.3-final`.

The real graphical Fabric client joined an isolated local Polymer server. The fixture seeded 15 locked maps: every scale in the Overworld, Nether and End. A mixin observed the existing MapStitch renderer without replacing it.

Four focused minimap cases passed:

- A normal atlas entry rendered 29 times.
- The same saved map still rendered 29 times when its atlas entry lacked `MAP_CENTER`.
- An entry with a deliberately wrong center rendered 29 times and cached the synchronized map's authoritative coordinates.
- A stale active map from another dimension rendered zero times.

The fixture then cleared all 16,384 client pixels of each of the 15 maps, without changing server data, and issued the actual `/repairmaps` command through the client connection. All pixels were restored from the server, the native cache-refresh packet arrived, and the minimap rendered 30 times. Its screenshot shows the restored pixels and the server command response.

Existing acceptance checks also passed: authoritative map dimensions/scales/centers/locks/pixels, preservation of an existing map object and its pixels/banner during metadata correction, rejection of invalid metadata, current-dimension exploration markers, 21 actual world-map views and two real dimension transfers. Every observed world-map tile matched its selected dimension and scale.

`result.json` records the exact bundle and coverage; `observations.json` contains 30 observation rows. Side-specific audits preserve loaded mod hashes, runtime versions and launch arguments, with machine-specific paths/classpaths and synthetic offline identifiers removed. `nested-modules.json` records the suite's 18 bundled modules. Screenshots are unmodified; ordinary Minecraft chat-trust/tutorial notifications partially cover the top-right minimap.

The separate `baseline-1.1.2/` record documents an expected reproducer failure against the published 1.1.2 binary: the normal minimap rendered 29 times, then the missing-center entry produced zero map-render calls. This confirms the renderer vulnerability. Normal server inventory ticking already repairs missing centers, so this does not establish the exact cause of the user's persistent failure. The pixel-repair case uses deterministic synthetic maps and does not claim to reproduce the user's world. Remapped was not installed in this focused client test.

Reproduce with `python3 qa-multiscale/run-client.py --label <unique-label> --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.1.3+26.3.jar`. The runner compiles temporary QA mods, launches a disposable server/client and retains the full local raw logs. Spectator mode keeps death and movement outside this focused test.
