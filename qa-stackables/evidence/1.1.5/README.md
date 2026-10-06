# Sensible Stackables developer-release suite checks

Verified on 2026-10-06 against suite `1.1.5+26.3`, SHA-256
`67be52fd07c2d3dcfda4045dff5463e45bc0d91d37d3f759f4ebb3be043cd8f5`.
The suite embeds the unmodified official Sensible Stackables `3.1.1+26.3`
developer binary and separate suite compatibility component `1.0.1+26.3`.

All six dedicated-server phases passed, with **519 assertions**:

| Profile | Initial | Restart |
| --- | ---: | ---: |
| Polymer, normal limits | 94 | 95 |
| Polymer, uncapped/common limit 2048 | 94 | 95 |
| No Polymer, uncapped/common limit 2048 | 70 | 71 |

Every profile boots without Defaulted. Exact dependency and fixture hashes are
recorded alongside [results](result.json) and [console checks](console-checks.json).
No final console has an error, failed assertion or exception cause line.

The earlier [4d0086 baseline](empty-regression-baseline-4d0086/README.md) fails the new empty-slot regression under uncapped Polymer mode. Its passing older mechanics checks and the intermediate [83fc3a candidate](candidate-83fc3a/README.md) remain preserved separately; their hashes are not relabeled as final acceptance.

New regressions cover:

- Actual Polymer transformation leaves the shared EMPTY singleton, zero-count
  items and AIR unchanged for native/fallback contexts. Component patches remain
  intact, and the optional empty-slot network codec stays empty. These checks
  prevent the crash found during connected-client inventory moves.

- The separate live override registry changes effective limits while vanilla
  item prototypes remain unchanged.
- All 16 cushion colors retain raw prototype limit 16 and effective default 64.
- Updating configuration changes existing stacks immediately; item overrides
  take priority over tags, and tags take priority over the common limit.
- Explicit per-stack limits survive live registry updates and clearing.
- Replacement configurations remove stale common/tag/item entries; an empty
  configuration restores vanilla defaults. Fixture changes are restored after
  each run.
- Actual Polymer item transformation preserves full quantities, projects limits
  below 99 correctly, caps fallback metadata at 99 for uncapped configurations,
  retains full native limits and restores metadata after overrides disappear.
- An isolated partial-native wire registry retains exact vanilla table entries,
  removes an item from an absent SSO module and leaves the original server table
  untouched. A full native table remains unchanged.
- Existing menu, drag, enchanted-book remainder, quantity conservation,
  persistent/network codec boundary, container drop and saved-chest restart
  regressions continue to pass.

Native transformation and partial table checks use isolated packet contexts;
these are not real network-client acceptance. Separate release network evidence
records actual client connections and inventory actions. The tests use disposable
worlds, not the user's server save, and do not reproduce the complete supplied
server mod stack. Historical Stackables 26.2 baseline/port evidence remains
separate and is not claimed as freshly rerun here.

Reproduce from the repository root:

```sh
python3 qa-stackables/run.py --label <new-label> --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.1.5+26.3.jar
```
