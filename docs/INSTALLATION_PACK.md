# Installation pack — Vanilla++ Quality of Life Suite 1.1.6+26.3

The **1.1.6+26.3** release fixes stored-item packet encoding and keeps the same external dependencies as 1.1.5. Replace the suite JAR on the server and matching native clients; [release instructions](RELEASE_1_1_6.md) and [packet verification](PACKET_FIX_1_1_6.md) explain the change. It does not require a settings reset or new resource pack.

The installation ZIP supplies the suite and its exact libraries for a native Fabric client or a dedicated server that also accepts vanilla players. Its helper selects the appropriate files for each side. **Internet access is required to obtain Fzzy Config from its official publisher.** Minecraft, Java, Fabric Loader and the server’s resource-pack hosting must be set up separately.

The [stable 1.1.6+26.3 release](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/releases/tag/v1.1.6%2B26.3) has four public assets:

- `README.md` — the accessible feature overview and installation entry point.
- `vanilla-plusplus-quality-of-life-suite-1.1.6+26.3.jar` — the combined mod, for an instance whose exact dependencies are already installed.
- `docs.zip` — documentation, technical records and release verification information.
- `vanilla-plusplus-installation-pack-1.1.6+26.3.zip` — the suite, permitted dependency binaries, licenses and installer described below.

There is one installation ZIP; no separate `.mrpack`, manual ZIP or library ZIP is published. The final suite digest is recorded in the [installation lock](installation-pack.lock.json) and [build verification](build-verification.json).

## Native client installation

1. Create a new **Minecraft 26.3 / Fabric Loader 0.19.5** instance in your launcher. Use **Java 25 or newer**.
2. Extract the installation ZIP into a separate setup directory. Install Python **3.9+** if needed, then open a terminal in the extracted directory.
3. Run the helper, replacing the instance path with the launcher instance’s Minecraft directory:

   ```sh
   python3 install.py --side client --instance /path/to/client-instance --dry-run
   python3 install.py --side client --instance /path/to/client-instance --with-optional
   ```

   `--with-optional` includes **Mod Menu 21.0.0**, the optional client settings entry. Omit that option if you do not want Mod Menu; the settings hotkey and `/suite-settings` remain available in-world.

4. Launch the instance. Keep separate copies of the original feature mods and older suite shims out of its `mods/` folder to avoid duplicates.

Bannerpoint and its compatibility component are already inside the suite JAR; do not add separate copies. Stable suite 1.1.6 includes both modules on `main`.

The client selection excludes Polymer. Native suite clients use the mod interfaces when joining a matching server. Vanilla players joining a Polymer-enabled server install none of these client files.

## Dedicated server installation

Use a new server directory, or stop and back up an existing instance before deliberately replacing its old mod files. The helper refuses to overwrite files with different contents and never changes world/configuration files.

