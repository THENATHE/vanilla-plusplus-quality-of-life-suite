# mapstitch developer component

Pinned upstream Fabric release **1.1.6+26.3**, mod ID `mapstitch`, Fabric metadata version `1.1.6`.

- `upstream/artifacts/`: exact official binary, published sources JAR and pinned GitHub ZIP.
- `upstream/source/`: extracted published sources, authoritative for this binary.
- `upstream/repository/`: pinned GitHub `6a10ed9ca26ffce49ab8cee03871a6e56d1eceb0` build/history snapshot. HEAD may contain unpublished changes.
- `upstream/metadata/`: captured provenance.
- `inputs.lock.json`: versions, SHA-256, dependencies and config IDs.
- `build.py`: integrity/hash-checks and stages the unchanged binary; does not rebuild or port it.

## Existing settings

- `mapstitch:client_config`
- `mapstitch:config`

Fzzy Config owns persistence, validation, sync and permissions. The suite's unified sidebar retains original active/default objects and IDs. Settings added by upstream appear automatically when registered in this component's namespace.

## Update procedure

Edit `tools/fetch-upstream.py` pinned version, run `python3 tools/fetch-upstream.py mapstitch`, review changes against the previous published source and any recorded suite patches, verify dependency changes, update root `locks/artifacts.json` and runtime notes, then rebuild the combined suite. Preserve the previous immutable sources/release records in their versioned release folder before updating snapshots. Do not replace library dependencies to make a build pass.

Original MIT notice: `upstream/repository/LICENSE`. Include it in the distributable outer suite and credits.

Source: https://github.com/pajicadvance/mapstitch. Release: https://modrinth.com/mod/mapstitch/version/kRE6nsB7.

SHA-256: `e1b768bbd1ae06f83305eeba3ab19bfe4eb99bf21de12d57364d04ce9a1cde81`.
