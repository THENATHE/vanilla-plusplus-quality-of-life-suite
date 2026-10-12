# Superseded physical QA candidate

Artifact SHA-256: `09f5e21c46b1ddfb835ad8f9f49f6b4a3545b6c21cb63490e142e672a2e6cc49`.

Native client on Polymer server passed actual 17-diamond magnet-to-backpack collection, key binding off/on, received item IDs/models, inventory packet actions, and visible inventory screenshot.

Fabric-only fallback client received the new item IDs/models but timed out before collection: its server report stayed at stage 0 with zero diamonds. The original Tool Pouch compatibility guard rejected passive pouch lookups for unsupported clients. Final verification must use the subsequent corrected artifact; this candidate is not release acceptance.

Full original run, audited binaries, logs and source: `qa-pickup/runs/final120-physical01/`.
