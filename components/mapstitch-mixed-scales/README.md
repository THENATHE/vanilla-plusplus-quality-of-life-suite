# Mixed-scale MapStitch module

Stable suite **1.1.5** on `main` includes this separately maintained addon. It extends the original MapStitch atlas while preserving the original developer JAR. The suite is maintained for modern Minecraft versions; exact installation requirements are listed in [the installation guide](../../docs/INSTALLATION_PACK.md).

| Component | Declared stable version |
| --- | --- |
| Suite | `1.1.5+26.3` |
| Shared coordinator | `1.1.1+26.3` |
| This addon | `1.1.5+26.3` |

The implementation is under `working/`, with output `working/build/libs/mapstitch-mixed-scales-1.1.5+26.3.jar`. The suite archive contains 18 nested mods, including this addon; do not install a duplicate standalone copy.

Atlases accept maps at all five scales. **S** changes the world-map viewing layer, **M** changes minimap scale, and **1/2/4/8/16** independently toggle generation. A single atlas remembers these choices without converting its existing maps. Using an atlas on a banner updates every enabled generation scale whose existing map covers the banner in the current dimension. It adds/updates marks together, or removes them together when all eligible maps already carry that mark; disabled scales and other dimensions remain unchanged. It creates no maps and consumes no blanks. This interaction belongs to this addon, while Bannerpoint’s separate compatibility module owns locator-bar icon delivery.

Minimap and full-screen atlas placement use authoritative saved-map centers when an atlas template’s item-center metadata is missing or stale. The full-screen tile/exploration-marker caches provide corrected positions to the original Ctrl+Q ejection lookup. `/atlas fix check` inspects coverage and saved-record problems; `/atlas fix` heals item centers and refreshes map data plus the owning atlas/inventory snapshot, even when zero centers need repair, without replacing map IDs or exploration. `/atlas dedupe` keeps one copy per exact map ID and returns one empty map per removed extra copy, inventory first with overflow dropped; different IDs and global map records remain intact. `/atlas repair` is equivalent to `/atlas fix`, including `check`. Atlas+ordinary-book cartography and `/atlas makecopy` copy all filled/explorer map entries across scales/dimensions/counts, keep the source and spend one book. The copy excludes blanks/paper, preserves options and map references, and receives a fresh atlas identity; command output uses inventory first with overflow dropped. See the [atlas maintenance commands](../../docs/ATLAS_CONTROLS.md) for scope and selection.

The addon also guards owned-book changes and extends the original keep-atlas-on-death setting to supported nested containers.

See [current controls](../../docs/ATLAS_CONTROLS.md), [implementation and maintenance](../../docs/MIXED_SCALES.md), [death retention](../../docs/ATLAS_DEATH_RETENTION.md), and [stable release testing](../../docs/RELEASE_1_1_5.md). Earlier branch results below describe their original artifacts and coupled controls, not new stable-release tests.

## Historical first experiment

Suite addon **1.0.0+26.3**, implemented separately under `working/`. The original developer MapStitch `1.1.6+26.3` JAR stays unchanged. This component is built into the independent `feat/mapstitch-mixed-scales` suite branch, version `1.0.2-multiscale.1+26.3`.

An atlas accepts ordinary maps at every scale, 0–4. Its existing scale component records the active exploration/minimap scale. The existing world-map scale button and scale keys change the displayed layer and the opened atlas's active scale. Stored map IDs and pixels are never converted or merged. Empty-map generation spends one existing blank for the selected scale only, retaining Shared Region Maps' dimension/region/scale sharing.

Install the matching suite on server and native clients. This is a separately maintained nested component, not a replacement MapStitch JAR. It depends on the suite coordinator, Shared Region Maps, and existing Tool Pouch atlas integration. Original MapStitch clients without the addon use Polymer fallback; that fallback does not provide the native atlas interface.

See [behavior, source targets and validation scope](../../docs/MIXED_SCALES.md). Build from the suite root with the pinned inputs; the component output is `working/build/libs/mapstitch-mixed-scales-1.0.0+26.3.jar`. No upstream source copy is modified by this component.
