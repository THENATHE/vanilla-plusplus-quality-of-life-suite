# Atlas lookup allocation regression

`AtlasCacheQa.java` is compiled by `qa-performance/integration/run.py` and called
by its Fabric client game test after a fresh test world has loaded. The helper
uses a real client player, pouch components, MapStitch lookup and suite mixins.
It restores the player's inventory, leggings and menu, plus both temporary
configuration changes, even if an assertion fails.

Checks cover missing sources, multiple atlases and mixed scale selections,
detached mutable return values, source replacement, container replacement on the
same stack, removal, reordering, inventory priority, configuration changes and
the open pouch menu guard. Live Sensible Stackables table updates exercise
atlas and nested-map limits changing from two to one and back to two while
the source pouch and its container component remain unchanged.
Candidate-only reflection checks confirm that stable
contents reuse materialized snapshots and that source/world cache keys and
separate cache instances invalidate or isolate them. The world-key check uses
the live level and a null sentinel; it does not claim to perform a dimension
travel or reconnect test.

The benchmark warms both paths, then records current-thread allocated bytes for
400 lookups of a pouch containing one atlas with 1,024 filled-map templates.
`baseline_lookup_bytes` runs the previous source expression explicitly:
`ToolPouchUtil.getItemsFromToolPouch(...).stream().map(ItemStackTemplate::create)`.
`candidate_lookup_bytes` calls the actual `AtlasBridge.atlases` method. Both
therefore use the same running suite's stack-size hook; this isolates the atlas
cache improvement. Running the helper with the previous suite JAR separately
measures the combined effect across binaries. These are synthetic allocation
measurements, not predicted FPS gains or GC pause measurements.
