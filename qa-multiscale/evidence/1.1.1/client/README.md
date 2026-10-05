# Native atlas metadata and renderer acceptance

Passed against suite `1.1.1+26.3`, SHA-256 `643a9ce8146c1ed7167f240b323044384027fb4990c722050b0ca66976bda86e`.

The disposable Polymer server first sent 15 real maps (Overworld, Nether and End at every scale) while the native client was in the Overworld. Both ordinary `getUpdatePacket` and forced atlas update paths were exercised. The client checked authoritative dimensions, scales, centers, locks and pixels, and verified repair of an existing incorrectly labeled map retained the same object, its pixels and its banner. Invalid IDs/scales were ignored. Before and after travel, the actual minimap marker preparation method was invoked with treasure maps from all three dimensions; only the current dimension’s marker was retained in all three checks.

An observer recorded the original MapStitch renderer during all 15 dimension/scale combinations, five Nether views after a real dimension transfer and one Overworld view after a second transfer. Every rendered tile matched the selected dimension and scale; each world-map cache held only that dimension’s five layers. This is 21 rendered views, not a replacement renderer or a fabricated cache.

Run: `python3 qa-multiscale/run-client.py --label <unique-label>`. The fixture uses spectator mode to keep movement/death outside this focused test. It does not independently exercise Remapped; the production repair keeps the existing map object and thus preserves optional integration state. Production map IDs and existing map records are not rewritten.
