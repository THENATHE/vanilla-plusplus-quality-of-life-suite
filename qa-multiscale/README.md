# Mixed-scale MapStitch verification

Stable **1.1.1** includes this separate module on `main`. Current release verification is recorded in [VALIDATION.md](../docs/VALIDATION.md); older records below remain historical.

Current final-JAR evidence: [770 dedicated atlas/extraction/restart assertions](evidence/1.1.1/server/result.json) and [native metadata/rendering acceptance](evidence/1.1.1/client/README.md). The separate client runner observes the original renderer across 21 dimension/scale views and three minimap marker checks; run `python3 qa-multiscale/run-client.py --label <unique>`.

Historical first-experiment artifact: **1.0.2-multiscale.1+26.3**, SHA-256 `0c590629dd4ba3f3a4cd21f0705430775799e8e096211419db7921910b185ddd`.

- [Dedicated mechanics and restart](evidence/final/mechanics.json): 118 assertions, Polymer and no-Polymer initial/restart profiles. Actual atlas insertion from both directions, cursor extraction/reinsertion, map ID conservation, all five selected layers, blank consumption, shared scale-specific regions, codec persistence, saved-world map data, pouch saveback, and duplicate inventory/pouch target selection passed.
- [Native and fallback connections](evidence/final/network.json): matching suite, Fabric API-only client, actual vanilla client, native suite without Polymer; all passed. Native clients operated original world-map button/scale keys and received all five selected scales while other books remained unchanged.
- [Client Polymer and duplicate-book equality](evidence/final/client-polymer.json): matching client/server Polymer profile passed. Asserted component-identical inventory/pouch copies before opening through the minimap source; explicit inventory use left the pouch unchanged, and keybind selection changed the pouch without changing the inventory books.

The dedicated fixture creates a disposable server player and sets its negotiated module state solely in the QA JAR. Real network cases perform the production handshake. QA fixtures are not shipped in installable mods. Raw disposable worlds and logs remain in ignored `runs/`; tracked evidence includes exact artifact/dependency hashes and launch commands.

For the current stable artifact, run `python3 qa-multiscale/run.py --label <unique> --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.1.1+26.3.jar`. The evidence above retains the original experimental artifact and its former controls; current behavior is documented in [ATLAS_CONTROLS.md](../docs/ATLAS_CONTROLS.md). For actual controls and connections, run `python3 qa/light.py --label <unique>`; add `--client-polymer-only` for the client-Polymer profile. Exact cached libraries and staging are explained in the suite maintenance guide.

Manual acceptance: try existing atlases with maps from other dimensions, explorer/locked/independent maps, Globetrotter paper, full atlases, and the installed accessory mods you use. Test long journeys and boundary crossings at each scale. Confirm your configured minimap position/HUD behavior and map ejection while the screen is open. None of these focused checks claims exhaustive coverage of every upstream integration.
