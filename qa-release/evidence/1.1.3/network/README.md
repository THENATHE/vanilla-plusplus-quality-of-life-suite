# 1.1.3 atlas maintenance network QA

Passed on suite `1.1.3+26.3`, SHA-256 `0492e88a51fb88cc5dbc0d815022d3d5a39f095fa299c5069f040ad5be63c69b`, run `atlas-maintenance-frozen113-local`.

Both real Fabric API-only and native suite clients executed registered `/repairmaps` and `/dedupemaps` on a seeded valid atlas. Each repaired missing centers, retained one map entry after removing one duplicate, preserved explored pixels and stayed connected for more than 100 server/client ticks afterward. The fallback received vanilla map packets and zero custom payloads. Native received two advertised refresh payloads and advertised metadata. Every recorded outgoing mod payload used an actually advertised channel.

`result.json` contains both observations, commands, capability state, outgoing channel records and explicit setup-attempt history. `launches.json` preserves sanitized mod/dependency names and hashes; `fixture-inputs.json` records the executed sources and observation JARs. Raw runs are ignored disposable local files.

This is bounded command network regression, not a repeated full suite or pack/GUI matrix. Native graphical refresh has separate evidence. No production change was required by these checks.
