# Bannerpoint support — experimental branch

The `feat/bannerpoint` branch adds [Pajic’s Bannerpoint](https://modrinth.com/mod/bannerpoint) to Vanilla++ Quality of Life Suite as a separate module. It targets modern Minecraft versions using the exact platform listed below. Stable `main` **1.1.1** is unchanged; this branch is packaged separately for testing.

Bannerpoint creates **locator-bar waypoints** for banners. These are separate from MapStitch’s atlas-map banner decorations. The original style and its four image files can be delivered through Polymer’s resource pack: a vanilla client already understands Minecraft’s waypoint-style resources and locator-bar packets. Bannerpoint’s custom name renderer is Java client code, so a pack supplies icons but cannot supply that name overlay.

## What players receive

| Client and server conditions | Bannerpoint result |
| --- | --- |
| Matching suite client | Original banner icons and custom name rendering; Polymer is optional. |
| Fabric client with original Bannerpoint and its dependencies | Original banner icons and custom name rendering, detected through its original advertised channel; no separate client shim is required. |
| Vanilla or Fabric client without Bannerpoint, verified current Polymer main server pack successfully loaded | Locator-bar banner icons with the original style and banner color. No Bannerpoint custom name labels. |
| Client without Bannerpoint, pack pending, downloading, declined, failed or removed | Bannerpoint waypoints are hidden. Missing-texture squares are not intentionally substituted. |
| Client without Bannerpoint, server without Polymer | Bannerpoint waypoints are hidden; other suite systems still follow their usual client requirements. |

Ordinary player waypoints remain available under Minecraft’s normal rules. Original-only clients still need Polymer for other suite systems whose client mods they lack. All cases remain subject to Bannerpoint’s original transmission settings, range and Minecraft’s locator-bar rules. The compatibility layer changes who receives banner-waypoint connections; it does not delete banners or replace saved world data.

## Installation and use

Use the branch release **1.1.2-bannerpoint.1+26.3** on the server and participating suite clients. Do not additionally install the original Bannerpoint JAR: it is already nested in the suite. [Installation instructions](INSTALLATION_PACK.md) cover the client/server kit and all external libraries.

For vanilla clients, install the release-selected Polymer Bundled on the server. Generate and host the server pack through [Polymer’s hosting workflow](https://polymer.pb4.eu/latest/user/resource-pack-hosting/), and have players accept it. The suite contributes Bannerpoint’s original assets automatically. A player who declines or fails to load the pack receives no Bannerpoint waypoint icons. Pack acceptance alone is insufficient: the server waits for successful loading.

Open **Bannerpoint Gameplay Settings** in the suite hub to control named-banner transmission, map-linked transmission and maximum range. Open **Bannerpoint Client Settings** to control the native client’s name color, shadow, background and background opacity. With Bannerpoint on the client, hold the player-list key or sneak while looking toward the waypoint to reveal its name. Original server settings retain their relog notice.

## Exact versions and retained dependencies

| Component | Version / requirement |
| --- | --- |
| Experimental suite | `1.1.2-bannerpoint.1+26.3`, branch `feat/bannerpoint` |
| Unchanged official Bannerpoint | `1.1.2+26.3`; Fabric metadata declares `1.1.2` |
| Separate Bannerpoint compatibility component | `1.0.0+26.3` |
| Minecraft / runtime / loader | `26.3` / Java `25+` / Fabric Loader `0.19.5` |
| Fabric API / Fzzy Config | `0.161.0+26.3` / `0.7.7+fix2+26.3` |
| Polymer for resource-pack clients | Full Polymer Bundled `0.18.2+26.3` on the server |
| Defaulted retained for the suite | Exact `1.3.8+26.3.dropfix.1`; unchanged |

The existing Kotlin, Cloth, Mixson and CodecUI inputs remain as recorded in [dependencies](DEPENDENCIES.md). No library was replaced for this integration. Bannerpoint is an official developer release, not a new version port; there is no separate ChatGPT Bannerpoint port to test. The unrelated SSO version-port track remains paused.

## How compatibility works

The separate `components/bannerpoint-compat/` module observes the original `bannerpoint:banner_name` channel. A client advertising that channel can receive native banner waypoints and the original name payload without installing a separate shim. Original custom name packets are never sent to a client that lacks this channel.

For other players, the compatibility module tracks the main pack’s offered hash and Minecraft’s actual successful-load response. Only a verified pack containing the original artwork and successful loading enable banner-waypoint connections. Polymer 0.18.2 treats acceptance/downloading as pack availability, so its broad `hasMainPack` check is insufficient here. This state is per connection; pack decline, failure, removal or disconnect must revoke eligibility. Becoming eligible refreshes banner-waypoint connections, and becoming ineligible removes those banner connections while preserving ordinary player waypoints. Pack UUID/state handling and connection refresh belong to the compatibility module rather than a patched original JAR.

The pack includes these unchanged original files:

- `assets/bannerpoint/waypoint_style/banner.json`
- `assets/bannerpoint/textures/gui/sprites/hud/locator_bar_dot/banner_0.png`
- `assets/bannerpoint/textures/gui/sprites/hud/locator_bar_dot/banner_1.png`
- `assets/bannerpoint/textures/gui/sprites/hud/locator_bar_dot/banner_2.png`
- `assets/bannerpoint/textures/gui/sprites/hud/locator_bar_dot/banner_3.png`

The server uses ordinary Minecraft waypoint packets for the icons. Bannerpoint’s native custom payload supplies its colored banner-name label, so it is channel-gated separately. The pack-only path intentionally preserves the icons without pretending that a resource pack can install that client code.

## Source and future updates

[Component documentation](../components/bannerpoint/README.md) identifies the unchanged publisher binary and preserved source. Keep source/binary snapshots separate from `components/bannerpoint-compat/`, which owns the compatibility changes. The suite root owns grouping, settings titles and nested-module metadata. [UPDATING.md](UPDATING.md) maps these locations and the full update procedure.

When Bannerpoint updates, compare its waypoint connection factories, naming channel, style resources and saved-data lifecycle with the compatibility module. Preserve banner UUIDs and world records. Recheck Polymer’s main-pack identity and successful-load semantics when updating Polymer; do not replace a success check with “pack requested” or “pack accepted.”

## Verification and player testing

[VALIDATION.md](VALIDATION.md) records the exact tested build, completed checks and any remaining limits. The behaviors described here are the intended branch contract; historical stable-release tests alone do not verify the newly added module.

For a manual acceptance check:

1. Enable the original named-banner or map-linked transmission setting, place an eligible colored banner, and test its locator-bar icon with the matching suite client.
2. Join without Bannerpoint and accept the Polymer pack. Confirm the icon appears after loading finishes; custom banner-name labels remain absent on this client.
3. Reconnect and decline the pack. Confirm the banner icon is absent while ordinary player waypoints continue working.
4. Test a failed pack download and pack removal. Confirm an already displayed banner icon disappears when eligibility is lost.
5. Break an eligible banner and restart the server with another banner retained. Confirm removal and persistence still follow Bannerpoint’s original behavior.
6. Open both Bannerpoint settings categories through the suite hub and check the original values, files, permissions and relog notices.

## Stale or externally hosted packs

The offered main pack must include a SHA-1 matching the local generated Polymer ZIP, with all five Bannerpoint resources verified byte-for-byte against the original mod. A pre-Bannerpoint pack, empty/mismatched hash, missing file or wrong artwork leaves banner waypoints hidden even after SUCCESSFULLY_LOADED. Generate the updated pack and serve those same bytes with the matching hash; the standard Polymer host does this. Verified archive results are cached by file identity, size and precise modification time, and invalidated after generation. Native Bannerpoint clients do not need a server resource pack.
