# Vanilla++ 1.4.0+26.3

Backpack crafting is now included in main as a separate suite-owned addon. An Iron Toolbox in the active Tool Pouch enables a compact floating 2×2 crafting panel and recipe book. A Netherite Toolbox enables 3×3. Recipe placement uses both backpack and player inventory. Original Tiered Backpacks and Tool Pouch sources/JARs remain intact.

## Items and recipes

Final selectors:

- `vpp_backpack_crafting:iron_toolbox`
- `vpp_backpack_crafting:netherite_toolbox`

The testing hammer item/recipe/model IDs are removed with no conversion or aliases. Existing testing setup markers do not block the new one-time pouch permissions. Later administrator edits remain authoritative.

Iron toolbox: seven iron ingots surrounding a crafting table, with an iron chain at top middle (`ICI / ITI / III`). Netherite upgrade: Netherite Upgrade template + Iron Toolbox + netherite ingot in a smithing table. The normal iron pickaxe recipe remains craftable. Both shipped icons retain their exact 16×16 pixels, limited palette and binary alpha.

## Installation and versions

Minecraft 26.3, Fabric Loader 0.19.5+, Java 25. Replace the suite JAR on the server and matching native clients. See [installation instructions](INSTALLATION_PACK.md) and [exact dependencies](DEPENDENCIES.md). External libraries and immutable developer inputs are unchanged. Polymer 0.18.2+26.3 remains optional for matching clients; it permits vanilla connectivity and item display on the server. Backpack screens require native clients. The SSO-port track remains paused.

Suite 1.4.0+26.3; Backpack Crafting 1.0.0+26.3; combined compatibility 1.4.0+26.3; Tool Pouch atlas/Elytra addon 1.0.10+26.3; mixed-scale addon 1.1.9+26.3. Optional BRBR compatibility continues entirely through addon mixins; its source/JARs remain untouched.

## Verification

Exact JAR SHA-256: `dc2e8d7c5bcf90ab22822e2886f766a1735bc6a2558d4794dbb51c816c689386`.

- Build, locked-input checks and 19-module archive verification passed.
- Native and Polymer server profiles each passed **198 checks**, including registered final IDs, absent retired items/recipes/assets, Tool Pouch permissions despite a testing marker, recipe selection and consumption, ordinary/shift smithing with a vanilla template-consumption control, menu conservation and ClientSort. [Evidence](../qa-backpack-crafting/evidence/1.4.0/server/verification.json).
- Installation ZIP and actual disposable client/server installs passed **13 checks**, including exact hashes, side selection, sources/notices, idempotency, rejected corrupt/unsafe inputs and official-download-only Fzzy. [Report](installation-pack-verification.json). Launcher GUI import was not separately tested.
- Hotkey Wheel **1.0.6+26.3** passed native and Polymer runtime profiles on the final candidate, with 62 assertions plus one context observation each: activation/selection, overlapping keybindings, suite menus and unified configuration. [Evidence](../qa-backpack-crafting/evidence/1.4.0/wheel/verification.json).
- All four connected profiles passed: native on Polymer, Fabric-only on Polymer, unmodified vanilla on Polymer, and native without Polymer. New toolbox IDs, safe fallback representations and unsupported backpack-opening guards were checked. [Evidence](../qa-backpack-crafting/evidence/1.4.0/connections/README.md).
- Published BRBR **1.11.1+26.3** with Polymer and local **1.11.2+26.3** without Polymer each passed eight connected GUI cases covering both toolbox tiers, leather/netherite capacity, ordinary/maximum crafting, backpack ingredients, ghost recipes, viewport bounds and conservation. [Evidence](../qa-backpack-crafting/evidence/1.4.0/gui/verification.json).

Earlier beta BRBR/singleplayer and gameplay evidence keeps its original hashes and scope in [backpack records](BACKPACK_CRAFTING.md) and [historical validation](VALIDATION.md). ClientSort server operations are covered; its client buttons and accelerated network packets are not independently exercised here.
