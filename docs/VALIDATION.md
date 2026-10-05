# Validation

## Merged presentation update 1.0.2-merged.3+26.3

Final SHA-256: `f8f4b9af150f1383084c8682fd50462155b056f0ac94eff74be4f0dbdc45fdf3`.

The [fresh native client](../qa-merged/evidence/merged3/atlas/result.json) passed top-right widget alignment, exact original right-sidebar geometry, reordered atlas tooltip, all five minimap scales, independent world-map/generation controls, duplicate atlas targeting and settings hotkey. The [fresh settings run](../qa-merged/evidence/merged3/settings/observations.json) checked all 12 Configure... labels, original named category titles/fields and the original Chalk navigation destination.

This is a small presentation update. [Comparison with merged.2](../qa-merged/evidence/merged3/previous-build-comparison.json) confirms 15 unchanged nested modules; only layout/tooltip mixin classes change in the map addon. Broad mechanics, death/HUD matrices and vanilla/Fabric fallback scenarios below were not repeated for this build. Previous results remain evidence for their exact artifacts, not newly executed tests.


## Historical merged testing branch 1.0.2-merged.2+26.3

Combined build and runtime evidence is recorded in [MERGED_TESTING.md](MERGED_TESTING.md). Prior branch and mainline evidence below is historical and does not stand in for fresh combined tests.

The previous merged update has focused evidence under [merged2](../qa-merged/evidence/merged2/). The final JAR hash and exact evidence paths are recorded in [build verification](build-verification.json).

- Matching native and Fabric API-only clients connected to the Polymer server. Native clients exercised all five M scales, independent S/generation controls, all-off/re-enable, duplicate inventory/pouch targeting and the in-world settings hotkey. Both clients moved all 2,048 stone through actual inventory packets with no loss; fallback maximum metadata remained 99.
- Dedicated map mechanics/restarts passed **158 assertions**, with and without Polymer: all five layers, independent generation, blank conservation, one cartography packet per creation batch, stored options/unrelated data, and stale/replaced book rejection.
- The requested one-off death checks passed **25 cases per profile, 254 assertions total**, with and without Polymer. Every ordinary supported container, nested combination, open menu, equipment attachment, cursor and overflow case preserved only atlases while conserving ordinary drops. Real bundle insertion accepted atlases. Optional external Trinkets/Ohmega accessory slots were not installed in this baseline.
- Final graphical HUD checks cover eight scenes: atlas/ordinary maps share anchors and details never overlap the observed map bounds; original settings synchronize and save; signed atlas offsets persist.
- The settings fixture checks actual Mod Menu grouping/hidden Colorful entry/Chalk factory, 12 functional configurations with correct titles and original translations, hotkey registration and the no-world guard.

The earlier mechanics/network/death candidate is identified in its result files. The final presentation fixes change only root client Mod Menu/title-resource code; [byte comparison](../qa-merged/evidence/merged2/candidate-to-final-equivalence.json) verifies all 16 nested modules are byte-for-byte identical. The final settings and rendered HUD checks run against the packaged JAR itself. Historical vanilla, client-Polymer, full Stackables menu and permission tests below were not rerun for this update; no new pass is claimed for those unchanged scenarios. Tests cover the requested changes rather than every possible mod arrangement or arbitrary nesting/count.

# Suite validation and test limits

## Sensible Stackables branch 1.0.2-stackables.1

This independent branch adds a separately built Sensible Stackables `3.0.3-port.1+26.3` module and `1.0.0+26.3` compatibility module. The [component record](../components/sensible-stackables/README.md) identifies the untouched developer 26.2 baseline, published-source comparison, exact per-track dependencies, and agreed fallback maximum-metadata limit. Native features and actual stack counts remain intact; vanilla clients advertise a maximum of 99 for hashing/prediction when the server's limit is higher.

Dedicated-server mechanics and restart records are maintained in [qa-stackables](../qa-stackables/README.md). They use actual server menu implementations with a fixture player, not a connected client. Original developer-source compilation and untouched 26.2 baseline results are separate from ported-suite results. The root network harness records real-client receipt and click behavior separately. Historical 1.0.1 and 1.0.0 results below are not claims that those profiles ran against this branch.

## Historical Sensible Stackables branch network and settings acceptance

