# Historical integration through suite 1.1.4

This document preserves the original Stackables port description and its dependency/test scope. It is not the current installation guide. Suite 1.1.5 uses the official developer release; see [current integration](README.md).

# Sensible Stackables integration

Stable suite **1.1** on `main` includes Pajic's [Sensible Stackables](https://github.com/pajicadvance/sensible-stackables) as its own module. The original mod ID, Fzzy configuration, Defaulted integration, Mixson recipes, menu fixes, uncapping option, throw cooldown, and native client count presentation are retained. This is an unofficial compatibility port for the suite’s supported Minecraft version. Machine-assisted suite updates are maintained independently while the original author continues their own work. See [installation requirements](../../docs/INSTALLATION_PACK.md) for the exact supported version.

| Component | Version | Target |
| --- | --- | --- |
| Preserved developer release | `3.0.3+26.2` (declares `3.0.3`) | Minecraft 26.2 |
| Version port | `3.0.3-port.1+26.3` | Minecraft 26.3 |
| Separate suite compatibility JAR | `1.0.0+26.3` | Exact version port above |
| Stable suite and shared coordinator | `1.1.0+26.3` | Minecraft 26.3 |
| Historical standalone experiment | `1.0.2-stackables.1+26.3` | Original independent branch |

The compatibility code is a separate nested JAR; it does not patch or redistribute a modified upstream binary. The port is compiled separately from the preserved developer source. No developer Minecraft 26.3 release was listed by the live Modrinth API at inspection on 2026-10-04. The suite's private official SSO input and exact Defaulted dropfix remain unchanged; the separate ChatGPT SSO-port track remains paused.

## Functional Polymer behavior

Install Polymer `0.18.2+26.3` on the server for clients without the suite. Polymer sends changed vanilla `MAX_STACK_SIZE` defaults explicitly, so ordinary defaults such as potions 3, saddles 16, and enchanted books 64 are available to those clients. No additional resource pack is needed for these vanilla items. Native clients retain the original mod's formatting, stack limits, and client features.

For uncapped limits above 99, vanilla's persistent component codec rejects larger `MAX_STACK_SIZE` values while hashing an inventory click. The fallback representation therefore advertises at most 99 **only in client prediction metadata**. The actual item count remains exact, the server retains the configured maximum, and completed container actions send a full authoritative state correction. No items are discarded, clamped, or split by this compatibility code. Vanilla clients show ordinary count text and can briefly predict smaller placement quantities before correction; they do not gain the native abbreviation/scaling UI. The wire count is an integer independent of this metadata limit. Large-count gameplay support is bounded by the recorded tests rather than a claim that every possible integer-sized inventory operation is safe.

`uncapStackSize` remains an upstream restart-required setting. Configure it before launching both the server and matching native clients when using uncapped counts. The suite settings hub includes the original Sensible Stackables settings, preserving their original configuration file and behavior.

Capability protocol v2 negotiates `sensible_stackables` independently using the exact module, compatibility JAR, Defaulted, Fzzy, and suite versions. A mismatch selects fallback for this module while leaving separately matched modules native. Version 1 clients safely lack the version 2 channels. The fallback correction skips intermediate drag packets and runs only after a complete action; native clients keep their original prediction behavior.

## Provenance and dependencies

The unmodified developer binary and published source archive are recorded in [upstream metadata](upstream/metadata/provenance.json), with publisher SHA-512 verification and local SHA-256 hashes. The published Java source matches GitHub revision `5152fe99424922d9bbdf209fc974ba5bebd7e11b` on `multicutter-v3` after newline normalization. Its repository contains a dormant prerelease target; that is not treated as a published 26.3 release.

Run `python3 components/sensible-stackables/fetch-upstream.py` from the root to restore the ignored publisher JARs with pinned SHA-256/SHA-512 verification.

`upstream/source/` preserves the published source archive and `upstream/repository/` preserves the source repository snapshot. `ported/src/` is an independent port source tree. Fabric source changes remove only build-time Entrypoint annotations and replace their generated declarations with explicit loader metadata; NeoForge platform sources are excluded from the Fabric build. Gameplay mixins and client feature code are retained. MIT notices and assets remain intact.

The 26.3 port uses Java 25, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Fzzy Config 0.7.7+fix2+26.3, Defaulted 1.3.8+26.3.dropfix.1, Mixson 2.2.1, and the suite's retained Kotlin/CodecUI dependency set. These are the existing exact suite inputs, not replacement libraries. See the per-track dependency lock JSON files and root `libs/SHA256SUMS.sha256`.

The developer baseline remains Minecraft 26.2 with its own original dependency set, never the 26.3 port's Defaulted/Fzzy stack. It is retained and independently checked. A 26.2 compatibility shim is not produced by this integration, so the 26.3 shim is not presented as an upstream-targeted artifact or as evidence of upstream vanilla interoperability.

## Build and verification

From the repository root, build with Java 25 and the documented Java 27 compiler override; `python3 tools/verify-bundle.py` validates all 16 nested modules. The standalone port and compatibility build outputs are in their separate `build/libs/` directories. Installation of the suite uses the root JAR plus its exact external dependencies; do not additionally install the two already nested Stackables JARs.

`python3 components/sensible-stackables/verify-upstream.py` independently compiles the 25 published Fabric source files against actual Minecraft 26.2 and recorded upstream dependencies. This is a Java compilation check, not a reproduction of the original multicutter release pipeline. `python3 qa-stackables/run.py --label <unique-label>` runs dedicated-server menu, conservation, codec, drop, and restart checks against the current suite artifact; add `--upstream-baseline` for the untouched developer binary. [Recorded mechanics evidence](../../qa-stackables/README.md) distinguishes these server fixtures from root QA's actual network-client gameplay tests.

## Historical branch records

The original integration branch was `feat/sensible-stackables`, followed by the combined `merged` experiments. Their exact versions, hashes and assertion totals remain in [Stackables evidence](../../qa-stackables/README.md) and [merged history](../../docs/MERGED_TESTING.md). Current stable-release checks are recorded separately in [validation](../../docs/VALIDATION.md) and [the stable guide](../../docs/RELEASE_1_1.md).
