# Vanilla++ Quality of Life Suite — project standards

Apply these conventions to every future update and release. User instructions take precedence. The workspace Minecraft compatibility/build rules still apply.

## Public documentation

- Keep README navigation links at the top, followed by What it is, features, the compatibility/shim explanation, installation, and technical info.
- Group features by their original mod. Put canonical Modrinth project links and creator thanks directly below each heading. Keep Chalk and Chalk: Colorful Addon together, with both project links and credits. Keep Sensible Stackables as a full feature section.
- Put suite tweaks beneath the upstream feature section. Describe wholly THENATHE-authored additions directly; do not add a redundant “My tweaks and additions” subsection to them.
- Use “modern Minecraft versions” in overview/feature prose. Describe occasional machine-assisted porting while original authors update independently. Identify ports as unofficial. Keep target/version details precise in installation help, technical references and tables.
- In the front-page dependency list only, omit pinned dependency versions and use canonical current Modrinth project pages. Link the publisher source if no Modrinth project exists. Direct readers to Releases for compatible files. Keep exact versions, hashes, optional integration limits and provenance in technical documentation.
- Update the README, changelog, release guide, installation/dependency records, source/update guide and validation reports with each release. Preserve historical test scope and creator licenses/credits.

## Releases and branches

- Release titles contain only the declared release number and version target, for example `1.1.0+26.3`. Git tags retain the `v` prefix.
- Publish four downloadable assets: `README.md`, the versioned suite `.jar`, `docs.zip`, and one versioned installation `.zip`. GitHub-generated source archives are separate platform features.
- `docs.zip` contains all tracked project documentation, technical dependency/build records, release checksums and recorded evidence. The installation ZIP contains the suite, exact required local libraries, permitted public dependencies, notices, appropriate corresponding sources and a side-aware installer.
- Respect dependency distribution terms. Dependencies that require official manifest downloads remain download-only and must be clearly documented; Fzzy Config is currently such a dependency. Do not bundle it or change libraries to avoid this rule.
- Keep Tool Pouch information placement independent of minimap placement. Synchronize only the atlas/ordinary-map controls and move information only when its rendered bounds actually overlap the map.
- Preserve exact historical Defaulted dropfix as compile-only input for guarded optional compatibility; current SSO/Stackables releases no longer require it at runtime. Keep the separate CodecUI runtime input, native features, saved IDs, separate modules and automatic client/server negotiation. Keep the separately maintained SSO port paused until explicitly resumed.
- Verify the exact final JAR and installer archives before publishing. Distinguish fresh checks from historical acceptance; repeat broader tests only where changed behavior warrants them.
- Store releases under the workspace Minecraft family/component/version hierarchy, with matching local SHA-256 records. Retain historical builds/tags/evidence.
- After completed work is merged and verified on main, remove finished feature/testing branches when authorized. Check ancestry first. Preserve local source worktrees as detached snapshots or archive them; do not discard uncommitted work. Record removed branch tips. Keep main as the active development branch.

The detailed procedure is [docs/RELEASE_WORKFLOW.md](docs/RELEASE_WORKFLOW.md).
