# Defaulted — local drop fix for Minecraft 26.3

Custom replacement build `1.3.8+26.3.dropfix.1`, based on the retained
`defaulted-1.3.8.release-26.3-fabric.jar`. This is a narrowly scoped modification,
not a new Minecraft version port or a Polymer shim. The original standalone fix was local; the suite now preserves its patch source
and offers the same exact MIT-licensed binary in its optional local-library archive. Original author: Alexandra; upstream project:
https://github.com/Alexandra-Myers/Defaulted (declared license: MIT).

## Problem and change

Creating a fresh item-frame or cushion drop writes its nullable custom name.
Defaulted previously recorded this pending write before its lazy prototype
refresh. The refresh could replay a removal marker into Minecraft's active patch
when the prototype had no corresponding component. Minecraft 26.3 then threw
`NullPointerException: defaultObj` in `PatchedDataComponentMap.set:94`.

Only `ReferentialDataComponentMap` is changed:

- Resolve the current prototype before tracking set, remove, or patch operations.
- Replay a saved patch through `clearPatch` and `applyPatch`, which canonicalize
  removals against the refreshed defaults and preserve copy-on-write ownership.
- Keep explicit overrides/removals in Defaulted's tracking map for future refreshes.

The initialization and raw restore callbacks remain unchanged. The other 186
archive entries are byte-for-byte identical, including bundled libraries, assets,
mixins, entrypoints, and access wideners. The only other modified entry is the
version field in `fabric.mod.json`. `build/build-report.json` verifies this.

## Build

Run `python3 Minecraft/defaulted-drop-fix/build.py` from the workspace root.
Requirements: Python 3, JDK 25 or newer, and ASM/ASM Tree 9.10.1 in the local Gradle
cache. The script compiles the small checked ASM patcher and applies it to the
hash-pinned input; it does not reconstruct or recompile unrelated mod classes.
The patcher rejects unexpected class or method layouts. Repeated builds produce
the same JAR SHA-256.

- Original input SHA-256: `0f6efc6423902b83da78ce85a119c69536517b1a9af52ed7046858859ff582a0`
- Original snapshot: `inputs/defaulted-1.3.8.release-26.3-fabric.jar`
- Exact patch source: `tools/PatchDefaulted.java`
- Output: `build/defaulted-1.3.8+26.3.dropfix.1-fabric.jar`
- Tests: `qa/` (disposable local server fixtures; never the live world)

## Target and preservation

This task targets only the exact installed 26.3 artifact above. Its upstream source
revision is not recorded in the retained artifact; provenance is therefore pinned
to its byte hash rather than an invented commit. The unchanged original remains
in `Builds/Minecraft/Defaulted/Main Plugin/1.3.8 - Fabric 26.3/` and is independently
tested as the failing control. The custom build has its own source, tests, and
release folder. No separate Defaulted Minecraft port is created by this task;
older Minecraft binaries are not replaced or claimed compatible. The paused SSO
port track is not used or changed.

## Installation requirements

Tested target: Minecraft 26.3, Java 25, Fabric Loader 0.19.5,
Fabric API 0.161.0+26.3, and external CodecUI 26.3-1.4.3.
Keep that external CodecUI JAR: the original Defaulted archive contains an older
bundled CodecUI, which is preserved unchanged and is superseded by the external
version already present in the user's server. No dependency is removed or replaced.

Stop the server, move the old Defaulted JAR outside `mods`, install this replacement,
and restart. Keep only one Defaulted JAR. Existing configuration and datapacks stay
in place. This is a server-side fix; no new client mod is required. It prevents
future failed drops but does not restore items already lost. To roll back, stop the
server and swap the original JAR back in; the original crash will remain.

## Suite source copy

This directory preserves the exact historical patcher and notes. The original workspace path above remains valid locally. In a standalone suite checkout, the source is `components/defaulted-dropfix/tools/PatchDefaulted.java`; place the original hash-pinned input under this directory’s `inputs/`, then run `JAVA_HOME=/path/to/jdk25 PATH=/path/to/jdk25/bin:$PATH python3 components/defaulted-dropfix/build.py` from the checkout root. The copied script resolves its paths relative to its own directory. This suite release does not rebuild or replace the already verified patch binary.
