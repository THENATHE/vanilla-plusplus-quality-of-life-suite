# One-off atlas death retention check

This fixture answers the specifically requested container/death case. It is not added to the general testing matrix or shipped in the release JAR.

Run against a packaged suite candidate:

```sh
python3 qa-death-once/run.py --label <unique-label> --jar build/libs/<suite-release>.jar
```

It runs in disposable dedicated worlds with and without Polymer. The fixture invokes the real player `dropEquipment` and `restoreFrom` methods and observes retained inventory and death-drop stacks, including save/load recovery of overflow. It does not claim an interactive death animation, external accessory mods' slots, or a disconnected client respawn UI.

See [the feature and verification notes](../docs/ATLAS_DEATH_RETENTION.md).
