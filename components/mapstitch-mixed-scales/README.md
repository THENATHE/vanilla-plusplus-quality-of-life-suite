# Mixed-scale MapStitch module

Stable suite **1.1.1** on `main` includes this separately maintained addon. It extends the original MapStitch atlas while preserving the original developer JAR. The suite is maintained for modern Minecraft versions; exact installation requirements are listed in [the installation guide](../../docs/INSTALLATION_PACK.md).

| Component | Declared stable version |
| --- | --- |
| Suite | `1.1.1+26.3` |
| Shared coordinator | `1.1.0+26.3` |
| This addon | `1.1.1+26.3` |

The implementation is under `working/`, with output `working/build/libs/mapstitch-mixed-scales-1.1.1+26.3.jar`. The suite archive contains 16 nested mods, including this addon; do not install a duplicate standalone copy.

Atlases accept maps at all five scales. **S** changes the world-map viewing layer, **M** changes minimap scale, and **1/2/4/8/16** independently toggle generation. A single atlas remembers these choices without converting its existing maps. The addon also guards owned-book changes and extends the original keep-atlas-on-death setting to supported nested containers.

See [current controls](../../docs/ATLAS_CONTROLS.md), [implementation and maintenance](../../docs/MIXED_SCALES.md), [death retention](../../docs/ATLAS_DEATH_RETENTION.md), and [stable release testing](../../docs/RELEASE_1_1_1.md). Earlier branch results below describe their original artifacts and coupled controls, not new stable-release tests.

## Historical first experiment

Suite addon **1.0.0+26.3**, implemented separately under `working/`. The original developer MapStitch `1.1.6+26.3` JAR stays unchanged. This component is built into the independent `feat/mapstitch-mixed-scales` suite branch, version `1.0.2-multiscale.1+26.3`.

An atlas accepts ordinary maps at every scale, 0–4. Its existing scale component records the active exploration/minimap scale. The existing world-map scale button and scale keys change the displayed layer and the opened atlas's active scale. Stored map IDs and pixels are never converted or merged. Empty-map generation spends one existing blank for the selected scale only, retaining Shared Region Maps' dimension/region/scale sharing.

Install the matching suite on server and native clients. This is a separately maintained nested component, not a replacement MapStitch JAR. It depends on the suite coordinator, Shared Region Maps, and existing Tool Pouch atlas integration. Original MapStitch clients without the addon use Polymer fallback; that fallback does not provide the native atlas interface.

See [behavior, source targets and validation scope](../../docs/MIXED_SCALES.md). Build from the suite root with the pinned inputs; the component output is `working/build/libs/mapstitch-mixed-scales-1.0.0+26.3.jar`. No upstream source copy is modified by this component.
