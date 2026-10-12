# Release 1.2.0

Vanilla++ Quality of Life Suite adds two nether-star upgrades for its existing Tool Pouch and Tiered Backpacks and incorporates THENATHE’s Debug Wrench. The external dependency set is unchanged from 1.1.6.

## Pickup upgrades

Put a Pouch Magnet in the active pouch to collect dropped items within a six-block spherical range. Put a Backpack Collector in the pouch to send normal pickups into the active backpack. Combine them for extended-range backpack collection, or use each separately. Attached pouches and attached backpacks follow the existing equipment selectors. Backpack selection prefers a supported accessory, then equipped chest storage, then the first valid inventory backpack.

The collector starts enabled. Bind **Toggle Backpack Collector** in Minecraft Controls, or use `/suite-pickup`, `/suite-pickup on` and `/suite-pickup off`. The hotkey is unbound initially. The preference belongs to the player and survives saved sessions and respawns; disabling it leaves the magnet active. Vanilla players use the commands without a client mod.

The magnet preserves dropped-item delay and ownership restrictions. Collector insertion obeys actual backpack capacity and nesting/shulker rules. Remaining items follow normal inventory pickup, staying in the world when inventory is full. A saved backpack with slots beyond a reduced configured capacity is left intact. Opening the pouch suspends both upgrades; opening the backpack suspends collector routing to avoid stale menu saveback.

Both items are added once to the existing pouch allowlist. Existing custom rules are retained. Later administrator removals or limits remain effective; the migration marker is `config/vanilla-plusplus-pickup-rules-v1`.

Craft these shapes in a crafting table. Each recipe makes one item; `S` is a nether star.

```text
Pouch Magnet           Backpack Collector
I . I                  L . L
I S I                  H S H
R . R                  L L L

I = iron ingot         L = leather
R = redstone dust      H = hopper
```

## Integrated Debug Wrench

Existing `debugwrench:debug_wrench` items and their `minecraft:debug_stick_state` selections retain their identity. The recipe stays unchanged:

```text
Iron   Empty  Iron
Empty  Iron   Empty
Empty  Copper Empty
```

Left-click selects the next block property and right-click changes its value. Sneak reverses either operation. Selection keeps the six-server-tick cooldown; right-click has no added delay. Survival/creative use needs no operator permission. Adventure/spectator restrictions, spawn protection, world-border/build-height checks and container preservation remain.

The updated suite capability handshake uses new versioned channels for the expanded module list, so older suite clients can use Polymer fallbacks without decoding the new message layout. Matching 1.2.0 clients retain native upgrade and wrench items.

All three new item sprites are fixed 16×16. The integrated wrench does not inspect resource-pack resolution or provide a 32×32 variant. Its former cosmetic resolution commands are retired with the standalone mod; gameplay remains available through the suite.

## Installation

Use Minecraft **26.3**, Fabric Loader **0.19.5**, Java **25+**, and the [locked external libraries](DEPENDENCIES.md). Install the suite on the server and matching native clients. Install full Polymer Bundled **0.18.2+26.3** on the server for vanilla clients. Native play without Polymer remains supported.

Stop and back up the instance, replace the suite JAR with `vanilla-plusplus-quality-of-life-suite-1.2.0+26.3.jar`, and **remove the separate Polymer Debug Wrench JAR** from both sides. Keep existing configuration/world files and external dependencies. Regenerate and host the Polymer pack so fallback clients receive the new artwork; no resource-pack-resolution detection is used. Accept the updated pack for custom sprites.

The new upgrades work server-side for vanilla players. The suite’s existing custom pouch/backpack screens still require the native client. A vanilla player can use already configured/attached storage and the collector commands. The hotkey requires the matching suite client.

The [generated modpack artwork](../artwork/vanilla-plusplus-modpack.png) combines the suite’s item motifs and is also the suite’s Mod Menu icon.

The standard release assets are `README.md`, the versioned suite JAR, `docs.zip` and `vanilla-plusplus-installation-pack-1.2.0+26.3.zip`. The installer retains side selection and official Fzzy Config downloads; internet access is needed for fresh installation. Release files belong under `Builds/Minecraft/Vanilla++ Quality of Life Suite/Main Plugin/1.2.0+26.3/`.

The standalone wrench source and historical releases remain preserved. The separately maintained ChatGPT SSO port remains paused. This release uses the established official developer inputs, including official SSO 2.10.0.

## Verification

The exact JAR SHA-256 is `69c2b7f7d345282143e4c7d1da8a9b4ea97032c61ff804348d50613a60349b75`. Dedicated native/Polymer initial/restart checks pass **1,342 pickup and 272 wrench assertions**; [record](pouch-utilities-verification-1.2.0.json).

All **eight upgrade connection profiles** pass: matching native suite, Fabric fallback, unmodified vanilla, a server without Polymer, a previous 1.1.6 client, native singleplayer with/without Polymer and Polymer on both sides. Real pickup, native hotkeys or fallback commands and item/model packets pass; six saved inventory screenshots were inspected. Packet/template regression passes **14,851 assertions**, and two physical clients receive the nested potion arrangement and move full 2,048-item stacks.

The final installation ZIP passes **13 fresh archive/installer checks**. Exact evidence and practical limits are recorded in [validation](VALIDATION.md). Earlier broad acceptance remains attributed to its original artifact.


## Player checks

1. Craft the upgrades and insert each into the active pouch. Check ordinary pickup with the magnet alone, backpack pickup with the collector alone, and distant backpack pickup with both.
2. Bind the collector control and toggle it; verify the magnet still works. Reconnect and confirm the preference remains.
3. Fill the backpack and check ordinary inventory overflow. Open its menu while picking up items and confirm contents stay intact after closing.
4. Confirm an existing wrench still remembers its block property, then craft another using the unchanged recipe. Check forward/backward selection and value cycling.
