# Release 1.1.5

Vanilla++ Quality of Life Suite updates Simple Smithing Overhaul, MapStitch and Sensible Stackables to current official developer releases for its Minecraft target. It preserves the existing atlas and pouch features, removes Defaulted from runtime installation, and fixes ordinary Elytra XP Mending in the active Tool Pouch when Clumps handles XP orbs.

## Official upstream updates

| Included mod | Previous suite input | Current official input | Change |
| --- | --- | --- | --- |
| [Simple Smithing Overhaul](https://modrinth.com/mod/simple-smithing-overhaul) | Private official 2.9.14+26.3 | [2.10.0+26.3](https://modrinth.com/mod/simple-smithing-overhaul/version/k5NaPhrq) | Replaces Defaulted with original repairables synchronization; live repair changes and rename/language handling fixes. |
| [MapStitch](https://modrinth.com/mod/mapstitch) | 1.1.6+26.3 | [1.1.7+26.3](https://modrinth.com/mod/mapstitch/version/oDB5IJ7O) | Russian translation fix; all 90 Java classes unchanged. |
| [Sensible Stackables](https://modrinth.com/mod/sensible-stackables) | Unofficial 3.0.3-port.1+26.3 | [3.1.1+26.3](https://modrinth.com/mod/sensible-stackables/version/oRpBICTz) | Official target support, live effective stack-limit updates, stale override-clearing fix and cushions stacking to 64; Defaulted removed, Mixson optional. |

Tool Pouch 1.1.10, Tiered Backpacks 1.0.20, MiscTweaks 1.4.4, Simple Death Improvements 1.6.0 and Bannerpoint 1.1.2 already match their latest Fabric releases for this target and retain their exact bytes. Releases for another Minecraft target are not substituted simply because their dates or numbers are newer. Depillage 1.0.2 in the supplied log already matches its latest compatible release; it remains a separate server mod. The [publisher audit](pajic-release-audit-2026-10-06.json) also records other pajic projects for reference; [verified input comparisons](pajic-input-verification-2026-10-06.json) record exact hashes and source context.

Thanks to **pajic** for the original mods and upstream updates. Previous official inputs, the historical Stackables port and all recorded release evidence remain preserved. The separate ChatGPT SSO port remains paused.

## Native synchronization and vanilla safety

SSO now synchronizes through `simple_smithing_overhaul:repairables`, and Stackables uses `sensible_stackables:stack_sizes`. The suite guards both send paths so clients without native support receive no unsupported payload. Matching native clients retain original synchronization and gameplay.

Stackables now supplies limits through getters rather than Defaulted item defaults. The compatibility module explicitly projects the effective limit into fallback metadata and excludes empty inventory slots from transformation. Full item counts remain unchanged; clients without the suite receive a maximum-stack prediction value capped at 99 when the actual configured limit is higher. Inventory correction stays authoritative on the server. This is the existing agreed fallback behavior, adapted to the new upstream implementation.

## Elytra Mending with Clumps

The user's server log includes Clumps 26.3.2. Its repair hook can return early from `ExperienceOrb.repairPlayerItems`, bypassing the suite's former return-only pouch hook. That explains why an ordinary Mending Elytra repairs when equipped or in the primary inventory but fails inside a pouch, without logging an exception. The isolated earlier fixture did not include this interaction.

The pouch addon now wraps the whole repair operation and offers its remaining eligible XP to the active pouch. It keeps normal equipped-item priority, honors the server's SSO regular-Mending setting and conserves XP. Standalone and attached netherite pouches use the same path. Damaged equipped Mending armor can still legitimately consume all available XP before the stored Elytra; pristine armor leaves XP for the wings.

SSO automatic material repair and enchantment-aware Unbreaking flight wear remain separate mechanisms. Automatic material repair still needs the original compatible whetstone, valid materials and server settings. Updating SSO alone would not have solved the XP-orb hook interaction: its ordinary Mending wrapper is unchanged.

The targeted fix does not require removing Armored Elytra. Verification distinguishes actual tested combinations from the inaccessible full remote server; see [validation](VALIDATION.md).

## Installation changes

| Requirement | Exact target |
| --- | --- |
| Suite | `1.1.5+26.3`, stable `main` |
| Minecraft / Fabric Loader / Java | `26.3` / `0.19.5` / `25+` |
| Combined compatibility | `1.1.1+26.3`, nested |
| Stackables compatibility | `1.0.1+26.3`, nested |
| Tool Pouch atlas/Elytra addon | `1.0.9-suite.1+26.3`, nested |
| Mixed-scale atlas addon / coordinator | `1.1.5+26.3` / `1.1.1+26.3` |
| Fzzy Config | Official `0.7.7+fix3+26.3`, download-only |
| CodecUI | Retained external `26.3-1.4.3` |
| Defaulted | No runtime requirement; exact historical compile input retained |
| Polymer for clients without the suite | Full Polymer Bundled `0.18.2+26.3` on the server |

Use matching suite files on the server and native clients, and preserve saved worlds/configuration files. Replace the previous suite JAR and update Fzzy Config to the release-selected fix3. Remove the old Defaulted JAR only if no other installed mod still requires it. Keep CodecUI and Mixson: Defaulted's removal does not remove their independent suite requirements. Do not install separate copies of already nested mods or duplicate suite versions.

[Releases](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/releases/tag/v1.1.5%2B26.3) provides the standard four assets:

- `README.md`
- `vanilla-plusplus-quality-of-life-suite-1.1.5+26.3.jar`
- `docs.zip`
- `vanilla-plusplus-installation-pack-1.1.5+26.3.zip`

The installation ZIP includes permitted dependency binaries, notices, source records and a side-aware installer. Fzzy Config is obtained directly from its official publisher, so a fresh installation needs internet access. [Installation instructions](INSTALLATION_PACK.md) explain the exact client/server setup. Existing Polymer hosting and successfully loaded-pack requirements remain unchanged.

## Verification and your testing steps

[Validation](VALIDATION.md) records the final tested artifact, checks and remaining limits. Historical results retain their original artifact identities; an unchanged original JAR does not establish new runtime acceptance.

1. Test on matching suite client/server files with no duplicate original mods. Confirm the configured Stackables limits and ordinary SSO repair interactions remain available.
2. Keep Clumps installed. Put a damaged ordinary Mending Elytra in a standalone netherite pouch, close its menu and collect XP from experience bottles. Confirm its damage decreases and reopen the menu to check saved contents.
3. Repeat with the pouch attached to pristine Mending leggings, then damaged leggings. Check normal equipped-item priority and repair from XP left after the leggings. Test an open pouch menu and persistence after closing it.
4. Disable regular XP Mending in the server's SSO settings and confirm XP does not repair the wings through the pouch addition. Restore the setting afterward.
5. Join the Polymer server using a Fabric client without the suite and a vanilla client. Confirm both stay connected without missing-payload errors; test a configured larger stack and ordinary inventory moves, preserving quantities.
6. Check known mixed-scale atlases, `/atlas fix`, `/atlas repair`, `/atlas dedupe`, `/atlas makecopy` and banner waypoints. Their existing behavior and saved map IDs should remain intact.
