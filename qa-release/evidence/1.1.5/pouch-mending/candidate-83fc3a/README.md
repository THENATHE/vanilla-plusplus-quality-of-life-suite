# Pouch Mending and enchantment regression — 1.1.5

Final suite SHA-256: `83fc3a07291cf9000a3e5a56fa1998ee47001eab3cf091ded152a0bf6e831f9a`.

All **370 checks in 133 cases** passed across six disposable dedicated-server launches. These runs used the complete frozen release without replacing nested modules. No run loaded Defaulted. Full-suite profiles loaded developer SSO 2.10.0, MapStitch 1.1.7 and Sensible Stackables 3.1.1, plus Fzzy Config `0.7.7+fix3+26.3`.

| Matrix | Polymer with native capability and unsupported-client guard | Native without Polymer | Tool Pouch + addon without SSO or coordinator |
| --- | ---: | ---: | ---: |
| Core, without optional Mending mods | 65 checks / 25 cases | 62 / 23 | 44 / 13 |
| Clumps + Inventory Mending + Better Than Mending + Armored Elytra + Collective | 75 checks / 29 cases | 72 / 27 | 52 / 16 |

The 1.1.4 baseline reproduced missing stored-wing repair with **Clumps 26.3.2 alone**. Clumps inserts a cancellable early return inside `ExperienceOrb.repairPlayerItems`; the suite's previous RETURN injection never sees that path. The corrected whole-method wrapper processes XP remaining after the original equipment repair pass, with an explicit optional SSO regular-Mending policy check. The private server log independently confirms Clumps 26.3.2, successful SSO configuration loading and native Tool Pouch negotiation; Inventory Mending was absent from that run's loaded-mod list.

The final matrix covers ordinary and standalone netherite pouches; actual netherite leggings attachments; pristine, damaged and partly repaired Mending leggings; live pouch menus and saveback; missing Mending; disabled flight; SSO regular-Mending and automatic-material-repair opt-outs; absent repair materials; broken-first glider selection; and unsupported Polymer clients. Flight tests compare 128 ordinary wear intervals and 128 Unbreaking III intervals per profile with the standard item durability oracle, with no interval mismatches.

With Clumps installed, a genuinely merged orb containing four entries repairs stored wings using the full returned XP budget and respects SSO's regular-Mending opt-out. With Armored Elytra installed, the fixture actually forges an armored Elytra through that mod's API, damages the result and verifies repair retains embedded armor/custom metadata. With Inventory Mending installed, an ordinary inventory sword repairs before stored wings receive leftover XP. Better Than Mending was loaded; its independent sneak-right-click action was not exercised.

- [Result summary](result.json), [core results](core-result.json), [Mending stack results](stack-result.json).
- [Exact launched mod hashes and runtime scope](launches.json), [fixture source hashes](fixture-inputs.json).
- [Expected-failure baseline observations](baseline-observation.json), [pinned upstream source and optional-mod provenance](upstream-source-research.json).
- [Sanitized user-log summary](user-log-summary.json); the raw private log is not published.

These are mechanics tests using actual orb pickups, inventory ticks, fall-flying updates and pouch menus, with an explicitly classified native-capability fixture player. They do not establish a real connected-client negotiation result, reproduce the complete private server stack or modify the user's save. Earlier addon-override candidates, the first rejected 1.1.5 dependency-resolution build and the preceding 4d0086 candidate are excluded from final acceptance.
