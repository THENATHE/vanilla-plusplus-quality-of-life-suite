# Retained 1.1.6 client acceptance

Frozen server suite SHA-256: `69c2b7f7d345282143e4c7d1da8a9b4ea97032c61ff804348d50613a60349b75`.

The retained `1.1.6+26.3` suite client joined successfully with the generated pack. Tool Pouch, Simple Smithing Overhaul and Sensible Stackables remained native through their advertised channels; other modules and the new items safely used fallback behavior. Client checks verified new carrier IDs and fixed custom item models. The server collected 17 diamonds from four blocks into the existing backpack, and observed command toggling off/on. The inventory screenshot shows the new magnet, basket and wrench sprites without missing models.

The observer explicitly enabled inventory pouch selection before seeding the test pouch. This configuration is fixture-only and was preserved in its source snapshot. Exact old-client artifact provenance is recorded in `LegacySuite/audit.json`.

Run: `qa-pickup/runs/final120-legacy01/`. The result does not assert native support for new hotkeys in the old client.
