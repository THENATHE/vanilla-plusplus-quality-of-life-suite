# toolpouch developer component

Pinned upstream Fabric release **1.1.10+26.3**, mod ID `toolpouch`, Fabric metadata version `1.1.10`.

- `upstream/artifacts/`: exact official binary, published sources JAR and pinned GitHub ZIP.
- `upstream/source/`: extracted published sources, authoritative for this binary.
- `upstream/repository/`: pinned GitHub `9a7e55837e92fd4eb2ea6ec61e395d31b052dc37` build/history snapshot. HEAD may contain unpublished changes.
- `upstream/metadata/`: captured provenance.
- `inputs.lock.json`: versions, SHA-256, dependencies and config IDs.
- `build.py`: integrity/hash-checks and stages the unchanged binary; does not rebuild or port it.

## Existing settings

- `toolpouch:client_config`
- `toolpouch:config`

Fzzy Config owns persistence, validation, sync and permissions. The suite's unified sidebar retains original active/default objects and IDs. Settings added by upstream appear automatically when registered in this component's namespace.

## Update procedure

Edit `tools/fetch-upstream.py` pinned version, run `python3 tools/fetch-upstream.py toolpouch`, review changes against the previous published source and any recorded suite patches, verify dependency changes, update root `locks/artifacts.json` and runtime notes, then rebuild the combined suite. Preserve the previous immutable sources/release records in their versioned release folder before updating snapshots. Do not replace library dependencies to make a build pass.

Original MIT notice: `upstream/repository/LICENSE`. Include it in the distributable outer suite and credits.

Source: https://github.com/pajicadvance/toolpouch. Release: https://modrinth.com/mod/tool-pouch/version/6Z3zMyN8.

SHA-256: `a5bb4f084e7d929f3a89cc43317af63bd45abe3e3f4fed8b45f99b2da846e903`.
