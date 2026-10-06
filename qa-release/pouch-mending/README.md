# Stored Elytra Mending and flight wear regression

This focused fixture runs on disposable dedicated servers using the tested suite's developer-release SSO and Tool Pouch, and matching libraries. The retained Defaulted dropfix is included only when the tested suite or a nested mod actually declares that dependency. It performs actual `ExperienceOrb.playerTouch`, `Inventory.tick` and `LivingEntity.updateFallFlying` calls. It never calls replacement repair/durability helpers directly.

The suite profiles use an isolated server player with a packet observer stub and explicitly classified native Tool Pouch capability. This is a mechanics test; actual client negotiation and packet safety have separate release evidence. A negative Polymer case clears the capability and verifies the existing unsupported-client passive guard. The standalone `nosso` profile loads the original Tool Pouch and candidate addon without SSO, the suite coordinator or Polymer, proving optional SSO class/mixin guards.

Run from the repository root after freezing the final artifact:

```sh
python3 qa-release/pouch-mending/run.py \
  --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.1.5+26.3.jar \
  --label stored-elytra-final115
```

The tests craft actual netherite pouch/netherite leggings attachments using the original attachment recipe. They check XP repair with pristine/damaged/partly damaged Mending leggings, equipment priority, leftover XP, disabled flight, missing Mending, SSO's regular-XP policy and live-menu saveback. Flight uses 128 actual wear intervals and compares every interval with ordinary `ItemStack.hurtAndBreak` under the same deterministic random state, including Unbreaking III, broken-first same-item selection and live-menu persistence. Original SSO resource auto-repair is checked with valid resources, disabled auto/rework, missing materials/enchantment, known BROKEN flags, attached/open pouches and disabled flight.

`--observe` records pre-fix behavior without enforcing candidate expectations; observation-mode output is never acceptance. `--compile-only` compiles the fixture without launching servers. Raw worlds, binaries and console logs remain in ignored `runs/`; compact evidence belongs under `qa-release/evidence/<release>/pouch-mending/`.

Repeat `--extra-mod /path/to/mod.jar` to exercise official optional mods in all profiles, including the standalone no-SSO profile. With Clumps installed, the fixture also collects a genuinely merged orb containing four entries, checks its full XP budget and preserves SSO's regular-Mending opt-out. It covers a standalone netherite pouch as well as the leggings attachment. With Armored Elytra installed, it calls that mod's actual forging API, damages the forged result and verifies XP repairs preserve its embedded armor metadata. With Inventory Mending installed, a damaged inventory sword must still repair before the stored wings receive leftover XP. These optional tests do not imply verification of the user's entire server stack or save.
