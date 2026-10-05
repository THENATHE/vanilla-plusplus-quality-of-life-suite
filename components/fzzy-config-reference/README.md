# Fzzy Config public dependency reference

Checked **2026-10-04**. This directory contains release metadata and read-only API/hash comparison, not a bundled library component.

## Existing dependency is already official

The existing `libs/fzzy_config-0.7.7+fix2+26.3.jar` is **byte-identical** to the publisher's public Fabric 26.3 release:

- Version: **0.7.7+fix2+26.3**, published 2026-09-24.
- Modrinth version ID: **thw1Z19c**.
- [Public installation page](https://modrinth.com/mod/fzzy-config/version/thw1Z19c).
- [Official download](https://cdn.modrinth.com/data/hYykXjDp/versions/thw1Z19c/fzzy_config-0.7.7%2Bfix2%2B26.3.jar).
- SHA-256: `138e377563a8ca536a9af00d8eeefb7fe160903392df8e4ca7cd4b5da6f0bb5f`.

Modrinth's SHA-1 and SHA-512 file hashes also match the downloaded reference. Because every byte is identical, its GUI internals, configuration codecs, networking, validation, registry accessors, reflection fields, and public API are identical to the runtime already tested by the suite. No replacement, API shim, compatible-version change, private dependency stack, or additional migration test is needed. Keep the existing exact version and supply its public download link.

## Latest fix3 reference

[Official fix3](https://modrinth.com/mod/fzzy-config/version/YkOumqzV) was published 2026-10-03. SHA-256: `3d98c61215ae5eb2fe08db5cac6b0da08dc1b6f0a9ac082a3a9ead8eed736b2a`. Its publisher changelog describes missed narration implementation fixes. A ZIP-entry comparison found only three changed class files: `ConfigScreen`, `ConfigScreenNarrator`, and its `MessageBuilder`. Configuration registries, manager/update-manager, config API, and codecs are byte-identical. The screen's private/public descriptor listing does change; see `fix3-config-screen-abi.diff`. This optional newer release was **not** installed or runtime tested. Do not infer fully validated GUI compatibility from a descriptor scan.

Exact factual comparison: `comparison.json`. Raw official API metadata: `upstream/metadata/`.

## Distribution

Fzzy is an external dependency. The publisher's [TDL-M license](https://github.com/fzzyhmstrs/fconfig/blob/master/LICENSE) and [README](https://github.com/fzzyhmstrs/fconfig) prohibit bundling/shading/JAR-in-JAR and distributing library copies without separate permission. Reference binaries in `upstream/artifacts/` are local read-only downloads and excluded from Git/source/release packages. Link users to the publisher, or use a permitted Modrinth/CurseForge download manifest. No Fzzy code was copied into the suite.

## CodecUI secondary availability check

`codecui-availability.json` records a separate quick check requested during this audit. Modrinth search found no CodecUI project, the publisher GitHub repository lists no releases, and its official somethingcatchy Maven metadata lists Fabric versions through **26.2-1.4.7**, with no 26.3 entry. The exact existing CodecUI `26.3-1.4.3` binary (`4d07c219bd85b58283a0317d6f43cca838adf9cd16be321316cd32f2fb1be898`) has a `HolderSetCodecExtension` API consistent with the Pajic 26.3 fork reference captured in `components/codecui-reference/`; its private build revision is not recorded. The separate historical ChatGPT port uses `HolderSetCodecAccess` and is not claimed as this binary’s source. This audit changes no CodecUI or Defaulted source, binary, classpath, or pin. CodecUI's MIT license is distinct from Fzzy's distribution restrictions.
