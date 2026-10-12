# Pouch upgrade and integrated wrench regression

Run from the repository root:

```sh
python3 qa-pickup/run.py --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.2.0+26.3.jar --label <unique-label>
```

`--compile-only` compiles the fixtures without launching a runtime. Runtime tests freeze the input JAR, record exact dependency/launch/source hashes, and run dedicated native and Polymer profiles through initial and restart phases. Disposable worlds and raw logs remain under ignored `runs/`.

The pickup fixture exercises real dropped-item `playerTouch` calls and the exact server magnet scan entry, actual crafting/attachment recipes, delay/ownership, spherical range, independent and combined routing, commands, inactive pouch/loose upgrades, live menu safeguards, full/partial inventory conservation, nesting/shulker rules, oversized saved-container protection, spectator guards and player save/load/respawn/restart. Players have stub packet listeners; this is dedicated mechanics evidence, not physical network acceptance.

The wrench fixture in `../qa-pouch-utilities/src/DebugWrenchChecks.java` runs the retained block-state/selection/cooldown/protection/crafting checks and fixed-sprite Polymer projections. Development fixtures are never packaged in the release mod.

## Physical clients and legacy negotiation

```sh
python3 qa-pickup/physical.py --jar <suite.jar> --label <unique-label> --stackables-actions --stackables-uncapped
python3 qa-pickup/physical.py --jar <suite.jar> --label <different-label> --legacy-suite <previous-1.1.6-suite.jar>
```

This isolated extension of the existing bounded connection fixture verifies a real connected player collects 17 distant diamonds into a backpack, receives native or Polymer item IDs with fixed model IDs, and toggles the collector off/on using the actual registered keybinding on native clients. Unsupported clients use the registered commands. Native/Fabric/legacy clients capture a rendered inventory screenshot with all three new sprites. The old suite profile proves the expanded versioned handshake avoids old-message decoder failures and selects the three independently advertised original capabilities while the root/wrench content uses fallback. `--all-profiles` includes pure vanilla and a no-Polymer native server; the pure vanilla process uses no loader or observation mod and has server-side pickup/command evidence only. Singleplayer uses `--integrated-only` and a disposable copied world template.

The physical source copies are independent of historical `qa/` fixtures. Fresh evidence is frozen with its executed source and exact JAR hash; no previous result is relabeled.
