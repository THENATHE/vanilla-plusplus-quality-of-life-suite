# Vanilla++ Quality of Life Suite

[What it is](#what-it-is) · [Features](#features) · [Vanilla compatibility](#the-shim-vanilla-and-modded-players-together) · [Installation](#installation) · [Technical info](#technical-info) · [Downloads](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/releases) · [Dependencies and compatible mods](docs/DEPENDENCIES.md)

This is the `merged` testing branch, version **1.0.2-merged.3+26.3**. It combines the [mixed-scale MapStitch addon](docs/MIXED_SCALES.md) and [Sensible Stackables port with Polymer compatibility](components/sensible-stackables/README.md). Both features remain separate modules. Main stays at its stable feature set.

## What it is

A collection of Minecraft quality-of-life mods brought together into one feature-packed **Fabric mod for Minecraft 26.3**, maintained by **THENATHE**. It combines better equipment repair and enchanting, useful storage, maps that work together, less punishing deaths, colorful cave markings, and everyday gameplay improvements.

The suite includes the original mods listed below, plus my own additions to help them work together. It also includes a compatibility layer that lets players without the suite join a server when Polymer is installed. Those players can use the supported vanilla-friendly features; custom backpack, pouch, and atlas screens still need the suite on the client.

This is an unofficial community project. Credit for the original mods belongs to their creators, thanked below. Many features are configurable, so your server's settings may differ from the defaults described here.

## Features

### Sensible Stackables

[Original mod on Modrinth](https://modrinth.com/mod/sensible-stackables)

Thanks for your hard work, **pajic**!

- Stack potions up to 3, saddles up to 16, and enchanted books up to 64 under the default rules.
- Configure common stack sizes and per-item or tag overrides.
- Optionally uncap stack sizes beyond the normal limit.
- Retain the original stacked-item menu fixes and throwable-potion cooldown.
- Native clients retain count abbreviation and display scaling.

#### My tweaks and changes

This branch uses an unofficial 26.3 port and a separate Polymer module. It keeps the original settings and server quantities. Clients without the suite receive safe stack defaults; above 99, only their prediction metadata is capped at 99, and completed inventory moves are corrected by the server. See [provenance, behavior, and verification](components/sensible-stackables/README.md).

The world-map screen now has independent controls: **S** selects the world-map view, **M1–M16** selects the minimap, and **1, 2, 4, 8, 16** toggle generation at each scale. Enabled generation buttons are green. Tooltips show the selected generation scales. Each enabled missing layer consumes one blank; existing maps keep their data. These options are saved per atlas. See [atlas controls](docs/ATLAS_CONTROLS.md).

Mod Menu shows original feature mods individually and groups additions beneath **Vanilla++ Quality of Life Suite**. The combined settings screen lists actual settings with consistent gameplay/client titles. Bind **Open Suite Settings** under the suite category in Minecraft Controls to open it from gameplay.

### Simple Smithing Overhaul

[Original mod on Modrinth](https://modrinth.com/mod/simple-smithing-overhaul)

Thanks for your hard work, **pajic**!

- Repair costs reflect how much material an item took to craft, rather than using the same amount for every item.
- Repair more vanilla items, and use diamonds to repair netherite equipment under the default rules.
- Keep broken equipment instead of losing it completely. It becomes unusable until you repair it.
- Repair equipment in the crafting grid with repair materials and flint or a whetstone.
- Enchant whetstones so they can repair enchanted equipment with matching enchantments.
- Use the configurable Mending rework to repair held items with materials and a suitable whetstone, including automatic repair when they break. Traditional XP Mending can be enabled in the settings.
- Rename items and repair unenchanted equipment without an XP charge under the default settings.
- Avoid increasing the anvil's prior-work penalty just by repairing an item.
- Use expensive anvil operations beyond vanilla's “Too Expensive!” limit.
- Repair damaged or broken anvils with iron blocks. A broken anvil stays in the world instead of disappearing.
- Use netherite scrap in a grindstone to reduce an item's accumulated repair penalty, and receive more XP from disenchanting.
- Find an Enchantment Upgrade template in End Cities and spend XP to raise an enchantment by one level. The amount of lapis selects which enchantment to upgrade.
- Find a Pinnacle template in Ancient Cities to push an eligible, fully enchanted item's enchantment beyond its normal maximum.
- Use rebalanced enchanting, villager book trades, enchanted loot, and experience bottles to progress through equipment upgrades.
- Follow a smithing advancement tree and customize the repair, enchanting, and integration rules.

#### My tweaks and additions

- Added the shared compatibility support described below, including preservation of the real broken-anvil appearance for matching suite clients.
- Connected the original settings to the suite's combined settings screen.
- Kept SSO's Mending choices authoritative: the pouch Elytra repair addition follows your regular-Mending setting.

### MapStitch

[Original mod on Modrinth](https://modrinth.com/mod/mapstitch)

Thanks for your hard work, **pajic**!

- Combine a filled map and a book to create an atlas that holds your maps together.
- Open a world map from your atlas, with panning, zooming, dimension/scale selection, a map grid, and player-follow controls.
- Automatically create maps as you explore, using empty maps stored in the atlas.
- Find the Globetrotter enchantment to let an atlas consume paper for exploration instead of prepared empty maps.
- Display a minimap using your atlas and compass, with configurable placement and appearance.
- Carry a compass for the player marker, coordinates, and grid under the default item requirements.
- Add explorer and treasure maps to see their destination markers, and use an atlas on a banner to mark it.
- Choose whether filled or empty maps are ejected first.
- Map the Nether, with configurable rendering for its ceiling-covered terrain.
- Craft maps from paper without spending a compass when the cheaper-map recipe is enabled.
- Optionally display time, coordinates, biome, and weather information. The matching server options must allow those details.

#### My tweaks and additions

- Made atlases work from inside the active Tool Pouch, including pouches attached to leggings. The world map, minimap, and exploration updates continue to work there.
- Let MapStitch find a compass or clock inside the pouch for its item requirements.
- Added shared regional map exploration through Shared Region Maps, described below.
- Store every vanilla map scale in one atlas. Use **S** for the world-map view, **M1–M16** for the minimap, and the **1/2/4/8/16** switches for which maps to generate.
- Keep only the atlas on death when the original retention setting is enabled, even from nested pouches, backpacks, shulkers or bundles; the containing item and its other contents drop normally.
- Included fixes for missing map centers, stale map selection, and atlas crafting previews/ingredient consumption in the compatibility component.
- Cleared stale minimap position information when changing worlds or atlas map centers.
- Share map/detail corner placement between MapStitch and Tool Pouch, with one layout for atlas and ordinary-map minimaps.

### Tool Pouch

[Original mod on Modrinth](https://modrinth.com/mod/tool-pouch)

Thanks for your hard work, **pajic**!

- Store useful equipment in a pouch and use its benefits without filling your hotbar.
- Open the pouch from your hand or with its configurable keybind.
- Upgrade the standard 16-slot pouch to a fireproof, 25-slot netherite pouch. Both capacities are configurable.
- Attach a pouch to leggings, then detach it again when needed. Netherite pouches require fireproof leggings.
- Dye pouches and wash the color off in a water-filled cauldron.
- See compass, clock, and recovery-compass information while those items are stored in the pouch.
- Keep a normal map updating in the pouch and display it as a minimap.
- Fly with an Elytra stored in the pouch while wearing a chestplate.
- Let a stored Totem of Undying protect you without holding it.
- Open a stored ender chest using a keybind.
- Use a stored spyglass for zooming, with mouse-wheel zoom adjustment.
- Open stored shulker boxes through a selection menu.
- Supply bows and crossbows with stored arrows, and choose ammunition through the pouch's selection menu.
- Use stored fireworks for Elytra boosts or crossbow ammunition.
- Add optional lantern lighting with [LambDynamicLights](https://modrinth.com/mod/lambdynamiclights), and container previews with [Shulker Box Tooltip](https://modrinth.com/mod/shulkerboxtooltip).
- Configure allowed items, storage limits, information fields, and overlay appearance.

#### My tweaks and additions

These include my **Tool Pouch Atlas & Elytra addon**, already built into the suite:

- Use MapStitch atlases directly from the pouch, including map updates and ejection.
- Toggle pouch-powered flight without removing the Elytra. Bind **Toggle Tool Pouch Elytra**, or use `/toolpouch-elytra`, `/toolpouch-elytra on`, and `/toolpouch-elytra off`.
- Remember the flight preference between sessions and respawns. Turning it off does not disable a normal chest-slot Elytra.
- Mend stored Elytra from collected XP **when SSO allows regular XP Mending**. Equipped Mending items take priority; pouch Elytra use the remaining XP. Flight can be switched off while repair still works.
- Preserve the larger netherite pouch capacity when attached to leggings, and correct stale pouch-tier information when detaching or replacing it.
- Add optional [ClientSort](https://modrinth.com/mod/clientsort/version/UWMryUad) sort, refill, and transfer controls while respecting pouch storage restrictions.
- Include safeguards against duplication and item loss when using a shulker box inside a pouch and changing or moving the owning pouch.
- Use the same spacing for ordinary maps and atlases, keeping pouch details clear of the minimap.
- Synchronize map and detail corner settings so changes in either mod apply to both minimap sources. This layout applies to Tool Pouch's own information overlay; an external replacement such as Immersive Overlays controls its own layout.

### Tiered Backpacks

[Original mod on Modrinth](https://modrinth.com/mod/tiered-backpacks)

Thanks for your hard work, **pajic**, and contributor **Moonkey**!

- Progress through six backpack tiers: leather, copper, iron, gold, diamond, and netherite.
- Start with 27 slots and upgrade through 36, 45, 54, 66, and 78 slots under the default settings.
- Configure each tier's capacity, up to 256 slots.
- Open a backpack from your hand or with its inventory keybind.
- Attach a backpack to a chestplate and open it while equipped. Netherite backpacks require fireproof chestplates.
- Dye non-netherite backpacks and keep their contents and color when upgrading, where supported by the resulting tier.
- Use supported accessory slots and optional Shulker Box Tooltip previews.
- Retain upstream support assets for [ItemSwapper](https://modrinth.com/plugin/itemswapper) and [Item Descriptions](https://modrinth.com/mod/item-descriptions). These are optional separate downloads; see the compatibility directory for their version and testing limits.
- Configure equipment, opening, and storage rules to suit your server.

#### My tweaks and additions

- Added optional ClientSort controls for sorting, refilling, and transferring items while preserving backpack nesting restrictions.
- Brought the original backpack settings into the combined suite settings screen.
- Included safe display and connection support for players without the suite; opening the custom backpack screen still needs the client mod.

### MiscTweaks

[Original mod on Modrinth](https://modrinth.com/mod/misctweaks)

Thanks for your hard work, **pajic**!

- Recover all block drops from creeper explosions when the corresponding tweak is enabled.
- Avoid berry-bush damage while sneaking or wearing body/leg armor, and reduce the slowdown when armored.
- Burn cobwebs away; additional burnable blocks can be configured.
- Transition from Elytra flight to swimming when entering water.
- Stop Thorns and Soul Speed from consuming armor durability.
- Mine obsidian-like blocks faster, with configurable block coverage and speed.
- Keep broken-block drops from scattering sideways. Crouch nearby to pull drops toward you.
- Find a broader selection of creeper music discs in loot chests.
- Let animals seek out and eat suitable food dropped on the ground; wolves are excluded by default.
- Optionally stop shulkers duplicating from shulker-bullet hits; this option is off by default.
- Adjust brightness separately for each dimension.
- Raise the hotbar slightly and lower the offhand shield for a clearer view. The hotbar adjustment yields to Raised when that mod is installed.
- Toggle and tune the individual tweaks rather than accepting every change as a fixed package.

#### My tweaks and additions

- Retained the original gameplay tweaks and exposed their server and client options in the combined settings screen.
- Kept the original optional integrations. See the [compatible-mod directory](docs/DEPENDENCIES.md) for matching downloads and limitations.

### Simple Death Improvements

[Original mod on Modrinth](https://modrinth.com/mod/simple-death-improvements)

Thanks for your hard work, **pajic**!

- Keep items dropped on death from despawning, giving you time to plan a recovery trip.
- Gather death drops and XP at one point instead of scattering them around.
- Try to relocate drops to a previously recorded safe spot when you die in or above lava or the void.
- Protect dropped items from explosions.
- Reduce the experience lost on death; the default loss is 20%.
- Optionally retain armor, hotbar items, offhand items, or equipped accessories on death. These retention options are off by default.
- Configure the death rules to match the level of risk you want in your world.

#### My tweaks and additions

- Kept the original death behavior and its options, and made those options accessible from the suite settings screen.
- Preserved the original accessory integrations and server control over these settings.

### Chalk — Fabric port

[Original mod on Modrinth](https://modrinth.com/mod/chalk)

Thanks for your hard work, **mortuusars and DaFuqs**, and contributors **MCLegoMan and Pintér Gábor**!

- Craft ordinary chalk from two calcite.
- Mark block surfaces to leave a trail through caves, tunnels, and builds.
- Draw directional arrows near a block's corners or an X near its center.
- Use glow chalk to make marks visible in darkness.
- Redraw marks or erase them when a route changes.

#### My tweaks and additions

- Included the local Minecraft 26.3 Fabric port.
- Added recoloring and glow conversions that preserve the chalk's remaining durability, custom name, and other item data.
- Connected the original particle setting to the suite settings screen.
- Added the shared native/vanilla compatibility described below. Players with the matching suite use Chalk's original rendering.

### Chalk: Colorful Addon

[Original addon on Modrinth](https://modrinth.com/mod/chalk-colorful-addon)

Thanks for your hard work, **DaFuqs and mortuusars**!

- Add chalk in all 16 dye colors.
- Use different colors to distinguish routes, rooms, destinations, or players' trails.
- Combine colored marks with glow chalk for visible markings in dark places.

#### My tweaks and additions

- Included the addon port for the suite's Minecraft 26.3 Chalk version.
- Made dye replace the chalk's current color, rather than mixing colors.
- Let you add dye, glow ink, or both in one crafting operation. Recoloring glowing chalk keeps it glowing.
- Added the following combinations through the suite's compatibility/crafting component:

| Ingredients | Result |
| --- | --- |
| Two calcite + glow ink sac | White glow chalk |
| Two calcite + red dye + glow ink sac | Red glow chalk |
| White chalk + red dye | Red chalk |
| Red chalk + blue dye | Blue chalk |
| White chalk + red dye + glow ink sac | Red glow chalk |
| Red chalk + blue dye + glow ink sac | Blue glow chalk |
| Any ordinary chalk + glow ink sac | Glowing chalk of the same color |

The same rules apply to the other dye colors. Converting existing chalk does not repair it.

### Shared Region Maps

[Original standalone project](https://github.com/THENATHE/shared-region-maps-shim) — a THENATHE addition with no separate Modrinth listing.

- Share exploration when players create ordinary maps for the same region, dimension, and scale.
- Let terrain discoveries and banner updates appear on copies of the shared map.
- Apply the same regional sharing to maps generated by MapStitch atlases.
- Keep sharing across server restarts.
- Preserve old and special maps rather than automatically merging every map in an existing world.
- Lock a map to keep an independent snapshot.

#### My tweaks and additions

- Focused the suite version on **vanilla maps and MapStitch**.
- Removed the Map Atlases adapter and the special Interdimensional Map Markers handling. Improved Maps is not an advertised integration.
- Preserved existing map identities and exploration data, and included the component for singleplayer as well as dedicated servers.
- This component has no settings to configure; its sharing applies automatically to eligible maps.

### Amethyst Curse Cleanser

[Original standalone project](https://github.com/THENATHE/amethyst-curse-cleanser) — a THENATHE addition with no separate Modrinth listing.

- Put a cursed item and one amethyst shard into a grindstone, in either input order, to remove Curse of Binding and/or Curse of Vanishing.
- Remove both supported curses with a single shard when the item has both.
- Keep other enchantments, durability, custom names, lore, and armor trims.
- Receive one echo shard when you take the cleansed item. It drops beside you if your inventory is full.
- Alternatively, use a smithing table with an empty template slot, the cursed item in the middle, and amethyst in the addition slot.
- Perform the operation without earning grindstone XP.

#### My tweaks and additions

- Brought my standalone curse-removal mod into the suite for Minecraft 26.3, retaining both grindstone and smithing use.
- Kept it usable by vanilla players on a server running the suite's vanilla-compatibility setup.

### One place for settings

- Open **Vanilla++ Quality of Life Suite** through [Mod Menu](https://modrinth.com/mod/modmenu), or use `/suite-settings` on the client.
- Browse the original mod settings together in one screen, grouped by module.
- Keep the original setting descriptions, validation, and server permissions.
- Change minimap/detail placement from either original client settings screen; atlas and ordinary pouch maps share the same layout. Keep appearance and displayed information controls in their original sections.
- Keep pending setting-change proposals when reopening the screen, and refresh correctly when the server sends updated settings.
- Leave each server's pending proposals and permissions behind when you disconnect.

## The shim: vanilla and modded players together

A **shim** is the suite's compatibility layer. With [Polymer](https://modrinth.com/mod/polymer) installed on the server, it translates supported mod content into forms that an ordinary Minecraft client can understand.

The suite checks support automatically when you join. Matching suite clients get the original modded items, rendering, and interfaces. Players without the suite get the supported vanilla presentation. There is no separate client shim to install and no UUID list to manage.

| How you play | What you get |
| --- | --- |
| Matching suite installed on server and client | Native mod features and interfaces. Client Polymer is optional. |
| Vanilla client joining a suite server with Polymer | Can connect and use supported server-side features, including SSO's compatible smithing/repair systems, Chalk drawing, and amethyst curse removal. |
| Fabric client without the suite joining that same server | Uses the vanilla-compatible path rather than receiving unsupported suite packets. |
| Suite server without Polymer | Normal modded play: clients need the suite and its dependencies. |

The compatibility layer includes:

- SSO repair, smithing, anvil, Mending, and enchantment support through the established vanilla-compatible interactions and guidance.
- Chalk drawing, colors, glow marks, recoloring, crafting, erasing, and durability handling for supported vanilla play.
- Safe visible representations of backpacks, pouches, and atlases for players without their client features.
- Clear install notices when an action needs the modded client.
- Automatic decisions for each supported module, so a mismatched module can use its fallback without forcing every other module into fallback too.
- Preservation of the real stored items and world data while their appearance is translated for another player.

**Vanilla players do not get custom backpack or pouch screens, MapStitch's world map/minimap, or the client-side HUD additions.** Install the matching suite on the client for those features. The compatibility layer does not turn every client-only feature into a vanilla one.

**Accept the server's generated resource pack for Chalk's vanilla-visible items and marks.** Server owners need to generate and host that pack through Polymer. Optional integration mods remain separate downloads; their presence does not mean every combination has been runtime-tested.

## Installation

The [merged testing checklist](docs/MERGED_TESTING.md) covers both new features. For a fresh test instance, use the release’s `.mrpack` or manual installation ZIP; [installation pack instructions](docs/INSTALLATION_PACK.md) explain their client/server dependency selection.

### Singleplayer or a fully modded server

1. Use **Minecraft 26.3**, **Java 25 or newer**, and **Fabric Loader 0.19.5**.
2. Download `vanilla-plusplus-quality-of-life-suite-1.0.2-merged.3+26.3.jar` from [Releases](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/releases/tag/v1.0.2-merged.3%2B26.3) and put it in `mods/`. For multiplayer, install the matching suite on the server and participating modded clients.
3. Install the required libraries below. The suite includes the feature mods, but these shared libraries remain separate.
4. Remove separate copies of the included feature mods, their addons, and the old compatibility shims from that instance. Keep your world and configuration files.
5. Launch the game. Optional **Mod Menu** adds a convenient settings entry; `/suite-settings` also opens the settings screen.

| Required library | Version to install |
| --- | --- |
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.161.0+26.3 |
| [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) | 1.14.1+kotlin.2.4.20 |
| [Fzzy Config](https://modrinth.com/mod/fzzy-config/version/thw1Z19c) | 0.7.7+fix2+26.3 |
| [Cloth Config](https://modrinth.com/mod/cloth-config) | 26.3.159 |
| [Mixson](https://modrinth.com/mod/mixson) | 2.2.1 |
| [Defaulted](https://modrinth.com/mod/defaulted) — use the suite's library ZIP | **1.3.8+26.3.dropfix.1** |
| [CodecUI](https://github.com/MehVahdJukaar/codecui) — use the suite's library ZIP; no Modrinth listing | **26.3-1.4.3** |

The release's `vanilla-plusplus-local-libraries-1.0.2-merged.3+26.3.zip` supplies the exact **Defaulted dropfix and CodecUI** builds. Extract its two JARs from `mods/` into your instance's `mods/` folder. Replace other external copies of those two libraries. Download the remaining libraries separately; the [dependency directory](docs/DEPENDENCIES.md) includes exact publisher download links. The ordinary Defaulted download does not replace the required dropfix build.

### Allow vanilla players to join

- Set up the server as above, then install **[Polymer Bundled 0.18.2+26.3](https://modrinth.com/mod/polymer/version/REBDssAz)** on the server.
- Use the complete Polymer Bundled download and configure its generated resource pack using the [Polymer resource-pack hosting guide](https://polymer.pb4.eu/latest/user/resource-pack-hosting/).
- Vanilla players install nothing. Players who want the full modded features install the matching suite and required libraries on their clients.
- Read the [compatibility section](#the-shim-vanilla-and-modded-players-together) for which features require the client mod.

### Optional extras

[Mod Menu 21.0.0](https://modrinth.com/mod/modmenu), [ClientSort 3.104.1+26.3](https://modrinth.com/mod/clientsort/version/UWMryUad), [Trinkets Updated](https://modrinth.com/mod/trinkets-updated), [Shulker Box Tooltip](https://modrinth.com/mod/shulkerboxtooltip), and [LambDynamicLights](https://modrinth.com/mod/lambdynamiclights) add settings access or supported integrations. They are not included in the suite. See [all dependencies and compatible mods](docs/DEPENDENCIES.md) for version requirements, installation roles, and testing limits.

For developers, server maintainers, or anyone troubleshooting a problem, the technical section below links the source layout, dependency records, update process, and test results. You do not need to build the mod yourself to use the downloads above.

## Technical info

### Packaging and provenance

This is a standalone, unofficial repository. One feature JAR contains separate modules with their original mod IDs, assets, configuration identities, and saved-data formats. Original notices remain in the source and production archive; see [credits and licensing](THIRD_PARTY_NOTICES.md).

The suite combines developer releases, local ports, and THENATHE additions. SSO uses the user-confirmed private official Minecraft 26.3 developer release. Its exact bytes and source audit are recorded separately from the public 26.2 baseline. The separate ChatGPT-produced SSO port remains paused. Chalk uses the recorded local 26.3 Fabric port, with the matching Colorful Addon metadata port. See [provenance and compatibility tracks](docs/PROVENANCE.md).

Defaulted is pinned to `1.3.8+26.3.dropfix.1`, SHA-256 `e339d6f0eb471a4ac41185fb9dbe0cfaa78a290c6ceedf92a49ba9110f732c61`. The unpatched release is not interchangeable. Keep CodecUI `26.3-1.4.3` external as documented: Defaulted's older nested dependency remains unchanged. Fzzy Config is an official external download and is excluded from the local-library ZIP. Exact artifact versions and hashes are recorded in [the dependency lock](locks/artifacts.json).

### Negotiation and configuration

The connection-scoped capability protocol checks module/addon versions and registry fingerprints. Native Chalk and SSO share confirmed block-state IDs after registry synchronization, preserving their native blocks independently. Without matching support, the affected module uses its Polymer fallback when available. Untouched original Tool Pouch/MapStitch clients can still be recognized through their advertised channels; untouched SSO/backpacks/Chalk clients cannot reliably prove installation without suite negotiation and use fallback.

The settings screen uses the original Fzzy configuration IDs, validation, permissions, and saving. Chalk's particle control saves to its original configuration. Settings managers preserve proposals within a connection, restore routing after invalidation, and clear connection-specific state on disconnect.

Implementation details: [negotiation](components/combined-compat/NEGOTIATION.md), [settings](docs/settings.md), [shared HUD layout](docs/HUD.md), and [shared-map changes](docs/shared-region-maps-changes.md).

### Building and updating

Follow the [source map and upstream update guide](docs/UPDATING.md) to stage the pinned inputs and bring in upstream changes. A GitHub fork is not required to inspect or update upstream source.

```sh
JAVA_HOME=/path/to/jdk25 ./gradlew build -PcompilerVersion=27 -Pjavac=/path/to/jdk27/bin/javac
```

The validated build uses a Java 27 compiler targeting Java 25; Java 25 is the runtime target. `verifyInputs` rejects missing or changed inputs before compilation. Branch output: `build/libs/vanilla-plusplus-quality-of-life-suite-1.0.2-merged.3+26.3.jar`.

Downloaded dependencies, Gradle caches, and QA worlds are excluded from Git. Previous standalone repositories and local source/build archives are documented in [local archive maintenance](docs/LOCAL_ARCHIVE.md); their historical releases remain separate from the suite.

### Changes and testing

Read the [changelog](CHANGELOG.md) and [validation report](docs/VALIDATION.md) for the exact tested artifacts, current regression coverage, historical gameplay/HUD evidence, and remaining manual checks. A listed integration or successful build alone is not a claim that every gameplay combination has passed testing.