Final JAR SHA-256: `40c7ad4b454e8caa2a4518fcd246aa8fb54feeb65cc98ecc87dfd2a4af334acb`.

- [Four connection profiles](../qa-stackables/evidence/network/connections.json) passed: matching suite, Fabric API-only client, actual vanilla client, and matching suite without server Polymer. Server used uncapping at 2048; observed native and Fabric client counts remain exactly 2048, potions 3. The unmodified vanilla client joined and remained connected; this profile does not inspect its screen or automate its clicks.
- [Actual client inventory packets](../qa-stackables/evidence/network/inventory-packets.json) passed for matching native and Fabric API-only clients. Each picked up all 2048 stone and placed it in another slot, with zero remainder/loss. Native maximum stayed 2048; fallback maximum was 99. The ordinary-item forced-transform correction prevents the confirmed persistent-codec hash crash.
- [Client-side Polymer](../qa-stackables/evidence/network/client-polymer.json) also passed native 2048 receipt, potion metadata and real inventory moves.
- [Unified settings](../qa-stackables/evidence/settings/observations.json) opened all 13 original suite/client configurations, including `sensible_stackables.config` and `sensible_stackables.client_config`, with nonempty native widgets and unchanged IDs. This was a standalone settings-screen test, not a new multiplayer permissions regression.
- [Dedicated mechanics](../qa-stackables/evidence/final-context-02/result.json) passed 131 assertions across six default/uncapped Polymer/native initial/restart cases. [Untouched developer 26.2](../qa-stackables/evidence/upstream-26.2-01/result.json) independently passed 82 assertions across four cases; no 26.2 vanilla shim or connected vanilla-client result is claimed.

Remaining manual checks: test your chosen item/tag overrides, throw cooldown, shift/drag behavior on actual vanilla clients, stacked-item workflows in other installed mod menus, native count formatting, and larger counts than 2048. The VarInt boundary was checked separately, but that is not exhaustive gameplay evidence for arbitrarily large counts. Back up existing worlds/configurations before installing an experimental branch.

## Historical 1.0.1 network evidence

The release `vanilla-plusplus-quality-of-life-suite-1.0.1+26.3.jar`, SHA-256 `8e4783b66633a7f6f5cfa38e285b530638d4d82326d5eca15376f7b698d0791a`, passed 12 bounded connection cases after the nested compatibility versions were corrected. Every result below comes from that exact artifact; earlier candidate runs remain local development records.

- [Four base profiles](../qa/evidence/1.0.1/connections.json) ([exact inputs](../qa/evidence/1.0.1/connections-inputs.json)): native suite with and without server Polymer, Fabric API-only fallback, and zero-mod vanilla. Instrumented clients asserted broken-anvil block and item identity, EAST facing, custom name, and the existing Chalk state/item/damage/name/display checks. Native SSO receives the real broken anvil; Fabric-only receives the safe damaged-anvil representation. Vanilla connected, loaded the generated pack, and stayed connected, but has no observation mod to inspect its decoded block/item state.
- [Five extended cases](../qa/evidence/1.0.1/extended-connections.json) ([exact inputs](../qa/evidence/1.0.1/extended-connections-inputs.json)): native connection, reconfiguration, same-process reconnect, SSO-only fingerprint mismatch, and Chalk-only mismatch. Each asserted client block/state/item results. A mismatched module fell back while the other retained its native blocks, with a shared confirmed 16-bit state map.
- [Client Polymer](../qa/evidence/1.0.1/client-polymer.json) ([exact inputs](../qa/evidence/1.0.1/client-polymer-inputs.json)): all ten modules remained native, with correct broken-anvil and Chalk client observations.
- [Integrated singleplayer with and without Polymer](../qa/evidence/1.0.1/integrated.json) ([exact inputs](../qa/evidence/1.0.1/integrated-inputs.json)): local memory connections retained native broken anvils and Chalk. Shared integrated registries correctly skip the remote state-ID proof.

These checks verify negotiation, registry/state translation, reconnect behavior, and the native broken-anvil regression. They do not replace exhaustive gameplay, persistence, or LAN-client acceptance. The separate ChatGPT SSO-port track remained paused and was not built or tested.

## Historical 1.0.1 settings and build evidence

