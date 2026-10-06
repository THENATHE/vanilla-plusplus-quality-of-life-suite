# 1.1.5 atlas death/container sanity check

On 2026-10-06, the unchanged published `1.1.5+26.3` suite passed **254 assertions across 50 death-case runs**: the same 25 scenarios with Polymer and without Polymer. Both dedicated servers exited normally with status 0. Defaulted was absent from both profiles.

Suite SHA-256: `67be52fd07c2d3dcfda4045dff5463e45bc0d91d37d3f759f4ebb3be043cd8f5`.

Coverage includes loose/offhand atlases; standalone and attached pouches; final contents of open pouch/backpack menus; every backpack tier; attached chestplates; shulker boxes; selected bundle contents and nested bundle/shulker/backpack storage; multiple atlases separated by empty slots; disabled retention and keep-inventory controls; vanishing atlases/containers; carried atlases with full inventory; and retained overflow saved, loaded and returned when space becomes available. The fixture verifies that only atlases are retained and unrelated containers/items are conserved in drops.

[result.json](result.json) contains exact artifact/fixture/source hashes and process outcomes. [matrix.json](matrix.json) lists all scenarios. Profile audits include installed mod hashes and launch commands. Console logs replace local path prefixes; raw logs, worlds and compiled QA fixture remain in the ignored runner directory.

```sh
python3 qa-death-once/run.py --label <unique-label> --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.1.5+26.3.jar
```

This reruns the previously requested one-off regression as part of the user's broad sanity pass. It invokes actual packaged death-drop, restore and player save/load methods. It does not claim interactive death animations, external accessory-mod slots, or remote respawn UI behavior.
