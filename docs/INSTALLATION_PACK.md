# Installation pack — Vanilla++ Quality of Life Suite 1.0.2-merged.3+26.3

The installation pack supplies the existing suite and its exact local libraries, then downloads public dependencies from their publishers on Modrinth. It supports a native Fabric client and a dedicated server that also accepts vanilla players. Internet access is required during installation. Minecraft, Java, the Fabric loader installation, and the server's resource-pack hosting still need to be set up.

Download from the [merged testing release](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/releases/tag/v1.0.2-merged.3%2B26.3):

- `vanilla-plusplus-installation-pack-1.0.2-merged.3+26.3.mrpack` — import with a launcher supporting Modrinth packs.
- `vanilla-plusplus-installation-pack-1.0.2-merged.3+26.3-manual.zip` — the same mod selection plus a Python installer for dedicated servers or manual client setup.

These are installation assets for the experimental **1.0.2-merged.3+26.3** release, combining both experimental modules. The suite JAR remains SHA-256 `f8f4b9af150f1383084c8682fd50462155b056f0ac94eff74be4f0dbdc45fdf3`.

## Client launcher installation

1. Import the `.mrpack` as a **new instance** in a launcher that supports the Modrinth modpack format, such as the Modrinth App or Prism Launcher.
2. Let the launcher install Minecraft **26.3**, Fabric Loader **0.19.5**, and the required manifest downloads. Use **Java 25 or newer**.
3. Enable the optional **Mod Menu 21.0.0** entry if the launcher presents optional-file choices. `/suite-settings` works without it.
4. Launch the instance. Do not add separate copies of the original feature mods or older suite shims.

The client selection excludes Polymer. It can join a matching suite server and use the native mod interfaces. Vanilla players joining the server do not install this client pack.

## Dedicated server installation

Use a new server directory, or stop and back up an existing instance before deliberately replacing its old mod files. The helper refuses to overwrite files with different contents and never changes world/configuration files.

