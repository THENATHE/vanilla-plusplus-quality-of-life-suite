# Colorful Addon port validation — 2026-10-03

The supplied developer JAR is preserved byte-for-byte in `upstream/` with SHA-256 `bf184e44ceb222e0831912acc2e26a17b7358e93162b60776dd646cf3780bbef`. Its declared version is `2.1.1+1.19.3`; the filename omits the Minecraft suffix. It contains metadata, a license, and an icon, with no Java code or entrypoints.

The explicit 26.3 port declares `2.1.1-port.1+26.3`, keeps mod ID `chalk-colorful-addon`, and requires Minecraft 26.3, Chalk `3.2.1+26.3`, Fabric Loader 0.19.5+, and Java 25+. Its deterministic build reproduces SHA-256 `69a2443250abd95100636194a145b5479d240151cd19bf4186766117d0710b34`. The original icon and license match byte-for-byte, and both archives pass ZIP integrity checks.

The original addon was tested with original Chalk `3.2.0+26.2` on Minecraft 26.2. The port was tested with the exact Chalk `3.2.1+26.3` port on Minecraft 26.3. Exact dependency inputs are recorded in [dependencies.lock.json](dependencies.lock.json). No Minecraft 1.19.3 runtime test is claimed.

With the addon, both stacks register 16 normal/glow colors and pass 1,728 mark-state placements at startup and after reload. Without the addon, the shim registers only the expected white variants and 108 states. Both final shim builds also preserve all 1,728 colored states, support blocks, glow properties, and display holders after a full server stop/restart.

The updated shim adds 752 valid colored conversion transitions, or two white-only glow transitions without the addon, while preserving damage, names, custom data, target defaults, and explicit component removals. Invalid ingredients, duplicate ingredients, unavailable colors, and redundant conversions are rejected. These recipes are features of the separate shim, not additions to the metadata-only addon.

See the [shim's current validation](../chalk-polymer-shim/VALIDATION.md) and [1.1.0 evidence](../chalk-polymer-shim/qa/evidence/1.1.0/) for actual network crafting, native/Polymer client detection, mixed clients, visual inspection, and compatibility limits. Initial addon-only compatibility evidence against unchanged shim 1.0.0 is retained separately in [the original audit](../chalk-polymer-shim/qa/evidence/colorful-addon-2026-10-03/).

All tests use disposable localhost worlds and test accounts. The original addon, Chalk releases, and dependency JARs remain unchanged. No public release or repository was published by this task.
