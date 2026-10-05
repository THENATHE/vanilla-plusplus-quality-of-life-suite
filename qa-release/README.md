# Stable suite release evidence

Mainline 1.1.0+26.3 evidence is kept under `evidence/1.1.0/`. Promotion compares all 605 packaged Java classes with the tested merged.3 release, then checks final-version native/Fabric fallback connections, actual atlas controls/hotkey/2048-item moves and real settings navigation. Prior broad gameplay/death/HUD evidence remains identified by its original artifact.

```sh
python3 qa/light.py --label <unique-label> --stackables-uncapped --stackables-actions
python3 qa-settings/run.py --suite build/libs/vanilla-plusplus-quality-of-life-suite-1.1.0+26.3.jar --label <unique-label> --settings-only
```

Fixture source and runs are development-only; no QA mod is packaged into the production suite.
