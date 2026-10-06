# Release evidence

Current release **1.1.5+26.3** records native/fallback payload synchronization, real inventory moves, original-module clients, Clumps Mending and clean-build/previous-input comparisons under `evidence/1.1.5/`. Dedicated Stackables override/restart checks and SSO/recipe/drop mechanics are in `../qa-stackables/evidence/1.1.5/` and `../qa-mechanics/evidence/1.1.5/`. Prior atlas graphical and detailed repair/copy acceptance remains identified by its original 1.1.4 artifact under `../qa-multiscale/evidence/1.1.4/`. Read [validation](../docs/VALIDATION.md) for the precise accepted artifact, candidate-versus-final results and historical limits. Development QA mods are never packaged into the production suite.

## Historical 1.1.1 HUD verification

Release **1.1.1+26.3** uses `evidence/1.1.1/` for connection and previous-build comparison records, `../qa-hud/evidence/1.1.1/` for graphical HUD checks, and `../qa-multiscale/evidence/1.1.1/` for server extraction/restart and client dimension/scale rendering checks. Exact artifact hashes and executed scopes are recorded in [validation](../docs/VALIDATION.md).

Mainline 1.1.0+26.3 evidence is kept under `evidence/1.1.0/`. Promotion compares all 605 packaged Java classes with the tested merged.3 release, then checks final-version native/Fabric fallback connections, actual atlas controls/hotkey/2048-item moves and real settings navigation. Prior broad gameplay/death/HUD evidence remains identified by its original artifact.

```sh
python3 qa/light.py --label <unique-label> --stackables-uncapped --stackables-actions
python3 qa-settings/run.py --suite build/libs/vanilla-plusplus-quality-of-life-suite-1.1.0+26.3.jar --label <unique-label> --settings-only
```

Fixture source and runs are development-only; no QA mod is packaged into the production suite.
