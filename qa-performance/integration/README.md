# Performance candidate integration checks

This isolated fixture tests the exact Vanilla++ 1.4.5+26.3 candidate with Hotkey
Wheel 1.0.8+26.3, official BuildPrint 0.1.25+26.3, Blocktone 0.1.25 and optional
Polymer 0.18.2+26.3. Better Recipe Book 1.11.7+26.3 remains present in both profiles.
The six integration fixtures are preserved copies from the Blocktone 0.1.25
integration fixture. `AllocationClientTest` additionally exercises the atlas
behavior and allocation helper from `qa-allocation/AtlasCacheQa.java` on the real
client thread in a fresh game-test world.

Run from the suite repository with a complete Java 25 JDK. Preparation copies only
into the selected disposable run directory and compiles fixtures without launching
Minecraft:

```sh
python3 qa-performance/integration/run.py --suite --prepare-only
python3 qa-performance/integration/run.py --polymer --prepare-only
```

After reserving the graphical display, run the two profiles sequentially:

```sh
DISPLAY=:1 python3 qa-performance/integration/run.py --suite
DISPLAY=:1 python3 qa-performance/integration/run.py --polymer
```

`HOTKEY_WHEEL_QA_RUN_ROOT` selects a disposable run directory. The runner records
exact installed JAR hashes and executed fixture source hashes, including the atlas
helper. The helper is compiled into the QA mod but is not an entrypoint. The seven
reports include physical Mouse 4 editor closure, wheel and configuration behavior,
and `allocation-results.json`. A preparation result is not runtime acceptance.
