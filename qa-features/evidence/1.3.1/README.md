# Native additions regression acceptance: 1.3.1+26.3

Exact candidate suite SHA-256: `7becece0f4bbe9ff85ad4cad8474c6b9a8b9dcbdf9940abeb5a31e0d0786db2f`.

Native and Polymer dedicated profiles each passed initial mechanics and an actual restart. There are 1014 individual assertion observations across the four phases; per-phase counts and exact inputs are recorded in `acceptance.json`, `result.json`, and each profile's `audit.json`. The `total=` summary markers are excluded from assertion counts.

New regressions cover normal grindstone netherite-scrap repair-cost reduction, preservation of the input item and consumption, soul-fire refusal, full-body lava clearance, occupied happy-ghast ceiling refusal without moving or dismounting the rider, successful occupied recall with clear headroom, and clearance for taller leashed companions. Existing feature, enchantment, ownership, item-component and unloaded-pet persistence checks were repeated.

These fixtures use real server entities, blocks, menus, and saved worlds with packet-suppressing test connections. They do not establish physical client input or connected-client presentation. Connected-client, configuration and Hotkey Wheel evidence is recorded separately.

`executed-sources/` and `executed-run.py` are the frozen executed fixtures. Logs and JSON replace local project/home paths with placeholders. Original rejected attempts remain in ignored local runs: the first could not open sockets inside the sandbox; later fixture corrections provided valid soul-fire support and an independent rider connection. No production changes were needed after the tested candidate was built.
