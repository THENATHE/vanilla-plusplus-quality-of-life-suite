# Simple Death Improvements component

The suite nests Pajic's unchanged official Fabric **1.6.0+26.3** release for Minecraft **26.3**. The Fabric metadata declares version `1.6.0`; the `+26.3` suffix belongs to the upstream release filename. This component has no suite-specific patches and no ChatGPT port counterpart.

## Preserved inputs

- `upstream/artifacts/`: official installable JAR, exact published sources JAR, pinned GitHub source archive.
- `upstream/source/`: extracted official sources JAR, the release-specific source authority.
- `upstream/repository/`: GitHub commit `51ed1dc83bde530d1b70dbcb56dfd11aa04d59f2` on `multicutter-v3`, including original build files and MIT copyright/license. A repository snapshot can include unpublished changes; use the published sources when reviewing this exact binary.
- `upstream/metadata/`: captured Modrinth project/version and GitHub revision metadata.
- `inputs.lock.json`: all SHA-256 checksums, provenance, dependencies and settings identifiers.
- `build.py`: verifies and stages the unmodified binary into `build/developer/`.

Binary SHA-256: `67070a8add053ee1a2cad1c310d36cdac0cf53c7950873e20d43d7da8a0ffcc2`.

## Settings retained

- `simple_death_improvements:config`

Fzzy Config still owns validation, persistence, server synchronization, permissions, restart notices and resource-reload notices. The suite settings page should open these original config identifiers rather than duplicate or migrate their fields. Existing config files continue to use the original mod namespace.

## Updating

1. Choose the next official Fabric release for the desired Minecraft version.
2. Download the official JAR and its accompanying sources JAR; verify Modrinth-provided SHA-512 and SHA-1.
3. Preserve a new pinned GitHub source snapshot and copyright/license.
4. Update `inputs.lock.json` and the suite's `docs/pajic-component-inputs.json` with new version, hashes and required dependencies.
5. Re-run `python3 build.py`; include the new staged JAR in the suite packager.
6. Check original configuration IDs and fields for changes. Keep existing settings/migrations rather than flattening them into the suite's own schema.

Source: https://github.com/pajicadvance/simple-death-improvements. Release: https://modrinth.com/mod/simple-death-improvements/version/AYTsBnki.

Simple Death Improvements retains optional accessory integrations with Ohmega/Trinkets on Fabric and Curios on NeoForge. The Fabric suite does not bundle those optional mods. Death item/XP behavior operates on vanilla objects and does not need a custom client packet.
