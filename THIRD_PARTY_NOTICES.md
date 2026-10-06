# Credits and licensing

Vanilla++ Quality of Life Suite is an unofficial combined distribution, maintained by THENATHE. It is a new repository, not a GitHub fork or an official Pajic release.

New suite, negotiation, compatibility and addon code uses the MIT license in LICENSE. That license does not override the licenses of included modules, dependencies, textures or original source snapshots. The complete retained copyright and permission notices are in licenses/ and META-INF/licenses/ in the production JAR.

| Included component | Original author / project | License |
| --- | --- | --- |
| Simple Smithing Overhaul, MapStitch, Tool Pouch, Tiered Backpacks, MiscTweaks, Simple Death Improvements, Sensible Stackables | Pajic, https://github.com/pajicadvance | MIT; each original copyright notice retained |
| Chalk | mortuusars; Fabric port by DaFuqs and contributors | MIT; original and Fabric-port notices retained |
| Chalk Colorful Addon | Original uploaded addon authors listed in its preserved metadata | MIT; its original license and icon are unchanged |
| Shared Region Maps | THENATHE | All rights reserved, retained; redistributed here by its owner |
| Historical compile input: Defaulted dropfix | Alexandra / Defaulted; Pajic fork reference; THENATHE dropfix | MIT; original notice retained |
| Installation ZIP: retained CodecUI build | MehVahdJukaar / CodecUI; Pajic fork API reference | MIT declaration and full terms retained; no invented source revision |
| Amethyst Curse Cleanser, atlas/Elytra addon, compatibility components | THENATHE | MIT |

Original nested dependency JARs, including MixinConstraints, are kept intact with their own metadata and licenses. This is not a claim that every library used by the suite is MIT. Shared runtime libraries are installed separately and retain their own terms: Fabric API and Fabric Language Kotlin, Fzzy Config, Cloth Config, CodecUI, Mixson, and optional Polymer/Mod Menu. Defaulted is retained only as an exact historical compile input for guarded optional support, with its source and notices preserved; it is no longer installed by the suite. Library versions and hashes are pinned separately in locks/; no library API has been replaced with local code.

[ClientSort](https://modrinth.com/mod/clientsort) is an optional runtime integration and pinned compile-only dependency; its binary is excluded from the suite and installation ZIP. Full dependency and integration download links are in [docs/DEPENDENCIES.md](docs/DEPENDENCIES.md).

## Mixed-scale atlas addon

MapStitch Mixed Scales is a separately authored THENATHE suite addon under the root MIT license. It targets the unchanged developer MapStitch 1.1.6+26.3 input and retains the upstream notices above. Its implementation and update boundaries are recorded in docs/MIXED_SCALES.md.

## Installation ZIP dependencies

The installation ZIP includes unchanged Fabric API and Fabric Language Kotlin (Apache-2.0), Mixson and optional Mod Menu (MIT), and Cloth Config and server Polymer (LGPL-3.0-only). These remain external libraries rather than nested suite modules. Publisher and nested-library notices are retained in the ZIP. Cloth Config and Polymer corresponding upstream source archives, GPL/LGPL text and immutable source/hash records accompany the binaries; see [distribution records](docs/dependency-distribution.lock.json) and [installation instructions](docs/INSTALLATION_PACK.md).

Fzzy Config remains an official manifest download, never a redistributed binary or copied source module. Its TDL-M modpack exception permits official publisher downloads. All exact dependency versions, official URLs, hashes, side selections and permissions are recorded in [the installation lock](docs/installation-pack.lock.json).
