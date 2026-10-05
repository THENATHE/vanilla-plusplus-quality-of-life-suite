# Native ClientSort integration QA

This fixture launches a dedicated server and real native Fabric client against the developer-release SSO stack only. The SSO-port track is intentionally excluded. Original Tool Pouch 1.1.10, Tiered Backpacks 1.0.20, Multi-Shim 1.0.3, and ClientSort 3.104.1 are retained unchanged; the supplied addon JAR is the candidate.

Ten menu variants cover ordinary/netherite Tool Pouches standalone and attached to equipped netherite leggings, plus all six backpack tiers. Nine operations per variant exercise bag sorting, player-inventory sorting, stack refill in both directions, matching transfer in both directions, bulk transfer in both directions, and sorting with an occupied cursor. The latter must leave carried items unchanged; client-only sorting safely skips while accelerated sorting can reorder the bag without using the cursor. Genuine ClientSort widgets receive normal left-click events. With ClientSort's default Inventory policy, refill remains keybind-only and its original operation entry point is used; `--refill-buttons` enables that user's Inventory button preference and tests visible refill widgets instead.

The fixture sets ClientSort's interaction interval to one tick and sorting order to alphabetic solely for repeatable QA. It configures the pouch firework category to permit one occupied stack, so refilling an existing partial stack exercises the full-quota case. It also checks the native compass per-slot cap, clock stack quota, forbidden stone in pouches, the default shulker restriction in backpacks, a forbidden item followed by allowed items, stationary owning bags, empty cursors, and exact item-count conservation. Sorted physical positions survive closing and reopening the native menu; withdrawn contents do not return on reopening.

Before candidate operations, client policy checks confirm an explicit bag `NONE` policy stays unchanged, absent bag policies follow both disabled and enabled chest preferences, and lookup does not mutate the saved policy map. These changes are restored before normal operations.

The accelerated profile also installs ClientSort on the server. An observational QA mixin counts genuine `ServerOperator` constructions per operation; every operation must use that route and produce the expected authoritative result. The server boundary helper separately verifies valid permutations, malformed mapping rejection, forbidden items, stack caps, rollback after a partial attempted change, cursor/inventory preservation, and partial-stack refill at the configured quota. Boundary cases call the integration helper directly; native operations use ClientSort's actual transport.

```sh
python qa/clientsort/run.py --label baseline-unique
python qa/clientsort/run.py --label candidate-native-unique --candidate --addon build/libs/toolpouch-atlas-elytra-compat-1.0.4+26.3.jar
python qa/clientsort/run.py --label candidate-accelerated-unique --port 25947 --candidate --server-clientsort --refill-buttons --addon build/libs/toolpouch-atlas-elytra-compat-1.0.4+26.3.jar
```

The default baseline uses released addon 1.0.3 and expects no bag-side buttons. Each label creates a fresh world and configuration directory; exact artifact hashes and original-JAR integrity checks are in `runs/<label>/evidence.json`. Screenshots are captured from rendered native screens on initial open and reopen. QA does not rewrite original mod JARs or enable a ClientSort server dependency for ordinary users.

## Recorded result

Released addon 1.0.3 reproduced missing bag controls on all ten menus. Final addon 1.0.4 (`48c218887c60fe3628822d79b608aeb25fff1a12590de736e799ce4133f4566b`) passed both developer-stack profiles: **90 native operations and 7,864 server assertions per profile**, plus six policy checks each. The accelerated profile recorded 120 genuine ServerOperator instances across its 90 operations and passed all seven server boundary groups. Both runners exited successfully and confirmed original JAR hashes remained unchanged.

During candidate QA, the accelerated transfer path was found to stop after filling a single custom-capacity destination. A scoped correction now continues through available pouch slots; final matching and bulk-transfer regressions verify all permitted items move without exceeding slot caps or quotas. Occupied-cursor sorting also has explicit coverage.

Sanitized manifests, observations, transport counts, and screenshot hashes are preserved in `docs/1.0.4/clientsort-baseline.json`, `clientsort-native.json`, and `clientsort-accelerated.json`. Representative attached netherite pouch and netherite backpack screenshots were inspected for both final profiles: native slots and sorted contents render correctly with three buttons per group by default, and four when the refill-button preference is enabled. The final screenshots are unobstructed; the baseline pouch screenshot contains a notification overlay, while independent widget counts establish that bag controls are absent.

This does not test all third-party resource packs, arbitrary ClientSort custom policies, or every GUI scale. The paused SSO-port profile was neither built nor tested for this feature.
