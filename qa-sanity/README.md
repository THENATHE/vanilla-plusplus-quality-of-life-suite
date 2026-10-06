# Broad suite sanity fixtures

These disposable dedicated fixtures extend the recent release checks with actual SSO menus/recipes/XP entities, backpack/pouch attachment and transfer/saveback mechanics, and all Chalk mark placement combinations. They invoke the packaged mod and do not replace gameplay implementations. They must never be installed on a real server.

Run from the repository root with the current exact JAR and a unique label:

```sh
python3 qa-sanity/run.py --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.1.5+26.3.jar --label my-sanity-run
```

The runner executes both native and Polymer dedicated profiles without Defaulted at runtime. Cached Java25 launch and Java27 release25 compilation inputs and local socket permissions are required. Accepted QA EULA is reused. Results/worlds remain under ignored `runs/` and are never released as gameplay mods. Synthetic players suppress real packet sending and fixture chunks are explicitly entity-tracked for immediate spawned-entity observations. Real GUI/packet tests are separate.

[The complete 1.1.5 report](../docs/SANITY_1_1_5.md) links the final source/input hashes, sanitized logs, observed checks, preserved earlier fixture-development failures, and practical coverage limits. Original mechanics and source snapshots supply the starting fixtures; obsolete API/reflection/loot-shape assumptions were adapted to the current packaged mods. No original artifact or production class was modified.
