# Source attribution and relationship to upstream

This is an unofficial modification/add-on maintained by THENATHE. It is not the original Tool Pouch or MapStitch project, and no upstream endorsement is implied.

## Original projects

- **Tool Pouch**, by pajic: https://github.com/pajicadvance/toolpouch — MIT, copyright (c) 2026 pajic. Full original notice: [licenses/Tool-Pouch-LICENSE.txt](licenses/Tool-Pouch-LICENSE.txt).
- **MapStitch**, by pajic: https://github.com/pajicadvance/mapstitch — MIT, copyright (c) 2026 pajic. Full original notice: [licenses/MapStitch-LICENSE.txt](licenses/MapStitch-LICENSE.txt).

The original MIT copyright and permission notice is retained in the root [LICENSE](LICENSE) and the release JAR. Additional original contributions in this repository are copyright (c) 2026 THENATHE, also under MIT. This does not transfer ownership of the original mods, code, names, or artwork.

## Adapted implementation

The atlas bridge, lookup ordering, and map-ejection routines adapt the original MapStitch/Tool Pouch integration logic. The player preference, persistence, and flight hooks extract the earlier local combined Tool Pouch modification into an add-on. Related upstream discussions and patches:

- https://github.com/pajicadvance/toolpouch/pull/10 — MapStitch atlas allowance.
- https://github.com/pajicadvance/toolpouch/pull/12 — pouch Elytra toggle.
- https://github.com/THENATHE/mapstitch — the related MapStitch compatibility work.

See `AtlasBridge`, `AtlasClientLookupMixin`, `AtlasEjectionMixin`, `PlayerPreferenceMixin`, `ServerPlayerPreferenceMixin`, and `PouchFlightMixin` for these adaptations. Compatibility changes belong to this add-on; the original gameplay systems remain supplied by the upstream mods.

## ClientSort integration

ClientSort is by TerminalMC / NotRyken: https://github.com/TerminalMC/ClientSort, licensed under Apache-2.0. The addon integrates with its policies, controls, sort orders and operation handling; ClientSort itself is supplied separately and remains optional. Tiered Backpacks is by pajic: https://github.com/pajicadvance/tiered_backpacks. Neither project endorses this unofficial addon.

## Distribution

Original mod JARs and development dependencies are not committed or bundled. Users obtain those separately from their authors. This add-on applies runtime mixins and leaves the installed original JAR files unchanged. The companion Polymer shim is a separate project: https://github.com/THENATHE/toolpouch-polymer-shim.

Fabric, Fzzy Config, Kotlin, and other external dependencies retain their respective licenses. A dependency or compatibility reference is not a claim of authorship over that project. Report add-on-specific issues here; use upstream issue trackers for problems reproduced with the unmodified original mods alone.
