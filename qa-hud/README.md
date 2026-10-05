# Shared atlas / ordinary-map HUD verification

The synchronized layout is included in stable **1.1** on `main`; current release checks are recorded in [VALIDATION.md](../docs/VALIDATION.md).

The current fixture uses eight bounded graphical cases to check synchronized native position controls, saved native files, atlas/ordinary-map identical corner anchors, signed offsets, actual map-versus-detail bounds, and the combined settings screen. It captures final renderer text coordinates and screenshots rather than guessing visibility from config values. See [shared HUD documentation](../docs/HUD.md).

Existing evidence below describes the previous independent-position implementation and is retained as history.

# Focused graphical HUD and settings smoke

This separate development fixture uses cached official Minecraft/Fabric runtime files. It never belongs inside the installable suite. It launches isolated localhost worlds and a real graphical client, records actual final Tool Pouch renderer text arguments, checks layout against current-frame minimap bounds, and saves screenshots. No original mod source or runtime class is replaced. QA mixins observe the rendering calls only.

```sh
python3 qa-hud/run.py --label compile-check --prepare-only
python3 qa-hud/run.py --label settings-only --settings-only
python3 qa-hud/run.py --label hud-smoke
```

Use a complete Java 25 runtime and the installed Java 27 compiler targeting Java 25. `DISPLAY=:1` is the existing graphical test display. Run labels must be unique. Full HUD smoke uses an existing accepted QA EULA; it does not make a new EULA decision. `--suite` selects the exact release candidate. Default JAR is `build/libs/vanilla-plusplus-quality-of-life-suite-1.0.0+26.3.jar`.

Cases: same-side top-left/right, opposite sides, larger map with its own information rows, minimap toggle off/on, atlas removal/restore, bottom-corner placement, and opening the combined settings screen with all six original configuration groups present. The server fixture prepares one pouch/atlas/compass/clock setup. It does not exercise full gameplay, config network permission changes, or save migration.

Evidence is under `runs/<label>/evidence.json`, with control observations, exact binary hashes, launch audits, console logs and client screenshots. Build success or `--prepare-only` is not runtime validation. The private official SSO input is authorized for suite testing. Prepared fixtures alone are not completed tests.
