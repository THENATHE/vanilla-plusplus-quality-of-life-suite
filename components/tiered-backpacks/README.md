# tiered-backpacks developer component

Pinned upstream Fabric release **1.0.20+26.3**, mod ID `tiered_backpacks`, Fabric metadata version `1.0.20`.

- `upstream/artifacts/`: exact official binary, published sources JAR and pinned GitHub ZIP.
- `upstream/source/`: extracted published sources, authoritative for this binary.
- `upstream/repository/`: pinned GitHub `f282104e9201c6d5200ca9469761cbc58ed0cf3e` build/history snapshot. HEAD may contain unpublished changes.
- `upstream/metadata/`: captured provenance.
- `inputs.lock.json`: versions, SHA-256, dependencies and config IDs.
- `build.py`: integrity/hash-checks and stages the unchanged binary; does not rebuild or port it.

## Existing settings

- `tiered_backpacks:config`

Fzzy Config owns persistence, validation, sync and permissions. The suite's unified sidebar retains original active/default objects and IDs. Settings added by upstream appear automatically when registered in this component's namespace.

## Update procedure

Edit `tools/fetch-upstream.py` pinned version, run `python3 tools/fetch-upstream.py tiered-backpacks`, review changes against the previous published source and any recorded suite patches, verify dependency changes, update root `locks/artifacts.json` and runtime notes, then rebuild the combined suite. Preserve the previous immutable sources/release records in their versioned release folder before updating snapshots. Do not replace library dependencies to make a build pass.

Original MIT notice: `upstream/repository/LICENSE`. Include it in the distributable outer suite and credits.

Source: https://github.com/pajicadvance/tiered_backpacks. Release: https://modrinth.com/mod/tiered-backpacks/version/GHnqX59p.

SHA-256: `b129b9b61ec1da62842dc2dfa5b166a6df8e91c796666dcee70f7b8acf1ecc33`.
