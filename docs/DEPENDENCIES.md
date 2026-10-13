# Dependencies and optional integrations

Applies to **Vanilla++ Quality of Life Suite 1.4.1+26.3**. The toolbox crafting layout and optional Sorted corrections add no runtime dependency. The existing native feature modules use the Fabric/Fzzy dependencies; the original Drops into Shulker, placement, grindstone and datapack JARs are not required. No Collective library is added. Remove replaced standalone implementations when their corresponding suite features are enabled. [Current installation and verification](RELEASE_1_4_1.md).

The publisher input checks below retain their original dates. Pajic inputs were verified on 2026-10-06, Bannerpoint on 2026-10-05, and other original dependencies/integrations on 2026-10-04. Their locked versions are unchanged; original links/source records are retained. Original author/project metadata for the native additions was checked on 2026-10-12 in [the attribution record](native-features-attribution-2026-10-12.json). A listed optional integration is not a claim of fresh gameplay acceptance; see [validation](VALIDATION.md).

## Required external libraries

Install these separately on the server and on clients using the suite. Vanilla clients do not install these libraries. Keep the validated versions below rather than choosing an arbitrary newest download.

| Library / publisher page | Validated version / download | Notes |
| --- | --- | --- |
| [Fabric API](https://modrinth.com/mod/fabric-api) | [0.161.0+26.3](https://modrinth.com/mod/fabric-api/version/bNnaTiuM) | Shared Fabric APIs. |
| [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) | [1.14.1+kotlin.2.4.20](https://modrinth.com/mod/fabric-language-kotlin/version/eRRZzGMc) | Required by Fzzy Config; includes Kotlin runtime libraries. |
| [Fzzy Config](https://modrinth.com/mod/fzzy-config) | [0.7.7+fix3+26.3](https://modrinth.com/mod/fzzy-config/version/YkOumqzV) | Exact suite pin. Public publisher download; downloaded by the installation helper from its official publisher; its binary is not redistributed. |
| [Cloth Config API](https://modrinth.com/mod/cloth-config) | [26.3.159+fabric](https://modrinth.com/mod/cloth-config/version/fg2uyxOW) | Preserves the original Chalk configuration backend. |
| [mixson](https://modrinth.com/mod/mixson) | [2.2.1](https://modrinth.com/mod/mixson/version/yWpBBcpq) | Required by SSO and MiscTweaks. |
| [CodecUI — publisher source](https://github.com/MehVahdJukaar/codecui) | **26.3-1.4.3** — [local installation instructions](INSTALLATION_PACK.md) | No publisher Modrinth project was found. This is the retained fork-family input; see [provenance](PROVENANCE.md). Retained as a separate external runtime input; removing Defaulted does not remove this library. |

**Platform:** Minecraft 26.3, Java 25+, and [Fabric Loader 0.19.5](https://fabricmc.net/use/installer/). Fabric Loader is installed through the launcher/installer; it is not a dependency mod downloaded from a Modrinth project. Java and Minecraft are platform prerequisites.

The release’s `vanilla-plusplus-installation-pack-1.4.1+26.3.zip` supplies the suite, its required local libraries, six permitted public dependencies, their notices and a side-aware Python installer. Fzzy Config remains an official manifest download. Follow [installation instructions](INSTALLATION_PACK.md); no separate library ZIP is published.

Defaulted is no longer installed by this release. The exact `1.3.8+26.3.dropfix.1` input and guarded compatibility hook are retained for compiling historical optional support; this does not make it a suite runtime dependency.

## Optional libraries and tools

| Modrinth link | Version / role | Installation and coverage |
| --- | --- | --- |
| [Polymer](https://modrinth.com/mod/polymer) | [0.18.2+26.3](https://modrinth.com/mod/polymer/version/REBDssAz); full **Polymer Bundled** | Install on the server for vanilla/Fabric-without-suite access. Client Polymer is optional. Native, fallback and client-Polymer profiles are recorded in validation. Keep core, resource-pack and virtual-entity modules together. |
| [Mod Menu](https://modrinth.com/mod/modmenu) | [21.0.0](https://modrinth.com/mod/modmenu/version/kyy7dbrZ) | Optional client entry into the unified settings screen; `/suite-settings` also works. |
| [Hotkey Wheel](https://github.com/THENATHE/hotkey-wheel) | **1.0.6+26.3** | Optional client radial controls. Final-suite wheel activation/selection and settings navigation are required release checks; exact candidate results and hashes are in validation. Not bundled in the suite or installer. |
| [Client Sort](https://modrinth.com/mod/clientsort) | [3.104.1+26.3](https://modrinth.com/mod/clientsort/version/UWMryUad) | Optional sorting integration for Tool Pouch; client install enables it, and its own server acceleration is optional. This exact version is a pinned **compile-only** build input, staged under `libs/compile-only/`; it is not bundled or required to launch. Its server schema/permutation regression is covered; client buttons and accelerated network packets are not independently exercised. |

Sorted [**0.1.26**](https://modrinth.com/mod/sorted/version/28s0yYqf) passed native and Polymer connected profiles for the backpack bridge; retained [**0.1.25**](https://modrinth.com/mod/sorted/version/UB75pgWu) also passed a connected native profile without BRBR. [Sorted](https://modrinth.com/mod/sorted) remains an unchanged external mod and a compile-only build input, never a nested suite module or installer dependency.

Install Sorted separately with its own required Fabric API and YACL dependencies, following its publisher instructions. The bridge identifies actual storage for both toolbox crafting layouts, leaving crafting inputs/output and player inventory separate. The original Tiered Backpacks menu is regression-tested with Sorted's own built-in support; this update adds no plain-screen bridge.

For source builds, place unchanged Sorted 0.1.25 from [official Modrinth version UB75pgWu](https://modrinth.com/mod/sorted/version/UB75pgWu) at `libs/compile-only/sorted-fabric-0.1.25-mc26.x.jar`; `verifyInputs` checks its locked SHA-256 `44cc1f50724264a37319dcb4358d071cb8e20a6a324ac595e68ba887750e7a71`. The storage transfer/filter/cache view excludes crafting and player slots; Sorted's global Alt keep rules remain separate. Current runtime coverage, exact hashes and verification status are in [the 1.4.1 record](RELEASE_1_4_1.md); API compatibility alone is not runtime acceptance.

Optional [Better Recipe Book Rebetterered](https://github.com/THENATHE/better-recipe-book-rebetterered) **1.11.7+26.3** and Hotkey Wheel **1.0.6+26.3** are the current optional integration inputs. Hotkey Wheel native and Polymer profiles pass on the final suite; BRBR with Sorted 0.1.26 passed eight connected recipe cases per profile, four ordinary and four maximum, with backpack ingredients. Exact results and hashes are recorded in the release guide. They remain separate downloads. The standard recipe book remains supported without BRBR, with native outside-click protection plus visible body/tab bounds in side and narrow overlay layouts. BRBR's optional hover and separate Pins controls remain protected. Earlier BRBR and wheel results retain their historical versions and scope.

## Additional integrations retained from the included mods

These are optional and are not bundled. Choose a Fabric build for Minecraft 26.3 and follow that project’s own dependency requirements. Version availability below is a publisher listing check, not a runtime compatibility certification. Source evidence comes from the retained module `fabric.mod.json`, `CompatFlags`, compatibility mixins and entrypoints.

| Modrinth link | Included module / integration | Fabric 26.3 listing at check time |
| --- | --- | --- |
| [Trinkets Updated](https://modrinth.com/mod/trinkets-updated) | MapStitch, Tool Pouch, Tiered Backpacks and Simple Death Improvements: accessory slots and associated item/death handling. | Available; use the updated API, not the legacy Trinkets project. |
| [Ohmega](https://modrinth.com/mod/ohmega) | Accessory integration in the same modules. | Available. Upstream metadata requires **>=1.5.21 and <1.6**; do not substitute 1.6+. |
| [Shulker Box Tooltip](https://modrinth.com/mod/shulkerboxtooltip) | Tool Pouch and Tiered Backpacks: container content tooltips. | Available. |
| [ItemSwapper](https://modrinth.com/plugin/itemswapper) | Tiered Backpacks includes device-group assets for quick item swapping. Install separately; not runtime-tested with this suite. | Fabric 26.3 beta listing available: 1.0.0-beta.3-26.3. |
| [Item Descriptions](https://modrinth.com/mod/item-descriptions) | Tiered Backpacks includes description translations for this optional client mod. Not runtime-tested with this suite. | Available; 2.8.4+26.2-fabric metadata also lists 26.3. |
| [LambDynamicLights - Dynamic Lights](https://modrinth.com/mod/lambdynamiclights) | Tool Pouch: light from a lantern carried in the pouch. | Available. |
| [Immersive Overlays](https://modrinth.com/mod/immersive-overlays) | Tool Pouch suppresses its own info overlay when this is installed, letting Immersive Overlays provide it. The suite’s minimap spacing fix targets Tool Pouch’s own overlay. | Available; the replacement overlay’s positioning is outside the suite HUD fix. |
| [Raised](https://modrinth.com/mod/raised) | Tool Pouch/MiscTweaks: compatibility with adjusted HUD positioning. | Available. |
| [Serene Seasons](https://modrinth.com/mod/serene-seasons) | Tool Pouch: calendar/season information. | Available; publisher version numbering retains 26.1.2 but its release metadata lists 26.3. |
| [Ok Zoomer - It's Zoom!](https://modrinth.com/mod/ok-zoomer) | Tool Pouch: pouch spyglass zoom support. | 26.3 beta listings only; not runtime-tested here. |
| [Zoomify (Zoom)](https://modrinth.com/mod/zoomify) | Tool Pouch: pouch spyglass zoom support. | Available. |
| [Spyglass Astronomy](https://modrinth.com/mod/spyglass-astronomy) | Tool Pouch: access to astronomy behavior with a pouch spyglass. | Available. |
| [Sodium](https://modrinth.com/mod/sodium) | MapStitch and MiscTweaks retain their Sodium config API entries. | Available; upstream suggests >=0.9.2. Use a matching Fabric release. |
| [Enchantment Disabler](https://modrinth.com/mod/enchantment-disabler) | SSO: respects disabled enchantments. | Available. |
| [Tax Free Levels](https://modrinth.com/mod/tax-free-levels) | SSO: anvil XP-cost integration. | Available. |
| [Better Tridents](https://modrinth.com/mod/better-tridents) | SSO: compatibility with trident enchantment rules. | Available. |
| [Penchant](https://modrinth.com/mod/penchant) | SSO: optional enchanting integration controlled by SSO’s mod-integration setting. | Available. |
| [Modest Magic](https://modrinth.com/mod/modest-magic) | SSO: tablet smithing integration. | Available; publisher version numbering retains 26.1.2 but its release metadata lists 26.3. |

Tool Pouch also retains configurable item allowlists for other mods. An item-storage rule is separate from a functional integration and does not establish that the other mod runs on Fabric 26.3; the table above covers the implemented integration hooks and metadata suggestions.

## Preserved upstream references without a current Fabric 26.3 download

| Project | Status |
| --- | --- |
| [Locator Lodestones](https://modrinth.com/mod/locator_lodestones) | Tool Pouch retains waypoint tracking support; no Fabric 26.3 version was listed by Modrinth at this check. |
| [Aileron](https://modrinth.com/mod/aileron) | Tool Pouch retains an integration hook; no Fabric 26.3 version was listed by Modrinth at this check. |
| [Remapped](https://modrinth.com/mod/remapped) | MapStitch retains palette integration; no Fabric 26.3 version was listed at this check. |
| [Trinkets](https://modrinth.com/mod/trinkets) | Historical API supported by some upstream branches. Use Trinkets Updated for the current suite’s target. |
| [Curios API](https://modrinth.com/mod/curios) | Upstream multi-loader source reference. Curios is not a Fabric dependency for this suite. |

Map Atlases and Improved Maps are intentionally excluded from Shared Region Maps in this suite. Their old adapters are not advertised as suite compatibility.

## Included feature mods: publisher links

These modules are already inside the suite JAR. Keep their separate original JARs out of the same instance to avoid duplicates. The links identify their publishers; they do not replace the suite’s locked inputs.

| Included module | Publisher link | Suite input note |
| --- | --- | --- |
| Simple Smithing Overhaul | [Modrinth](https://modrinth.com/mod/simple-smithing-overhaul) | Official 2.10.0+26.3 developer release. |
| Bannerpoint | [Modrinth](https://modrinth.com/mod/bannerpoint) | Official 1.1.2+26.3, unchanged; separate suite compatibility 1.0.1+26.3. |
| MapStitch | [Modrinth](https://modrinth.com/mod/mapstitch) | Official 1.1.7+26.3. |
| Tool Pouch | [Modrinth](https://modrinth.com/mod/tool-pouch) | Official 1.1.10+26.3. |
| Tiered Backpacks | [Modrinth](https://modrinth.com/mod/tiered-backpacks) | Official 1.0.20+26.3. |
| MiscTweaks | [Modrinth](https://modrinth.com/mod/misctweaks) | Official 1.4.4+26.3. |
| Simple Death Improvements | [Modrinth](https://modrinth.com/mod/simple-death-improvements) | Official 1.6.0+26.3. |
| Chalk (Fabric) | [Modrinth](https://modrinth.com/mod/chalk) | Suite uses the local Fabric 26.3 port. |
| Chalk: Colorful Addon | [Modrinth](https://modrinth.com/mod/chalk-colorful-addon) | Suite uses the local metadata port for Chalk 26.3. |

Shared Region Maps (**1.0.5-combo.1+mc26.3**), Amethyst Curse Cleanser, the atlas/Elytra addon, and suite compatibility components are locally maintained here; no separate Modrinth listing is claimed. Their source locations are in [UPDATING.md](UPDATING.md). The bundled [MixinConstraints library](https://github.com/Moulberry/MixinConstraints) stays inside the original feature JARs; no additional download is required and no Modrinth listing was found.

## Included Sensible Stackables module

[Pajic’s Sensible Stackables](https://modrinth.com/mod/sensible-stackables) is nested as the unchanged official **3.1.1+26.3** release, alongside its separate Polymer compatibility module. No external copy is needed. Its current upstream dependency is Fzzy Config; Mixson is optional for Stackables but remains required by other suite mods. Defaulted is no longer needed. The previous **3.0.3-port.1+26.3** and untouched **3.0.3+26.2** baseline remain independently identifiable historical tracks. [Per-track provenance, dependency locks and verification](../components/sensible-stackables/README.md).

## Included Bannerpoint module

[Pajic’s Bannerpoint](https://modrinth.com/mod/bannerpoint) is already included in suite 1.3.0 as the unchanged official **1.1.2+26.3** Fabric release. Its separate **1.0.1+26.3** compatibility module keeps the original native behavior and contributes its waypoint assets through the existing Polymer resource-pack dependency. No additional library is required. Do not install a separate Bannerpoint JAR alongside the suite. Native Bannerpoint clients work without Polymer; clients without Bannerpoint see its locator-bar icons only after the Polymer server pack successfully loads. Custom name labels require Bannerpoint’s client code. See [Bannerpoint support](BANNERPOINT.md) and [component provenance](../components/bannerpoint/README.md).