[Settings lifecycle results](../qa-settings/evidence/lifecycle-1.0.1.json) passed all 11 native configuration screens, representative Chalk/Tool Pouch persistence, operator and guest permissions, pending proposal preservation across reopening, and routing recovery after an actual Fzzy client update invalidated its cache. Real different-server reconnects passed in both permission directions and cleared previous proposals. Forwarded proposals were injected through the original Fzzy receiver; this was not a two-player forwarding network test.

The full settings matrix used candidate `47e656b8856b9c36164fe47189b6e798f1139ce8af1e2fce7174d0ef5e58cd3d`. [All 658 Java classes match the final artifact](../qa-settings/evidence/lifecycle-1.0.1-class-equivalence.json); only stale nested version metadata was corrected afterward. The final `8e4783...` artifact also passed the focused graphical lifecycle smoke recorded in the same results file.

A [clean exported-source build](clean-build-verification.json) passed with all 16 exact inputs staged at the root and no ignored component-local libraries or project caches. It reused the existing Gradle dependency cache, so it does not establish a build from an empty network cache. All 1,247 recursive files, including 658 classes and nested metadata, match the final runtime artifact; ZIP container bytes differ. The archive verifier checks declared nested versions, original input hashes, licensing, and the exact Defaulted dropfix requirement. It also rejected the stale-metadata candidate. Public ClientSort staging passed SHA-256 validation and rejected a corrupted download.

The unchanged gameplay and HUD components retain the historical 1.0.0 evidence below; those broad mechanics/HUD suites were not rerun for 1.0.1.

## Historical 1.0.0 evidence

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
| Suite + external libraries + full Polymer | Same suite + external libraries, no client Polymer | Native modules confirmed; real Chalk and broken-anvil block/state and item-slot packets preserved |
| Same server | Fabric API only, no suite/original mods | Fallback; generated pack loads; safe Chalk item/block plus virtual mark and damaged-anvil fallback observed |
| Same server | Official vanilla Main, zero Fabric/observer mods | Fallback login and pack load; stays connected after mark/slot updates |
| Suite + external libraries, no Polymer | Same suite + external libraries, no Polymer | Native modules confirmed; real Chalk and broken-anvil block/state and item-slot packets preserved |

The suite JAR is frozen into each run and hashed. Each server/client audit records launch arguments and exact mod hashes. The two QA fixtures observe completed login and per-module native decisions, create a disposable red glow Chalk mark, named damaged Chalk item, and named broken anvil block/item, and inspect their client representations after several frames. They modify only the disposable test world and inventory; the production suite JAR stays unchanged.

Run against a frozen suite candidate:

```sh
python3 qa/light.py --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.0.1+26.3.jar --label <unique-label>
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

## Historical 1.0.0 parent build and archive verification

The root Gradle build completed successfully with the pinned inputs and `--release 25`. `tools/verify-bundle.py` verified archive integrity, all 13 nested module identities, unchanged original input hashes, retained complete license notices, absence of duplicate root coordinator classes, and the exact Defaulted dropfix requirement. The built candidate hash is `0312a5d7ce8cf1e597e21a9d111f8efaa47fa7c01c6b18effb48977cd9bbc957`. Runtime testing was authorized on 2026-10-04; compilation is not runtime acceptance.

## Historical 1.0.0 combined mechanics and persistence

The final release `0312a5d7ce8cf1e597e21a9d111f8efaa47fa7c01c6b18effb48977cd9bbc957` passed [actual-suite mechanics and persistence](../qa-mechanics/evidence/mechanics-and-persistence.json). With and without Polymer, all 752 positive Chalk conversion transitions and 32 calcite recipes passed, including durability/name/custom-component preservation and invalid ingredient rejection. Actual curse-removal menus passed both grindstone orders and smithing, with retained enchantments and exactly-once ingredient/byproduct behavior. All 19 Defaulted lifecycle/drop regressions passed in both profiles. Shared-map checks passed 606 assertions initially and 616 after restart with MapStitch loaded, including correct center metadata.

The only adjustment was to the original map QA fixture: a standalone vanilla expectation disallowed MapStitch's normal MAP_CENTER component. The combined test now permits that known component and verifies its coordinates; the production map implementation was unchanged. The raw failed fixture run is development evidence, not a production failure or a published pass.