1. Install Java **25+** and create a **Minecraft 26.3 / Fabric Loader 0.19.5** server using the [official Fabric installer](https://fabricmc.net/use/server/).
2. Extract the manual ZIP into a separate setup directory. Install Python **3.9+** if it is not available.
3. Run the helper from that setup directory, replacing the instance path with your Fabric server directory:

   ```sh
   python3 install.py --side server --instance /path/to/fabric-server --dry-run
   python3 install.py --side server --instance /path/to/fabric-server
   ```

   On Windows, use `py -3 install.py --side server --instance "C:\Minecraft\suite-server"`.

4. The helper verifies every download's size, SHA-1 and SHA-512, plus the additional recorded SHA-256. It installs the three included JARs and six server downloads, including **Polymer Bundled 0.18.2+26.3**. Mod Menu is excluded.
5. Review and accept Minecraft's EULA yourself, then start the Fabric server normally. The helper neither accepts the EULA nor starts the server.
6. Generate and host Polymer's server resource pack using the [Polymer hosting guide](https://polymer.pb4.eu/latest/user/resource-pack-hosting/). Players should accept that pack for Chalk's vanilla-visible items and marks. A general installation bundle cannot supply the resource pack for your particular server configuration or hosting address.

A server-aware `.mrpack` importer can use the same manifest directly. The helper also accepts `--pack /path/to/pack.mrpack` instead of an extracted kit. For a manual native-client installation, use `--side client --with-optional` against a separately prepared Minecraft/Fabric client instance; `--with-optional` includes Mod Menu. The helper installs mod files only, not a game launcher or server executable.

Vanilla clients still do not get custom backpack/pouch screens, MapStitch's world map/minimap, or client HUD features. See the [suite's compatibility overview](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/tree/merged#the-shim-vanilla-and-modded-players-together).

## Exact contents and permissions

Both archives embed only these three mod JARs:

| Included file | SHA-256 | License/source context |
| --- | --- | --- |
| Suite 1.0.2-merged.3+26.3 | `f8f4b9af150f1383084c8682fd50462155b056f0ac94eff74be4f0dbdc45fdf3` | Existing authorized suite distribution; all original notices preserved in its JAR and the kit. [Source](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite). |
| Defaulted 1.3.8+26.3.dropfix.1 | `e339d6f0eb471a4ac41185fb9dbe0cfaa78a290c6ceedf92a49ba9110f732c61` | MIT; exact local repair. [Patch source](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/tree/merged/components/defaulted-dropfix). |
| CodecUI 26.3-1.4.3 | `4d07c219bd85b58283a0317d6f43cca838adf9cd16be321316cd32f2fb1be898` | MIT declaration and retained notices; precise private build revision is unrecorded. [Source-family record](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/tree/merged/components/codecui-reference). |

Public binaries remain publisher downloads:

| Publisher version | Client | Dedicated server | Publisher license |
| --- | --- | --- | --- |
| [Fabric API 0.161.0+26.3](https://modrinth.com/mod/fabric-api/version/bNnaTiuM) | Required | Required | Apache-2.0 |
| [Fabric Language Kotlin 1.14.1+kotlin.2.4.20](https://modrinth.com/mod/fabric-language-kotlin/version/eRRZzGMc) | Required | Required | Apache-2.0 |
| [Fzzy Config 0.7.7+fix2+26.3](https://modrinth.com/mod/fzzy-config/version/thw1Z19c) | Required | Required | TDL-M 1.3 |
| [Cloth Config 26.3.159+fabric](https://modrinth.com/mod/cloth-config/version/fg2uyxOW) | Required | Required | LGPL-3.0-only |
| [Mixson 2.2.1](https://modrinth.com/mod/mixson/version/yWpBBcpq) | Required | Required | MIT |
| [Mod Menu 21.0.0](https://modrinth.com/mod/modmenu/version/kyy7dbrZ) | Optional | Excluded | MIT |
| [Polymer Bundled 0.18.2+26.3](https://modrinth.com/mod/polymer/version/REBDssAz) | Excluded | Required by this server pack | LGPL-3.0-only |

Mod Menu's exact JAR already includes Text Placeholder API; no separate copy is added. Other optional integrations, including ClientSort, are outside this pack.

Fzzy Config is **not inside either archive**, including nested suite JARs. Its [TDL-M 1.3 license, section 2.2](https://github.com/fzzyhmstrs/fconfig/blob/master/LICENSE#L25), permits modpacks to obtain it through a manifest from the official Modrinth/CurseForge source. Both installation methods read the same manifest and download the unchanged pinned file directly from `cdn.modrinth.com`; the distribution contains only its URL and hashes. The other public dependencies follow that same download-only approach.

## Format, lock, and verification

The pack follows the [official Modrinth format](https://support.modrinth.com/en/articles/8802351-modrinth-modpack-format-mrpack): a ZIP-based `.mrpack`, root `modrinth.index.json`, format version 1, required SHA-1/SHA-512 fields, and per-file client/server environments. `overrides/` supplies only the authorized local JARs and notices. “Unsupported” in an environment entry means that this pack does not install that file on that side; Polymer itself can be installed on compatible clients separately if wanted.

The [installation lock](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/blob/merged/docs/installation-pack.lock.json) records the official API version/project URLs, publisher licenses, all three hashes, download sizes, environment choices and embedded files. Metadata and downloads were checked against the [official version API](https://docs.modrinth.com/api/operations/getversion/) on **2026-10-04**. The lock is separate from the production JAR's build lock; creating these installation assets does not rebuild that JAR.

`tools/package-installation.py` reproduces both archives from the existing verified artifacts and lock. `tools/install-pack.py` is the helper included as `install.py`. **`INSTALLATION_PACK_SHA256SUMS.sha256`** covers both installation archives, `INSTALLATION_PACK.md`, and `INSTALLATION_PACK_VERIFICATION.json`. The main `SHA256SUMS.sha256` covers the suite JAR and library/source archives; installation packaging does not change those files. Verification results are also retained in the repository's installation-pack evidence. Existing [gameplay/runtime validation](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/blob/merged/docs/VALIDATION.md) applies to the unchanged JARs; pack validation separately checks download bytes, archive/import structure, environment filtering and helper installation. A launcher GUI import is only claimed if separately recorded.
