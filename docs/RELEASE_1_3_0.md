# Vanilla++ 1.3.0+26.3

Targets Minecraft 26.3, Fabric Loader 0.19.5 and Java 25+. This is a Vanilla++ suite release; the new feature implementations are maintained here independently of their original projects.

## Changes

Adds offhand shulker pickup; native beacon/conduit construction; grindstone enchantment extraction and book splitting; netherite instamining; enderman grief prevention; Swift Flight; pet ownership transfer; six goat-horn animal calls; and all-weather Channeling. See [features and author attribution](../README.md#features), [native module documentation](NATIVE_FEATURES.md) and [changelog](../CHANGELOG.md).

Mod Menu groups original and authored feature entries beneath Vanilla++, with one Compatibility + Shims entry. The parent configuration action opens all registered configurations. Internal Fabric identities and negotiation remain separate.

## Install

Replace the previous suite JAR with `vanilla-plusplus-quality-of-life-suite-1.3.0+26.3.jar`. Keep the existing dependency set in [DEPENDENCIES.md](DEPENDENCIES.md). No Collective library or separate new-feature JAR is needed. Use the side-aware installation ZIP for a fresh setup; Fzzy Config remains an official publisher download rather than a redistributed binary.

Remove old standalone versions/datapacks when their equivalent suite module is enabled. Swift Flight retains existing enchantments, enchanted books and harnesses under the original identifier. Legacy horn bindings and other datapack state are not migrated; rebind existing horns. Keep existing pets and world data.

Polymer is optional for matching modded clients and singleplayer; install the pinned server Polymer bundle to admit vanilla clients. The additions use vanilla items/menus/entities. Existing custom pouch/backpack/atlas interfaces continue requiring native client support.

## Verification

Final suite SHA-256: `e57862d94d4b4331ddd5245e916793a16fa9258d3cceca0d2b3ac891b77aea72`.

- Four dedicated native/Polymer initial/restart phases pass **934 individual assertions**, including real unloaded-pet recovery, preserved Swift Flight books/harnesses, actual flight attributes and 1,000 End City loot draws per phase.
- Three actual native/Fabric grindstone profiles pass ordinary client clicks, item/enchantment conservation, XP charging and continued connection.
- Three native settings/Hotkey Wheel profiles pass: operator with Polymer, operator without Polymer and non-operator with Polymer. Each renders fifteen functional config screens, checks the Mod Menu parent/individual feature actions, and verifies server-file persistence or permission protection. Hotkey Wheel **1.0.3+26.3** is tested through native keyboard/mouse/SDL focus events; hardware key automation is not claimed.
- Four physical connection profiles, two integrated singleplayer profiles and all thirteen installer/archive checks pass on the exact final JAR.

[Full validation, exact evidence and scope](VALIDATION.md#current-130--native-features-and-mod-menu-consolidation).

The anvil probe applies real Swift Flight books normally for 4/8/12 XP levels. Shift-click conserves the harness/book/enchantment but charges zero under the existing SSO behavior, also reproduced with a vanilla enchantment and unchanged baseline anvil classes; this release does not fix that general behavior. Occupied happy-ghast player passengers were not independently tested. Offline-player shoulder parrots become callable when their holder returns.

## Downloads and source

Repository: https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite

The release uses four assets: README.md, the suite JAR, docs.zip and `vanilla-plusplus-installation-pack-1.3.0+26.3.zip`. Local artifacts and matching SHA-256 records belong under `Builds/Minecraft/Vanilla++ Quality of Life Suite/Main Plugin/1.3.0+26.3/`.
