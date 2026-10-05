# sso developer component

Pinned upstream Fabric release **2.9.14+26.2**, mod ID `simple_smithing_overhaul`, Fabric metadata version `2.9.14`.

- `upstream/artifacts/`: exact official binary, published sources JAR and pinned GitHub ZIP.
- `upstream/source/`: extracted published sources, authoritative for this binary.
- `upstream/repository/`: pinned GitHub `979a4d39763c63c7d0956f8c39b6ca56600962e5` build/history snapshot. HEAD may contain unpublished changes.
- `upstream/metadata/`: captured provenance.
- `inputs.lock.json`: versions, SHA-256, dependencies and config IDs.
- `build.py`: integrity/hash-checks and stages the unchanged binary; does not rebuild or port it.

## Existing settings

- `simple_smithing_overhaul:config-v2`

Fzzy Config owns persistence, validation, sync and permissions. The suite's unified sidebar retains original active/default objects and IDs. Settings added by upstream appear automatically when registered in this component's namespace.

## Update procedure

Edit `tools/fetch-upstream.py` pinned version, run `python3 tools/fetch-upstream.py sso`, review changes against the previous published source and any recorded suite patches, verify dependency changes, update root `locks/artifacts.json` and runtime notes, then rebuild the combined suite. Preserve the previous immutable sources/release records in their versioned release folder before updating snapshots. Do not replace library dependencies to make a build pass.

Original MIT notice: `upstream/repository/LICENSE`. Include it in the distributable outer suite and credits.

Source: https://github.com/pajicadvance/simple-smithing-overhaul. Release: https://modrinth.com/mod/simple-smithing-overhaul/version/yzzzEFXt.

SHA-256: `f393e8b48d6bdf06e69290843c0d041d5f70449f2af7355eaf4c7249bc97fc56`.

## Private official developer release for26.3

The suite's26.3 runtime uses `private-developer-release/simple_smithing_overhaul-fabric-2.9.14+26.3.jar`, confirmed by the user as a private official developer release on2026-10-04. It is retained unchanged with SHA-256 `fb6cbf8c13938d68fb19171dac386b5567d71625bce727b22d3380f6b7880126`. Its separate lock records dependencies and provenance; no public Modrinth version ID is invented for it.

The public Modrinth2.9.14+26.2 binary and published sources remain preserved independently in `upstream/`. That public binary requires `~26.2` and is not used in the26.3 suite. The source audit found81/83 compiled classes identical to the public developer release; the two differing classes correspond to26.3 branches already present in the public sources. The access widener has identical contents under its26.3 filename. This supports source correspondence, but does not establish an exact private build source revision. See `upstream/metadata/local26.3-analysis.json`.

The separate ChatGPT SSO-port remains paused. No artifact from that port track is included, rebuilt or tested by this suite.
