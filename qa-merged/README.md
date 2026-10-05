# Historical merged-branch QA

Stable **1.1** now includes these modules on `main`. Current release checks are reported in [validation](../docs/VALIDATION.md), and current installation/manual testing steps are in [RELEASE_1_1.md](../docs/RELEASE_1_1.md). This directory preserves the original branch artifacts, hashes and observations without relabeling them as tests of the stable release. Generic build/validation links in older records refer to their original publication context.

# Historical merged.3 focused verification

Evidence is under `evidence/merged3`: one final native/client-Polymer atlas control and tooltip run, final 12-category Configure... labels/navigation run, and previous-build module comparison. Broad gameplay matrices were not rerun for this small presentation change.

```sh
python3 qa/light.py --label <unique-label> --client-polymer-only
python3 qa-settings/run.py --suite build/libs/vanilla-plusplus-quality-of-life-suite-1.0.2-merged.3+26.3.jar --label <unique-label> --settings-only
```

# Combined branch QA

Historical testing target: **1.0.2-merged.2+26.3** on `merged`. The final hash and published verification scope are in [build verification](../docs/build-verification.json) and [validation](../docs/VALIDATION.md). Historical evidence at the original `evidence/` paths below still belongs to merged.1; never attribute its hash or assertion totals to merged.2.

Focused commands from the repository root:

```sh
python3 qa/light.py --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.0.2-merged.2+26.3.jar --label merged2-network-repeat --stackables-uncapped --stackables-actions
python3 qa-multiscale/run.py --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.0.2-merged.2+26.3.jar --label merged2-maps-repeat --only maps
python3 qa-settings/run.py --suite build/libs/vanilla-plusplus-quality-of-life-suite-1.0.2-merged.2+26.3.jar --label merged2-settings-repeat --settings-only
python3 qa-hud/run.py --suite build/libs/vanilla-plusplus-quality-of-life-suite-1.0.2-merged.2+26.3.jar --label merged2-hud-repeat
```

Use a new label for every run. These are focused rerun recipes, not a claim that each example label was executed. The native/Fabric connection pair exercises actual atlas buttons and 2,048-item inventory moves. Dedicated map checks exercise generation masks, independent minimap selection, codecs/restarts, source guards and creation sound packets. Settings checks exercise actual Mod Menu roots/children/hidden state and Chalk's config factory, real native configuration labels/widgets, persistence, and the registered hotkey. HUD and nested retention coverage is recorded separately in final validation.

Each runtime captures its JAR hash and inputs. If a final presentation-only rebuild follows successful mechanics/HUD/network checks, compare production entries and retain both hashes plus the final settings run; this is evidence reuse with an explicit scope, not a fresh execution of the older tests against the final JAR. QA fixtures remain development-only and are excluded from the installable artifact. Exact Defaulted dropfix and the private official original SSO release remain in use; the paused ChatGPT SSO port is not tested.

See [historical branch manual testing steps](../docs/MERGED_TESTING.md).

# Historical merged.1 QA

Evidence under `evidence/` belongs to merged suite 1.0.2-merged.1+26.3, SHA-256 b04ca184a6b8e82700a2b0df96f1fcdbd06e485cc778a6a0fad5666d571bbe9b. Per-profile inputs are retained alongside each result. Test fixtures are development-only and excluded from installable JARs.

Commands run from the repository root against that frozen artifact:

```sh
python3 qa/light.py --label merged-network-02 --stackables-uncapped --stackables-actions --all-profiles
python3 qa/light.py --label merged-client-polymer-01 --stackables-uncapped --stackables-actions --client-polymer-only
python3 qa-multiscale/run.py --label merged-maps-04
python3 qa-stackables/run.py --label merged-stackables-02
python3 qa-settings/run.py --suite build/libs/vanilla-plusplus-quality-of-life-suite-1.0.2-merged.1+26.3.jar --label merged-settings-01 --settings-only
```

Use new labels on repeat runs. See [release scope and manual steps](../docs/MERGED_TESTING.md). Original SSO 26.3 is used; the paused SSO port was not built or tested. The separate unchanged original Stackables 26.2 baseline remains historical; these combined checks target the suite's 26.3 port.
