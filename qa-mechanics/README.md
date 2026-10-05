# Suite mechanics and persistence regressions

`run.py` freezes a production JAR and runs disposable dedicated servers using the exact pinned shared libraries, including the requested Defaulted dropfix. It compiles a separate QA mod; no test fixture is packaged in the suite.

The suite regression combines existing focused test code without altering the historical standalone tests:

- All 752 valid Chalk recolor/glow transitions, both input orders, invalid/no-op ingredients, name/damage/custom-data/default-removal preservation, vanilla recipe displays and target models.
- All 32 calcite/color/glow recipes.
- Actual grindstone menus in both input orders and the preserved smithing interaction; curse removal, retained normal enchantments/name/damage, exactly-once consumption and echo-shard reward.
- Nineteen Defaulted regressions: nullable names, returned previous values, prototype refresh/override/removal persistence, copy isolation/serialization, cushion/frame drops and shulker-bullet damage.
- Shared Region Maps canonical map IDs, map pixels/markers, creation, cartography zoom/lock, exclusions, corrupt/missing records and persisted indexes across restart. The combined fixture explicitly accepts and verifies MapStitch's map-center metadata.

```sh
python3 qa-mechanics/run.py --label <unique-label>
python3 qa-mechanics/run.py --label <unique-label> --only maps
```

Full worlds/logs stay under ignored `runs/`; the reviewed summary is in `evidence/`. These direct game-object regressions are separate from GUI and network packet tests and do not establish every mod's gameplay coverage.
