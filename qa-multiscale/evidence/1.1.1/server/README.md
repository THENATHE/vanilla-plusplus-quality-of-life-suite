# Atlas command, mechanics and restart checks

Final suite `1.1.1+26.3`, SHA-256 `643a9ce8146c1ed7167f240b323044384027fb4990c722050b0ca66976bda86e`. Both Polymer and no-Polymer disposable dedicated-server profiles passed initial and saved-world restart phases: 227 + 158 assertions each, 770 total.

The existing real atlas insertion, codec, generation, sound, pouch saveback and book-targeting cases run alongside 12 actual Brigadier extraction forms and a no-atlas case in each phase. Command cases cover combined/individual filters, empty maps and paper, explicit prefixes, main/offhand and active-pouch books, unknown map data, no matches, invalid scales, inventory-first delivery and real overflow drops. Returned map IDs/custom components, quantities, unselected books, atlas identity/name/options and pouch contents are checked.

The fixture invokes real `ServerPlayer.drop` and records only item entities returned by that call. Querying chunk entities during restart could also include asynchronously reloaded drops from the prior phase; that preliminary fixture observation was corrected. The production release JAR did not change. These are dedicated-server mechanics checks with a disposable fixture player, not connected-client command UI tests.

Run `python3 qa-multiscale/run.py --label <unique-label>`. Real-client renderer and negotiation evidence is maintained separately.
