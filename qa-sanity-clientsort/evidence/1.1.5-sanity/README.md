# Client Sort acceptance — 1.1.5 sanity pass

Date: 2026-10-06. Exact suite SHA-256: `67be52fd07c2d3dcfda4045dff5463e45bc0d91d37d3f759f4ebb3be043cd8f5`.

Both real-network profiles passed against the complete released suite, with Fzzy Config fix3 and no Defaulted installed:

| Profile | Server invariant checks | Policy checks | Real operations | Variants | Rendered menu captures |
| --- | ---: | ---: | ---: | ---: | ---: |
| Client Sort only on native client | 7,864 | 6 | 90 | 10 | 30 |
| Client Sort on native server and client | 7,864 | 6 | 90 | 10 | 30 |

Each profile exercised standalone and leggings-attached ordinary/netherite pouches plus leather, copper, iron, gold, diamond and netherite backpacks. Genuine Client Sort buttons generated operations for bag/inventory sorting, refill, matching transfer, bulk transfer, and a held-cursor sort case. Every transaction checked item conservation, cursor behavior, owner protection, slot limits, reopening persistence, and physical ordering after saved sorted contents were reopened. The accelerated profile verified actual `ServerOperator` dispatch for every operation and additionally passed seven directed transaction boundary groups for malformed mappings, quotas, allowlists, limits and rollback. Those directed groups are separate from the 7,864 counted server checks.

The netherite attached pouch and widest backpack captures were visually inspected. Native container rendering and all four Client Sort operation buttons are present and unobstructed. Fixtures/source/dependency hashes and full operation observations are in each profile's `results.json`. All input JAR checksums were unchanged after the runs. No production source or upstream binary was modified.

Scope is isolated local native clients and servers with controlled fixture configurations. This is not a test of the user's inaccessible remote production server. Existing 1.1.5 Mending, high-stack synchronization and installer checks were not repeated.
