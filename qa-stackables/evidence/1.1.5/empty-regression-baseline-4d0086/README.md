# Expected empty-slot regression failure

The new empty-slot fixture was run against suite SHA-256 `4d0086b3472ea890a9ed6813ef8e32936e4d29b495858c4ca937ede60a16d2f3` on 2026-10-06. The normal-limit initial/restart phases passed; the uncapped Polymer phase failed with `Polymer preserves empty slot without constructing transformed air native=false`. This confirms the fixture detects the defect observed during actual connected-client inventory moves. No failed phase is counted as release acceptance.

The common-limit override table could include AIR. Matching it as a Polymer item transformed empty inventory slots; copying/modifying an empty item could mutate the shared EMPTY singleton. Final compatibility guards reject empty counts/AIR before transformation and skip modification of empty originals/results. Current checks are recorded [one directory up](../README.md).
