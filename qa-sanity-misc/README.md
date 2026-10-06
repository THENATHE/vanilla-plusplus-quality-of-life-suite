# MiscTweaks and Simple Death Improvements gameplay sanity QA

This fixture exercises the packaged suite in disposable dedicated worlds with and without Polymer. It has no client entrypoint and is never included in the suite JAR.

```sh
python3 qa-sanity-misc/run.py --label <unique-label> --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.1.5+26.3.jar
```

[Current evidence](evidence/1.1.5-sanity/README.md) records 308 assertions against the exact unchanged release.

The fixture loads small test chunks and explicitly marks their entity sections tracked. It uses real `Block.popResource`, item constructors, pickaxe Tool component getters, fire odds getters, cow goals and `aiStep`, `ServerPlayer.doTick`, player serialization, death-item creation/ticks, reward/orb spawning, death-drop and restore methods. Stub networking avoids requiring an external test account. Teleported test players have their airborne support cache explicitly cleared; safe-position delay is shortened only in the isolated fixture.

Coverage includes block-drop position/velocity settings, player/crouch/range targeted fling, obsidian/crying-obsidian/anchor mining controls, configured cobweb fire registration, adult/baby food eligibility and actual cow food pickup; death XP percentages and vanilla/keep-inventory controls, real XP orbs, death-item lifetime/splatter and explosion immunity predicates, airborne lava and void rescue, safe-position save/load, and armor/hotbar/offhand retention with exclusion lists.

Limits: no full explosion blast simulation, long running animal travel, random fire spread, berry/creeper/disc loot/Soul Speed/Thorns behavior, SDI third-party accessory retention, or lifetime-timer persistence after a dropped item's save/load is claimed. Client rendering and negotiation are covered separately. These tests use the native suite configuration defaults plus fixture-specific toggles, not the unavailable remote server's entire mod stack.
