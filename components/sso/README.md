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

## SSO26.3 provenance blocker

The requested developer26.3 release is not published on Modrinth and this repository has no GitHub releases. The preserved official Fabric2.9.14+26.2 binary requires `~26.2`; it is not an installable26.3 input as-is. The pre-existing local file named `simple_smithing_overhaul-fabric-2.9.14+26.3.jar` differs in two compiled classes, manifest, metadata and access-widener path. Its developer-release provenance must be resolved separately. The paused SSO-port track is not built, modified or tested by this source-capture work.
