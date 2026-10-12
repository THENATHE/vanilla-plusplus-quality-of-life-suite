# Anvil XP regression

`SwiftAnvilProbe.java` runs real `AnvilMenu.clicked` pickup and quick-move actions in a survival server player fixture. It checks that the previewed XP cost is charged exactly for real End City Swift Flight I, II and III books and a vanilla Unbreaking book, preserves names and enchantments, and conserves input/output items. It also checks ordinary and quick-move repairs of an unenchanted diamond pickaxe with zero player levels: SSO's free-repair option must still permit both actions without charging XP.

Run from the repository with the exact candidate JAR:

```sh
python3 qa-features/anvil/run.py build/libs/vanilla-plusplus-quality-of-life-suite-1.3.1+26.3.jar --work /tmp/vpp-anvil-regression
```

The work directory must be new. The runner resolves retained QA helpers through `tools/workspace_paths.py`, prepares the cached Minecraft classpath, selects the current locked libraries and supplied suite candidate, and runs server profiles with and without Polymer. It uses the same retained access-widened Minecraft compile fixture as `qa-features/run.py`; no previous ignored QA run is required. Local socket access is required. Results record the tested JAR hash. This is server menu gameplay coverage, not a physical client click test.
