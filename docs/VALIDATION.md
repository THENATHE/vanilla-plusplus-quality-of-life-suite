# Suite validation and test limits

## Current evidence

The shared negotiation and both compatibility source variants passed direct Java compilation (83 source files) against cached Loom-patched Minecraft 26.3, Fabric API 0.161.0+26.3, Polymer 0.18.2+26.3, local compatibility inputs, and the explicitly requested Defaulted dropfix build. Evidence: `components/combined-compat/build/light-compile/result.json`. This checks source compatibility only; it does not prove connection or gameplay behavior or classify the provenance of the local SSO input.

Defaulted must be `defaulted-1.3.8+26.3.dropfix.1-fabric.jar`, SHA-256 `e339d6f0eb471a4ac41185fb9dbe0cfaa78a290c6ceedf92a49ba9110f732c61`. The QA harness asserts this exact binary before launching a profile.

Suite runtime profiles are currently paused pending resolution of the local SSO artifact's provenance and the standing SSO-port pause. No completed suite runtime result is claimed here. The separate standalone shim acceptance records are historical evidence; these changed combined binaries need their own checks.

## Bounded connection smoke

`qa/light.py` contains four disposable-world profiles:

| Server | Client | Expected result |
| --- | --- | --- |
| Suite + external libraries + full Polymer | Same suite + external libraries, no client Polymer | Native support confirmed for all suite modules; in-world ticks succeed |
| Same server | Fabric API only, no suite/original mods | All suite capabilities fallback; generated pack loads; in-world ticks succeed |
| Same server | Official vanilla Main, zero Fabric/observer mods | Fallback login succeeds and generated pack loads |
| Suite + external libraries, no Polymer | Same suite + external libraries, no Polymer | Ordinary modded server starts; native capabilities confirmed; in-world ticks succeed |

The suite JAR is frozen into each run and hashed. Each server/client audit records launch arguments and exact mod hashes. The two tiny QA fixtures observe completed login, record per-module native decisions, and wait for several client frames. They do not modify gameplay or the production suite.

Run only after the SSO provenance/pause condition is resolved:

```sh
python3 qa/light.py --jar build/libs/thenathe-mod-suite-1.0.0+26.3.jar --label <unique-label>
```

This requires the existing cached official libraries, assets, a graphical display (`DISPLAY=:1`), and local socket permission. It launches only disposable localhost worlds under `qa/runs/`. Check `qa/runs/<label>/result.json` and server/client `console.log` files. A success here is bounded connection evidence, not proof of every original mod system.

## Manual gameplay acceptance

Use a copy of a disposable world first. Keep the exact server/client module and external library versions recorded by the release.

1. With suite installed on both sides, exercise SSO smithing/anvil/enchanting/mending; ensure actual mod items and native screens appear. Exercise native Tiered Backpacks storage/upgrades/equip/unequip and Tool Pouch storage/dye/attached pouch/shulker opening, including switching the owning pouch while a child shulker is open.
2. Exercise MapStitch world-map screen and atlas creation/exploration, Tool Pouch atlas/elytra behavior, Shared Region Maps creation/zooming/locking/restart sharing. Map Atlases and Improved Maps integrations should be absent from the combined variant.
3. Exercise every relevant MiscTweaks and Simple Death Improvements feature you enable. Confirm settings changes affect their original configuration files and remain available through the suite settings hub.
4. Exercise Chalk white/all colors, recolor/glow combinations, durability/custom-name preservation, native mark rendering and glow. Reconnect and move between newly generated chunks to check native mark/state mapping.
5. With Polymer on the server, repeat supported vanilla gameplay using a Fabric API-only client and an actual vanilla client with the generated pack. Compare against the documented original shim feature limits. Inspect missing/mismatched client behavior and unsupported-action notices.
6. Exercise amethyst curse removal in the grindstone with each curse and multiple curses. Confirm the documented ingredient cost, retained normal enchantments, durability, custom data, and behavior when no curse is present.
7. Restart the server and confirm backpack/pouch contents, Chalk marks, shared map indexes, and configurations persist. Preserve existing world save and configuration identifiers.
8. Check singleplayer/LAN separately with and without Polymer. Integrated-server mixins are enabled where applicable, but dedicated-server connection tests do not prove this physical-client environment.

A mismatch in the suite's version/registry fingerprints should select fallback for the affected module when Polymer is available. Untouched Tool Pouch/MapStitch clients retain their existing advertised-channel detection; untouched SSO/backpacks/Chalk clients do not receive invented compatibility proof. Full automatic native negotiation comes from the bundled client code, with no external shim or UUID override.

## Parent build and archive verification

The root Gradle build completed successfully with the pinned inputs and `--release 25`. `tools/verify-bundle.py` verified archive integrity, all 13 nested module identities, unchanged original input hashes, retained complete license notices, absence of duplicate root coordinator classes, and the exact Defaulted dropfix requirement. [Machine-readable record](build-verification.json). The built candidate hash is `b2345736cdae8818aadfd2d98f63be19b8d0f2d9aefc315ee1a6767cadb74555`. Runtime testing and publication remain pending the SSO scope clarification; compilation is not runtime acceptance.
