# Native feature additions

The 1.3.0 suite additions, with 1.3.1 safety and compatibility fixes, are maintained in this repository independently of the original projects. They use the existing Fabric/Fzzy infrastructure and vanilla items, menus and entities. Collective and the original feature JARs/datapack command systems are not installation requirements.

## Credits and source boundaries

Original project authors were verified against the public Modrinth project and team APIs on 2026-10-12. The exact records, source URLs and published license declarations are in [the attribution audit](native-features-attribution-2026-10-12.json). A project's license in that audit describes the original project; it does not relicense the independently authored suite code.

Thanks for the idea, **Serilum**! Better Beacon Placement and Better Conduit Placement inspired the gameplay concepts. Their suite implementations were written independently using vanilla Minecraft APIs/geometry and the public feature descriptions. No Serilum source code or assets were copied or adapted; the original ARR mods and Collective are not bundled.

| Suite functionality | Original project / author | Suite relationship |
| --- | --- | --- |
| Offhand shulker insertion | [Drops into Shulker](https://modrinth.com/mod/drops-into-shulker), 1bacon / onebacon; original modded-container contribution by Meshiest | Independent native implementation. Original CC0 mod JAR is not bundled. |
| Beacon placement | [Better Beacon Placement](https://modrinth.com/mod/better-beacon-placement), Serilum | Independent native implementation from documented behavior. Original ARR source/JAR and Collective are not bundled. |
| Conduit placement | [Better Conduit Placement](https://modrinth.com/mod/better-conduit-placement), Serilum | Independent native implementation from documented behavior. Original ARR source/JAR and Collective are not bundled. |
| Grindstone extraction and book splitting | [Grind Enchantments](https://modrinth.com/mod/grind-enchantments), mschae23 | Independent suite implementation; original GPL mod source/JAR is not bundled. |
| Netherite instamining | [Netherite Instamine](https://modrinth.com/datapack/netherite-instamine), TheLokin | Native temporary attribute modifier replacing command ticking. |
| Enderman grief prevention, Swift Flight, ownership transfer, animal calls and Channeling | [Jodek's projects](https://modrinth.com/user/Jodek) | Native mechanics replacing command machinery; retained Swift Flight data attribution/license accompanies adapted definition data. Individual project links are in the README and audit. |

## Installation and existing data

Remove the old standalone mod/datapack for a feature when enabling the corresponding suite module. Running both versions can execute two independent pickup, placement, teleport or lightning paths. Disable a suite feature in settings if you wish to keep another implementation.

Swift Flight preserves `swift_flight:swift_flight`, its harness support, three levels and movement-speed effect. Existing harnesses and books retain the same enchantment rather than receiving replacement items. Keep the suite installed when loading those saved enchanted items after retiring the original pack. Swift Flight and its End City book-loot switches require a restart, as shown in their native configuration notices.

Legacy animal-call horns, command storage, scoreboards and pet indexes are not converted or adopted, as explicitly requested. Bind horns again using the native interaction. Existing pets are retained; binding/calling moves the actual entity rather than making a replacement copy. Ordinary horns without a native binding retain their normal behavior.

## Calling and ownership

Name the supported animal, hold an ordinary goat horn and crouch while looking at it for four seconds. Reset by crouching and looking straight up for two seconds. A successful native binding stores the animal UUID and tracks its world location. Using the horn calls the tracked animal with ordinary goat-horn sound/cooldown behavior.

The settings screen exposes feature switches, configurable mount/nautilus type lists, mounting, water and cross-dimension checks. Ownerless happy ghast and permitted ownerless mount horns can be shared by default; owned animals remain protected. Occupied happy ghasts may be called by default, while other occupied pets require enabling the corresponding option. Cross-dimension movement and unloaded tracking use native entity/chunk handling. The validation record distinguishes direct mechanics fixtures from actual client interactions and restart acceptance.

## Grindstone behavior and costs

Extraction returns the original equipment by default, retaining damage, name and curses that are not selected for extraction. Its default XP-level cost is `ceil(sum of selected enchantment minimum powers × 0.3 + 8)`. Book splitting moves the first eligible enchantment in normal tooltip order; its default cost is `ceil(level × (8 for treasure / 3 otherwise) × 0.5 + 0.5)`. These factors, offsets, XP points versus levels, curse extraction, first-only extraction and source consumption are individually configurable.

The result preview displays cost in lore for native and fallback clients. Taken books retain their actual item data without preview-only cost lore. Existing SSO netherite-scrap penalty reduction and amethyst curse cleansing continue using their original inputs. The original project's arbitrary cost-expression DSL is not copied; the suite owns its validated numeric settings.

## Settings and presentation

The Vanilla++ parent opens the combined settings hub. Each original feature and authored addition stays visible beneath it; compatibility modules share one Compatibility + Shims display entry. Fabric IDs, nested modules, packet negotiation and per-module support checks are unchanged by this presentation grouping.

The native additions use ordinary items/screens and require no new sprites or resource-pack detection. Optional Polymer continues serving the existing suite compatibility path. Native-only servers retain normal modded operation without Polymer.

## 1.3.1 review corrections

The suite restores SSO netherite scrap acceptance in the grindstone additional slot while respecting SSO's recipe setting. Its anvil compatibility passes the original result stack to output-taking logic after a full shift-click transfer, so enchantments remain available when SSO decides whether the repair should be free. Original SSO and curse-cleanser JARs remain unchanged.

Pet recalls check translated bounds for the pet, existing passengers, preserved leash companions and the optional automatic rider before moving anything. Hazard checks cover the occupied volume and supporting surface, including soul fire and nearby lava. A rejected recall leaves the group in place.
