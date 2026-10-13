# Backpack Crafting addon

Separate suite-owned addon for unchanged developer releases of Tiered Backpacks and Tool Pouch. See [usage, provenance and validation](../../docs/BACKPACK_CRAFTING.md).

The `menu` package owns server crafting and the combined ingredient view. The `client` package uses vanilla's recipe-book component. Mixins are isolated under their own packages. Optional BRBR hooks extend its existing planners; Polymer overlays load only when Polymer is installed. The suite coordinator fingerprints these registry entries together with the root suite while preserving the v3 wire format.

Generated art originals are retained in `artwork/`. Shipped textures are nearest-neighbor samples at exactly 16×16 with binary alpha. Original developer sources and JARs are not edited.
