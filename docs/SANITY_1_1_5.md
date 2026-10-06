# 1.1.5 general sanity check — 2026-10-06

The current released suite passed the additional server and rendered-client checks recorded below after its official upstream update and Defaulted runtime removal. This report supplements the [original 1.1.5 acceptance](VALIDATION.md#115--upstream-synchronization-and-clumps-pouch-mending), with completed release tests reused rather than unnecessarily repeated. The published JAR and production sources remain unchanged. In total, **55,911 additional counted assertions/policy checks**, **180 actual sorting operations**, **140 rendered screenshots**, and the separate settings/rendering/connection observations passed. Large counts include systematic Chalk placement and inventory-conservation matrices; they are assertions, not that many independent play sessions.

## Exact target

| Input | Verified value |
| --- | --- |
| Suite | `1.1.5+26.3` |
| SHA-256 | `67be52fd07c2d3dcfda4045dff5463e45bc0d91d37d3f759f4ebb3be043cd8f5` |
| Release source | `a9af44ff0b1385c5a67b1dabc38516126e220d2d` |
| Minecraft / loader | `26.3` / Fabric `0.19.5` |
| Official updated inputs | SSO `2.10.0`, MapStitch `1.1.7`, Sensible Stackables `3.1.1` |
| Fzzy Config | Official `0.7.7+fix3+26.3` |
| Polymer profiles | Bundled `0.18.2+26.3`, plus separate native profiles without Polymer |
| Defaulted | Absent from every current runtime; historical guarded compile-only input preserved |
| Test isolation | Disposable local worlds, synthetic offline accounts, dedicated and physical clients; no real hosted world modified |

## Additional coverage

| Area | Fresh result | Evidence |
| --- | --- | --- |
| Atlas server behavior | 5,314 assertions: native/Polymer initial and restart; scales/dimensions, repeated generation, extraction, repairs/resends, dedupe, copying/cartography, enabled-scale banners and pouch saveback | [Server results](../qa-multiscale/evidence/1.1.5-sanity/server/result.json) |
| Upgrade from 1.1.4 | 1,294 assertions after copying a preserved exact 1.1.4 world, config and atlas into 1.1.5 without Defaulted; old data/input hashes retained | [Upgrade record](../qa-multiscale/evidence/1.1.5-sanity/upgrade/result.json) |
| Atlas death retention | 254 assertions across 50 scenario runs: all backpack tiers, standalone/attached pouches, shulkers/bundles, nesting, open menus, curses, cursor/full inventory, saved overflow recovery and exclusion of unrelated contents | [Death record](../qa-death-once/evidence/1.1.5-sanity/result.json) |
| Shared Region Maps | 1,222 assertions in initial and saved-world restart phases | [Shared-map record](../qa-sanity/evidence/1.1.5-sanity/shared-region-maps/result.json) |
| Broad SSO | 245 assertions across native/Polymer dedicated profiles; flint/whetstone crafting repairs and restrictions, broken-item attributes, anvil degradation/repair/rename/high costs, scrap cost reduction, enchanting/bookshelf controls, XP reward/bottles, villager-book caps, enchantment upgrades, pinnacle rerolls, pickup/shift-click XP and ingredient conservation, recipes, advancements and current weighted loot | [Gameplay record](../qa-sanity/evidence/1.1.5-sanity/gameplay/result.json) |
| Pouches/backpacks | 360 additional dedicated assertions: all six backpack and both pouch tiers, attachment/detachment with retained armor damage/enchantments/dye/contents, native menus and saveback, shift moves, nesting/item restrictions, nonfireproof armor rejection and actual netherite-backpack smithing pickup | [Gameplay record](../qa-sanity/evidence/1.1.5-sanity/gameplay/result.json) |
| Chalk placement | 31,174 assertions; 1,728 placements per profile across all 16 colors, ordinary/glow variants, six faces and nine arrow regions, plus correct variant/orientation, survival, support removal, shapes, piston reaction and durability | [Gameplay record](../qa-sanity/evidence/1.1.5-sanity/gameplay/result.json) |
| MiscTweaks / Simple Death Improvements | 308 dedicated assertions: real drop positions/velocities and targeted fling controls; obsidian/anchor mining controls, cobweb fire registration, actual cow food consumption; SDI XP percentages/orbs, item lifetimes/splatter/explosion controls, saved safe position, airborne lava/void rescue, armor/hotbar/offhand retention and exclusions | [Misc/SDI record](../qa-sanity-misc/evidence/1.1.5-sanity/result.json) |
| Native configuration | 118 observations and 30 actual screenshots: all 14 pages, original fields, uniform Configure buttons, Mod Menu nesting, hotkey, persistence, permission-aware controls/proposals, invalidation and operator→guest plus guest→operator reconnection | [Operator→guest](../qa-settings/evidence/1.1.5-sanity/operator-to-guest/results.json), [guest→operator](../qa-settings/evidence/1.1.5-sanity/guest-to-operator/results.json) |
| HUD separation | 22 rendered scenes/screenshots across native and client-Polymer profiles: left/right separation, atlas/map parity, overlap displacement, independent application/saved offsets, signed offsets | [Native HUD](../qa-hud/evidence/1.1.5-sanity/native-client/results.json), [client Polymer HUD](../qa-hud/evidence/1.1.5-sanity/client-polymer/results.json) |
| Full atlas renderer | Complete matrix: 21 scale/dimension views, dimension travel, four minimap regressions, full 16,384-pixel resend of each of 15 maps, 12 stale/missing-center Ctrl+Q packet/drop cases, five inventory/pouch sources; 22 retained screenshots | [Rendered atlas record](../qa-multiscale/evidence/1.1.5-sanity/complete-client/results.json) |
| Bannerpoint and Polymer pack | Five actual profiles, no-Polymer restart, 12 observations, seven negative pack states and six visually inspected screenshots; current/stale/removed/declined artwork, ordinary locator survival, native/original-only labels, genuine vanilla protocol, saved UUID/name/map-link/transmitter persistence and banner breaking. 41 graphics/style resources match exact upstream bytes; SSO language equivalent; 43 input JSON/PNG assets validate | [Bannerpoint report](../qa-bannerpoint/evidence/1.1.5-sanity/README.md) |
| Client Sort | 15,728 server invariants, 12 policy checks, 180 actual operations and 60 rendered screenshots across client-only and accelerated transports/all ten pouch/backpack variants; seven accelerated malformed-transaction/quota/rollback boundary groups | [Client-only](../qa-sanity-clientsort/evidence/1.1.5-sanity/client-only/results.json), [accelerated](../qa-sanity-clientsort/evidence/1.1.5-sanity/accelerated/results.json) |
| Extended negotiation / singleplayer | Five actual reconnect/reconfiguration/mismatch records, plus two physical-client integrated worlds with and without Polymer; native capability and independent safe SSO/Chalk fallback projections retained | [Negotiation](../qa-sanity/evidence/1.1.5-sanity/negotiation/result.json), [singleplayer](../qa-sanity/evidence/1.1.5-sanity/singleplayer/result.json) |

## Checks reused from the latest release

The [original acceptance record](VALIDATION.md#115--upstream-synchronization-and-clumps-pouch-mending) already verifies the same final JAR with 370 pouch-Mending checks across 133 cases and six servers, 519 Stackables assertions across six initial/restart phases, Chalk conversion and calcite recipes, curse-removal menus, original drop/lifecycle behavior, native/Fabric-only/vanilla/no-Polymer connections, full 2048 counts with safe fallback metadata, actual inventory packets, untouched-original negotiation, both new SSO/Stackables payloads and live updates, actual atlas commands/cartography clicks, clean build/archive provenance and 13 installation checks. Those completed tests were reused at the user's request. Their original records remain unchanged.

## Findings and practical limits

No gameplay regression or missing runtime dependency was confirmed in the added coverage. The new official SSO release includes optional `rename_xp_bottle` and `rename_mending_description` builtin resource packs with an old `pack_format: 15`; Minecraft removes them as incompatible. This affects their optional text renaming, not repair, XP, enchanting or native/fallback connection behavior. The older official input already used the old format for its root pack, but did not include these two optional builtin packs. This inherited warning is documented; the original publisher JAR was preserved.

Archived QA fixtures needed routine API/expectation fixes: current inventory API, updated library filenames, absence of Defaulted, wrapped grindstone output slots, effective 10:90 item/empty weighted loot instead of a chance condition, entity tracking for synthetic startup-only players and expected storage snapshots taken before consumed stacks become empty. Earlier failed fixture attempts remain isolated; final passing records bind to their actual sources and inputs. No production fix or new release was required.

Dedicated mechanics use packaged production methods and synthetic player networking; real packet/rendered coverage is separately labeled. The preserved upgrade world is a QA snapshot, not the user's remote server. The entire supplied third-party server stack, every optional accessory integration, every MiscTweaks option, probabilistic full fire/explosion simulations and dropped-item timer persistence across save/load are not exhaustively certified. Pure vanilla pack/waypoint protocol checks use an unmodified client, while fallback rendering is captured on a Fabric API-only client without suite/Bannerpoint client code. Reused Mending coverage includes the supplied relevant repair-mod combinations.

Expected disposable-account authentication errors, missing host narrator library and OpenGL failure followed by successful Vulkan rendering are retained and classified separately from mod behavior. Final dedicated servers exited normally; no unclassified mod runtime errors remain in these records.

The distinct ChatGPT SSO version-port track remains paused and was neither built nor tested. Published artifacts/checksums were not replaced during this QA round; the additional local/source evidence is collected here for review and the next normal documentation package.
