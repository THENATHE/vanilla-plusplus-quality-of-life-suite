# 1.1.5 dedicated atlas sanity check

On 2026-10-06, the unchanged published `1.1.5+26.3` suite passed **5,314 assertions** across four dedicated-server runs: initial and saved-world restart, each with and without Polymer. All four processes exited normally with status 0. Defaulted was absent from both runtime profiles. The loaded upstream inputs included SSO 2.10.0, MapStitch 1.1.7, Sensible Stackables 3.1.1, and Fzzy Config 0.7.7+fix3+26.3.

Suite SHA-256: `67be52fd07c2d3dcfda4045dff5463e45bc0d91d37d3f759f4ebb3be043cd8f5`.

The existing fixtures checked actual atlas insertion and cursor extraction, map-ID conservation, generation at all five scales, shared-region identity, generation masks and minimap selection, creation sound packets, codec/world restart persistence, pouch saveback and target identity, and scale/dimension extraction with inventory-first overflow handling. They also exercised banner edits across enabled scales, unrelated dimensions and edge preflight; authoritative map-center repair and full artwork resend; exact-ID duplicate cleanup and blank-map refunds; and command/cartography copies of 905 filled maps at all scales and dimensions with original retention, book costs, capacity independence and vanilla cartography behavior.

[result.json](result.json) records assertions, artifact/fixture/source hashes, shutdown outcomes and log hashes. Each profile audit records installed mod hashes and its launch command. Console files are sanitized only to replace local filesystem prefixes; raw hashes and disposable worlds remain in the ignored runner directory.

```sh
python3 qa-multiscale/run.py --label <unique-label> --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.1.5+26.3.jar
```

These are real packaged server methods with in-process test players and captured outgoing packets. Native capability state is explicitly supplied by the fixture. This record does not claim real network negotiation, an interactive remote inventory, client rendering, exhaustive travel, or third-party accessory slots; separate checks cover those where recorded.
