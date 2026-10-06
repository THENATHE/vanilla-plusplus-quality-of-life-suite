# Atlas banner, command, mechanics and restart checks

Final suite `1.1.2+26.3`, SHA-256 `c21a103fc19be5121ab3c21bf50c02133dff2719812985010767e3c28cc7c3dd`. Disposable dedicated-server profiles with Polymer and without Polymer both passed initial and saved-world restart phases: 297 + 228 assertions each, **1,050 total**. The source/artifact audit records run label `banner-scales-1.1.2-c` and exact fixture hashes.

Each phase includes **70 new banner assertions**, 280 across the four phases, using actual server `AtlasItem.useOn` calls. These cover a generation subset independent of minimap selection; all five scales; all scales disabled; duplicate saved map IDs; mixed marker state; coherent add/remove; unchanged map objects, IDs and atlas contents; live banner names/colors and renaming; other dimensions and distant maps; unavailable enabled scales; no existing coverage without blank consumption; locked-map marker support; real tracked-decoration capacity; atomic preflight failures on a full map or a covered border outside vanilla's marker area; and unchanged ordinary filled-map banner use.

Bannerpoint tied-to-map checks confirm that removing enabled-scale markers keeps waypoint transmission when a disabled scale still stores that marker, and removing the last marker in the tested atlas stops transmission. This checks the final atlas batch result; it does not introduce or claim a global reference count across unrelated atlases, ordinary maps or unloaded saved data.

The prior 770 atlas insertion, extraction-command, codec, generation, sound, pouch saveback, book-targeting and saved-world restart assertions were rerun against this exact release artifact. These mechanics checks use a disposable fixture player, rather than a connected client. Bannerpoint's real client/pack/waypoint rendering evidence is maintained separately.

Two preliminary fixture failures were resolved without changing the production JAR: current banner blocks/items require registry lookup instead of removed colored static fields; vanilla's capacity predicate is `count > 256`, so filling exactly 256 tracked decorations does not yet reject another banner. The final fixture fills to the exact rejecting boundary and preserves vanilla's full-map no-op/removal behavior.

Run `python3 qa-multiscale/run.py --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.1.2+26.3.jar --label <unique-label>`.
