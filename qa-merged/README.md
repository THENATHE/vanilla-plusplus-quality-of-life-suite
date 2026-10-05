# Combined branch QA

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
