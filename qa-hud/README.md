# Independent minimap / Tool Pouch information HUD verification

Stable **1.1.1** restores independent corners for Tool Pouch information and the minimap. Atlas and ordinary-map position controls continue to share only their map settings. Current release scope is recorded in [VALIDATION.md](../docs/VALIDATION.md), with behavior explained in [HUD.md](../docs/HUD.md).

This separate development fixture launches an isolated localhost world and a real graphical client using cached official runtime files. It records final Tool Pouch text coordinates after placement wrappers, compares them against measured current-frame minimap bounds, and saves screenshots. Observer mixins belong only to QA and are never packaged in the suite. Original mod source and runtime classes remain unchanged.

```sh
python3 qa-hud/run.py --label compile-check --prepare-only
python3 qa-hud/run.py --label settings-only --settings-only
python3 qa-hud/run.py --label hud-smoke
```

Use Java 25 and the installed Java 27 compiler targeting Java 25. `DISPLAY=:1` is the existing graphical test display. Each label must be unique. Full smoke uses an existing accepted QA EULA and makes no new EULA decision. `--suite` selects the exact candidate; the default is `build/libs/vanilla-plusplus-quality-of-life-suite-1.1.1+26.3.jar`.

Eleven bounded graphical scenes cover:

- First startup with conflicting native map configurations: existing MapStitch map corner/offsets win, while saved Tool Pouch information stays untouched.
- Atlas and ordinary-map minimaps at top right with information at top left.
- Native Tool Pouch information Apply changing its corner/offsets without moving the minimap.
- Native MapStitch and Tool Pouch map Applies synchronizing only map fields, without moving information.
- Atlas and ordinary-map anchor agreement, including signed map offsets.
- Same-corner top and bottom overlap protection.
- Same-side opposite vertical corners and separated same-corner offsets retaining their natural information placement.
- Precise saved values in both original client configuration files after every scene.
- Opening the combined settings screen with original configuration groups present.

Raw evidence is under `runs/<label>/evidence.json`, with control observations, exact binary hashes, launch audits, console logs and screenshots. Compact acceptance evidence for this release is under `evidence/1.1.1/`. A build or `--prepare-only` result does not demonstrate runtime acceptance. The private official SSO input is authorized for these checks. This fixture does not exercise full gameplay, config permission networking or external accessories, and retained older run records describe their own historical scope.