1. Install Java **25+** and prepare a **Minecraft 26.3 / Fabric Loader 0.19.5** server using the [official Fabric installer](https://fabricmc.net/use/server/).
2. Extract the installation ZIP into a separate setup directory and install Python **3.9+** if needed.
3. Run the helper from that setup directory, replacing the instance path with your Fabric server directory:

   ```sh
   python3 install.py --side server --instance /path/to/fabric-server --dry-run
   python3 install.py --side server --instance /path/to/fabric-server
   ```

   On Windows, use `py -3 install.py --side server --instance "C:\Minecraft\suite-server"`.

4. The helper verifies the selected binaries and installs the suite, CodecUI and required external libraries, including **Polymer Bundled 0.18.2+26.3**. It excludes Mod Menu. Fzzy Config is fetched from its official Modrinth CDN URL.
5. Review and accept Minecraft’s EULA yourself, then start the Fabric server normally. The helper neither accepts the EULA nor starts the server.
6. Generate and host Polymer’s server resource pack using the [Polymer hosting guide](https://polymer.pb4.eu/latest/user/resource-pack-hosting/). Players should accept that pack for Chalk’s vanilla-visible items and marks and Bannerpoint’s locator-bar icons. Bannerpoint icons stay hidden until the pack successfully loads; custom banner-name labels require Bannerpoint on the client. The installation ZIP cannot supply the resource pack for your particular server configuration or hosting address.

The installer copies mod files; it does not install a launcher, Minecraft, Fabric Loader or Java. Vanilla clients still do not get custom backpack/pouch screens, MapStitch’s world map/minimap or client HUD features. See the [suite’s compatibility overview](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/tree/main#the-shim-vanilla-and-modded-players-together).

Defaulted is no longer required or supplied by this kit. When upgrading, remove the old Defaulted JAR only if no other installed mod still requires it. Keep existing world/configuration files and do not remove CodecUI: it remains a separate retained suite input.

## Exact contents and permissions

Cloth Config and Polymer corresponding upstream source archives and license texts are included under `overrides/suite-installation/dependency-sources/` and `licenses/`; their pinned records are in [dependency-distribution.lock.json](dependency-distribution.lock.json). The installer also copies these records into `suite-installation/` for reference.

The ZIP includes these two local files under `overrides/mods/`:

| Included file | SHA-256 | License/source context |
| --- | --- | --- |
| Suite 1.1.6+26.3 | See the installation lock and build verification | Existing authorized suite distribution; original notices remain in its JAR and the kit. [Source](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite). |
| CodecUI 26.3-1.4.3 | `4d07c219bd85b58283a0317d6f43cca838adf9cd16be321316cd32f2fb1be898` | MIT declaration and retained notices; precise private build revision is unrecorded. [Source-family record](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/tree/main/components/codecui-reference). |

Six public publisher binaries are cached under `downloads/mods/`, with licenses and hashes retained. Fzzy Config has only its official download entry:

| Publisher version | Client | Dedicated server | Distribution / license |
| --- | --- | --- | --- |
| [Fabric API 0.161.0+26.3](https://modrinth.com/mod/fabric-api/version/bNnaTiuM) | Required | Required | Included; Apache-2.0 |
| [Fabric Language Kotlin 1.14.1+kotlin.2.4.20](https://modrinth.com/mod/fabric-language-kotlin/version/eRRZzGMc) | Required | Required | Included; Apache-2.0 |
| [Fzzy Config 0.7.7+fix3+26.3](https://modrinth.com/mod/fzzy-config/version/YkOumqzV) | Required | Required | Official download only; TDL-M 1.3 |
| [Cloth Config 26.3.159+fabric](https://modrinth.com/mod/cloth-config/version/fg2uyxOW) | Required | Required | Included; LGPL-3.0-only |
| [Mixson 2.2.1](https://modrinth.com/mod/mixson/version/yWpBBcpq) | Required | Required | Included; MIT |
| [Mod Menu 21.0.0](https://modrinth.com/mod/modmenu/version/kyy7dbrZ) | Optional | Excluded | Included; MIT |
| [Polymer Bundled 0.18.2+26.3](https://modrinth.com/mod/polymer/version/REBDssAz) | Excluded | Required by this server kit | Included; LGPL-3.0-only |

Mod Menu’s exact JAR already includes Text Placeholder API; no separate copy is added. Other optional integrations, including ClientSort, are outside this kit. Dependency project pages and optional integration links are in [DEPENDENCIES.md](DEPENDENCIES.md).

Fzzy Config is **not inside the ZIP**, including nested suite JARs. Its [TDL-M 1.3 license, section 2.2](https://github.com/fzzyhmstrs/fconfig/blob/master/LICENSE#L25), permits modpacks to obtain it through a manifest from the official Modrinth/CurseForge source. The helper downloads the unchanged pinned file directly from `cdn.modrinth.com`; the distribution includes only its URL and hashes. This is why a new installation needs internet access even though the other selected binaries are included.

## Technical records and verification

The kit uses `modrinth.index.json` to retain publisher URLs, pinned hashes and per-file client/server selections. It is distributed as a ZIP with `install.py`; it is not presented as a launcher-importable `.mrpack`. “Unsupported” in a manifest environment means that this kit does not install that file on that side. Polymer itself can be installed on compatible clients separately if wanted.

The [installation lock](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/blob/main/docs/installation-pack.lock.json) records publisher API version/project URLs, licenses, sizes, SHA-1/SHA-512/SHA-256 hashes, side selections and local overrides. The lock is separate from the production JAR’s build lock. `tools/package-installation.py` reproduces the kit; `tools/install-pack.py` is included as `install.py`.

The helper verifies selected included files and its official download before writing them to the instance. Release checksum and installation verification records accompany the documentation in `docs.zip` and remain available locally. [Runtime validation](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/blob/main/docs/VALIDATION.md) identifies the exact branch artifact and separates fresh release checks from historical evidence; installation verification separately covers archive bytes, side filtering and helper installation. A launcher GUI import is not claimed for this installer ZIP.
