# Chalk: Colorful Addon — 26.3 port

Unofficial metadata-only port by THENATHE of DaFuqs and mortuusars' MIT-licensed addon. The exact supplied `chalk-colorful-addon-2.1.1.jar` is preserved in `upstream/`, alongside its complete extracted contents. It declares version `2.1.1+1.19.3`, but contains no Java classes, entrypoints, mixins, or Minecraft version restriction. Chalk checks for its mod ID and supplies the colored items, recipes, blocks, textures, and gameplay itself.

The original addon already enables colors with the existing Chalk 26.3 port. This explicit 26.3 package retains that behavior, declares version `2.1.1-port.1+26.3`, pins Minecraft and Chalk, removes obsolete build-tool manifest information, and preserves the original license, icon, authors, and project links. It does not rewrite Chalk or duplicate its implementation.

## Tracks and installation

| Track | Minecraft | Addon | Chalk | Fabric API | Cloth Config | Optional Polymer / shim |
|---|---|---|---|---|---|---|
| Original developer artifacts | 26.2 | Original `2.1.1+1.19.3` | Original `3.2.0+26.2` | `0.161.0+26.2` | `26.2.155+fabric` | `0.17.5+26.2` / `1.1.0+26.2` |
| ChatGPT port | 26.3 | `2.1.1-port.1+26.3` | Port `3.2.1+26.3` | `0.161.0+26.3` | `26.3.159+fabric` | `0.18.2+26.3` / `1.1.0+26.3` |

Both test stacks use Fabric Loader 0.19.5 and Java 25. Exact binary hashes and local inputs are in [dependencies.lock.json](dependencies.lock.json). The upstream label refers to the original addon and original Chalk running together on 26.2; it does not claim this task tested Minecraft 1.19.3.

For a normal modded installation, install matching Chalk, Fabric API, Cloth Config, and one addon on the server and native clients. For vanilla-client access, add matching Polymer Bundled and the separate [Chalk Polymer shim](../chalk-polymer-shim/README.md) to the server. Enable Polymer pack hosting with `enabled: true` and `required: true` in `config/polymer/auto-host.json`, and accept the pack on clients. The shim is required on the server; installing its optional client companion alongside native Chalk enables automatic native rendering negotiation. Vanilla clients need no mods; native clients retain all colors by installing the matching addon alongside Chalk.

Do not install the original and ported addons together: both intentionally use `chalk-colorful-addon`. The addon is optional for the shim. Without it, Chalk registers white normal/glow chalk; with it, all 16 normal/glow colors register. Keep the addon installed in worlds containing colored chalk; removing a registry-providing addon from such a world is different from running a fresh white-only installation.

## Build and source

Run `python3 build.py` from this directory. Python's standard library creates the deterministic installable JAR under `ported/build/libs/`; no Java compiler or dependency downloads are needed for this metadata-only addon. The script validates the original binary hash, unchanged upstream artwork/license, metadata, and archive integrity.

`upstream/` is the immutable developer baseline; `ported/src/main/resources/` is the complete separately maintained port source. The user-supplied binary is the provenance source; no upstream source commit is inferred. The Chalk port and production shim revisions are recorded separately in the dependency lock. Shim `1.1.0` retains dynamic variant support and adds durability-preserving recoloring/glow recipes and optional client negotiation. The historical `1.0.0` releases remain available.

See [VALIDATION.md](VALIDATION.md) for fresh verification and limitations. This work creates local release artifacts; it does not publish a new GitHub release.

## Credits

Original addon: DaFuqs and mortuusars, [ChalkColorful](https://github.com/DaFuqs/ChalkColorful). Original license and copyright remain in `upstream/source/LICENSE_chalk-colorful-addon` and the port JAR. THENATHE maintains this unofficial port; Chalk and Polymer remain separate required mods for their respective functionality.
