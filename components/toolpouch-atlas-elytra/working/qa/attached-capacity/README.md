# Attached Tool Pouch capacity regression

This fixture uses original Tool Pouch 1.1.10, released Multi-Shim 1.0.3, and the supplied addon JAR. It launches an isolated dedicated server and a real native Fabric client; original mod JARs are checked for modification before and after each run. No production classes are replaced by the fixture.

Eight scenarios cover ordinary and netherite pouches, standalone and attached to equipped netherite leggings. Four use default dimensions (ordinary 4×4, netherite 5×5); four explicitly configure ordinary 2×3 and netherite 5×7 on both endpoints. Contents are uniquely named compasses, one per slot.

Each scenario exercises the original ToolPouchUtil replacement method at the final physical slot, opens the actual native menu using the original networking payload, observes client and server slot counts, withdraws the final accessible item using an ordinary quick-move click, deposits it using ordinary pickup clicks, closes, reopens, and verifies exact item conservation. Native screenshots are captured at each opening. Attach/detach behavior is exercised through the original recipe `matches`, `assemble`, and remainder methods; this is recipe integration coverage, not a claim that the crafting-grid UI was driven.

The lifecycle regression also verifies stored dye, armor damage, correct detached pouch type, cleanup of the old tier marker, and reattachment of an ordinary pouch to leggings bearing an obsolete netherite marker. The final fixture adds explicit false/bare/unrelated marker controls, inventory duplicate checks after deposit, and complete item-component NBT encode/decode equality after each close.

Baseline addon 1.0.2 reproduces two failures: attached netherite pouches use ordinary dimensions, and detaching leaves a tier marker that upgrades a subsequently attached ordinary pouch. It intentionally checks for the known broken results so the full baseline suite completes. At default dimensions, opening and closing retains 16 of 25 stored items; the custom-dimension case retains 6 of 35. This happens in a disposable fixture world only.

Example commands (from the addon repository):

```sh
python qa/attached-capacity/run.py --track developer --label baseline-unique --addon '/path/to/toolpouch-atlas-elytra-compat-1.0.2+26.3.jar'
python qa/attached-capacity/run.py --track developer --label fixed-developer-unique --candidate --addon build/libs/toolpouch-atlas-elytra-compat-1.0.3+26.3.jar
python qa/attached-capacity/run.py --track sso-port --label fixed-port-unique --candidate --addon build/libs/toolpouch-atlas-elytra-compat-1.0.3+26.3.jar
```

Developer and SSO-port tracks use separate dependency lists and ports (25896 and 25897); `--port` overrides either. `--track isolated` runs Tool Pouch and its required libraries with the addon and no combined shim/SSO stack. The existing developer staging baseline provides loader/runtime dependencies, and official Tiered Backpacks 1.0.20 is selected explicitly for full-stack tests. Every run creates a fresh directory and records exact artifact hashes in `runs/<label>/evidence.json`.

Scope: custom dimensions are deliberately installed in both client and server fixture configuration; the suite does not test Fzzy Config's live configuration-sync UI. NBT roundtrips test serialized item preservation, not a full process restart. Native screenshots may include tutorial notifications or hover tooltips; behavioral assertions establish exact final-slot preservation independently of those overlays.

## Recorded results

- Released addon 1.0.2 baseline: eight scenarios completed, 146 assertions; both capacity/data-loss and tier-marker bugs reproduced.
- Candidate addon 1.0.3 developer-release stack: eight scenarios passed, 178 assertions, original JARs unchanged.
- Candidate addon 1.0.3 SSO-port stack: the same final fixture passed all 178 assertions, original JARs unchanged.

The baseline fixture predates 28 additional boundary/conservation assertions plus the four successful netherite utility checks that are expected to reject on the baseline. It exercises the same eight menu scenarios. Both final candidate runs use identical fixture source and assertion coverage.

Sanitized manifests and observations are preserved as `docs/1.0.3/capacity-baseline.json`, `capacity-developer.json`, and `capacity-sso-port.json`; representative actual GUI screenshots are in `docs/1.0.3/screenshots/`. Visual inspection confirms the baseline 4×4 attached grid becomes 5×5 by default and 5×7 with the custom configuration. Screenshots contain overlays; real clicks and client inventory observations are paired with server assertions of exact last-slot preservation and total contents.
