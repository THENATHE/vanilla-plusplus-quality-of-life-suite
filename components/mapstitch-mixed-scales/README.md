# MapStitch Mixed Scales

On the current `merged` branch, both experiments are included in suite **1.0.2-merged.1+26.3**. Coordinator is **1.0.7-merged.1+26.3**; the map addon is **1.0.1-merged.1+26.3** with its matching dependency pin. See `docs/MERGED_TESTING.md` for combined acceptance. Individual-branch versions and evidence below are retained history.


Suite addon **1.0.0+26.3**, implemented separately under `working/`. The original developer MapStitch `1.1.6+26.3` JAR stays unchanged. This component is built into the independent `feat/mapstitch-mixed-scales` suite branch, version `1.0.2-multiscale.1+26.3`.

An atlas accepts ordinary maps at every scale, 0–4. Its existing scale component records the active exploration/minimap scale. The existing world-map scale button and scale keys change the displayed layer and the opened atlas's active scale. Stored map IDs and pixels are never converted or merged. Empty-map generation spends one existing blank for the selected scale only, retaining Shared Region Maps' dimension/region/scale sharing.

Install the matching suite on server and native clients. This is a separately maintained nested component, not a replacement MapStitch JAR. It depends on the suite coordinator, Shared Region Maps, and existing Tool Pouch atlas integration. Original MapStitch clients without the addon use Polymer fallback; that fallback does not provide the native atlas interface.

See [behavior, source targets and validation scope](../../docs/MIXED_SCALES.md). Build from the suite root with the pinned inputs; the component output is `working/build/libs/mapstitch-mixed-scales-1.0.0+26.3.jar`. No upstream source copy is modified by this component.
