# Release procedure

These are the standing project conventions requested by THENATHE. Apply them to future releases as well as the stable 1.1 promotion. [Project agreements](../AGENTS.md) record the same defaults for future development sessions.

## Documentation

The README starts with navigation and presents What it is, the feature list grouped by original mod, the shim explanation, installation and technical info. Each original mod receives its project link and creator thanks. Chalk and Colorful Chalk share one section with both links. Sensible Stackables retains a full section. Upstream feature sections can have suite tweaks; locally authored features explain their behavior directly.

Use modern Minecraft versions in overview/feature prose and describe occasional machine-assisted porting while authors update independently. Keep ports identified as unofficial. Installation help, tables and technical records retain exact targets. Only the front-page dependency list omits dependency version pins and uses canonical Modrinth project links; Releases and the technical dependency directory select the compatible versions.

Update README, CHANGELOG, the current release guide, installation instructions, dependency locks, provenance/source map and validation reports together. Keep historical artifact hashes, tests and source snapshots intact; previous passes do not become fresh passes through a version change.

## Build and validation

1. Merge the desired, reviewed modules into main without flattening their source boundaries. Synchronize the suite, coordinator, addon metadata and build versions wherever an exact dependency requires it.
2. Build with the exact locked inputs. Keep the requested Defaulted dropfix and external CodecUI; keep the SSO-port track paused. Run the archive verifier and focused runtime regressions appropriate to the changes. Record the final artifact hash and test scope.
3. Run `python3 tools/stage-installation.py` to stage allowed publisher dependency binaries under ignored `libs/installation/`, and corresponding source archives/licenses as recorded in `docs/dependency-distribution.lock.json`. Their hashes must match the installation lock. Do not stage Fzzy for redistribution: its official manifest download is required by its license.
4. Build the installation ZIP, run actual client/server installation checks in disposable directories and run archive/installer regressions. Confirm side selection, hashes, no Fzzy binary, idempotency and preservation of existing files. Store the resulting report under docs.
5. Commit final source, documentation and evidence. `tools/package-release.py` requires a clean committed checkout and a runtime-verified final JAR. It creates the four public assets and local release records/checksums under the workspace family/component/version folder.

## Public assets and title

The release title is only `<declared release number>+<Minecraft target>` — for example **1.1.0+26.3**. Do not add the product name, “testing”, marketing text or other labels to the title. The tag is `v` followed by the same declared version; GitHub's prerelease status communicates whether it is experimental.

Publish exactly these four uploaded assets:

| Asset | Contents |
| --- | --- |
| `README.md` | Accessible feature/installation overview, with resolvable repository links. |
| `vanilla-plusplus-quality-of-life-suite-<version>.jar` | Verified combined feature mod. |
| `docs.zip` | All tracked project documentation, technical locks/reports, evidence, notices and release checksums. |
| `vanilla-plusplus-installation-pack-<version>.zip` | Suite, exact Defaulted/CodecUI, permitted publisher binaries, licenses/corresponding sources, manifest and `install.py`. |

The installation helper selects client/server files, including optional Mod Menu only when requested on the client and Polymer on the server. Fzzy downloads directly from its official publisher URL; disclose that internet is needed. Minecraft, Fabric Loader, Java and server pack hosting remain prerequisites rather than redistributed installables. GitHub generates source ZIP/tar downloads itself; those do not count as uploaded release assets.

Keep local `SHA256SUMS.sha256` and publication records alongside the four public files. They are also accessible through documentation/checksum records rather than extra public attachments. Never overwrite historical developer/ported release counterparts.

## Publication and branch cleanup

Push main and its version tag, publish the stable release as latest (or mark an experimental release as prerelease), then download and hash all four assets and verify their names, title and commit. Update the workspace release index with paths, source revision and evidence.

After a requested cleanup, confirm each finished feature/testing branch tip is an ancestor of main and record its commit. Detach its local worktree to preserve the source snapshot and any ignored development files. Delete the local and remote branch refs once main is published and verified. Keep historical release tags/builds. The stable 1.1 cleanup removes `merged`, `feat/mapstitch-mixed-scales` and `feat/sensible-stackables`; their commits remain in main history and existing prerelease tags.
