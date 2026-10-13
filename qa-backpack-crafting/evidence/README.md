# Evidence scope

All fixtures are development-only and use real Minecraft menus or connected client/server processes. JSON records identify the tested artifact hashes. Screenshots support layout review; they do not substitute for server-side conservation assertions.

- `clientsort-server`: 126 native checks and 126 Polymer checks, including actual ClientSort schema/handler validation and occupied crafting-slot protection.
- `gui/native-12` and `gui/polymer-12`: native recipe-book clicks, backpack-only ingredients, actual output clicks, ghost ingredients, and viewport/tab bounds for leather/netherite backpacks with both hammer tiers. Tested compact-panel candidate `aa29bbb1fe278b5fbf1f280513160c563e2bb5e2dd3e636c4ec29133d5979c52` before the later optional BRBR metadata and coordinator-version changes.
- `gui/brbr-trace-15`: four ordinary instant-craft cases with published BRBR 1.11.1 on suite `52db5d7deb0e291a04468284578fbd7d6cad7168fb33640d8a093c8474508dc8`.
- `gui/brbr-local-16`: eight ordinary instant-craft cases with local BRBR 1.11.2 on the same suite candidate.
- `gui/brbr-baseline20`: unchanged vanilla crafting-table baseline, four ordinary and four maximum instant-craft cases with published BRBR 1.11.1 on suite `0310791d5d23428b6e0304c694d0c0d065e8446c8c259c11e619f7ed6f104a06`.
- `gui/brbr-custom21`: eight backpack cases on that same suite and published BRBR, including four ordinary and four maximum instant crafts. All ingredients came from backpack storage, all outputs were acknowledged by the server, and conservation held.
- `gui/brbr-intermittent`: bounded evidence of an earlier intermittent cancellation in BRBR's output/grid-match guard. No ingredients were lost. The later passing repeat did not by itself establish that the intermittent issue was resolved; final guard verification is recorded separately below.

The `executed-source` snapshots and fixture class SHA-256 manifests make successful runs distinguishable from later harness edits. Connection boundaries and Hotkey Wheel checks are recorded by the suite's separate integration fixtures.

Additional suite verification:

- [Final artifact and sprite metadata](artifact.json) records the final JAR hash and exact pixel-grid properties.
- [Connection and integrated-world checks](connections/README.md) cover native, Fabric-only and vanilla boundaries, including unsupported backpack opening and hammer representations.
- [Hotkey Wheel integration](wheel/) contains exact native/Polymer wheel and configuration navigation evidence.
- [Suite regression checks](suite-regression/) preserve server gameplay regression results and their tested hashes separately from the final optional BRBR client guard.

Final BRBR verification: [stable machine-readable summary](gui/final-verification.json). Published 1.11.1 and local 1.11.2 each passed eight ordinary/maximum cases on final suite 23459; four additional synthetic incomplete-grid cases passed with 12 deliberately unmatched predicate checks. Timeout, user-input cancellation and correction cancellation are code-preserved, not separately exercised by these final cases. Duplicate historical screenshots were trimmed; exact JSON, source snapshots and fixture class hashes remain.
