# Client Sort full-suite sanity QA

Runs the retained original real-operation fixture against the frozen 1.1.5 suite, current dependencies, and no Defaulted. Two profiles cover client-only click transport and server-installed accelerated operations. Every profile covers four standalone/attached pouch variants and all six backpack tiers, sorting both directions, refill, matching and bulk transfer, occupied cursor, owner protection, item conservation, actual saved position after reopening, and explicit/default Client Sort policies.

Fixture source was preserved from the archived addon QA snapshot. The separately maintained SSO port is not used. Use `python qa-sanity-clientsort/run.py --label <unique> --candidate --refill-buttons`; add `--server-clientsort` for accelerated operation validation or `--prepare-only` for compilation without a runtime.
