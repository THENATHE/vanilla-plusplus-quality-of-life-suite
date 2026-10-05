# Bannerpoint testing release

Experimental suite **1.1.2-bannerpoint.1+26.3** is on `feat/bannerpoint`; stable main remains 1.1.1+26.3. It adds the official Bannerpoint mod and a separate compatibility module without changing the existing atlas or other gameplay modules.

Bannerpoint adds banner markers to the **locator bar**, using Minecraft's standard waypoint styles. Polymer's generated resource pack now includes the original banner style and four sprites. Clients with Bannerpoint retain the native display and custom name labels. Other clients receive the original colored icons only after the Polymer pack successfully loads; while pending, declined, failed or removed, Bannerpoint waypoints are hidden. Normal player locator-bar waypoints remain intact. A resource pack cannot add Bannerpoint's custom name-rendering code.

## Installation

| Requirement | Exact version |
| --- | --- |
| Minecraft | 26.3 |
| Fabric Loader | 0.19.5 |
| Java | 25+ |
| Original Bannerpoint, already nested | 1.1.2+26.3 (internal 1.1.2) |
| Bannerpoint compatibility, already nested | 1.0.0+26.3 |
| Optional server Polymer | 0.18.2+26.3 |
| Required Defaulted | 1.3.8+26.3.dropfix.1 |

Download the [experimental release](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/releases/tag/v1.1.2-bannerpoint.1%2B26.3). Use the [installation guide](INSTALLATION_PACK.md) for exact external dependencies and the included side-aware installer. Replace the old suite JAR and remove a separate Bannerpoint JAR from the same instance. Mainline worlds/configs should be preserved before trying the branch. Without Polymer, native Bannerpoint clients work; clients lacking its artwork receive no banner waypoints.

The four standard uploaded assets are README.md, the suite JAR, docs.zip and the installation ZIP. The exact Defaulted dropfix and CodecUI are retained. Permitted external dependencies are included; Fzzy Config downloads directly from its official publisher URL during installation, which needs internet. Generate/host the updated Polymer pack and ensure the server offers those same bytes with the matching SHA-1. Stale packs without Bannerpoint artwork, missing hashes and mismatched hashes leave banner waypoints hidden.

## Testing

[Validation](VALIDATION.md) records final hashes, completed checks and historical limits. [Bannerpoint guide](BANNERPOINT.md) gives player testing steps and settings: named/map-linked banner tracking, transmission range, colored custom names, text background/shadow/opacity. Both original configuration pages are available in the suite hub. The integration preserves original banner UUIDs, saved data, ordinary map banner markers, and existing locator-bar player waypoints.
