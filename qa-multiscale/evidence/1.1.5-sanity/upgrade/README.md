# Preserved 1.1.4 atlas world upgraded to 1.1.5

On 2026-10-06, current `1.1.5+26.3` passed **1,294 assertions** while opening a copied preserved final `1.1.4+26.3` Polymer QA world, configuration and serialized atlas. It exited normally with status 0 without Defaulted. The previous JAR was never launched for this check and the original world was preserved.

- Current suite SHA-256: `67be52fd07c2d3dcfda4045dff5463e45bc0d91d37d3f759f4ebb3be043cd8f5`.
- Origin suite SHA-256: `2c1273947279693e5ec3c78b2c39793c3b5fdbc320216476e04d16f79b2a1754`.
- Origin run: `qa-multiscale/runs/atlas-copy-release-final3/maps-polymer`, whose final initial/restart matrix had passed.

The serialized prior atlas retained all five distinct saved map IDs/scales, selected scale and generation choices, unrelated custom data and underlying world artwork. The current fixture then exercised extraction, banner edits, integrity repair/resend, exact-ID cleanup and book/cartography copies against the upgraded world.

[result.json](result.json) records the current results and fixture hashes. The profile audit contains hashes of every original world/config file and the saved atlas before copying, plus the actual current launch command and installed mod hashes. Its sanitized console log and result are retained beside the audit.

With both preserved origin and completed current fixture installations available, the checked launch can be reproduced using:

```sh
python3 qa-multiscale/run-upgrade.py --label <unique-label>
```

This is one genuine prior-version world/config/atlas upgrade path, not exhaustive coverage of every possible saved player inventory or server configuration. Native/Polymer same-version persistence is recorded separately in [the dedicated server results](../server/README.md).
