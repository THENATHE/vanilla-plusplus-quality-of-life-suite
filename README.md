# Vanilla++ Quality of Life Suite

[Downloads](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/releases) · [Installation](#installation) · [Update guide](docs/UPDATING.md) · [Validation](docs/VALIDATION.md) · [Dependency and compatible-mod downloads](docs/DEPENDENCIES.md)

Unofficial Fabric gameplay suite for Minecraft **26.3**, maintained by THENATHE. One feature JAR packages separate modules with their existing mod IDs, assets, settings and saved-data formats. The modular structure keeps upstream updates reviewable. This is a new standalone repository, not a fork of Pajic's repositories.

Includes Simple Smithing Overhaul, MapStitch, Tool Pouch, Tiered Backpacks, MiscTweaks, Simple Death Improvements, Shared Region Maps, the local Chalk Fabric port, Chalk Colorful Addon, the atlas/Elytra addon and Amethyst Curse Cleanser. Original third-party notices are retained; see [credits and licensing](THIRD_PARTY_NOTICES.md).

## Installation

Minecraft 26.3, Java 25 or newer, Fabric Loader 0.19.5. Install `vanilla-plusplus-quality-of-life-suite-1.0.1+26.3.jar` on the server and on clients that want the native features. Remove separate copies of its included feature mods, addons and old compatibility shims from that instance's mods directory. Existing configurations and world data stay in place.

Shared libraries remain separate so their dependency identities and licensing are preserved. The release includes an optional `vanilla-plusplus-local-libraries-1.0.1+26.3.zip` containing the exact MIT-licensed Defaulted dropfix and CodecUI inputs; extract its two JARs into `mods/`. Download the remaining libraries from their publishers. Fzzy Config is a separate official download and is excluded from that archive. Use the exact validated runtime:

| Required library | Version |
| --- | --- |
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.161.0+26.3 |
| [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) | 1.14.1+kotlin.2.4.20 |
| [Fzzy Config](https://modrinth.com/mod/fzzy-config/version/thw1Z19c) | 0.7.7+fix2+26.3 (official public release) |
| [Cloth Config](https://modrinth.com/mod/cloth-config) | 26.3.159 |
| **[Defaulted, local drop fix](https://modrinth.com/mod/defaulted)** | **1.3.8+26.3.dropfix.1** |
| [CodecUI](https://github.com/MehVahdJukaar/codecui) (no Modrinth listing) | 26.3-1.4.3 |
| [Mixson](https://modrinth.com/mod/mixson) | 2.2.1 |
| [Mod Menu](https://modrinth.com/mod/modmenu), optional client settings entry | 21.0.0 |
| [Polymer Bundled](https://modrinth.com/mod/polymer), optional server vanilla support | 0.18.2+26.3 |

Optional integrations include [ClientSort](https://modrinth.com/mod/clientsort/version/UWMryUad), [Trinkets Updated](https://modrinth.com/mod/trinkets-updated), [Shulker Box Tooltip](https://modrinth.com/mod/shulkerboxtooltip), [LambDynamicLights](https://modrinth.com/mod/lambdynamiclights), and others. See the [complete dependency and compatible-mod directory](docs/DEPENDENCIES.md) for publisher links, supported version constraints, installation roles and testing limits.

Defaulted is explicitly pinned to the drop-fix build; the unpatched 1.3.8 release is not an interchangeable dependency. Its expected SHA-256 is `e339d6f0eb471a4ac41185fb9dbe0cfaa78a290c6ceedf92a49ba9110f732c61`. Keep the external CodecUI version above: Defaulted preserves its older nested dependency unchanged.

With server Polymer installed, the suite negotiates native support once during connection. A matching suite client needs no separate shim or UUID commands and does not require client Polymer. A vanilla client or a Fabric client without the suite uses the established Polymer behavior. Untouched original Tool Pouch/MapStitch clients can still be recognized by their native channels; untouched SSO/backpacks/Chalk clients cannot reliably prove their installation without suite negotiation and use fallback.

Without Polymer, the suite works as a native modded installation; joining clients need the suite and its dependencies. Vanilla access requires Polymer on the server. Generate and serve the Polymer resource pack through the existing Polymer setup; Chalk's fallback marks require the pack to render correctly. Install the complete Polymer Bundled version above, rather than a partial selection of its modules.

## Features and changes

- One connection-scoped capability negotiation, with exact module/addon versions and registry fingerprints. Native Chalk and SSO share confirmed block-state IDs after registry synchronization, preserving real Chalk marks and broken anvils independently. No UUID opt-in file or commands are included in the suite.
- Existing SSO and Chalk functional vanilla compatibility remains available. Tool Pouch, Tiered Backpacks and MapStitch retain connection/display compatibility: their custom interfaces require a native client. Unsupported actions provide the existing client-mod-required notice.
- Shared Region Maps keeps vanilla and MapStitch sharing. Map Atlases, Improved Maps and the old specialized adapters are removed from that component. Existing map IDs and the region index format are preserved.
- Dye recolors existing Chalk; glow ink sacs add glow, separately or in the same craft. Two calcite plus glow make white glow Chalk; adding a dye makes colored glow Chalk. Damage and other item components are preserved when recoloring existing Chalk.
- Amethyst Curse Cleanser removes curses with amethyst in a grindstone and retains its existing smithing interaction. It consumes the amethyst and returns the existing echo-shard byproduct while preserving other enchantments/components.
- MapStitch atlases in a pouch reserve space for pouch details when their HUDs share a side. Details appear below the actual scaled map, or above a bottom map if there is no room below. Minimap and detail position/offset controls remain independent.
- All original Fzzy settings appear in one suite screen, with original configuration IDs, validation, permissions and saving. Chalk particles are bridged to the same screen and save to the original Chalk configuration.

Open the suite through Mod Menu, or use `/suite-settings` on the client. See [settings](docs/settings.md), [HUD behavior](docs/hud-layout-fix.md), [negotiation](components/combined-compat/NEGOTIATION.md), and [map-sharing changes](docs/shared-region-maps-changes.md).

## Source and maintenance

See [source map and upstream update procedure](docs/UPDATING.md), [provenance and compatibility tracks](docs/PROVENANCE.md), and [validation and test limits](docs/VALIDATION.md). SSO uses the user-confirmed private official 26.3 developer release; its original bytes and source audit are recorded separately from the public 26.2 baseline. The historical standalone projects and releases remain separate; the existing SSO port project remains paused unless explicitly resumed.

Build from the pinned inputs with `JAVA_HOME=/path/to/jdk25 ./gradlew build -PcompilerVersion=27 -Pjavac=/path/to/jdk27/bin/javac`. Java 27 is an optional compiler used here with `--release 25`; Java 25 is the runtime target. `verifyInputs` rejects a missing or changed artifact before compilation. Output: `build/libs/vanilla-plusplus-quality-of-life-suite-1.0.1+26.3.jar`.

The repository excludes downloaded dependency JARs, Gradle caches and QA worlds. Pinned fetch and local-source build instructions are documented in docs/UPDATING.md. Published validation claims refer to the exact tested artifact, not to historical standalone results.
