# Suite validation and test limits

## Current evidence

The shared negotiation and both compatibility source variants passed direct Java compilation (83 source files) against cached Loom-patched Minecraft 26.3, Fabric API 0.161.0+26.3, Polymer 0.18.2+26.3, local compatibility inputs, and the explicitly requested Defaulted dropfix build. This preliminary direct compile is superseded by the complete clean Gradle build and exact-final runtime records below. This checks source compatibility only; it does not prove connection or gameplay behavior or classify the provenance of the local SSO input.

Defaulted must be `defaulted-1.3.8+26.3.dropfix.1-fabric.jar`, SHA-256 `e339d6f0eb471a4ac41185fb9dbe0cfaa78a290c6ceedf92a49ba9110f732c61`. The QA harness asserts this exact binary before launching a profile.

User confirmation resolved SSO provenance: this bundle uses a private official Pajic Minecraft 26.3 release. The separate ChatGPT SSO-port track remains paused and was not built or tested.

The final release JAR, SHA-256 `0312a5d7ce8cf1e597e21a9d111f8efaa47fa7c01c6b18effb48977cd9bbc957`, passed all four connection profiles with actual Chalk block and inventory packets. Sanitized results and exact profile inputs are in [final-connections.json](../qa/evidence/final-connections.json) and [final-connection-inputs.json](../qa/evidence/final-connection-inputs.json). The matching suite client reported all ten modules native with and without server Polymer, received a real red glow Chalk mark with its UP facing/orientation 3 and the real Chalk item, and preserved damage 13 and custom name. No virtual mark substitute was present. With server Polymer, the native client also completed the Chalk state-ID proof at 16 bits.

The Fabric API-only client received a safe AIR block, a PAPER Chalk item with the same damage/custom name, and exactly one nearby Polymer item display for the mark; its generated resource pack loaded. A genuine zero-mod vanilla client also connected, loaded the pack, and remained connected after the server sent the mark and inventory updates. Vanilla has no observation mod, so that profile does not independently inspect its decoded item/block state. Every profile omitted client Polymer, and both servers shut down cleanly. These results exercise negotiation and real native/fallback packet translation; they do not prove every original item/UI/gameplay feature.

Additional connection behavior passed on the prior functional candidate `ee06bdf23509e0a2f0c619b72126b8d875c42f2d77dba84fc6d89bb799791936`: native reconfiguration, reconnect in the same client process, and an intentionally incorrect SSO fingerprint selecting fallback for SSO while the other nine modules stayed native ([extended-connections.json](../qa/evidence/extended-connections.json), [inputs](../qa/evidence/extended-connection-inputs.json)). Native configurations confirmed the final Chalk state width of 16 bits. A matching suite client with Polymer installed also retained all ten native capabilities and the Chalk state proof ([client-polymer.json](../qa/evidence/client-polymer.json), [inputs](../qa/evidence/client-polymer-inputs.json)). Integrated singleplayer with and without Polymer confirmed local memory connections, all ten native modules, and client/world ticks ([integrated.json](../qa/evidence/integrated.json), [inputs](../qa/evidence/integrated-inputs.json)). Integrated registries are shared, so the post-Fabric Chalk state-ID proof is correctly skipped there. These additional runs used the prior candidate; all 107 compatibility/coordinator class files are byte-for-byte identical in the final release ([comparison](../qa/evidence/compatibility-bytecode-equivalence.json)). Root settings/resource changes occurred afterward and have separate validation. The standalone shim acceptance records remain historical evidence rather than proof of the changed binaries.

## Bounded connection smoke

`qa/light.py` contains four disposable-world profiles:

| Server | Client | Expected result |
| --- | --- | --- |
| Suite + external libraries + full Polymer | Same suite + external libraries, no client Polymer | Native modules confirmed; real Chalk mark/state and item-slot packets preserved |
| Same server | Fabric API only, no suite/original mods | Fallback; generated pack loads; safe Chalk item/block plus virtual mark observed |
| Same server | Official vanilla Main, zero Fabric/observer mods | Fallback login and pack load; stays connected after mark/slot updates |
| Suite + external libraries, no Polymer | Same suite + external libraries, no Polymer | Native modules confirmed; real Chalk mark/state and item-slot packets preserved |

The suite JAR is frozen into each run and hashed. Each server/client audit records launch arguments and exact mod hashes. The two QA fixtures observe completed login and per-module native decisions, create one disposable red glow Chalk mark and named damaged Chalk item, and inspect their client representations after several frames. They modify only the disposable test world and inventory; the production suite JAR stays unchanged.

Run against a frozen suite candidate:

```sh
python3 qa/light.py --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.0.0+26.3.jar --label <unique-label>
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
8. Integrated singleplayer login/native negotiation and in-world ticks were checked with and without Polymer. Exercise gameplay and LAN joins separately: the bounded integrated checks do not prove every original gameplay system or remote LAN-client combination.

A mismatch in the suite's version/registry fingerprints should select fallback for the affected module when Polymer is available. Untouched Tool Pouch/MapStitch clients retain their existing advertised-channel detection; untouched SSO/backpacks/Chalk clients do not receive invented compatibility proof. Full automatic native negotiation comes from the bundled client code, with no external shim or UUID override.

## Parent build and archive verification

The root Gradle build completed successfully with the pinned inputs and `--release 25`. `tools/verify-bundle.py` verified archive integrity, all 13 nested module identities, unchanged original input hashes, retained complete license notices, absence of duplicate root coordinator classes, and the exact Defaulted dropfix requirement. [Machine-readable record](build-verification.json). The built candidate hash is `0312a5d7ce8cf1e597e21a9d111f8efaa47fa7c01c6b18effb48977cd9bbc957`. Runtime testing was authorized on 2026-10-04; compilation is not runtime acceptance.

## Combined mechanics and persistence

The final release `0312a5d7ce8cf1e597e21a9d111f8efaa47fa7c01c6b18effb48977cd9bbc957` passed [actual-suite mechanics and persistence](../qa-mechanics/evidence/mechanics-and-persistence.json). With and without Polymer, all 752 positive Chalk conversion transitions and 32 calcite recipes passed, including durability/name/custom-component preservation and invalid ingredient rejection. Actual curse-removal menus passed both grindstone orders and smithing, with retained enchantments and exactly-once ingredient/byproduct behavior. All 19 Defaulted lifecycle/drop regressions passed in both profiles. Shared-map checks passed 606 assertions initially and 616 after restart with MapStitch loaded, including correct center metadata.

The only adjustment was to the original map QA fixture: a standalone vanilla expectation disallowed MapStitch's normal MAP_CENTER component. The combined test now permits that known component and verifies its coordinates; the production map implementation was unchanged. The raw failed fixture run is development evidence, not a production failure or a published pass.
