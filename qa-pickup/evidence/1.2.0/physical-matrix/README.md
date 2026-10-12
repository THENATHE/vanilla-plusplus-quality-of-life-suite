# Final physical connection and pickup matrix

Frozen suite SHA-256: `69c2b7f7d345282143e4c7d1da8a9b4ea97032c61ff804348d50613a60349b75`.

All four profiles passed on the same binary:

| Profile | Server | Client | Pickup and toggle |
| --- | --- | --- | --- |
| SuiteNative | Suite + Polymer | Matching suite, no client Polymer | 17 diamonds at four blocks collected into the backpack; client key binding off/on |
| FabricOnly | Suite + Polymer | Fabric API and QA observer, no suite | 17 diamonds collected; command off/on; fixed models received |
| SuiteVanilla | Suite + Polymer | Unmodified Minecraft client | 17 diamonds collected; command off/on; server observed stable connection |
| SuiteNativeNP | Suite without Polymer | Matching suite without Polymer | 17 diamonds collected; client key binding off/on |

The matching client key mapping was assigned F8 in the disposable fixture, then activated through Minecraft's key mapping event queue. The server observed disabled and re-enabled states through the real payload handler. Client checks verified all three new item registry IDs and fixed item-model IDs; fallback Fabric checks verified their vanilla carrier IDs and custom model IDs. Unmodified vanilla clients have no QA observer, so their item-model appearance was not independently asserted by injected client code.

Fabric fallback and no-Polymer inventory screenshots were inspected. The first dedicated native profile reported its scheduled screenshot before asynchronous disk writing completed; that PNG was not retained. Its packet and gameplay assertions still passed. Separate singleplayer native screenshots verify the same native item models. Magnet, basket and wrench sprites are visible with crisp pixels and no missing-model texture. Matching and Fabric clients also passed existing inventory packet movements with 2048-count common stacks. Exact observer source, audits and screenshots accompany `result.json`.

Run: `qa-pickup/runs/final120-physical02/`. This is bounded acceptance for the changed pickup and packet/rendering paths, not an exhaustive gameplay claim.
