# Final physical singleplayer acceptance

Frozen suite SHA-256: `69c2b7f7d345282143e4c7d1da8a9b4ea97032c61ff804348d50613a60349b75`.

Both physical singleplayer profiles passed: matching suite without Polymer, and matching suite with Polymer. Each local-memory connection retained all 13 native modules, received the three real item IDs and fixed models, collected 17 diamonds at four blocks into the existing backpack, and toggled the collector off/on through its client key mapping. Both in-game inventory screenshots were inspected and show the magnet, basket and wrench sprites with crisp pixels and no missing-model texture.

The fixture used a disposable copy of `qa/runs/final120-matrix01/server-native/world`; the source world was preserved. Exact observer source, audited mod hashes, client/server assertions and screenshots accompany `result.json`.

Run: `qa-pickup/runs/final120-integrated01/`. This covers the changed pickup path and local native negotiation, not exhaustive singleplayer gameplay.
