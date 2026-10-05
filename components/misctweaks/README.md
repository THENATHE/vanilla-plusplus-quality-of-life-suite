# MiscTweaks component

The suite nests Pajic's unchanged official Fabric **1.4.4+26.3** release for Minecraft **26.3**. The Fabric metadata declares version `1.4.4`; the `+26.3` suffix belongs to the upstream release filename. This component has no suite-specific patches and no ChatGPT port counterpart.

## Preserved inputs

- `upstream/artifacts/`: official installable JAR, exact published sources JAR, pinned GitHub source archive.
- `upstream/source/`: extracted official sources JAR, the release-specific source authority.
- `upstream/repository/`: GitHub commit `265a64d4565fbe4aef324eeaa6d9a82606171a46` on `multicutter-v3`, including original build files and MIT copyright/license. A repository snapshot can include unpublished changes; use the published sources when reviewing this exact binary.
- `upstream/metadata/`: captured Modrinth project/version and GitHub revision metadata.
- `inputs.lock.json`: all SHA-256 checksums, provenance, dependencies and settings identifiers.
- `build.py`: verifies and stages the unmodified binary into `build/developer/`.

Binary SHA-256: `97fe263764cb6b93822e13fe9423cf91c3a6a0169aeeb8734b65136cc9ea9647`.

## Settings retained

- `misctweaks:config`
- `misctweaks:client_config`

Fzzy Config still owns validation, persistence, server synchronization, permissions, restart notices and resource-reload notices. The suite settings page should open these original config identifiers rather than duplicate or migrate their fields. Existing config files continue to use the original mod namespace.

## Updating

1. Choose the next official Fabric release for the desired Minecraft version.
2. Download the official JAR and its accompanying sources JAR; verify Modrinth-provided SHA-512 and SHA-1.
3. Preserve a new pinned GitHub source snapshot and copyright/license.
4. Update `inputs.lock.json` and the suite's `docs/pajic-component-inputs.json` with new version, hashes and required dependencies.
5. Re-run `python3 build.py`; include the new staged JAR in the suite packager.
6. Check original configuration IDs and fields for changes. Keep existing settings/migrations rather than flattening them into the suite's own schema.

Source: https://github.com/pajicadvance/misctweaks. Release: https://modrinth.com/mod/misctweaks/version/c2JwxbVC.

MiscTweaks requires Mixson, Fabric API and Fzzy Config. Its client appearance settings and Sodium/Raised integration require the client component; server gameplay tweaks operate on vanilla objects. Do not remove Mixson asset patching to simplify packaging.
