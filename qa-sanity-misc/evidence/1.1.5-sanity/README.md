# 1.1.5 miscellaneous gameplay sanity results

On 2026-10-06, **154 assertions passed in each of two dedicated profiles** (native and Polymer), **308 total**. Both processes exited normally with status 0 and neither loaded Defaulted. The unchanged suite SHA-256 is `67be52fd07c2d3dcfda4045dff5463e45bc0d91d37d3f759f4ebb3be043cd8f5`.

The checks exercised MiscTweaks block drop position and velocity controls, targeted player fling/crouch/range, registered pickaxe mining speeds for obsidian/crying obsidian/respawn anchors, custom cobweb fire odds and stone control, cow search-goal enablement, adult/baby food eligibility, and actual AI pickup consuming precisely one wheat and restoring loot permission.

SDI checks covered actual saved-safe-position tracking and player serialization; 1/80/100% rewards at levels 0/1/10/30/50 while restoring the player's level; vanilla XP cap and keep-inventory controls; actual full-reward single-orb and vanilla-split-orb spawning with merged-orb multiplicities; death item splatter/unlimited/timed lifetime controls and normal-item lifetime; explosion immunity predicates; airborne lava and void safe-drop toggles; and all eight combinations of armor/hotbar/offhand retention plus per-slot exclusions and unrelated inventory loss.

[result.json](result.json) records exact artifact, fixture/source/runner hashes, totals, process outcomes, limits and fixture corrections. Profile audits contain launch commands and installed mod hashes. Console `.txt` files replace local path prefixes only. Earlier fixture development failures are preserved in ignored runs and did not require a production change.

See [the runner README](../../README.md) for reproduction and precise limits. This test does not claim full physical explosion damage, every MiscTweaks option, external accessory slots, or remote multiplayer client behavior.
