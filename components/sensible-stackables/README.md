# Sensible Stackables integration

Stable suite **1.1.6** on `main` includes pajic's [Sensible Stackables](https://modrinth.com/mod/sensible-stackables) as its own unchanged official developer module. The original mod ID, Fzzy configuration, menu fixes, uncapping option, throw cooldown and native count presentation remain available. A separate suite compatibility module negotiates client support and projects safe metadata for players without the suite. [Installation requirements](../../docs/INSTALLATION_PACK.md) identify the exact platform and libraries.

| Component | Version | Target / role |
| --- | --- | --- |
| Current official developer release | `3.1.1+26.3` (declares `3.1.1`) | Minecraft 26.3; unchanged nested input |
| Separate suite compatibility JAR | `1.0.2+26.3` | Official release above |
| Suite / shared coordinator | `1.1.6+26.3` / `1.1.1+26.3` | Current root / shared coordinator |
| Historical developer baseline | `3.0.3+26.2` | Independently preserved Minecraft 26.2 input |
| Historical version port | `3.0.3-port.1+26.3` | Preserved source, dependencies and evidence; not nested now |
| Historical standalone experiment | `1.0.2-stackables.1+26.3` | Original independent branch |

Thanks to **pajic** for the original mod and upstream updates. The previous unofficial port and untouched baseline remain separate historical records in [HISTORICAL_PORT.md](HISTORICAL_PORT.md). No historical artifact or test result is relabeled as the current developer release. The separately maintained ChatGPT SSO port remains paused.

## Upstream changes

The official release no longer patches item defaults through Defaulted. It tracks configured limits itself and intercepts effective stack-size getters, synchronizing them with `sensible_stackables:stack_sizes`. In-game limit changes apply immediately; 3.1.1 fixes stale limits not clearing properly on configuration updates. Default cushions stack to 64. Original configuration and item/tag overrides remain supported.

Mixson is optional for Stackables and still supplies its dynamic all-block and minecart tags when present. It remains installed because other suite modules require it. Defaulted is no longer a suite runtime dependency. Existing guarded optional Defaulted compatibility retains the exact historical compile input; this does not reinstall the old library.

## Functional Polymer behavior

Stable suite **1.1.6+26.3** advances this separate compatibility module to **1.0.2+26.3**, preserving the exact official 3.1.1 input. The common template getter now mirrors upstream's live effective limits while honoring explicit additions and removals. This prevents valid stored stacks from becoming empty during nested-container validation. [Reproduction and verification](../../docs/PACKET_FIX_1_1_6.md) identify fresh final-artifact checks separately from historical 1.1.5 evidence.

Install Polymer `0.18.2+26.3` on the server for players without the suite. Because limits now come from upstream getters, the compatibility module explicitly projects their effective values into vanilla `MAX_STACK_SIZE` metadata. This preserves defaults such as potions 3, saddles 16, enchanted books 64 and cushions 64. No resource pack is needed for these vanilla items. Native clients retain original limits and formatting.

For configured limits above 99, fallback clients advertise at most 99 **only in prediction metadata**. Actual item counts remain exact and the server retains the configured maximum. Completed menu actions receive authoritative state correction; intermediate drag packets retain the existing handling. The compatibility code does not discard, clamp or split item quantities. Fallback clients can briefly predict smaller moves before correction and do not receive the native abbreviation/scaling UI. Coverage of large-count operations is bounded by [recorded validation](../../docs/VALIDATION.md).

`uncapStackSize` remains restart-required upstream. Configure it before launching the server and matching native clients. Other stack-size configuration updates remain live. Original server permissions, validation and settings files remain authoritative through the suite settings hub.

Fallback clients do not receive a preview component whose descendants contain a count above 99: vanilla cannot materialize such a stored-item template even with safe maximum metadata. This affects only outgoing container/bundle/projectile/remainder previews; real server contents and quantities remain intact, and top-level stacks still carry full counts when delivered through supported inventory menus. Safe previews of 99 or fewer remain available. Native Stackables clients keep all previews. The final Polymer projection also guards partial native carrier clients that do not support Stackables.

Capability protocol v2 negotiates Stackables using its module, compatibility, Fzzy and suite versions; Defaulted is no longer part of this current module's fingerprint. A mismatch selects this module's fallback independently. The new upstream stack-size payload is native-guarded; fallback clients receive item metadata rather than an unknown custom packet.

## Provenance and dependencies

The unchanged official [Modrinth release oRpBICTz](https://modrinth.com/mod/sensible-stackables/version/oRpBICTz) has SHA-256 `bf750cbe40394caffe3fd7c9bc2b57985d4110c5cae51dc726f948b55a25598a`. Its exact published source/binary records are under `developer-release/`, selected by `inputs.lock.json`. GitHub snapshot `3f2cd300564bd2e0c28adcf9b4192aa836a96320` is reference context, not an invented exact binary build revision. [Publisher comparison](../../docs/pajic-input-verification-2026-10-06.json) records the source/dependency audit.

Current platform: Minecraft 26.3, Java 25+, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3 and official Fzzy Config 0.7.7+fix3+26.3. The suite's Kotlin/CodecUI/Mixson inputs remain separately locked. Fzzy stays external and official-download-only. See [dependencies](../../docs/DEPENDENCIES.md) and root `locks/artifacts.json` for exact hashes and roles.

`upstream/` preserves the old 26.2 publisher source/binary records; `ported/` preserves the independent historical 26.3 port. Their own locks retain their original library requirements. Do not remove Defaulted from those old records or infer fresh compatibility from the current official release. The old component-local fetch/verify scripts and 26.2 baseline option refer to that historical track; root staging selects the new locked official input.

## Build, verification and updates

Build from the repository root using the [source/update guide](../../docs/UPDATING.md). `tools/verify-bundle.py` validates the 18 nested modules, original input identities and current dependency set. The compatibility output remains a separate module, while the original developer JAR is nested unchanged. Do not separately install either already nested Stackables JAR.

When updating upstream, inspect `StackSizeOverrides`, effective item/stack getters, configuration callbacks and original payload registration/send paths. Keep native packet guards, full counts, safe fallback metadata, server correction and configuration reset behavior aligned. Current evidence is in [validation](../../docs/VALIDATION.md). Historical menu, conservation, drop, codec and restart checks remain in [Stackables evidence](../../qa-stackables/README.md) and [merged history](../../docs/MERGED_TESTING.md); those results retain their original artifacts and targets.
