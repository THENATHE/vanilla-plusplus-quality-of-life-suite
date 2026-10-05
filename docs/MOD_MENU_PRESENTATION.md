# Mod Menu presentation

The merged suite shows each original Pajic feature mod and Chalk as an individual
Mod Menu entry. Expand **Vanilla++ Quality of Life Suite** to see the suite's
addons and compatibility modules; the shared coordinator is named
**Vanilla / Polymer Shim**. Colorful Chalk remains installed and functional but
its separate icon is hidden. Mod Menu's explicit “show hidden” option can reveal
it for inspection.

Authored modules declare the suite as their parent using Mod Menu's
[supported metadata](https://github.com/TerraformersMC/ModMenu#parents-parent-mod_id-or-).
An optional client mixin adjusts only Mod Menu's `FabricMod.getParent()` and
`isHidden()` views for the immutable bundled components and original feature
entries. It does not edit original JARs, Fabric mod identities, registries,
dependencies, or gameplay. The mixin is guarded by `isModLoaded("modmenu")` and
never applies on a dedicated server. Mod Menu is optional.

Chalk's own Mod Menu factory is redirected to the suite's real Chalk settings
bridge, which preserves the original Chalk configuration file and particle
setting. This second client mixin requires both Chalk and Mod Menu. Mod Menu
21.0.0's provided-factory API uses `putIfAbsent`, so it cannot replace Chalk's
already registered native factory; the runtime QA caught and corrected that
initial approach. The original Chalk JAR remains unchanged.

Presentation integration is in `src/main/java/com/thenathe/suite/client/` and
`src/main/resources/suite_mod_menu.mixins.json`. The supported implementation was
checked against the pinned Mod Menu **21.0.0** JAR. When upgrading Mod Menu,
verify that its `FabricMod` class still exposes `getParent()` and `isHidden()`,
then check the Mods screen with libraries and hidden entries both filtered and
shown. Other installed mods retain their own display settings.
