# Anvil regression verification, 1.3.1+26.3

Both native and Polymer dedicated-server profiles passed using the exact candidate hash in `inputs.json` and `result.json`. The executed source and runner are retained beside sanitized console logs and dependency audits.

Swift Flight I, II and III cost 4, 8 and 12 levels respectively for both normal pickup and shift-click. Vanilla Unbreaking I costs one level for both routes. Both unenchanted repair routes remain free and usable at zero XP. Each profile also checks input/output conservation, enchantment retention and item names.

The test executes real server `AnvilMenu.clicked` actions through a survival player fixture. This is server menu gameplay coverage; it is not an automated physical-client input test. A preliminary sandbox attempt failed before gameplay because local server socket creation was denied; this recorded final run used permitted socket access.

Reproduce with `qa-features/anvil/run.py` and the exact suite candidate. The runner uses retained workspace QA helpers and cached inputs, not a previous ignored run directory.
