# Atlas server mechanics verification

Suite **1.1.4+26.3**, SHA-256 `2c1273947279693e5ec3c78b2c39793c3b5fdbc320216476e04d16f79b2a1754`. Final suite artifact verification. Run `atlas-copy-release-final3` passed four disposable dedicated-server phases, initial and saved-world restart both with Polymer and without Polymer: **5,314 assertions total**. Input and fixture/source hashes are recorded beside this file. Launch paths use `$WORKSPACE` and `$USER_DIR`; no live worlds, player records, raw logs or QA binaries are published. An earlier same-artifact run completed its assertions but exceeded the fixture timeout during vanilla region saving; stack diagnosis and the clean four-phase retry are summarized in [prior-save-timeout.json](prior-save-timeout.json).

## Repair item resynchronization

The exact stable 1.1.3 artifact reproduces the unchanged-item resend failure in [baseline-item-resend-reproduction.json](baseline-item-resend-reproduction.json). The fixture attaches the actual vanilla inventory synchronizer, establishes matching remote contents, then invokes real `/atlas fix` execution again with zero center changes. Held, offhand and active Tool Pouch cases each resend an actual inventory item packet containing the atlas or its pouch, plus every unique full color record. `/atlas fix check` and `/atlas repair check` remain read-only and send no inventory snapshot or map pixels. Book settings, map counts, selected entry and every saved record stay unchanged.

Earlier map integrity checks also rerun: all fifteen dimension/scale combinations, absent and stale ordinary/explorer centers, full 128 by 128 snapshots applied to cold map data, banners/red-X, locked and blank records, unavailable IDs, names, negative aligned cell bounds and generation settings. `/atlas dedupe` keeps distinct IDs at the same grid, preserves the first copy's components, refunds exactly one blank per extra identical ID, handles pouch saveback and inventory-first overflow, and never changes world map records.

## Whole-atlas copy

The configured atlas limit is temporarily reduced to four, then a collection containing **905 filled-map items across all fifteen dimension/scale combinations** is copied without truncation. The exact filtered template list, quantities, order, names and components are checked. Explorer/treasure maps and unknown saved IDs are copied as stored references; paper, empty maps and bare filled items without an ID are excluded. Copies retain generation/minimap choices and other custom data, receive distinct atlas identities, rebase selection and recalculate fullness. Source atlases and all world map objects/pixels remain unchanged.

Actual `/atlas makecopy` command execution covers main hand, offhand, active pouch, full-inventory overflow, absent atlas, enchanted-book rejection, missing ordinary book and zero-filled atlas. It consumes exactly one ordinary book per copy, puts output in inventory first and drops only overflow. The source and another unselected book remain unchanged. Survival and creative full-inventory overflow both preserve exactly one output. The pre-fix candidate reproduces creative output loss in [baseline-creative-copy-overflow.json](baseline-creative-copy-overflow.json).

Actual vanilla cartography menu handling covers shift-clicking atlas/book inputs, output pickup, three successive shift-click copies with unique identities, number-key swap, throwing output, cancellation, exhausted-book retries, and ordinary vanilla map duplication. Source stays in its input slot and each successful copy consumes one book. Closing without taking returns the source and unconsumed book only. Helper, commands, previews, shift-click, number-key and drop preserve rebased selection; a normal primary pickup retains the upstream atlas behavior of clearing bundle selection.

## Ejection and reuse

The actual `ServerNetworkEvents.ejectMap(C2SEjectMap)` server handler is called for held and pouch atlases. The exact requested ID is dropped once and removed before the next exploration tick, without changing another region or unselected atlas. With that layer still enabled and a blank available, generation consumes exactly one blank and reuses the same Shared Region Maps ID and saved object. This explains why an ejected map can reappear without new world map data; total inventory-plus-drop item quantity is conserved.

These are mechanics players with packet-observing mock connections. The fixture verifies real packet objects and menu operations, but does not claim actual client connection negotiation, GUI mouse/key events, fallback screen local prediction, network codec delivery, visual rendering or optional Remapped runtime support. Real network/graphical acceptance is recorded separately. The client graphical fixture separately sends actual Ctrl+Q input and observes its target ID and drop.

Run `python3 qa-multiscale/run.py --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.1.4+26.3.jar --label <unique-label>`.
