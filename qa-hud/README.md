# Independent minimap / Tool Pouch information HUD verification

Suite **1.4.4+26.3** includes HUD addon **1.0.11+26.3**. The addon measures the actual map and information rectangles before applying overlap protection. Independent information corners and saved offsets remain intact. Current suite release scope is recorded in [VALIDATION.md](../docs/VALIDATION.md), with behavior explained in [HUD.md](../docs/HUD.md).

This development fixture launches an isolated localhost world and a real graphical client using cached official runtime files. Observer mixins record the native renderer's actual element and prepared text bounds, including map backgrounds, canvas, decorations, named markers, captions, clipping and native information. Raw caller coordinates remain separate diagnostic receipts. The fixture compares the complete received rectangles and records exact configurations after every scene. Observer classes are never packaged in the suite.

```sh
python3 qa-hud/run.py --suite build/libs/vanilla-plusplus-quality-of-life-suite-1.4.4+26.3.jar --label hud-prepare --prepare-only
python3 qa-hud/run.py --suite build/libs/vanilla-plusplus-quality-of-life-suite-1.4.4+26.3.jar --label hud-native
python3 qa-hud/run.py --suite build/libs/vanilla-plusplus-quality-of-life-suite-1.4.4+26.3.jar --label hud-client-polymer --client-polymer
```

Use Java 25 and the installed Java 27 compiler targeting Java 25. `DISPLAY=:1` is the shared graphical test display and must be reserved before launching. Each label must be unique. Runtime smoke uses an existing accepted QA EULA. Always select the exact candidate with `--suite`; historical evidence retains its original candidate and dependencies.

Thirty graphical scenes cover:

- Startup with conflicting native map configurations, independent information corner/offsets, native information Apply, and native MapStitch/Tool Pouch map Applies.
- Atlas and ordinary-map anchor agreement, including signed offsets.
- Original top/bottom overlap cases, opposite vertical corners, and separated vertical offsets.
- Atlas and ordinary maps in top/bottom corners with horizontally separate or actually intersecting information rectangles, using map scales 0.5, 0.75 and 1.25.
- Actual horizontal overlap despite opposite configured corner names, for atlas/ordinary maps at both top and bottom.
- Backgroundless atlas captions with an actual one-pixel overlap and a separate touching-boundary control, measured from native prepared text.
- Real named white banners near the map edge with ordinary/atlas maps and clear/backgroundless styles, plus a named marker with genuinely separate information bounds. Measurement scopes are closed after each scene.
- Precise original settings in both native client configuration files after every scene, and the combined settings screen with original configuration groups present.

Fresh evidence is organized beneath [evidence/](evidence/):

| Evidence folder | Purpose |
| --- | --- |
| `2026-10-13-baseline-1.4.3` | Exact old suite fails the same-corner, horizontally disjoint case. Captures executed observer source, bytecode hashes, settings, draw rectangles and screenshots. |
| `2026-10-13-initial-1.4.4` | First fixed candidate passes 23 scenes on both profiles. Historical candidate, superseded before release. |
| `2026-10-13-initial-1.4.4-shadow-failure` | Earlier approximate font-box observer reports a caption-shadow failure; native prepared-text measurements later classify it as a fixture false positive. Executed observer source/classes remain preserved. |
| `2026-10-13-final-1.4.4-before-canvas-observer` | Final production passes 24 scenes on both profiles with background/caption/information receipts. Retained before adding the canvas observer. |
| `2026-10-13-ca8a-edge-name-failure` | The intermediate ca8a candidate fails actual native edge-name overlap on an ordinary clear map. Real banner data, source/classes and graphical proof retained. |
| `2026-10-13-e466-native-bounds-fixture-correction` | Exact native rectangles touch at Y149 and correctly retain native placement; old expected-overlap assertion is corrected. |
| `2026-10-13-final-1.4.4-native-bounds` | Expanded final30-scene matrix measures native element/prepared-text submissions, including edge names and scope cleanup. |
| `2026-10-13-final-1.4.4-complete-bounds` | Initial29-scene observer snapshot before correcting its caption overlap expectation. |
| `2026-10-13-final-1.4.4` | Intermediate ca8a candidate passes 24 scenes with actual canvas submissions, then fails the later real edge-name regression. Preserved with its original scope. |

Raw run evidence remains under `runs/<label>/evidence.json`. A build or `--prepare-only` result does not establish runtime acceptance. The exact official developer SSO 2.10.0+26.3 input remains included; the paused ChatGPT SSO port is excluded. This fixture verifies HUD geometry, saved native settings and configuration navigation. Full gameplay, connection capability networking and all-four wheel/BRBR interactions have separate acceptance profiles.

QA binaries remain local execution artifacts and are excluded from public source/documentation packages. Recorded hashes, fixture source and sanitized evidence identify exact execution without distributing those binaries.
