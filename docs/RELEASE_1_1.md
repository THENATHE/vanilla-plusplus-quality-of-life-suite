# Vanilla++ Quality of Life Suite 1.1

This mainline release brings the tested atlas and stack-size additions into the suite for modern Minecraft versions. The modules remain separate so future upstream changes can be imported independently.

## Included features

- Sensible Stackables: configurable stack limits, native count presentation and safe Polymer fallback with full item counts.
- MapStitch atlases store all vanilla map scales, with independent world-map, minimap and generation controls. The top-right control group and original sidebar spacing are retained.
- Atlas tooltips show storage, enabled generation scales, minimap scale, then the original counts and ejection preference. Map generation plays one cartography chime per batch.
- Atlas and ordinary Tool Pouch maps share corner/offset placement and keep pouch details clear of the minimap.
- The original keep-atlas-on-death option rescues atlases from nested pouches, backpacks, shulkers and bundles. Other contents and the containing item drop normally unless a separate retention option is enabled.
- Original feature mods remain individually visible in Mod Menu; suite additions are grouped beneath Vanilla++. Real settings categories retain their names, with Configure... action buttons and a rebindable Open Suite Settings hotkey.
- Chalk and Colorful Chalk have one combined feature description, with both original links and credits. THENATHE's own additions are described directly.

When needed, machine-assisted porting helps keep features available on newer Minecraft releases while original authors update their projects independently. These ports remain unofficial; exact support and provenance are recorded below and in the technical guides.

## Installation

| Requirement | Exact release target |
| --- | --- |
| Suite | 1.1.0+26.3 |
| Minecraft | 26.3 |
| Fabric Loader | 0.19.5 |
| Java | 25 or newer |
| Defaulted | 1.3.8+26.3.dropfix.1; use the installation ZIP |
| CodecUI | 26.3-1.4.3 |
| Polymer, for fallback clients | 0.18.2+26.3, installed on the server |

Download [the mainline release](https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/releases/tag/v1.1.0%2B26.3). Replace the old suite JAR on both server and modded clients. Preserve world and configuration files. Do not install separate copies of the already bundled feature mods/addons/shims. The feature JAR is `vanilla-plusplus-quality-of-life-suite-1.1.0+26.3.jar`. The four downloads are README.md, the feature JAR, docs.zip and one installation ZIP. The installation ZIP includes exact permitted dependency binaries and an installer that obtains Fzzy Config from its official publisher; internet is required. See [installation-pack instructions](INSTALLATION_PACK.md) and [all dependencies/compatible downloads](DEPENDENCIES.md).

## Verification and technical references

[Validation](VALIDATION.md) and [build verification](build-verification.json) record the exact final hash, fresh promotion checks and historical acceptance. All 605 root/nested Java classes match the tested merged.3 build; only declared suite/coordinator/map-addon versions and documentation changed during promotion. Source/module locations and future update procedure are in [UPDATING.md](UPDATING.md), with separate [provenance](PROVENANCE.md). Historical experimental releases/evidence remain preserved, and the separate SSO port stays paused.

## Your quick check

1. Open a multi-scale atlas. Try S, M1–M16 and several generation toggles, then travel with blanks in the book. Check tooltips, creation sound and saved options after restart.
2. Switch between an atlas and an ordinary map in the pouch. Change the corner through either original client config and confirm the details do not overlap the minimap.
3. Open suite settings and use Configure... for a named category. Bind Open Suite Settings in Controls if desired; it begins unbound.
4. Try your configured stack limits and item overrides, including inventory moves on native and fallback clients. Uncapping requires restarting both matching native sides. Counts above 99 keep their full quantity while fallback maximum/prediction metadata is capped at 99.
5. With the original keep-atlas option enabled and independent item-retention options off, die with an atlas and spare item in a supported nested container. Only the atlas should return after respawn.

Actual external accessory/sorting integrations, extremely large counts and launcher GUI importing retain the limits stated in the validation record. The prior one-off death matrix was not added to the ordinary release test matrix.
