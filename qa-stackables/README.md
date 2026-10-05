# Sensible Stackables conservation and restart checks

`run.py` creates disposable dedicated servers, pins every runtime mod by SHA-256, and runs actual Minecraft menu implementations with a server-player fixture. It checks half pickup, single placement, remainder placement, shift movement, drag distribution, invalid-slot conservation, one-book anvil combination with both input remainders, item serialization, container drops, and persisted chest contents after a real process restart.

Default stack size 64 and restart-required uncapped size 2048 are tested. Raw vanilla item stream-codec round trips also check counts 99, 100, 32768, and 2147483647 with corresponding maximum metadata. Those codec boundaries demonstrate transport capacity; they do not claim complete gameplay at Integer.MAX_VALUE or successful unmodified-client hashing of metadata greater than 99. Actual fallback packets cap only the latter metadata to 99.

The suite run covers Polymer defaults, Polymer uncapped, and native server uncapped. The original developer baseline covers defaults and uncapped using untouched Sensible Stackables 3.0.3+26.2 and its independent dependency set. These fixtures do not connect a client and are not substitutes for `qa/light.py` native, Fabric-only, and vanilla click tests. They do not claim a developer-targeted Polymer shim exists.

Run from the repository root:

```sh
python3 qa-stackables/run.py --label conservation
python3 qa-stackables/run.py --label developer-baseline --upstream-baseline
```

Tracked `evidence/` contains result JSON and exact runtime input audits. Local disposable worlds/logs under `runs/` are ignored. Preliminary fixture-development failures are not release evidence.

## Recorded results

- [Final ported suite](evidence/final-context-02/result.json): six cases, 131 assertions, all passed against SHA-256 `40c7ad4b454e8caa2a4518fcd246aa8fb54feeb65cc98ecc87dfd2a4af334acb`. Polymer defaults, Polymer uncapped, and native uncapped each passed before and after process restart. Direct Polymer transformation also preserved exact counts and server originals while producing safe fallback maximum metadata.
- [Untouched developer release](evidence/upstream-26.2-01/result.json): four cases, 82 assertions, all passed against original 26.2 binary SHA-256 `3adefd8ae5fb59502e52cd830e9fe28cded4f8c71009122d3c06bfbd8107633b`. Defaults and uncapped each passed before and after restart.
- [Published source compilation](../components/sensible-stackables/upstream/metadata/source-compilation.json): 25 Fabric source files compiled successfully against Minecraft 26.2 and the separate recorded upstream dependency set.
